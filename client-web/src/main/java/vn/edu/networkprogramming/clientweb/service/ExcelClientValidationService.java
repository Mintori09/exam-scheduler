package vn.edu.networkprogramming.clientweb.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import vn.edu.networkprogramming.clientweb.model.ClientValidationResult;

public class ExcelClientValidationService {

    public ClientValidationResult validateStaffFile(String fileName, byte[] content) {
        return validateWorkbook(fileName, content, List.of("STT", "Họ và tên", "Ngày sinh", "Mã cán bộ", "Đơn vị công tác"));
    }

    public ClientValidationResult validateRoomFile(String fileName, byte[] content) {
        return validateWorkbook(fileName, content, List.of("STT", "Phòng thi", "Địa điểm"));
    }

    public ClientValidationResult validateSessionCount(String sessionCount) {
        try {
            int value = Integer.parseInt(sessionCount);
            return value > 0
                    ? new ClientValidationResult(true, "OK")
                    : new ClientValidationResult(false, "Số ca thi phải là số nguyên dương");
        } catch (NumberFormatException exception) {
            return new ClientValidationResult(false, "Số ca thi phải là số nguyên dương");
        }
    }

    private ClientValidationResult validateWorkbook(String fileName, byte[] content, List<String> headers) {
        if (fileName == null || !fileName.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            return new ClientValidationResult(false, "Chỉ chấp nhận file .xlsx");
        }
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            var sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return new ClientValidationResult(false, "Workbook không hợp lệ");
            }
            Row header = sheet.getRow(0);
            if (header == null) {
                return new ClientValidationResult(false, "Thiếu dòng tiêu đề");
            }
            for (int i = 0; i < headers.size(); i++) {
                String actual = cell(header, i).trim().toLowerCase(Locale.ROOT);
                String expected = headers.get(i).trim().toLowerCase(Locale.ROOT);
                if (!expected.equals(actual)) {
                    return new ClientValidationResult(false, "Sai cấu trúc tiêu đề Excel");
                }
            }
            if (sheet.getLastRowNum() < 1) {
                return new ClientValidationResult(false, "File Excel phải có ít nhất 1 dòng dữ liệu");
            }
            return new ClientValidationResult(true, "OK");
        } catch (IOException exception) {
            return new ClientValidationResult(false, "Không mở được workbook");
        }
    }

    private String cell(Row row, int index) {
        return row.getCell(index) == null ? "" : row.getCell(index).getStringCellValue();
    }
}
