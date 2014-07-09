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
