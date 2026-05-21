package vn.edu.networkprogramming.assignserver.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import vn.edu.networkprogramming.assignserver.repository.SchedulingRepository;

class AssignmentApplicationServiceTest {

    @Test
    void uploadsDatasetsCreatesBranchAndExports() throws Exception {
        Path tempDir = Files.createTempDirectory("exam-scheduler-test");
        String jdbcUrl = "jdbc:h2:mem:exam_scheduler_test_" + System.nanoTime() + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        SchedulingRepository repository = new SchedulingRepository(jdbcUrl, "sa", "");
        repository.initialize();
        AssignmentApplicationService service = new AssignmentApplicationService(
                new ExcelAssignmentInputService(),
                new AssignmentPlanner(),
                new AssignmentWorkbookExportService(),
                repository,
                new JsonService(),
                tempDir
        );

        byte[] staffWorkbook = TestWorkbookFactory.staffWorkbook(new String[][]{
                {"1", "A", "01/01/1980", "GV01", "K1"},
                {"2", "B", "01/01/1980", "GV02", "K1"},
                {"3", "C", "01/01/1980", "GV03", "K1"},
                {"4", "D", "01/01/1980", "GV04", "K1"},
                {"5", "E", "01/01/1980", "GV05", "K1"},
                {"6", "F", "01/01/1980", "GV06", "K1"}
        });
        byte[] roomWorkbook = TestWorkbookFactory.roomWorkbook(new String[][]{
                {"1", "P101", "CS1"},
                {"2", "P102", "CS1"}
        });

        var staffDataset = service.uploadStaffDataset("Staff A", "staff.xlsx", staffWorkbook);
        var roomDataset = service.uploadRoomDataset("Room A", "room.xlsx", roomWorkbook);
        assertFalse(staffDataset.reused());
        assertFalse(roomDataset.reused());

        var branch = service.createBranchAndFirstSession("Branch 1", staffDataset.dataset().datasetId(), roomDataset.dataset().datasetId(), 6, 2);
        assertEquals(1, branch.sessionCreatedCount());

        branch = service.appendNextSession(branch.branchId());
        assertEquals(2, branch.sessionCreatedCount());

        var preview = service.previewBranch(branch.branchId());
        assertEquals(3, preview.nextSessionNo());

        var detail = service.getBranchDetail(branch.branchId());
        assertEquals(2, detail.sessions().size());

        var invFile = service.getInvigilatorFile(branch.branchId());
        assertNotNull(invFile);
        try (XSSFWorkbook workbook = new XSSFWorkbook(new java.io.ByteArrayInputStream(invFile.content()))) {
            assertEquals(2, workbook.getNumberOfSheets());
            assertTrue(workbook.getSheetAt(0).getLastRowNum() > 0);
        }
    }

    @Test
    void appendsSessionsWithOverrideCounts() throws Exception {
        Path tempDir = Files.createTempDirectory("exam-scheduler-test");
        String jdbcUrl = "jdbc:h2:mem:exam_scheduler_test_" + System.nanoTime() + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        SchedulingRepository repository = new SchedulingRepository(jdbcUrl, "sa", "");
        repository.initialize();
        AssignmentApplicationService service = new AssignmentApplicationService(
                new ExcelAssignmentInputService(),
                new AssignmentPlanner(),
                new AssignmentWorkbookExportService(),
                repository,
                new JsonService(),
                tempDir
        );

        byte[] staffWorkbook = TestWorkbookFactory.staffWorkbook(new String[][]{
                {"1", "A", "01/01/1980", "GV01", "K1"},
                {"2", "B", "01/01/1980", "GV02", "K1"},
                {"3", "C", "01/01/1980", "GV03", "K1"},
                {"4", "D", "01/01/1980", "GV04", "K1"},
                {"5", "E", "01/01/1980", "GV05", "K1"},
                {"6", "F", "01/01/1980", "GV06", "K1"},
                {"7", "G", "01/01/1980", "GV07", "K1"},
                {"8", "H", "01/01/1980", "GV08", "K1"}
        });
        byte[] roomWorkbook = TestWorkbookFactory.roomWorkbook(new String[][]{
                {"1", "P101", "CS1"},
                {"2", "P102", "CS1"},
                {"3", "P103", "CS1"}
        });

        var staffDataset = service.uploadStaffDataset("Staff A", "staff.xlsx", staffWorkbook);
        var roomDataset = service.uploadRoomDataset("Room A", "room.xlsx", roomWorkbook);
        var branch = service.createBranchAndFirstSession("Branch 1", staffDataset.dataset().datasetId(), roomDataset.dataset().datasetId(), 8, 3);

        branch = service.appendNextSessions(branch.branchId(), 1, 6, 2);
        assertEquals(2, branch.sessionCreatedCount());

        var detail = service.getBranchDetail(branch.branchId());
        assertEquals(2, detail.sessions().size());
        assertEquals(8, detail.sessions().get(0).requestedStaffCount());
        assertEquals(3, detail.sessions().get(0).requestedRoomCount());
        assertEquals(6, detail.sessions().get(1).requestedStaffCount());
        assertEquals(2, detail.sessions().get(1).requestedRoomCount());
    }
}
