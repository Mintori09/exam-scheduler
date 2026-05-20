package vn.edu.networkprogramming.assignserver.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

final class TestWorkbookFactory {

    private TestWorkbookFactory() {
    }

    static byte[] staffWorkbook(String[][] rows) throws IOException {
        return workbook(new String[]{"STT", "Họ và tên", "Ngày sinh", "Mã cán bộ", "Đơn vị công tác"}, rows);
    }

    static byte[] roomWorkbook(String[][] rows) throws IOException {
        return workbook(new String[]{"STT", "Phòng thi", "Địa điểm"}, rows);
    }

    private static byte[] workbook(String[] headers, String[][] rows) throws IOException {
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
            return output.toByteArray();
        }
    }
}
