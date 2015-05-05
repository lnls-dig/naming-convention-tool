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
package org.openepics.names.model;

import com.google.common.base.Preconditions;

import javax.persistence.Entity;
import java.util.Objects;
import java.util.UUID;

/**
 * An entity representing a device.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Entity
public class Device extends Persistable {

    private String uuid;

    protected Device() {}

    /**
     * @param uuid the universally unique identifier
     */
    public Device(UUID uuid) {
        Preconditions.checkNotNull(uuid);
        this.uuid = uuid.toString();
    }

    /**
     * The universally unique identifier.
     */
    public UUID getUuid() { return UUID.fromString(uuid); }

    @Override public boolean equals(Object other) {
        return other instanceof Device && ((Device) other).getUuid().equals(getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUuid());
    }
}
