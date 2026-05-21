package vn.edu.networkprogramming.assignserver.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import vn.edu.networkprogramming.assignserver.model.AssignmentResult;
import vn.edu.networkprogramming.assignserver.model.HallMonitorAssignment;
import vn.edu.networkprogramming.assignserver.model.RoomAssignment;
import vn.edu.networkprogramming.assignserver.model.RoomRecord;
import vn.edu.networkprogramming.assignserver.model.SessionAssignment;
import vn.edu.networkprogramming.assignserver.model.StaffRecord;

class AssignmentWorkbookExportServiceTest {

    @Test
    void splitsSingleSessionAcrossSheetsAndWritesNationalHeader() throws Exception {
        AssignmentWorkbookExportService service = new AssignmentWorkbookExportService();

        List<RoomAssignment> roomAssignments = new ArrayList<>();
        for (int index = 1; index <= 16; index++) {
            roomAssignments.add(new RoomAssignment(
                    new RoomRecord(index, "P" + (100 + index), "CS1"),
                    new StaffRecord(index * 2 - 1, "Can bo " + (index * 2 - 1), "01/01/1990", "GV" + (index * 2 - 1), "Khoa"),
                    new StaffRecord(index * 2, "Can bo " + (index * 2), "01/01/1990", "GV" + (index * 2), "Khoa")
            ));
        }

        List<HallMonitorAssignment> monitors = new ArrayList<>();
        for (int index = 1; index <= 31; index++) {
            monitors.add(new HallMonitorAssignment(
                    new StaffRecord(index, "Giam sat " + index, "01/01/1990", "GS" + index, "Khoa"),
                    "Từ P" + (100 + index) + " đến P" + (100 + index + 1),
                    2
            ));
        }

        SessionAssignment session = new SessionAssignment(1, roomAssignments, monitors);
        AssignmentResult result = new AssignmentResult("SUCCESS", "ok", List.of(session));

        byte[] invigilatorBytes = service.createInvigilatorWorkbookBytes(result);
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(invigilatorBytes))) {
            assertEquals(2, workbook.getNumberOfSheets());
            assertEquals("CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM", workbook.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
            assertEquals("Độc lập - Tự do - Hạnh phúc", workbook.getSheetAt(0).getRow(1).getCell(0).getStringCellValue());
        }

        byte[] monitorBytes = service.createMonitorWorkbookBytes(result);
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(monitorBytes))) {
            assertEquals(2, workbook.getNumberOfSheets());
            assertEquals("CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM", workbook.getSheetAt(1).getRow(0).getCell(0).getStringCellValue());
        }
    }
}
