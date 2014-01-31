package org.openepics.names.model;

import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Entity
@Table(name = "device")
public class Device extends Persistable {

    private String uuid;

    protected Device() {}

    public Device(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() { return uuid; }

    @Override public boolean equals(Object other) {
        return other instanceof Device && ((Device) other).getUuid().equals(getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUuid());
    }
}
