package vn.edu.networkprogramming.assignserver.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import vn.edu.networkprogramming.assignserver.model.AssignmentRun;
import vn.edu.networkprogramming.assignserver.repository.AssignmentRunRepository;

class AssignmentApplicationServiceIntegrationTest {

    @ParameterizedTest(name = "sessionCount={0}")
    @ValueSource(ints = {1, 2, 3, 4})
    void createsExportWorkbooksForRealDatasetWhenSessionCountIsWithinSupportedRange(int sessionCount) throws Exception {
        Path staffFile = Path.of("..", "CANBOCOITHI.XLSX").toAbsolutePath().normalize();
        Path roomFile = Path.of("..", "PHONGTHI.XLSX").toAbsolutePath().normalize();
        assertTrue(Files.exists(staffFile), "Missing test fixture: " + staffFile);
        assertTrue(Files.exists(roomFile), "Missing test fixture: " + roomFile);

        Path testOutputRoot = prepareTestOutputRoot(sessionCount);
        Path dbFile = testOutputRoot.resolve("assignments.db");
        Path storageRoot = testOutputRoot.resolve("runs");
        AssignmentRunRepository repository = new AssignmentRunRepository(dbFile);
        repository.initialize();
        AssignmentApplicationService service = new AssignmentApplicationService(
                new ExcelAssignmentInputService(),
                new AssignmentPlanner(),
                new AssignmentWorkbookExportService(),
                repository,
                new JsonService(),
                storageRoot
        );

        AssignmentRun run;
        try (InputStream staffStream = Files.newInputStream(staffFile);
             InputStream roomStream = Files.newInputStream(roomFile)) {
            run = service.createAssignment(staffStream, roomStream, sessionCount);
        }

        assertNotNull(run.assignmentId());
        assertEquals("SUCCESS", run.status());
        assertEquals("Phan cong thanh cong", run.message());
        assertEquals(sessionCount, run.sessionCount());
        assertEquals(sessionCount, run.completedSessionCount());
        assertEquals(1200, run.roomCount());
        assertEquals(2500, run.staffCount());
        var invigilatorFile = repository.findFile(run.assignmentId(), AssignmentApplicationService.FILE_ROLE_OUTPUT_INVIGILATORS);
        var monitorFile = repository.findFile(run.assignmentId(), AssignmentApplicationService.FILE_ROLE_OUTPUT_MONITORS);
        assertNotNull(invigilatorFile);
        assertNotNull(monitorFile);
        assertWorkbookHasSessionSheetsAndData(invigilatorFile.content(), sessionCount);
        assertWorkbookHasSessionSheetsAndData(monitorFile.content(), sessionCount);

        AssignmentRun persisted = repository.findById(run.assignmentId());
        assertNotNull(persisted);
        assertEquals(run.status(), persisted.status());
        assertEquals(run.message(), persisted.message());
    }

    @ParameterizedTest(name = "sessionCount={0}")
    @ValueSource(ints = {5, 10, 20})
    void createsExportWorkbooksForRealDatasetForHigherSessionCounts(int sessionCount) throws Exception {
        Path staffFile = Path.of("..", "CANBOCOITHI.XLSX").toAbsolutePath().normalize();
        Path roomFile = Path.of("..", "PHONGTHI.XLSX").toAbsolutePath().normalize();
        assertTrue(Files.exists(staffFile), "Missing test fixture: " + staffFile);
        assertTrue(Files.exists(roomFile), "Missing test fixture: " + roomFile);

        Path testOutputRoot = prepareTestOutputRoot(sessionCount);
        Path dbFile = testOutputRoot.resolve("assignments.db");
        Path storageRoot = testOutputRoot.resolve("runs");
        AssignmentRunRepository repository = new AssignmentRunRepository(dbFile);
        repository.initialize();
        AssignmentApplicationService service = new AssignmentApplicationService(
                new ExcelAssignmentInputService(),
                new AssignmentPlanner(),
                new AssignmentWorkbookExportService(),
                repository,
                new JsonService(),
                storageRoot
        );

        AssignmentRun run;
        try (InputStream staffStream = Files.newInputStream(staffFile);
             InputStream roomStream = Files.newInputStream(roomFile)) {
            run = service.createAssignment(staffStream, roomStream, sessionCount);
        }

        assertNotNull(run.assignmentId());
        assertEquals("SUCCESS", run.status());
        assertEquals("Phan cong thanh cong", run.message());
        assertEquals(sessionCount, run.sessionCount());
        assertEquals(sessionCount, run.completedSessionCount());
        assertEquals(1200, run.roomCount());
        assertEquals(2500, run.staffCount());
        var invigilatorFile = repository.findFile(run.assignmentId(), AssignmentApplicationService.FILE_ROLE_OUTPUT_INVIGILATORS);
        var monitorFile = repository.findFile(run.assignmentId(), AssignmentApplicationService.FILE_ROLE_OUTPUT_MONITORS);
        assertNotNull(invigilatorFile);
        assertNotNull(monitorFile);
        assertWorkbookHasSessionSheetsAndData(invigilatorFile.content(), sessionCount);
        assertWorkbookHasSessionSheetsAndData(monitorFile.content(), sessionCount);

        AssignmentRun persisted = repository.findById(run.assignmentId());
        assertNotNull(persisted);
        assertEquals(run.status(), persisted.status());
        assertEquals(run.message(), persisted.message());
    }

    private void assertWorkbookHasSessionSheetsAndData(byte[] workbookBytes, int sessionCount) throws Exception {
        try (InputStream inputStream = new ByteArrayInputStream(workbookBytes);
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            assertEquals(sessionCount, workbook.getNumberOfSheets());
            assertEquals("Ca 1", workbook.getSheetAt(0).getSheetName());
            assertNotNull(workbook.getSheetAt(0).getRow(0));
            assertNotNull(workbook.getSheetAt(0).getRow(1));
            assertTrue(workbook.getSheetAt(0).getPhysicalNumberOfRows() > 1);
        }
    }

    private Path prepareTestOutputRoot(int sessionCount) throws Exception {
        Path testOutputRoot = Path.of("target", "test-exports", "session-" + sessionCount).toAbsolutePath().normalize();
        deleteDirectoryIfExists(testOutputRoot);
        Files.createDirectories(testOutputRoot);
        return testOutputRoot;
    }

    private void deleteDirectoryIfExists(Path directory) throws Exception {
        if (Files.notExists(directory)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (Exception exception) {
                            throw new RuntimeException(exception);
                        }
                    });
        }
    }
}
