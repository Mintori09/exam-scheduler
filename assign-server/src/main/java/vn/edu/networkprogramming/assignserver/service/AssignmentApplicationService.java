package vn.edu.networkprogramming.assignserver.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
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
    private static final int SESSION_DB_BATCH_SIZE = 10;

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
                "NONE",
                null,
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

    public AssignmentRun findRunById(String assignmentId) throws SQLException {
        return repository.findById(assignmentId);
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
        ensureOutputFilesGeneratedOnDemand(assignmentId);
        return repository.findFile(assignmentId, FILE_ROLE_OUTPUT_INVIGILATORS);
    }

    public AssignmentFileContent getMonitorFile(String assignmentId) throws SQLException {
        ensureOutputFilesGeneratedOnDemand(assignmentId);
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
            List<AssignmentRunRepository.SessionRow> pendingRows = new ArrayList<>();
            AssignmentResult result = planner.plan(input, session -> {
                try {
                    AssignmentSummary summary = new AssignmentSummary(
                            session.sessionNo(),
                            session.roomAssignments().size(),
                            session.hallMonitorAssignments().size()
                    );
                    pendingRows.add(new AssignmentRunRepository.SessionRow(
                            session.sessionNo(),
                            jsonService.toJson(session),
                            jsonService.toJson(summary)
                    ));
                    if (pendingRows.size() >= SESSION_DB_BATCH_SIZE) {
                        repository.upsertSessionsBatch(assignmentId, pendingRows);
                        pendingRows.clear();
                    }
                    int done = completed.incrementAndGet();
                    if (done % SESSION_DB_BATCH_SIZE == 0 || done == input.sessionCount()) {
                        repository.updateRunStatus(assignmentId, "RUNNING", "Dang xu ly", done);
                    }
                } catch (SQLException exception) {
                    throw new IllegalStateException(exception);
                }
            });
            if (!pendingRows.isEmpty()) {
                repository.upsertSessionsBatch(assignmentId, pendingRows);
                pendingRows.clear();
            }
            repository.updateRunStatus(assignmentId, "RUNNING", "Dang tong hop ket qua", completed.get());

            List<String> sessionJsonRows = repository.findSessionJsonByAssignmentId(assignmentId);
            List<SessionAssignment> persistedSessions = new ArrayList<>();
            for (String sessionJson : sessionJsonRows) {
                persistedSessions.add(jsonService.fromJson(sessionJson, SessionAssignment.class));
            }
            List<AssignmentSummary> summaries = buildSummaries(persistedSessions);
            // Large aggregate JSON payloads are no longer needed because
            // session/summary rows are persisted incrementally in assignment_sessions.
            String sessionsJson = "[]";
            String summaryJson = "[]";

            repository.completeRun(
                    assignmentId,
                    result.status(),
                    result.message(),
                    completed.get(),
                    sessionsJson,
                    summaryJson
            );

            if (!"SUCCESS".equals(result.status())) {
                repository.updateOutputStatus(assignmentId, "NONE", null);
            }
        } catch (Exception exception) {
            try {
                repository.updateRunStatus(assignmentId, "FAILED", exception.getMessage(), 0);
                repository.updateOutputStatus(assignmentId, "FAILED", exception.getMessage());
            } catch (SQLException ignored) {
                // Ignore secondary failures in background worker.
            }
        }
    }

    private void generateOutputFilesAsync(String assignmentId, List<SessionAssignment> persistedSessions, String message) {
        Path invFile = null;
        Path monFile = null;
        try {
            Path outputRoot = storageRoot.resolve("output-cache").resolve(assignmentId);
            Files.createDirectories(outputRoot);
            invFile = outputRoot.resolve("DANHSACH_PHANCONG.xlsx");
            monFile = outputRoot.resolve("DANHSACH_GIAMSAT.xlsx");

            AssignmentResult exportResult = new AssignmentResult("SUCCESS", message, persistedSessions);
            exportService.writeInvigilatorWorkbook(invFile, exportResult);
            exportService.writeMonitorWorkbook(monFile, exportResult);

            try (InputStream invStream = Files.newInputStream(invFile);
                 InputStream monStream = Files.newInputStream(monFile)) {
                repository.upsertFileFromStream(
                        assignmentId,
                        FILE_ROLE_OUTPUT_INVIGILATORS,
                        "DANHSACH_PHANCONG.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        invStream,
                        Files.size(invFile)
                );
                repository.upsertFileFromStream(
                        assignmentId,
                        FILE_ROLE_OUTPUT_MONITORS,
                        "DANHSACH_GIAMSAT.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        monStream,
                        Files.size(monFile)
                );
            }
            repository.updateOutputStatus(assignmentId, "READY", null);
        } catch (Exception exception) {
            try {
                repository.updateOutputStatus(assignmentId, "FAILED", formatErrorMessage(exception));
            } catch (SQLException ignored) {
                // Ignore secondary failures in output worker.
            }
        } finally {
            deleteQuietly(invFile);
            deleteQuietly(monFile);
        }
    }

    private void ensureOutputFilesGeneratedOnDemand(String assignmentId) throws SQLException {
        AssignmentRun run = repository.findById(assignmentId);
        if (run == null) {
            return;
        }
        if (!"SUCCESS".equals(run.status())) {
            return;
        }
        boolean hasInv = repository.findFile(assignmentId, FILE_ROLE_OUTPUT_INVIGILATORS) != null;
        boolean hasMon = repository.findFile(assignmentId, FILE_ROLE_OUTPUT_MONITORS) != null;
        if (hasInv && hasMon) {
            if (!"READY".equals(run.outputStatus())) {
                repository.updateOutputStatus(assignmentId, "READY", null);
            }
            return;
        }
        synchronized (assignmentId.intern()) {
            AssignmentRun latest = repository.findById(assignmentId);
            if (latest == null || !"SUCCESS".equals(latest.status())) {
                return;
            }
            boolean nowHasInv = repository.findFile(assignmentId, FILE_ROLE_OUTPUT_INVIGILATORS) != null;
            boolean nowHasMon = repository.findFile(assignmentId, FILE_ROLE_OUTPUT_MONITORS) != null;
            if (nowHasInv && nowHasMon) {
                if (!"READY".equals(latest.outputStatus())) {
                    repository.updateOutputStatus(assignmentId, "READY", null);
                }
                return;
            }
            repository.updateOutputStatus(assignmentId, "GENERATING", null);
            try {
                List<String> sessionJsonRows = repository.findSessionJsonByAssignmentId(assignmentId);
                List<SessionAssignment> sessions = new ArrayList<>();
                for (String sessionJson : sessionJsonRows) {
                    sessions.add(jsonService.fromJson(sessionJson, SessionAssignment.class));
                }
                generateOutputFilesAsync(assignmentId, sessions, latest.message());
            } catch (Exception exception) {
                repository.updateOutputStatus(assignmentId, "FAILED", formatErrorMessage(exception));
                throw new SQLException("Khong tao duoc file output", exception);
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

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Ignore cleanup errors.
        }
    }

    private String formatErrorMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return exception.getClass().getSimpleName() + ": " + message;
    }
}
