package org.openepics.names.model;

import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Entity
@Table(name = "name_part")
public class NamePart extends Persistable {

    private String uuid;

    protected NamePart() {}

    public NamePart(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() { return uuid; }

    @Override public boolean equals(Object other) {
        return other instanceof NamePart && ((NamePart) other).getUuid().equals(getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUuid());
    }
}
