package vn.edu.networkprogramming.assignserver.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import vn.edu.networkprogramming.assignserver.model.AssignmentFileContent;
import vn.edu.networkprogramming.assignserver.model.AssignmentResult;
import vn.edu.networkprogramming.assignserver.model.AssignmentSummary;
import vn.edu.networkprogramming.assignserver.model.BranchDetail;
import vn.edu.networkprogramming.assignserver.model.BranchPreview;
import vn.edu.networkprogramming.assignserver.model.BranchSessionRecord;
import vn.edu.networkprogramming.assignserver.model.DatasetUploadResult;
import vn.edu.networkprogramming.assignserver.model.NextSessionPlan;
import vn.edu.networkprogramming.assignserver.model.RoomDataset;
import vn.edu.networkprogramming.assignserver.model.RoomRecord;
import vn.edu.networkprogramming.assignserver.model.ScheduleBranch;
import vn.edu.networkprogramming.assignserver.model.SessionAssignment;
import vn.edu.networkprogramming.assignserver.model.StaffDataset;
import vn.edu.networkprogramming.assignserver.model.StaffRecord;
import vn.edu.networkprogramming.assignserver.repository.SchedulingRepository;
import vn.edu.networkprogramming.assignserver.service.exception.ValidationException;

public class AssignmentApplicationService {

    private static final DateTimeFormatter DEFAULT_NAME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
    private static final Logger LOGGER = Logger.getLogger(AssignmentApplicationService.class.getName());

    private final ExcelAssignmentInputService inputService;
    private final AssignmentPlanner planner;
    private final AssignmentWorkbookExportService exportService;
    private final SchedulingRepository repository;
    private final JsonService jsonService;
    private final Path storageRoot;

    public AssignmentApplicationService(
            ExcelAssignmentInputService inputService,
            AssignmentPlanner planner,
            AssignmentWorkbookExportService exportService,
            SchedulingRepository repository,
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

    public DatasetUploadResult<StaffDataset> uploadStaffDataset(String name, String fileName, byte[] content) throws IOException, SQLException {
        LOGGER.info(() -> "Nhan yeu cau upload dataset can bo, fileName=" + fileName + ", bytes=" + content.length);
        List<StaffRecord> records = inputService.parseStaff(new ByteArrayInputStream(content));
        String hash = sha256(content);
        StaffDataset existing = repository.findStaffDatasetByHash(hash);
        if (existing != null) {
            LOGGER.info(() -> "Dataset can bo da ton tai, hash=" + hash + ", datasetId=" + existing.datasetId());
            if (existing.archived()) {
                repository.unarchiveStaffDataset(existing.datasetId());
                LOGGER.info(() -> "Da hien lai dataset can bo dang bi an, datasetId=" + existing.datasetId());
            }
            return new DatasetUploadResult<>(repository.findStaffDatasetById(existing.datasetId()), true);
        }
        String datasetId = UUID.randomUUID().toString();
        repository.createStaffDataset(
                datasetId,
                defaultDatasetName(name, fileName, "Staff"),
                hash,
                normalizeFileName(fileName, "CANBOCOITHI.xlsx"),
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                content,
                records.size()
        );
        LOGGER.info(() -> "Da tao dataset can bo moi, datasetId=" + datasetId + ", staffCount=" + records.size());
        return new DatasetUploadResult<>(repository.findStaffDatasetById(datasetId), false);
    }

    public DatasetUploadResult<RoomDataset> uploadRoomDataset(String name, String fileName, byte[] content) throws IOException, SQLException {
        LOGGER.info(() -> "Nhan yeu cau upload dataset phong, fileName=" + fileName + ", bytes=" + content.length);
        List<RoomRecord> records = inputService.parseRooms(new ByteArrayInputStream(content));
        String hash = sha256(content);
        RoomDataset existing = repository.findRoomDatasetByHash(hash);
        if (existing != null) {
            LOGGER.info(() -> "Dataset phong da ton tai, hash=" + hash + ", datasetId=" + existing.datasetId());
            if (existing.archived()) {
                repository.unarchiveRoomDataset(existing.datasetId());
                LOGGER.info(() -> "Da hien lai dataset phong dang bi an, datasetId=" + existing.datasetId());
            }
            return new DatasetUploadResult<>(repository.findRoomDatasetById(existing.datasetId()), true);
        }
        String datasetId = UUID.randomUUID().toString();
        repository.createRoomDataset(
                datasetId,
                defaultDatasetName(name, fileName, "Room"),
                hash,
                normalizeFileName(fileName, "PHONGTHI.xlsx"),
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                content,
                records.size()
        );
        LOGGER.info(() -> "Da tao dataset phong moi, datasetId=" + datasetId + ", roomCount=" + records.size());
        return new DatasetUploadResult<>(repository.findRoomDatasetById(datasetId), false);
    }

    public List<StaffDataset> findAllStaffDatasets(boolean includeArchived) throws SQLException {
        return repository.findAllStaffDatasets(includeArchived);
    }

    public List<RoomDataset> findAllRoomDatasets(boolean includeArchived) throws SQLException {
        return repository.findAllRoomDatasets(includeArchived);
    }

    public void archiveStaffDataset(String datasetId) throws SQLException {
        repository.archiveStaffDataset(datasetId);
    }

    public void renameStaffDataset(String datasetId, String name) throws SQLException {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Tên bộ dữ liệu cán bộ không được để trống");
        }
        repository.renameStaffDataset(datasetId, name.trim());
    }

