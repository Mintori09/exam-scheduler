package vn.edu.networkprogramming.assignserver.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;

public final class ExcelCellValueReader {

    private static final DataFormatter FORMATTER = new DataFormatter();

    private ExcelCellValueReader() {
    }

    public static String read(Cell cell) {
        return cell == null ? "" : FORMATTER.formatCellValue(cell).trim();
    }
}
