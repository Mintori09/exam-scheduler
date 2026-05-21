package vn.edu.networkprogramming.assignserver.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import vn.edu.networkprogramming.assignserver.model.AssignmentInput;
import vn.edu.networkprogramming.assignserver.model.RoomRecord;
import vn.edu.networkprogramming.assignserver.model.StaffRecord;
import vn.edu.networkprogramming.assignserver.service.exception.ValidationException;
import vn.edu.networkprogramming.assignserver.util.ExcelCellValueReader;

public class ExcelAssignmentInputService {

    private static final Logger LOGGER = Logger.getLogger(ExcelAssignmentInputService.class.getName());

    public AssignmentInput parse(InputStream staffStream, InputStream roomStream, int sessionCount) throws IOException {
        LOGGER.info(() -> "Bat dau doc cap file Excel dau vao, sessionCount=" + sessionCount);
        if (sessionCount <= 0) {
            throw new ValidationException("So ca thi phai la so nguyen duong");
        }
        List<StaffRecord> staffRecords = parseStaff(staffStream);
        List<RoomRecord> roomRecords = parseRooms(roomStream);
        if (staffRecords.isEmpty()) {
            throw new ValidationException("Danh sach can bo khong duoc rong");
        }
        if (roomRecords.isEmpty()) {
            throw new ValidationException("Danh sach phong thi khong duoc rong");
        }
        LOGGER.info(() -> "Doc xong cap file Excel: staffRecords=" + staffRecords.size() + ", roomRecords=" + roomRecords.size());
        return new AssignmentInput(staffRecords, roomRecords, sessionCount);
    }

    public List<StaffRecord> parseStaff(InputStream inputStream) throws IOException {
        LOGGER.info("Bat dau doc file danh sach can bo");
        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            var sheet = workbook.getSheetAt(0);
            ensureHasColumns(sheet.getRow(0), 5);
            List<StaffRecord> result = new ArrayList<>();
            Set<String> seenCodes = new HashSet<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isBlankRow(row, 5)) {
                    continue;
                }
                int stt = Integer.parseInt(ExcelCellValueReader.read(row.getCell(0)));
                String staffCode = required(ExcelCellValueReader.read(row.getCell(1)), "Ma can bo");
                String fullName = required(ExcelCellValueReader.read(row.getCell(2)), "Ho va ten");
                String birthDate = required(ExcelCellValueReader.read(row.getCell(3)), "Ngay sinh");
                String department = required(ExcelCellValueReader.read(row.getCell(4)), "Don vi cong tac");
                if (!seenCodes.add(staffCode)) {
                    throw new ValidationException("Trung ma can bo: " + staffCode);
                }
                result.add(new StaffRecord(stt, fullName, birthDate, staffCode, department));
            }
            LOGGER.info(() -> "Doc xong file danh sach can bo, so dong hop le=" + result.size());
            return result;
        } catch (ValidationException exception) {
            LOGGER.warning(() -> "Loi validate file can bo: " + exception.getMessage());
            throw exception;
        } catch (Exception exception) {
            LOGGER.warning(() -> "Khong doc duoc file can bo: " + exception.getMessage());
            throw new ValidationException("Khong doc duoc file danh sach can bo");
        }
    }

    public List<RoomRecord> parseRooms(InputStream inputStream) throws IOException {
        LOGGER.info("Bat dau doc file danh sach phong thi");
        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            var sheet = workbook.getSheetAt(0);
            ensureHasColumns(sheet.getRow(0), 3);
            List<RoomRecord> result = new ArrayList<>();
            Set<String> seenRooms = new HashSet<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isBlankRow(row, 3)) {
                    continue;
                }
                int stt = Integer.parseInt(ExcelCellValueReader.read(row.getCell(0)));
                String roomName = required(ExcelCellValueReader.read(row.getCell(1)), "Phong thi");
                String location = required(ExcelCellValueReader.read(row.getCell(2)), "Dia diem");
                if (!seenRooms.add(roomName)) {
                    throw new ValidationException("Trung phong thi: " + roomName);
                }
                result.add(new RoomRecord(stt, roomName, location));
            }
            LOGGER.info(() -> "Doc xong file danh sach phong thi, so dong hop le=" + result.size());
            return result;
        } catch (ValidationException exception) {
            LOGGER.warning(() -> "Loi validate file phong thi: " + exception.getMessage());
            throw exception;
        } catch (Exception exception) {
            LOGGER.warning(() -> "Khong doc duoc file phong thi: " + exception.getMessage());
            throw new ValidationException("Khong doc duoc file danh sach phong thi");
        }
    }

    private void ensureHasColumns(Row row, int expectedColumns) {
        if (row == null) {
            throw new ValidationException("Thieu dong tieu de");
        }
        for (int index = 0; index < expectedColumns; index++) {
            if (row.getCell(index) == null) {
                throw new ValidationException("Sai cau truc cot Excel");
            }
        }
    }

    private boolean isBlankRow(Row row, int width) {
        for (int i = 0; i < width; i++) {
            if (!ExcelCellValueReader.read(row.getCell(i)).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String required(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " khong duoc de trong");
        }
        return value.trim();
    }

}