    public void unarchiveStaffDataset(String datasetId) throws SQLException {
        repository.unarchiveStaffDataset(datasetId);
    }

    public void archiveRoomDataset(String datasetId) throws SQLException {
        repository.archiveRoomDataset(datasetId);
    }

    public void renameRoomDataset(String datasetId, String name) throws SQLException {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Tên bộ dữ liệu phòng không được để trống");
        }
        repository.renameRoomDataset(datasetId, name.trim());
    }

    public void unarchiveRoomDataset(String datasetId) throws SQLException {
        repository.unarchiveRoomDataset(datasetId);
    }

    public ScheduleBranch createBranchAndFirstSession(
            String name,
            String staffDatasetId,
            String roomDatasetId,
            int requestedStaffCount,
            int requestedRoomCount
    ) throws Exception {
        return createBranchAndSessions(name, staffDatasetId, roomDatasetId, requestedStaffCount, requestedRoomCount, 1);
    }

    public ScheduleBranch createBranchAndSessions(
            String name,
            String staffDatasetId,
            String roomDatasetId,
            int requestedStaffCount,
            int requestedRoomCount,
            int sessionCount
    ) throws Exception {
        LOGGER.info(() -> "Bat dau tao nhanh moi, name=" + name + ", staffDatasetId=" + staffDatasetId
                + ", roomDatasetId=" + roomDatasetId + ", requestedStaffCount=" + requestedStaffCount
                + ", requestedRoomCount=" + requestedRoomCount + ", sessionCount=" + sessionCount);
        StaffDataset staffDataset = requireActiveStaffDataset(staffDatasetId);
        RoomDataset roomDataset = requireActiveRoomDataset(roomDatasetId);
        validateRequestedCounts(staffDataset.staffCount(), roomDataset.roomCount(), requestedStaffCount, requestedRoomCount);
        validateSessionCount(sessionCount);

        Instant now = Instant.now();
        String branchId = UUID.randomUUID().toString();
        ScheduleBranch branch = new ScheduleBranch(
                branchId,
                defaultBranchName(name),
                "RUNNING",
                "Đang tạo ca 1",
                now,
                now,
                staffDataset.datasetId(),
                staffDataset.name(),
                roomDataset.datasetId(),
                roomDataset.name(),
                requestedStaffCount,
                requestedRoomCount,
                1,
                0,
                false,
                "NONE",
                null
        );
        repository.createBranch(branch);
        LOGGER.info(() -> "Da tao branch record, branchId=" + branchId);
        try {
            appendNextSessions(branchId, sessionCount);
        } catch (Exception exception) {
            repository.archiveBranch(branchId);
            LOGGER.warning(() -> "Tao branch that bai, da an branchId=" + branchId + ", reason=" + exception.getMessage());
            throw exception;
        }
        LOGGER.info(() -> "Tao nhanh thanh cong, branchId=" + branchId);
        return repository.findBranchById(branchId);
    }

    public ScheduleBranch appendNextSession(String branchId) throws Exception {
        return appendNextSessions(branchId, 1);
    }

    public ScheduleBranch appendNextSessions(String branchId, int sessionCount) throws Exception {
        return appendNextSessions(branchId, sessionCount, null, null);
    }

    public ScheduleBranch appendNextSessions(
            String branchId,
            int sessionCount,
            Integer requestedStaffCountOverride,
            Integer requestedRoomCountOverride
    ) throws Exception {
        ScheduleBranch branch = requireActiveBranch(branchId);
        validateSessionCount(sessionCount);
        int effectiveRequestedStaffCount = requestedStaffCountOverride == null
                ? branch.requestedStaffCount()
                : requestedStaffCountOverride;
        int effectiveRequestedRoomCount = requestedRoomCountOverride == null
                ? branch.requestedRoomCount()
                : requestedRoomCountOverride;
        StaffDataset staffDataset = requireActiveStaffDataset(branch.staffDatasetId());
        RoomDataset roomDataset = requireActiveRoomDataset(branch.roomDatasetId());
        validateRequestedCounts(
                staffDataset.staffCount(),
                roomDataset.roomCount(),
                effectiveRequestedStaffCount,
                effectiveRequestedRoomCount
        );
        LOGGER.info(() -> "Bat dau tao them ca cho branchId=" + branchId
                + ", sessionCount=" + sessionCount
                + ", effectiveRequestedStaffCount=" + effectiveRequestedStaffCount
                + ", effectiveRequestedRoomCount=" + effectiveRequestedRoomCount);
        int createdInRequest = 0;
        for (int index = 0; index < sessionCount; index++) {
            List<StaffRecord> staffPool = loadStaffPool(branch.staffDatasetId());
            List<RoomRecord> roomPool = loadRoomPool(branch.roomDatasetId());
            List<BranchSessionRecord> history = findBranchSessions(branchId);
            long seedBase = Instant.now().toEpochMilli() + history.size() * 104_729L + index * 8191L;
            try {
                int targetSessionNo = history.size() + 1;
                long currentSeedBase = seedBase;
                LOGGER.info(() -> "Dang lap ke hoach cho branchId=" + branchId
                        + ", sessionNo=" + targetSessionNo
                        + ", historySize=" + history.size()
                        + ", staffPoolSize=" + staffPool.size()
                        + ", roomPoolSize=" + roomPool.size()
                        + ", seedBase=" + currentSeedBase);
                NextSessionPlan plan = planner.planNextSession(
                        staffPool,
                        roomPool,
                        effectiveRequestedStaffCount,
                        effectiveRequestedRoomCount,
                        history,
                        seedBase
                );

                AssignmentSummary summary = new AssignmentSummary(
                        plan.session().sessionNo(),
                        plan.session().roomAssignments().size(),
                        plan.session().hallMonitorAssignments().size()
                );
                repository.insertBranchSession(
                        branchId,
                        plan.session().sessionNo(),
                        plan.selectionSeed(),
                        effectiveRequestedStaffCount,
                        effectiveRequestedRoomCount,
                        jsonService.toJson(plan.selectedStaffCodes()),
                        jsonService.toJson(plan.selectedRoomNames()),
                        jsonService.toJson(plan.session()),
                        jsonService.toJson(summary)
                );
                LOGGER.info(() -> "Da lap xong va luu ca thi, branchId=" + branchId
                        + ", sessionNo=" + plan.session().sessionNo()
                        + ", roomAssignments=" + plan.session().roomAssignments().size()
                        + ", hallMonitors=" + plan.session().hallMonitorAssignments().size()
                        + ", selectionSeed=" + plan.selectionSeed());
                repository.updateBranchAfterSession(
                        branchId,
                        "SUCCESS",
                        "Da tao den ca " + plan.session().sessionNo(),
                        plan.session().sessionNo() + 1,
                        history.size() + 1
                );
                createdInRequest++;
                branch = repository.findBranchById(branchId);
            } catch (ValidationException exception) {
                int createdSoFar = createdInRequest;
                LOGGER.warning(() -> "Khong tao duoc ca moi cho branchId=" + branchId
                        + ", createdInRequest=" + createdSoFar + ": " + exception.getMessage());
                if (createdInRequest > 0) {
                    throw new ValidationException("Đã tạo được " + createdInRequest + "/" + sessionCount
                            + " ca. " + exception.getMessage());
                }
                throw exception;
            }
        }
        repository.updateBranchOutputStatus(branchId, "NONE", null);
        int totalCreated = createdInRequest;
        LOGGER.info(() -> "Hoan tat tao ca cho branchId=" + branchId + ", createdInRequest=" + totalCreated);
        return repository.findBranchById(branchId);
    }

    public ScheduleBranch resetBranch(String branchId, String overrideName) throws Exception {
        ScheduleBranch source = requireBranch(branchId);
        return createBranchAndFirstSession(
                overrideName == null || overrideName.isBlank() ? source.name() + " (reset)" : overrideName,
                source.staffDatasetId(),
                source.roomDatasetId(),
                source.requestedStaffCount(),
                source.requestedRoomCount()
        );
    }

    public List<ScheduleBranch> findAllBranches(boolean includeArchived) throws SQLException {
        return repository.findAllBranches(includeArchived);
    }

    public ScheduleBranch findBranchById(String branchId) throws SQLException {
        return repository.findBranchById(branchId);
    }

    public void archiveBranch(String branchId) throws SQLException {
        repository.archiveBranch(branchId);
    }

    public void renameBranch(String branchId, String name) throws SQLException {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Tên nhánh phân công không được để trống");
        }
        requireBranch(branchId);
        repository.renameBranch(branchId, name.trim());
    }

    public void unarchiveBranch(String branchId) throws SQLException {
        repository.unarchiveBranch(branchId);
    }

    public BranchDetail getBranchDetail(String branchId) throws Exception {
        ScheduleBranch branch = repository.findBranchById(branchId);
        if (branch == null) {
            return null;
        }
        return new BranchDetail(
                branch,
                findBranchSessions(branchId),
                repository.findBranchFile(branchId, SchedulingRepository.FILE_ROLE_OUTPUT_INVIGILATORS) != null,
                repository.findBranchFile(branchId, SchedulingRepository.FILE_ROLE_OUTPUT_MONITORS) != null
        );
    }

    public BranchSessionRecord getBranchSessionDetail(String branchId, int sessionNo) throws Exception {
        SchedulingRepository.BranchSessionRow row = repository.findBranchSessionRow(branchId, sessionNo);
        return row == null ? null : mapBranchSession(row);
    }

    public BranchPreview previewBranch(String branchId) throws Exception {
        ScheduleBranch branch = requireActiveBranch(branchId);
        List<StaffRecord> staffPool = loadStaffPool(branch.staffDatasetId());
        List<RoomRecord> roomPool = loadRoomPool(branch.roomDatasetId());
        List<BranchSessionRecord> history = findBranchSessions(branchId);
        boolean canCreate = planner.canCreateNextSession(
                staffPool,
                roomPool,
                branch.requestedStaffCount(),
                branch.requestedRoomCount(),
                history,
                Instant.now().toEpochMilli()
        );
        String message = canCreate
                ? "Có thể tạo thêm ca thi"
                : "Không thể tạo thêm ca thi. Hãy làm lại nhánh hoặc chọn bộ dữ liệu khác";
        return new BranchPreview(
                canCreate,
                branch.nextSessionNo(),
                staffPool.size(),
                roomPool.size(),
                planner.usedPairCount(history),
                planner.constrainedStaffCount(history),
                message
        );
    }

    public AssignmentFileContent getInvigilatorFile(String branchId) throws Exception {
        ensureOutputFilesGenerated(branchId);
        return repository.findBranchFile(branchId, SchedulingRepository.FILE_ROLE_OUTPUT_INVIGILATORS);
    }

    public AssignmentFileContent getMonitorFile(String branchId) throws Exception {
        ensureOutputFilesGenerated(branchId);
        return repository.findBranchFile(branchId, SchedulingRepository.FILE_ROLE_OUTPUT_MONITORS);
    }

    public AssignmentFileContent getSessionInvigilatorFile(String branchId, int sessionNo) throws Exception {
        ScheduleBranch branch = requireActiveBranch(branchId);
        BranchSessionRecord session = getRequiredBranchSession(branch.branchId(), sessionNo);
        AssignmentResult exportResult = new AssignmentResult(
                "SUCCESS",
                branch.message(),
                List.of(session.session())
        );
        return new AssignmentFileContent(
                "session_invigilators",
                "DANHSACH_COITHI_CA_" + sessionNo + ".xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                exportService.createInvigilatorWorkbookBytes(exportResult)
        );
    }

    public AssignmentFileContent getSessionMonitorFile(String branchId, int sessionNo) throws Exception {
        ScheduleBranch branch = requireActiveBranch(branchId);
        BranchSessionRecord session = getRequiredBranchSession(branch.branchId(), sessionNo);
        AssignmentResult exportResult = new AssignmentResult(
                "SUCCESS",
                branch.message(),
                List.of(session.session())
        );
        return new AssignmentFileContent(
                "session_monitors",
                "DANHSACH_GIAMSAT_CA_" + sessionNo + ".xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                exportService.createMonitorWorkbookBytes(exportResult)
        );
    }

    public List<BranchSessionRecord> findBranchSessions(String branchId) throws Exception {
        List<SchedulingRepository.BranchSessionRow> rows = repository.findBranchSessionRows(branchId);
        List<BranchSessionRecord> result = new ArrayList<>();
        for (SchedulingRepository.BranchSessionRow row : rows) {
            result.add(mapBranchSession(row));
        }
        return result;
    }

    private BranchSessionRecord mapBranchSession(SchedulingRepository.BranchSessionRow row) throws Exception {
        return new BranchSessionRecord(
                row.branchId(),
                row.sessionNo(),
                row.selectionSeed(),
                row.requestedStaffCount(),
                row.requestedRoomCount(),
                jsonService.fromJson(row.selectedStaffCodesJson(), List.class),
                jsonService.fromJson(row.selectedRoomNamesJson(), List.class),
                jsonService.fromJson(row.sessionJson(), SessionAssignment.class),
                jsonService.fromJson(row.summaryJson(), AssignmentSummary.class),
                row.createdAt()
        );
    }

    private BranchSessionRecord getRequiredBranchSession(String branchId, int sessionNo) throws Exception {
        BranchSessionRecord session = getBranchSessionDetail(branchId, sessionNo);
        if (session == null) {
            throw new ValidationException("Không tìm thấy chi tiết ca thi");
        }
        return session;
    }

    private void ensureOutputFilesGenerated(String branchId) throws Exception {
        ScheduleBranch branch = requireBranch(branchId);
        if (branch.sessionCreatedCount() == 0) {
            throw new ValidationException("Nhánh này chưa có ca thi để xuất file");
        }
        boolean hasInv = repository.findBranchFile(branchId, SchedulingRepository.FILE_ROLE_OUTPUT_INVIGILATORS) != null;
        boolean hasMon = repository.findBranchFile(branchId, SchedulingRepository.FILE_ROLE_OUTPUT_MONITORS) != null;
        if (hasInv && hasMon && "READY".equals(branch.outputStatus())) {
            LOGGER.info(() -> "Bo qua xuat file vi cache da san sang, branchId=" + branchId);
            return;
        }

        List<BranchSessionRecord> sessions = findBranchSessions(branchId);
        LOGGER.info(() -> "Bat dau xuat file tong hop cho branchId=" + branchId + ", sessionCount=" + sessions.size());
        AssignmentResult exportResult = new AssignmentResult(
                "SUCCESS",
                branch.message(),
                sessions.stream().map(BranchSessionRecord::session).toList()
        );
        byte[] invigilatorBytes = exportService.createInvigilatorWorkbookBytes(exportResult);
        byte[] monitorBytes = exportService.createMonitorWorkbookBytes(exportResult);
        repository.upsertBranchFile(
                branchId,
                SchedulingRepository.FILE_ROLE_OUTPUT_INVIGILATORS,
                "DANHSACH_PHANCONG.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                invigilatorBytes
        );
        repository.upsertBranchFile(
                branchId,
                SchedulingRepository.FILE_ROLE_OUTPUT_MONITORS,
                "DANHSACH_GIAMSAT.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                monitorBytes
        );
        repository.updateBranchOutputStatus(branchId, "READY", null);
        cleanupOutputCache(branchId);
        LOGGER.info(() -> "Xuat file tong hop thanh cong cho branchId=" + branchId
                + ", invigilatorBytes=" + invigilatorBytes.length
                + ", monitorBytes=" + monitorBytes.length);
    }

    private StaffDataset requireActiveStaffDataset(String datasetId) throws SQLException {
        StaffDataset dataset = repository.findStaffDatasetById(datasetId);
        if (dataset == null) {
            throw new ValidationException("Không tìm thấy bộ dữ liệu cán bộ");
        }
        if (dataset.archived()) {
            throw new ValidationException("Bộ dữ liệu cán bộ đang ở trạng thái ẩn");
        }
        return dataset;
    }

    private RoomDataset requireActiveRoomDataset(String datasetId) throws SQLException {
        RoomDataset dataset = repository.findRoomDatasetById(datasetId);
        if (dataset == null) {
            throw new ValidationException("Không tìm thấy bộ dữ liệu phòng");
        }
        if (dataset.archived()) {
            throw new ValidationException("Bộ dữ liệu phòng đang ở trạng thái ẩn");
        }
        return dataset;
    }

    private ScheduleBranch requireActiveBranch(String branchId) throws SQLException {
        ScheduleBranch branch = requireBranch(branchId);
        if (branch.archived()) {
            throw new ValidationException("Nhánh phân công đang ở trạng thái ẩn");
        }
        return branch;
    }

    private ScheduleBranch requireBranch(String branchId) throws SQLException {
        ScheduleBranch branch = repository.findBranchById(branchId);
        if (branch == null) {
            throw new ValidationException("Không tìm thấy nhánh phân công");
        }
        return branch;
    }

    private List<StaffRecord> loadStaffPool(String datasetId) throws IOException, SQLException {
        byte[] content = repository.findStaffDatasetContent(datasetId);
        if (content == null) {
            throw new ValidationException("Không đọc được nội dung bộ dữ liệu cán bộ");
        }
        LOGGER.info(() -> "Nap lai staff pool tu datasetId=" + datasetId + ", bytes=" + content.length);
        return inputService.parseStaff(new ByteArrayInputStream(content));
    }

    private List<RoomRecord> loadRoomPool(String datasetId) throws IOException, SQLException {
        byte[] content = repository.findRoomDatasetContent(datasetId);
        if (content == null) {
            throw new ValidationException("Không đọc được nội dung bộ dữ liệu phòng");
        }
        LOGGER.info(() -> "Nap lai room pool tu datasetId=" + datasetId + ", bytes=" + content.length);
        return inputService.parseRooms(new ByteArrayInputStream(content));
    }

    private void validateRequestedCounts(int staffDatasetSize, int roomDatasetSize, int requestedStaffCount, int requestedRoomCount) {
        if (requestedStaffCount <= 0 || requestedRoomCount <= 0) {
            throw new ValidationException("Số cán bộ và số phòng phải là số nguyên dương");
        }
        if (requestedStaffCount > staffDatasetSize) {
            throw new ValidationException("Số cán bộ vượt quá dữ liệu hiện có");
        }
        if (requestedRoomCount > roomDatasetSize) {
            throw new ValidationException("Số phòng vượt quá dữ liệu hiện có");
        }
        if (requestedStaffCount <= requestedRoomCount * 2) {
            throw new ValidationException("Số cán bộ phải lớn hơn gấp đôi số phòng để có ít nhất 1 giám sát hành lang");
        }
    }

    private void validateSessionCount(int sessionCount) {
        if (sessionCount <= 0) {
            throw new ValidationException("Số ca thi phải là số nguyên dương");
        }
    }

    private String defaultDatasetName(String name, String fileName, String prefix) {
        if (name != null && !name.isBlank()) {
            return name.trim();
        }
        return normalizeFileName(fileName, prefix) + " " + DEFAULT_NAME_FORMATTER.format(Instant.now());
    }

    private String defaultBranchName(String name) {
        if (name != null && !name.isBlank()) {
            return name.trim();
        }
        return "Branch " + DEFAULT_NAME_FORMATTER.format(Instant.now());
    }

    private String normalizeFileName(String fileName, String fallback) {
        return fileName == null || fileName.isBlank() ? fallback : fileName;
    }

    private String sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private void cleanupOutputCache(String branchId) {
        try {
            Path outputRoot = storageRoot.resolve("output-cache").resolve(branchId);
            if (Files.exists(outputRoot)) {
                Files.walk(outputRoot)
                        .sorted((a, b) -> b.compareTo(a))
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException ignored) {
                            }
                        });
            }
        } catch (IOException ignored) {
        }
    }
}
