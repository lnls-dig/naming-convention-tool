package org.openepics.names.util;

/**
 * A static utility class for marking places in code in a standard, IDE-aware fashion.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public class Marker {

    /**
     * Marks that the code branch does nothing.
     */
    public static void doNothing() {}

    /**
     * TODO marker.
     *
     * @param comment description of the task
     */
    public static void todo(String comment) {}
}
