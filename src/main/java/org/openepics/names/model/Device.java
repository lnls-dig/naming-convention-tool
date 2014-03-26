package org.openepics.names.model;

import com.google.common.base.Preconditions;

import javax.persistence.Entity;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Entity
public class Device extends Persistable {

    private UUID uuid;

    protected Device() {}

    public Device(UUID uuid) {
        Preconditions.checkNotNull(uuid);
        this.uuid = uuid;
    }

    public UUID getUuid() { return uuid; }

    @Override public boolean equals(Object other) {
        return other instanceof Device && ((Device) other).getUuid().equals(getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUuid());
    }
}
