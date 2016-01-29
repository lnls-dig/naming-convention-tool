/*-
* Copyright (c) 2014 European Spallation Source
* Copyright (c) 2014 Cosylab d.d.
*
* This file is part of Naming Service.
* Naming Service is free software: you can redistribute it and/or modify it under
* the terms of the GNU General Public License as published by the Free
* Software Foundation, either version 2 of the License, or any newer version.
*
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
* FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
* more details.
*
* You should have received a copy of the GNU General Public License along with
* this program. If not, see https://www.gnu.org/licenses/gpl-2.0.txt
*/
package org.openepics.names.util;

import javax.annotation.Nullable;

import org.apache.poi.ss.usermodel.Cell;

/**
 * A static utility class for reading single Excel file cell
 *
 * @author Andraz Pozar  
 */
public class ExcelCell {
    
    /**
     * Creating a String from Excel file cell. 
	 * @param cell Excel file cell. 
	 * @return Cell value as string. If cell contains numeric value, this value is cast to String. If there is no value for this cell, null is returned.
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
	 * @param cell Excel file cell. 
	 * @return Cell value as number. 
	 */
    public static double asNumber(Cell cell) {
        return cell.getNumericCellValue();
    }
}
