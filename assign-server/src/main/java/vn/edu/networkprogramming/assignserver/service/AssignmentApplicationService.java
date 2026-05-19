package vn.edu.networkprogramming.assignserver.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import vn.edu.networkprogramming.assignserver.model.AssignmentDetail;
import vn.edu.networkprogramming.assignserver.model.AssignmentFileContent;
import vn.edu.networkprogramming.assignserver.model.AssignmentInput;
import vn.edu.networkprogramming.assignserver.model.AssignmentResult;
import vn.edu.networkprogramming.assignserver.model.AssignmentRun;
import vn.edu.networkprogramming.assignserver.model.AssignmentSummary;
import vn.edu.networkprogramming.assignserver.model.SessionAssignment;
import vn.edu.networkprogramming.assignserver.model.StoredAssignmentRun;
import vn.edu.networkprogramming.assignserver.repository.AssignmentRunRepository;

public class AssignmentApplicationService {

    public static final String FILE_ROLE_INPUT_STAFF = "input_staff";
    public static final String FILE_ROLE_INPUT_ROOM = "input_room";
    public static final String FILE_ROLE_OUTPUT_INVIGILATORS = "output_invigilators";
    public static final String FILE_ROLE_OUTPUT_MONITORS = "output_monitors";

    private final ExcelAssignmentInputService inputService;
    private final AssignmentPlanner planner;
    private final AssignmentWorkbookExportService exportService;
    private final AssignmentRunRepository repository;
    private final JsonService jsonService;
    private final Path storageRoot;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public AssignmentApplicationService(
            ExcelAssignmentInputService inputService,
            AssignmentPlanner planner,
            AssignmentWorkbookExportService exportService,
            AssignmentRunRepository repository,
            JsonService jsonService,
            Path storageRoot
    ) {
        this.inputService = inputService;
        this.planner = planner;
        this.exportService = exportService;
        this.repository = repository;
        this.jsonService = jsonService;
        this.storageRoot = storageRoot;
    }

    public AssignmentRun createAssignmentAsync(
            String staffFilename,
            byte[] staffContent,
            String roomFilename,
            byte[] roomContent,
            int sessionCount
    ) throws IOException, SQLException {
        AssignmentInput input = inputService.parse(
                new ByteArrayInputStream(staffContent),
                new ByteArrayInputStream(roomContent),
                sessionCount
        );
        String assignmentId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        AssignmentRun run = new AssignmentRun(
                assignmentId,
                "RUNNING",
                "Dang xu ly",
                now,
                now,
                input.sessionCount(),
                0,
                input.roomRecords().size(),
                input.staffRecords().size(),
                null,
                null
        );
        repository.createRun(run);
        repository.upsertFile(
                assignmentId,
                FILE_ROLE_INPUT_STAFF,
                staffFilename == null || staffFilename.isBlank() ? "CANBOCOITHI.XLSX" : staffFilename,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                staffContent
        );
        repository.upsertFile(
                assignmentId,
                FILE_ROLE_INPUT_ROOM,
                roomFilename == null || roomFilename.isBlank() ? "PHONGTHI.XLSX" : roomFilename,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                roomContent
        );

        executorService.submit(() -> processAssignment(assignmentId, staffContent, roomContent, sessionCount));
        return run;
    }

    public AssignmentRun createAssignment(java.io.InputStream staffStream, java.io.InputStream roomStream, int sessionCount) throws IOException, SQLException {
        byte[] staffBytes = staffStream.readAllBytes();
        byte[] roomBytes = roomStream.readAllBytes();
        AssignmentRun run = createAssignmentAsync("CANBOCOITHI.XLSX", staffBytes, "PHONGTHI.XLSX", roomBytes, sessionCount);
        waitUntilFinished(run.assignmentId());
        AssignmentRun latest = repository.findById(run.assignmentId());
        if (latest == null) {
            throw new IOException("Khong tim thay ket qua sau khi xu ly");
        }
        return latest;
    }

    public List<AssignmentRun> findAllRuns() throws SQLException {
        return repository.findAll();
    }

    public AssignmentDetail getAssignmentDetail(String assignmentId) throws SQLException {
        StoredAssignmentRun stored = repository.findStoredById(assignmentId);
        if (stored == null) {
            return null;
        }
        List<String> summaryJsonRows = repository.findSummaryJsonByAssignmentId(assignmentId);
        List<AssignmentSummary> summaries = new ArrayList<>();
        for (String summaryJson : summaryJsonRows) {
            summaries.add(jsonService.fromJson(summaryJson, AssignmentSummary.class));
        }
        if (summaries.isEmpty() && stored.summaryJson() != null && !stored.summaryJson().isBlank()) {
            AssignmentSummary[] fallback = jsonService.fromJson(stored.summaryJson(), AssignmentSummary[].class);
            summaries.addAll(Arrays.asList(fallback));
        }
        return new AssignmentDetail(
                stored.run(),
                summaries,
                repository.findFile(assignmentId, FILE_ROLE_OUTPUT_INVIGILATORS) != null,
                repository.findFile(assignmentId, FILE_ROLE_OUTPUT_MONITORS) != null
        );
    }

