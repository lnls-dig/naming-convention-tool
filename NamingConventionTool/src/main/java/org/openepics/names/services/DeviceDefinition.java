package org.openepics.names.services;

import com.google.common.base.Objects;

import org.openepics.names.model.NamePart;

import javax.annotation.Nullable;

/**
 * Specifies all the parameters that define a new device to be added to the system.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public final class DeviceDefinition {
    private final NamePart section;
    private final NamePart deviceType;
    private final @Nullable String instanceIndex;
    private final @Nullable String additionalInfo;

    /**
     * @param section the section containing the device
     * @param deviceType the type of the device
     * @param instanceIndex an additional identifier that, in combination with other attributes, determine the unique
     * convention name of the device. Null if omitted.
     */
    public DeviceDefinition(NamePart section, NamePart deviceType, @Nullable String instanceIndex, @Nullable String additionalInfo) {
        this.section = section;
        this.deviceType = deviceType;
        this.instanceIndex = instanceIndex;
        this.additionalInfo=additionalInfo;
    }

    /**
     * The section containing the device.
     */
    public NamePart section() { return section; }

    /**
     * The type of the device.
     */
    public NamePart deviceType() { return deviceType; }

    /**
     * An additional identifier that, in combination with other attributes, determine the unique convention name of the
     * device. Null if omitted.
     */
    public @Nullable String instanceIndex() { return instanceIndex; }

    @Override public boolean equals(Object other) {
        if (other instanceof DeviceDefinition) {
            final DeviceDefinition otherDevice = (DeviceDefinition) other;
            return Objects.equal(section, otherDevice.section()) && Objects.equal(deviceType, otherDevice.deviceType()) && Objects.equal(instanceIndex, otherDevice.instanceIndex());
        } else {
            return false;
        }
    }

    @Override public int hashCode() {
        return Objects.hashCode(section(), deviceType(), instanceIndex());
    }

	public @Nullable String additionalInfo() {
		return additionalInfo;
	}
}
