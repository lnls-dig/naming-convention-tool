package org.openepics.names.model;

import com.google.common.base.Preconditions;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Entity
public class NamePart extends Persistable {

    private UUID uuid;

    @Enumerated(EnumType.STRING)
    private NamePartType namePartType;

    protected NamePart() {}

    public NamePart(UUID uuid, NamePartType namePartType) {
        Preconditions.checkNotNull(uuid);
        Preconditions.checkNotNull(namePartType);
        this.uuid = uuid;
        this.namePartType = namePartType;
    }

    public UUID getUuid() { return uuid; }

    public NamePartType getNamePartType() { return namePartType; }

    @Override public boolean equals(Object other) {
        return other instanceof NamePart && ((NamePart) other).getUuid().equals(getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUuid());
    }
}
