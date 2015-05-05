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

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;

/**
 * A static utility class for casting @Nullable values to non-@Nullable.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public class As {
    /**
     * The cast of the value declared nullable to the same type that does not permit null values. Throws an exception if
     * the input value is, in fact, null.
     */
    public static <T> T notNull(@Nullable T value) {
        Preconditions.checkNotNull(value);
        return value;
    }
}
