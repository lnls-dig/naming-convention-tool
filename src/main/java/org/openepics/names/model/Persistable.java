package org.openepics.names.model;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@MappedSuperclass
public class Persistable implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    protected Integer id;

    @Basic(optional = false)
    @NotNull
    @Column(name = "version")
    @Version
    private Integer version;

    public Integer getId() {
        return id;
    }

    public Integer getVersion() {
        return version;
    }
}
