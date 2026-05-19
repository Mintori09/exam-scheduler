package vn.edu.networkprogramming.assignserver.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import vn.edu.networkprogramming.assignserver.model.AssignmentInput;
import vn.edu.networkprogramming.assignserver.model.RoomRecord;
import vn.edu.networkprogramming.assignserver.model.StaffRecord;
import vn.edu.networkprogramming.assignserver.service.exception.ValidationException;
import vn.edu.networkprogramming.assignserver.util.ExcelCellValueReader;

public class ExcelAssignmentInputService {

    public AssignmentInput parse(InputStream staffStream, InputStream roomStream, int sessionCount) throws IOException {
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
        return new AssignmentInput(staffRecords, roomRecords, sessionCount);
    }

    private List<StaffRecord> parseStaff(InputStream inputStream) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            var sheet = workbook.getSheetAt(0);
            requireHeaders(sheet.getRow(0), List.of("STT", "Họ và tên", "Ngày sinh", "Mã cán bộ", "Đơn vị công tác"));
            List<StaffRecord> result = new ArrayList<>();
            Set<String> seenCodes = new HashSet<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isBlankRow(row, 5)) {
                    continue;
                }
                int stt = Integer.parseInt(ExcelCellValueReader.read(row.getCell(0)));
                String fullName = required(ExcelCellValueReader.read(row.getCell(1)), "Ho va ten");
                String birthDate = required(ExcelCellValueReader.read(row.getCell(2)), "Ngay sinh");
                String staffCode = required(ExcelCellValueReader.read(row.getCell(3)), "Ma can bo");
                String department = required(ExcelCellValueReader.read(row.getCell(4)), "Don vi cong tac");
                if (!seenCodes.add(staffCode)) {
                    throw new ValidationException("Trung ma can bo: " + staffCode);
                }
                result.add(new StaffRecord(stt, fullName, birthDate, staffCode, department));
            }
            return result;
        } catch (ValidationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ValidationException("Khong doc duoc file danh sach can bo");
        }
    }

    private List<RoomRecord> parseRooms(InputStream inputStream) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            var sheet = workbook.getSheetAt(0);
            requireHeaders(sheet.getRow(0), List.of("STT", "Phòng thi", "Địa điểm"));
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
            return result;
        } catch (ValidationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ValidationException("Khong doc duoc file danh sach phong thi");
        }
    }

    private void requireHeaders(Row row, List<String> expectedHeaders) {
        if (row == null) {
            throw new ValidationException("Thieu dong tieu de");
        }
        for (int i = 0; i < expectedHeaders.size(); i++) {
            String actual = normalize(ExcelCellValueReader.read(row.getCell(i)));
            String expected = normalize(expectedHeaders.get(i));
            if (!expected.equals(actual)) {
                throw new ValidationException("Sai cau truc tieu de Excel");
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

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
