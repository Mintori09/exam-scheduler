package vn.edu.networkprogramming.assignserver.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import vn.edu.networkprogramming.assignserver.model.AssignmentResult;
import vn.edu.networkprogramming.assignserver.model.HallMonitorAssignment;
import vn.edu.networkprogramming.assignserver.model.RoomAssignment;
import vn.edu.networkprogramming.assignserver.model.SessionAssignment;

public class AssignmentWorkbookExportService {

    private static final int STREAMING_WINDOW_SIZE = 200;
    private static final int ROWS_PER_SHEET = 24;
    private static final int INVIGILATOR_COLUMN_COUNT = 6;
    private static final int MONITOR_COLUMN_COUNT = 4;
    private static final String NATIONAL_TITLE = "CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM";
    private static final String NATIONAL_SUBTITLE = "Độc lập - Tự do - Hạnh phúc";
    private static final Logger LOGGER = Logger.getLogger(AssignmentWorkbookExportService.class.getName());

    public void writeInvigilatorWorkbook(Path targetFile, AssignmentResult result) throws IOException {
        Files.createDirectories(targetFile.getParent());
        try (OutputStream outputStream = Files.newOutputStream(targetFile)) {
            outputStream.write(createInvigilatorWorkbookBytes(result));
        }
    }

    public void writeMonitorWorkbook(Path targetFile, AssignmentResult result) throws IOException {
        Files.createDirectories(targetFile.getParent());
        try (OutputStream outputStream = Files.newOutputStream(targetFile)) {
            outputStream.write(createMonitorWorkbookBytes(result));
        }
    }

