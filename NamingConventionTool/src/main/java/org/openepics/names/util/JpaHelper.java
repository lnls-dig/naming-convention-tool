package org.openepics.names.util;

import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * A static utility class containing useful JPA methods.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public class JpaHelper {
    /**
     * Executes the query and returns the single expected result, or null if the result is empty.
     *
     * @param query the query to execute
     * @throws NonUniqueResultException if the query returned more than a single result.
     */
    public static <T> T getSingleResultOrNull(TypedQuery<T> query) {
        final List<T> results = query.getResultList();
        if (results.isEmpty()) return null;
        else if (results.size() == 1) return results.get(0);
        else throw new NonUniqueResultException();
    }
}
