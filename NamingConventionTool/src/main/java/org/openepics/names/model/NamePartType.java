package org.openepics.names.model;

/**
 * Type of a NamePart specifying whether it belongs to the Logical Area Structure or the Device
 * Category Structure.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public enum NamePartType {
    /** A (sub)section of the Logical Area Structure. */
    SECTION,

    /** A device (sub)type of the Device Category Structure. */
    DEVICE_TYPE
}
