package org.openepics.names.model;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.io.Serializable;

/**
 * A superclass implementing the properties required by JPA. It that should be extended by all classes that need to be
 * persisted to the database.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@MappedSuperclass
public class Persistable implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected @Nullable Long id;

    @Version
    private @Nullable Integer version;

    /**
     * The JPA entity ID.
     */
    public @Nullable Long getId() { return id; }

    /**
     * The JPA entity version.
     */
    public @Nullable Integer getVersion() { return version; }
}
