package org.openepics.names.util;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public class As {
    public static <T> T notNull(@Nullable T value) {
        Preconditions.checkNotNull(value);
        return value;
    }
}
