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

    public Device(UUID uuid) {
        Preconditions.checkNotNull(uuid);
        this.uuid = uuid.toString();
    }

    public UUID getUuid() { return UUID.fromString(uuid); }

    @Override public boolean equals(Object other) {
        return other instanceof Device && ((Device) other).getUuid().equals(getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUuid());
    }
}
