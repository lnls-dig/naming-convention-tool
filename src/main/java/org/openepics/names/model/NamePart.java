package org.openepics.names.model;

import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

/**
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Entity
@Table(name = "name_part")
public class NamePart extends Persistable {

    private String uuid;

    @Enumerated(EnumType.STRING)
    private NamePartType namePartType;

    protected NamePart() {}

    public NamePart(String uuid, NamePartType namePartType) {
        this.uuid = uuid;
        this.namePartType = namePartType;
    }

    public String getUuid() { return uuid; }

    public NamePartType getNamePartType() { return namePartType; }

    @Override public boolean equals(Object other) {
        return other instanceof NamePart && ((NamePart) other).getUuid().equals(getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUuid());
    }
}
