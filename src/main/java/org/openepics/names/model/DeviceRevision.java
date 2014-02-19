package org.openepics.names.model;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author <a href="mailto:jakob.battelino@cosylab.com">Jakob Battelino Prelog</a>
 */
@Entity
@Table(name = "device_name", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"section_id", "device_type_id", "qualifier"})
})
@XmlRootElement
public class DeviceRevision extends Persistable {

    @JoinColumn(name = "device_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Device device;

    @JoinColumn(name = "requested_by", referencedColumnName = "id")
    @ManyToOne(optional = true)
    private UserAccount requestedBy;

    @Basic(optional = false)
    @NotNull
    @Column(name = "request_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date requestDate;

    @Column(name = "deleted")
    private boolean deleted;

    @JoinColumn(name = "section_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private NamePart section;

    @JoinColumn(name = "device_type_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private NamePart deviceType;

    @Column(name = "qualifier")
    private String qualifier;

    protected DeviceRevision() {}

    public DeviceRevision(Device device, UserAccount requesedBy, Date requestDate, boolean deleted, NamePart section, NamePart deviceType, String qualifier) {
        this.device = device;
        this.requestedBy = requesedBy;
        this.requestDate = requestDate;
        this.section = section;
        this.deviceType = deviceType;
        this.qualifier = qualifier;
        this.deleted = deleted;
    }

    public Device getDevice() { return device; }

    public UserAccount getRequestedBy() { return requestedBy; }

    public Date getRequestDate() { return requestDate; }

    public boolean isDeleted() { return deleted; }

    public NamePart getSection() { return section; }

    public NamePart getDeviceType() { return deviceType; }

    public String getQualifier() { return qualifier; }
}
