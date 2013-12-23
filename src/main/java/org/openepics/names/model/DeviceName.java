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
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author <a href="mailto:jakob.battelino@cosylab.com">Jakob Battelino Prelog</a>
 */
@Entity
@Table(name = "device_name", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"section_id", "device_type_id", "instance_index"})
})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "DeviceName.findAll", query = "SELECT n FROM DeviceName n"),
    @NamedQuery(name = "DeviceName.findById", query = "SELECT n FROM DeviceName n WHERE n.id = :id"),
    @NamedQuery(name = "DeviceName.findBySection", query = "SELECT n FROM DeviceName n WHERE n.section = :section"),
    @NamedQuery(name = "DeviceName.findByDiscipline", query = "SELECT n FROM DeviceName n WHERE n.deviceType = :deviceType"),
    @NamedQuery(name = "DeviceName.findByStatus", query = "SELECT n FROM DeviceName n WHERE n.status = :status"),
    @NamedQuery(name = "DeviceName.findByParts", query = "SELECT n FROM DeviceName n WHERE n.section = :section AND n.deviceType = :deviceType AND n.instanceIndex = :instanceIndex")})
public class DeviceName extends Persistable {

    @Size(max = 64)
    @Column(name = "name_id")
    private String nameId;

    @JoinColumn(name = "section_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private NameEvent section;

    @JoinColumn(name = "device_type_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private NameEvent deviceType;

    @Column(name = "instance_index", length = 32)
    @Size(min = 1, max = 32)
    private String instanceIndex;

    @Basic(optional = false)
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private NameStatus status;

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

    protected DeviceName() {}

    public DeviceName(NameEvent section, NameEvent deviceType, String instanceIndex, NameStatus status) {
        this.section = section;
        this.deviceType = deviceType;
        this.instanceIndex = instanceIndex;
        this.status = status;
    }

    public NameEvent getSection() { return section; }
    public void setSection(NameEvent section) { this.section = section; }

    public NameEvent getDeviceType() { return deviceType; }
    public void setDeviceType(NameEvent deviceType) { this.deviceType = deviceType; }

    public String getInstanceIndex() { return instanceIndex; }
    public void setInstanceIndex(String instanceIndex) { this.instanceIndex = instanceIndex; }

    public NameStatus getStatus() { return status; }
    public void setStatus(NameStatus status) { this.status = status; }

    public String getNameId() { return nameId; }
    public void setNameId(String nameId) { this.nameId = nameId; }

    public Privilege getRequestedBy() { return requestedBy; }
    public void setRequestedBy(Privilege requestedBy) { this.requestedBy = requestedBy; }

    public Date getRequestDate() { return requestDate; }

    public Privilege getProcessedBy() { return processedBy; }
    public void setProcessedBy(Privilege processedBy) { this.processedBy = processedBy; }

    public Date getProcessDate() { return processDate; }
    public void setProcessDate(Date processDate) { this.processDate = processDate; }
}
