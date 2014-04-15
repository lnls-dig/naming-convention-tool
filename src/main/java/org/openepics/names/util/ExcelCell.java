package org.openepics.names.util;

import javax.annotation.Nullable;

import org.apache.poi.ss.usermodel.Cell;

/**
 * A static utility class for reading single Excel file cell
 *
 * @author Andraz Pozar <andraz.pozar@cosylab.com>
 */
public class ExcelCell {
    
    /**
     * Creating a String from Excel file cell. If cell contains numeric value, this value is cast to String. If there is no
     * value for this cell, null is returned.
     */
    public static @Nullable String asString(@Nullable Cell cell) {
        if (cell != null) {
            if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                return String.valueOf(cell.getNumericCellValue());
            } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                return cell.getStringCellValue() != null ? cell.getStringCellValue() : null;
            } else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                return null;
            } else {
                throw new UnhandledCaseException();
            }
        } else {
            return null;
        }
    }
    
    /**
     * Reading Excel file cell with numeric value and returning its value
     */
    public static double asNumber(Cell cell) {
        return cell.getNumericCellValue();
    }
}
