package vn.edu.networkprogramming.assignserver.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import vn.edu.networkprogramming.assignserver.model.AssignmentInput;
import vn.edu.networkprogramming.assignserver.service.exception.ValidationException;

class ExcelAssignmentInputServiceTest {

    private final ExcelAssignmentInputService service = new ExcelAssignmentInputService();

    @Test
    void parsesValidWorkbooks() throws Exception {
        AssignmentInput input = service.parse(
                workbookWithStaff(new String[][]{
                        {"1", "Nguyen Van A", "01/01/1980", "GV01", "Khoa A"},
                        {"2", "Nguyen Van B", "01/01/1981", "GV02", "Khoa B"},
                        {"3", "Nguyen Van C", "01/01/1982", "GV03", "Khoa C"},
                        {"4", "Nguyen Van D", "01/01/1983", "GV04", "Khoa D"}
                }),
                workbookWithRooms(new String[][]{
                        {"1", "P101", "Co so 1"},
                        {"2", "P102", "Co so 1"}
                }),
                1
        );

        assertEquals(4, input.staffRecords().size());
        assertEquals(2, input.roomRecords().size());
        assertEquals(1, input.sessionCount());
    }

    @Test
    void rejectsDuplicateStaffCodes() throws Exception {
        ValidationException error = assertThrows(ValidationException.class, () -> service.parse(
                workbookWithStaff(new String[][]{
                        {"1", "Nguyen Van A", "01/01/1980", "GV01", "Khoa A"},
                        {"2", "Nguyen Van B", "01/01/1981", "GV01", "Khoa B"}
                }),
                workbookWithRooms(new String[][]{
                        {"1", "P101", "Co so 1"}
                }),
                1
        ));

        assertEquals("Trung ma can bo: GV01", error.getMessage());
    }

    @Test
    void rejectsNonPositiveSessionCount() throws Exception {
        ValidationException error = assertThrows(ValidationException.class, () -> service.parse(
                workbookWithStaff(new String[][]{
                        {"1", "Nguyen Van A", "01/01/1980", "GV01", "Khoa A"},
                        {"2", "Nguyen Van B", "01/01/1981", "GV02", "Khoa B"}
                }),
                workbookWithRooms(new String[][]{
                        {"1", "P101", "Co so 1"}
                }),
                0
        ));

        assertEquals("So ca thi phai la so nguyen duong", error.getMessage());
    }

    private ByteArrayInputStream workbookWithStaff(String[][] rows) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("CanBo");
            var header = sheet.createRow(0);
            header.createCell(0).setCellValue("STT");
            header.createCell(1).setCellValue("Họ và tên");
            header.createCell(2).setCellValue("Ngày sinh");
            header.createCell(3).setCellValue("Mã cán bộ");
            header.createCell(4).setCellValue("Đơn vị công tác");
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

    private ByteArrayInputStream workbookWithRooms(String[][] rows) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("PhongThi");
            var header = sheet.createRow(0);
            header.createCell(0).setCellValue("STT");
            header.createCell(1).setCellValue("Phòng thi");
            header.createCell(2).setCellValue("Địa điểm");
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
