package org.openepics.names.model;

import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
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
@NamedQueries({
    @NamedQuery(name = "DeviceRevision.findAll", query = "SELECT n FROM DeviceRevision n"),
    @NamedQuery(name = "DeviceRevision.findById", query = "SELECT n FROM DeviceRevision n WHERE n.id = :id"),
    @NamedQuery(name = "DeviceRevision.findBySection", query = "SELECT n FROM DeviceRevision n WHERE n.section = :section"),
    @NamedQuery(name = "DeviceRevision.findByDiscipline", query = "SELECT n FROM DeviceRevision n WHERE n.deviceType = :deviceType"),
    @NamedQuery(name = "DeviceRevision.findByStatus", query = "SELECT n FROM DeviceRevision n WHERE n.status = :status"),
    @NamedQuery(name = "DeviceRevision.findByParts", query = "SELECT n FROM DeviceRevision n WHERE n.section = :section AND n.deviceType = :deviceType AND n.qualifier = :qualifier")
})
public class DeviceRevision extends Persistable {

    @JoinColumn(name = "device_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Device device;

    @JoinColumn(name = "section_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private NamePart section;

    @JoinColumn(name = "device_type_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private NamePart deviceType;

    @Column(name = "qualifier")
    private String qualifier;

    @Basic(optional = false)
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private DeviceRevisionType status;

    @JoinColumn(name = "requested_by", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Privilege requestedBy;

    @Basic(optional = false)
    @NotNull
    @Column(name = "request_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date requestDate;

    @JoinColumn(name = "processed_by", referencedColumnName = "id")
    @ManyToOne(optional = true)
    private Privilege processedBy;

    @Column(name = "process_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date processDate;

    protected DeviceRevision() {}

    public DeviceRevision(NamePart section, NamePart deviceType, String qualifier, DeviceRevisionType status) {
        this.section = section;
        this.deviceType = deviceType;
        this.qualifier = qualifier;
        this.status = status;
    }

    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }

    public NamePart getSection() { return section; }
    public void setSection(NamePart section) { this.section = section; }

    public NamePart getDeviceType() { return deviceType; }
    public void setDeviceType(NamePart deviceType) { this.deviceType = deviceType; }

    public String getQualifier() { return qualifier; }
    public void setInstanceIndex(String qualifier) { this.qualifier = qualifier; }

    public DeviceRevisionType getStatus() { return status; }
    public void setStatus(DeviceRevisionType status) { this.status = status; }

    public Privilege getRequestedBy() { return requestedBy; }
    public void setRequestedBy(Privilege requestedBy) { this.requestedBy = requestedBy; }

    public Date getRequestDate() { return requestDate; }

    public Privilege getProcessedBy() { return processedBy; }
    public void setProcessedBy(Privilege processedBy) { this.processedBy = processedBy; }

    public Date getProcessDate() { return processDate; }
    public void setProcessDate(Date processDate) { this.processDate = processDate; }
}
