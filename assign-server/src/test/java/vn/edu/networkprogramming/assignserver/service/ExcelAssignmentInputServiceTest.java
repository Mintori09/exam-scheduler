package vn.edu.networkprogramming.assignserver.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import vn.edu.networkprogramming.assignserver.service.exception.ValidationException;

class ExcelAssignmentInputServiceTest {

    private final ExcelAssignmentInputService service = new ExcelAssignmentInputService();

    @Test
    void parsesStaffByColumnOrder() throws Exception {
        assertEquals(2, service.parseStaff(workbookWithRows(new String[]{"TT", "Mã GV", "Họ Tên", "Ngày sinh", "Đơn vị công tác"},
                new String[][]{
                        {"1", "GV01", "Nguyen Van A", "01/01/1980", "Khoa A"},
                        {"2", "GV02", "Nguyen Van B", "01/01/1981", "Khoa B"}
                })).size());
    }

    @Test
    void parsesRoomsByColumnOrder() throws Exception {
        assertEquals(2, service.parseRooms(workbookWithRows(new String[]{"STT", "Phòng thi", "Ghi chú"},
                new String[][]{
                        {"1", "P101", "Da Nang"},
                        {"2", "P102", "Hue"}
                })).size());
    }

    @Test
    void rejectsDuplicateStaffCodes() throws Exception {
        ValidationException error = assertThrows(ValidationException.class, () -> service.parseStaff(
                workbookWithRows(new String[]{"TT", "Mã GV", "Họ Tên", "Ngày sinh", "Đơn vị công tác"},
                        new String[][]{
                                {"1", "GV01", "Nguyen Van A", "01/01/1980", "Khoa A"},
                                {"2", "GV01", "Nguyen Van B", "01/01/1981", "Khoa B"}
                        })));
        assertEquals("Trung ma can bo: GV01", error.getMessage());
    }

    private ByteArrayInputStream workbookWithRows(String[] headers, String[][] rows) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Sheet1");
            var header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            for (int i = 0; i < rows.length; i++) {
                var row = sheet.createRow(i + 1);
                for (int j = 0; j < rows[i].length; j++) {
                    row.createCell(j).setCellValue(rows[i][j]);
                }
            }
            workbook.write(output);
            return new ByteArrayInputStream(output.toByteArray());
        }
    }
}
