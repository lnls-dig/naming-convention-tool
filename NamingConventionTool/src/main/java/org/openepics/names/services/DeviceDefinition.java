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
package org.openepics.names.services;

import com.google.common.base.Objects;

import org.openepics.names.model.NamePart;

import javax.annotation.Nullable;

/**
 * Specifies all the parameters that define a new device to be added to the system.
 *
 * @author Marko Kolar 
 * @author Karin Rathsman 
 */
public final class DeviceDefinition {
    private final NamePart section;
    private final NamePart deviceType;
    private final @Nullable String instanceIndex;
    private final @Nullable String additionalInfo;

    /**
     * @param section the section containing the device
     * @param deviceType the type of the device
     * @param instanceIndex an additional identifier that, in combination with other attributes, determine the unique convention name of the device. Null if omitted.
     * @param additionalInfo Comment or description of the device
     */
    public DeviceDefinition(NamePart section, NamePart deviceType, @Nullable String instanceIndex, @Nullable String additionalInfo) {
        this.section = section;
        this.deviceType = deviceType;
        this.instanceIndex = instanceIndex;
        this.additionalInfo=additionalInfo;
    }

    /**
     * @return The section containing the device.
     */
    public NamePart section() { return section; }

    /**
     * @return The type of the device.
     */
    public NamePart deviceType() { return deviceType; }

    /**
     * @return An additional identifier that, in combination with other attributes, determine the unique convention name of the device. Null if omitted.
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
