package org.openepics.names.model;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.io.Serializable;

/**
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

    public @Nullable Long getId() { return id; }

    public @Nullable Integer getVersion() { return version; }
}
