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

import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * A static utility class containing useful JPA methods.
 *
 * @author Marko Kolar  
 */
public class JpaHelper {
    /**
     * Executes the query and returns the single expected result, or null if the result is empty.
     * @param <T> class T 
     * @param query the query to execute
     * @throws NonUniqueResultException if the query returned more than a single result.
     * @return single result or null of the query.
     */
    public static <T> T getSingleResultOrNull(TypedQuery<T> query) {
        final List<T> results = query.getResultList();
        if (results.isEmpty()) return null;
        else if (results.size() == 1) return results.get(0);
        else throw new NonUniqueResultException();
    }
}