    public byte[] createInvigilatorWorkbookBytes(AssignmentResult result) throws IOException {
        LOGGER.info(() -> "Bat dau tao workbook coi thi, sessions=" + result.sessions().size());
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(STREAMING_WINDOW_SIZE);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ExportStyles styles = createStyles(workbook);
            for (SessionAssignment session : result.sessions()) {
                writeInvigilatorSession(workbook, styles, session);
            }
            workbook.write(outputStream);
            byte[] resultBytes = outputStream.toByteArray();
            workbook.dispose();
            LOGGER.info(() -> "Tao xong workbook coi thi, bytes=" + resultBytes.length);
            return resultBytes;
        }
    }

    public byte[] createMonitorWorkbookBytes(AssignmentResult result) throws IOException {
        LOGGER.info(() -> "Bat dau tao workbook giam sat, sessions=" + result.sessions().size());
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(STREAMING_WINDOW_SIZE);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ExportStyles styles = createStyles(workbook);
            for (SessionAssignment session : result.sessions()) {
                writeMonitorSession(workbook, styles, session);
            }
            workbook.write(outputStream);
            byte[] resultBytes = outputStream.toByteArray();
            workbook.dispose();
            LOGGER.info(() -> "Tao xong workbook giam sat, bytes=" + resultBytes.length);
            return resultBytes;
        }
    }

    private void writeInvigilatorSession(SXSSFWorkbook workbook, ExportStyles styles, SessionAssignment session) {
        List<InvigilatorRowData> rows = new ArrayList<>();
        int stt = 1;
        for (RoomAssignment assignment : session.roomAssignments()) {
            rows.add(new InvigilatorRowData(
                    stt++,
                    assignment.invigilatorOne().staffCode(),
                    assignment.invigilatorOne().fullName(),
                    "X",
                    "",
                    assignment.room().roomName()
            ));
            rows.add(new InvigilatorRowData(
                    stt++,
                    assignment.invigilatorTwo().staffCode(),
                    assignment.invigilatorTwo().fullName(),
                    "",
                    "X",
                    assignment.room().roomName()
            ));
        }

        int sheetCount = Math.max(1, ceilSheetCount(rows.size()));
        LOGGER.info(() -> "Dang ghi session coi thi, sessionNo=" + session.sessionNo()
                + ", rows=" + rows.size() + ", sheetCount=" + sheetCount);
        for (int sheetIndex = 0; sheetIndex < sheetCount; sheetIndex++) {
            int fromIndex = sheetIndex * ROWS_PER_SHEET;
            int toIndex = Math.min(fromIndex + ROWS_PER_SHEET, rows.size());
            Sheet sheet = workbook.createSheet(sheetName("Ca " + session.sessionNo(), sheetIndex, sheetCount));
            configureInvigilatorColumns(sheet);
            int rowIndex = writeSheetPreamble(sheet, styles, INVIGILATOR_COLUMN_COUNT - 1);
            rowIndex = writeInvigilatorHeader(sheet, styles, rowIndex);
            for (int index = fromIndex; index < toIndex; index++) {
                writeInvigilatorDataRow(sheet, styles, rowIndex++, rows.get(index));
            }
        }
    }

    private void writeMonitorSession(SXSSFWorkbook workbook, ExportStyles styles, SessionAssignment session) {
        List<MonitorRowData> rows = new ArrayList<>();
        int stt = 1;
        for (HallMonitorAssignment assignment : session.hallMonitorAssignments()) {
            rows.add(new MonitorRowData(
                    stt++,
                    assignment.staff().staffCode(),
                    assignment.staff().fullName(),
                    assignment.rangeText()
            ));
        }

        int sheetCount = Math.max(1, ceilSheetCount(rows.size()));
        LOGGER.info(() -> "Dang ghi session giam sat, sessionNo=" + session.sessionNo()
                + ", rows=" + rows.size() + ", sheetCount=" + sheetCount);
        for (int sheetIndex = 0; sheetIndex < sheetCount; sheetIndex++) {
            int fromIndex = sheetIndex * ROWS_PER_SHEET;
            int toIndex = Math.min(fromIndex + ROWS_PER_SHEET, rows.size());
            Sheet sheet = workbook.createSheet(sheetName("Ca " + session.sessionNo(), sheetIndex, sheetCount));
            configureMonitorColumns(sheet);
            int rowIndex = writeSheetPreamble(sheet, styles, MONITOR_COLUMN_COUNT - 1);
            rowIndex = writeMonitorHeader(sheet, styles, rowIndex);
            for (int index = fromIndex; index < toIndex; index++) {
                writeMonitorDataRow(sheet, styles, rowIndex++, rows.get(index));
            }
        }
    }

    private int writeSheetPreamble(Sheet sheet, ExportStyles styles, int lastColumnIndex) {
        Row titleRow = sheet.createRow(0);
        createMergedCell(titleRow, 0, NATIONAL_TITLE, styles.titleStyle());
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, lastColumnIndex));

        Row subtitleRow = sheet.createRow(1);
        createMergedCell(subtitleRow, 0, NATIONAL_SUBTITLE, styles.subtitleStyle());
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, lastColumnIndex));

        sheet.createRow(2);
        return 3;
    }

    private int writeInvigilatorHeader(Sheet sheet, ExportStyles styles, int rowIndex) {
        Row row = sheet.createRow(rowIndex);
        createStyledCell(row, 0, "STT", styles.headerStyle());
        createStyledCell(row, 1, "Mã cán bộ", styles.headerStyle());
        createStyledCell(row, 2, "Họ và tên", styles.headerStyle());
        createStyledCell(row, 3, "Giám thị 1", styles.headerStyle());
        createStyledCell(row, 4, "Giám thị 2", styles.headerStyle());
        createStyledCell(row, 5, "Phòng thi", styles.headerStyle());
        return rowIndex + 1;
    }

    private int writeMonitorHeader(Sheet sheet, ExportStyles styles, int rowIndex) {
        Row row = sheet.createRow(rowIndex);
        createStyledCell(row, 0, "STT", styles.headerStyle());
        createStyledCell(row, 1, "Mã cán bộ", styles.headerStyle());
        createStyledCell(row, 2, "Họ và tên", styles.headerStyle());
        createStyledCell(row, 3, "Phòng thi được giám sát", styles.headerStyle());
        return rowIndex + 1;
    }

    private void writeInvigilatorDataRow(Sheet sheet, ExportStyles styles, int rowIndex, InvigilatorRowData rowData) {
        Row row = sheet.createRow(rowIndex);
        createStyledCell(row, 0, rowData.stt(), styles.bodyCenterStyle());
        createStyledCell(row, 1, rowData.staffCode(), styles.bodyStyle());
        createStyledCell(row, 2, rowData.fullName(), styles.bodyStyle());
        createStyledCell(row, 3, rowData.invigilatorOneMark(), styles.bodyCenterStyle());
        createStyledCell(row, 4, rowData.invigilatorTwoMark(), styles.bodyCenterStyle());
        createStyledCell(row, 5, rowData.roomName(), styles.bodyStyle());
    }

    private void writeMonitorDataRow(Sheet sheet, ExportStyles styles, int rowIndex, MonitorRowData rowData) {
        Row row = sheet.createRow(rowIndex);
        createStyledCell(row, 0, rowData.stt(), styles.bodyCenterStyle());
        createStyledCell(row, 1, rowData.staffCode(), styles.bodyStyle());
        createStyledCell(row, 2, rowData.fullName(), styles.bodyStyle());
        createStyledCell(row, 3, rowData.rangeText(), styles.bodyStyle());
    }

    private void configureInvigilatorColumns(Sheet sheet) {
        sheet.setColumnWidth(0, 10 * 256);
        sheet.setColumnWidth(1, 18 * 256);
        sheet.setColumnWidth(2, 30 * 256);
        sheet.setColumnWidth(3, 14 * 256);
        sheet.setColumnWidth(4, 14 * 256);
        sheet.setColumnWidth(5, 18 * 256);
    }

    private void configureMonitorColumns(Sheet sheet) {
        sheet.setColumnWidth(0, 10 * 256);
        sheet.setColumnWidth(1, 18 * 256);
        sheet.setColumnWidth(2, 30 * 256);
        sheet.setColumnWidth(3, 34 * 256);
    }

    private ExportStyles createStyles(Workbook workbook) {
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 15);

        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleStyle.setFont(titleFont);

        Font subtitleFont = workbook.createFont();
        subtitleFont.setBold(true);
        subtitleFont.setFontHeightInPoints((short) 13);

        CellStyle subtitleStyle = workbook.createCellStyle();
        subtitleStyle.setAlignment(HorizontalAlignment.CENTER);
        subtitleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        subtitleStyle.setFont(subtitleFont);

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setFont(headerFont);

        CellStyle bodyStyle = workbook.createCellStyle();
        bodyStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        bodyStyle.setBorderTop(BorderStyle.THIN);
        bodyStyle.setBorderBottom(BorderStyle.THIN);
        bodyStyle.setBorderLeft(BorderStyle.THIN);
        bodyStyle.setBorderRight(BorderStyle.THIN);

        CellStyle bodyCenterStyle = workbook.createCellStyle();
        bodyCenterStyle.cloneStyleFrom(bodyStyle);
        bodyCenterStyle.setAlignment(HorizontalAlignment.CENTER);

        return new ExportStyles(titleStyle, subtitleStyle, headerStyle, bodyStyle, bodyCenterStyle);
    }

    private void createMergedCell(Row row, int columnIndex, String value, CellStyle style) {
        createStyledCell(row, columnIndex, value, style);
    }

    private void createStyledCell(Row row, int columnIndex, String value, CellStyle style) {
        var cell = row.createCell(columnIndex);
        cell.setCellValue(value == null ? "" : value);
        cell.setCellStyle(style);
    }

    private void createStyledCell(Row row, int columnIndex, int value, CellStyle style) {
        var cell = row.createCell(columnIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private int ceilSheetCount(int rowCount) {
        return (rowCount + ROWS_PER_SHEET - 1) / ROWS_PER_SHEET;
    }

    private String sheetName(String base, int sheetIndex, int totalSheets) {
        if (totalSheets <= 1) {
            return base;
        }
        return base + " - " + (sheetIndex + 1);
    }

    private record ExportStyles(
            CellStyle titleStyle,
            CellStyle subtitleStyle,
            CellStyle headerStyle,
            CellStyle bodyStyle,
            CellStyle bodyCenterStyle
    ) {
    }

    private record InvigilatorRowData(
            int stt,
            String staffCode,
            String fullName,
            String invigilatorOneMark,
            String invigilatorTwoMark,
            String roomName
    ) {
    }

    private record MonitorRowData(
            int stt,
            String staffCode,
            String fullName,
            String rangeText
    ) {
    }
}
