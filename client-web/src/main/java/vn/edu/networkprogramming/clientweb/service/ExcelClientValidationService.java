package vn.edu.networkprogramming.clientweb.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import vn.edu.networkprogramming.clientweb.model.ClientValidationResult;

public class ExcelClientValidationService {

    public ClientValidationResult validateStaffFile(String fileName, byte[] content) {
        return validateWorkbook(content, 5);
    }

    public ClientValidationResult validateRoomFile(String fileName, byte[] content) {
        return validateWorkbook(content, 3);
    }

    public ClientValidationResult validatePositiveCount(String count, String label) {
        try {
            int value = Integer.parseInt(count);
            return value > 0
                    ? new ClientValidationResult(true, "OK")
                    : new ClientValidationResult(false, label + " phải là số nguyên dương");
        } catch (NumberFormatException exception) {
            return new ClientValidationResult(false, label + " phải là số nguyên dương");
        }
    }

    private ClientValidationResult validateWorkbook(byte[] content, int expectedColumns) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            var sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return new ClientValidationResult(false, "Workbook không hợp lệ");
            }
            Row header = sheet.getRow(0);
            if (header == null) {
                return new ClientValidationResult(false, "Thiếu dòng tiêu đề");
            }
            for (int index = 0; index < expectedColumns; index++) {
                if (header.getCell(index) == null) {
                    return new ClientValidationResult(false, "Sai cấu trúc cột Excel");
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

}