    public SessionAssignment getSessionDetail(String assignmentId, int sessionNo) throws SQLException {
        String sessionJson = repository.findSessionJsonByAssignmentIdAndSessionNo(assignmentId, sessionNo);
        if (sessionJson != null) {
            return jsonService.fromJson(sessionJson, SessionAssignment.class);
        }
        StoredAssignmentRun stored = repository.findStoredById(assignmentId);
        if (stored == null || stored.sessionsJson() == null || stored.sessionsJson().isBlank()) {
            return null;
        }
        SessionAssignment[] sessions = jsonService.fromJson(stored.sessionsJson(), SessionAssignment[].class);
        for (SessionAssignment session : sessions) {
            if (session.sessionNo() == sessionNo) {
                return session;
            }
        }
        return null;
    }

    public AssignmentFileContent getInvigilatorFile(String assignmentId) throws SQLException {
        return repository.findFile(assignmentId, FILE_ROLE_OUTPUT_INVIGILATORS);
    }

    public AssignmentFileContent getMonitorFile(String assignmentId) throws SQLException {
        return repository.findFile(assignmentId, FILE_ROLE_OUTPUT_MONITORS);
    }

    private void processAssignment(String assignmentId, byte[] staffContent, byte[] roomContent, int sessionCount) {
        try {
            AssignmentInput input = inputService.parse(
                    new ByteArrayInputStream(staffContent),
                    new ByteArrayInputStream(roomContent),
                    sessionCount
            );
            AtomicInteger completed = new AtomicInteger(0);
            AssignmentResult result = planner.plan(input, session -> {
                try {
                    AssignmentSummary summary = new AssignmentSummary(
                            session.sessionNo(),
                            session.roomAssignments().size(),
                            session.hallMonitorAssignments().size()
                    );
                    repository.upsertSession(
                            assignmentId,
                            session.sessionNo(),
                            jsonService.toJson(session),
                            jsonService.toJson(summary)
                    );
                    repository.updateRunStatus(assignmentId, "RUNNING", "Dang xu ly", completed.incrementAndGet());
                } catch (SQLException exception) {
                    throw new IllegalStateException(exception);
                }
            });

            List<String> sessionJsonRows = repository.findSessionJsonByAssignmentId(assignmentId);
            List<SessionAssignment> persistedSessions = new ArrayList<>();
            for (String sessionJson : sessionJsonRows) {
                persistedSessions.add(jsonService.fromJson(sessionJson, SessionAssignment.class));
            }
            List<AssignmentSummary> summaries = buildSummaries(persistedSessions);
            String sessionsJson = jsonService.toJson(persistedSessions);
            String summaryJson = jsonService.toJson(summaries);

            if ("SUCCESS".equals(result.status())) {
                AssignmentResult exportResult = new AssignmentResult("SUCCESS", result.message(), persistedSessions);
                byte[] invigilatorWorkbook = exportService.createInvigilatorWorkbookBytes(exportResult);
                byte[] monitorWorkbook = exportService.createMonitorWorkbookBytes(exportResult);
                repository.upsertFile(
                        assignmentId,
                        FILE_ROLE_OUTPUT_INVIGILATORS,
                        "DANHSACH_PHANCONG.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        invigilatorWorkbook
                );
                repository.upsertFile(
                        assignmentId,
                        FILE_ROLE_OUTPUT_MONITORS,
                        "DANHSACH_GIAMSAT.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        monitorWorkbook
                );
            }

            repository.completeRun(
                    assignmentId,
                    result.status(),
                    result.message(),
                    completed.get(),
                    sessionsJson,
                    summaryJson
            );
        } catch (Exception exception) {
            try {
                repository.updateRunStatus(assignmentId, "FAILED", exception.getMessage(), 0);
            } catch (SQLException ignored) {
                // Ignore secondary failures in background worker.
            }
        }
    }

    private void waitUntilFinished(String assignmentId) throws SQLException, IOException {
        long deadline = System.currentTimeMillis() + 600_000L;
        while (System.currentTimeMillis() < deadline) {
            AssignmentRun run = repository.findById(assignmentId);
            if (run != null && !"RUNNING".equals(run.status())) {
                return;
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IOException("Bi ngat khi cho xu ly assignment", exception);
            }
        }
        throw new IOException("Qua thoi gian cho xu ly assignment");
    }

    private List<AssignmentSummary> buildSummaries(List<SessionAssignment> sessions) {
        return sessions.stream()
                .map(session -> new AssignmentSummary(
                        session.sessionNo(),
                        session.roomAssignments().size(),
                        session.hallMonitorAssignments().size()
                ))
                .toList();
    }
}
