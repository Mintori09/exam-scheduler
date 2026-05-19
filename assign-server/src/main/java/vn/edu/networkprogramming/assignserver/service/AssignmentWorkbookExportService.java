package vn.edu.networkprogramming.assignserver.service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import vn.edu.networkprogramming.assignserver.model.AssignmentResult;
import vn.edu.networkprogramming.assignserver.model.HallMonitorAssignment;
import vn.edu.networkprogramming.assignserver.model.RoomAssignment;
import vn.edu.networkprogramming.assignserver.model.SessionAssignment;

public class AssignmentWorkbookExportService {

    private static final int STREAMING_WINDOW_SIZE = 200;

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
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(STREAMING_WINDOW_SIZE);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (SessionAssignment session : result.sessions()) {
                Sheet sheet = workbook.createSheet("Ca " + session.sessionNo());
                var header1 = sheet.createRow(0);
                header1.createCell(0).setCellValue("STT");
                header1.createCell(1).setCellValue("Mã GV");
                header1.createCell(2).setCellValue("Họ và tên");
                header1.createCell(3).setCellValue("Giám thị 1");
                header1.createCell(4).setCellValue("Giám thị 2");
                header1.createCell(5).setCellValue("Phòng thi");
                int rowIndex = 1;
                int stt = 1;
                for (RoomAssignment assignment : session.roomAssignments()) {
                    rowIndex = writeInvigilatorRow(sheet, rowIndex, stt++, assignment, true);
                    rowIndex = writeInvigilatorRow(sheet, rowIndex, stt++, assignment, false);
                }
            }
            workbook.write(outputStream);
            byte[] resultBytes = outputStream.toByteArray();
            workbook.dispose();
            return resultBytes;
        }
    }

    public byte[] createMonitorWorkbookBytes(AssignmentResult result) throws IOException {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(STREAMING_WINDOW_SIZE);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (SessionAssignment session : result.sessions()) {
                Sheet sheet = workbook.createSheet("Ca " + session.sessionNo());
                var header = sheet.createRow(0);
                header.createCell(0).setCellValue("STT");
                header.createCell(1).setCellValue("Mã GV");
                header.createCell(2).setCellValue("Họ và tên");
                header.createCell(3).setCellValue("Phòng thi được giám sát");
                int rowIndex = 1;
                int stt = 1;
                for (HallMonitorAssignment assignment : session.hallMonitorAssignments()) {
                    var row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(stt++);
                    row.createCell(1).setCellValue(assignment.staff().staffCode());
                    row.createCell(2).setCellValue(assignment.staff().fullName());
                    row.createCell(3).setCellValue(assignment.rangeText());
                }
            }
            workbook.write(outputStream);
            byte[] resultBytes = outputStream.toByteArray();
            workbook.dispose();
            return resultBytes;
        }
    }

    private int writeInvigilatorRow(Sheet sheet, int rowIndex, int stt, RoomAssignment assignment, boolean first) {
        var staff = first ? assignment.invigilatorOne() : assignment.invigilatorTwo();
        var row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue(stt);
        row.createCell(1).setCellValue(staff.staffCode());
        row.createCell(2).setCellValue(staff.fullName());
        row.createCell(3).setCellValue(first ? "X" : "");
        row.createCell(4).setCellValue(first ? "" : "X");
        row.createCell(5).setCellValue(assignment.room().roomName());
        return rowIndex;
    }

}
