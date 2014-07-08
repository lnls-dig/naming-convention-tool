package org.openepics.names.model;

import com.google.common.base.Preconditions;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Objects;
import java.util.UUID;

/**
 * An entity representing either a named section of the Area Structure or a named device type of the Device
 * Structure, depending on the specified namePartType.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Entity
public class NamePart extends Persistable {

    private String uuid;

    @Enumerated(EnumType.STRING)
    private NamePartType namePartType;

    protected NamePart() {}

    /**
     * @param uuid the universally unique identifier
     * @param namePartType the type of the NamePart
     */
    public NamePart(UUID uuid, NamePartType namePartType) {
        Preconditions.checkNotNull(uuid);
        Preconditions.checkNotNull(namePartType);
        this.uuid = uuid.toString();
        this.namePartType = namePartType;
    }

    /**
     * The universally unique identifier.
     */
    public UUID getUuid() { return UUID.fromString(uuid); }

    /**
     * The type of the NamePart.
     */
    public NamePartType getNamePartType() { return namePartType; }

    @Override public boolean equals(Object other) {
        return other instanceof NamePart && ((NamePart) other).getUuid().equals(getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUuid());
    }
}
