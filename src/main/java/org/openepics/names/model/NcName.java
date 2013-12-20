package org.openepics.names.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * TODO
 * 
 * @author <a href="mailto:jakob.battelino@cosylba.com">Jakob Battelino
 *         Prelog</a>
 */
@Entity
@Table(name = "NC_name", uniqueConstraints = { @UniqueConstraint(columnNames = { "section_id", "discipline_id", "signal_id",
		"instance_index" }) })
@XmlRootElement
@NamedQueries({
		@NamedQuery(name = "NcName.findAll", query = "SELECT n FROM NcName n"),
		@NamedQuery(name = "NcName.findById", query = "SELECT n FROM NcName n WHERE n.id = :id"),
		@NamedQuery(name = "NcName.findBySection", query = "SELECT n FROM NcName n WHERE n.section = :section"),
		@NamedQuery(name = "NcName.findByDiscipline", query = "SELECT n FROM NcName n WHERE n.discipline = :discipline"),
		@NamedQuery(name = "NcName.findBySignal", query = "SELECT n FROM NcName n WHERE n.signal = :signal"),
		@NamedQuery(name = "NcName.findByName", query = "SELECT n FROM NcName n WHERE n.name = :name"),
		@NamedQuery(name = "NcName.findByStatus", query = "SELECT n FROM NcName n WHERE n.status = :status"),
		@NamedQuery(name = "NcName.findByParts", query = "SELECT n FROM NcName n WHERE n.section = :section AND n.discipline = :device AND n.signal = :signal AND n.instanceIndex = :instanceIndex") })
public class NcName implements Serializable {
	private static final long serialVersionUID = 3745635930595784338L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "id")
	private Integer id;

	@Size(max = 64)
    @Column(name = "name_id")
    private String nameId;
    
	@JoinColumn(name = "section_id", referencedColumnName = "id")
	@ManyToOne(optional = false)
	private NameEvent section;

	@JoinColumn(name = "discipline_id", referencedColumnName = "id")
	@ManyToOne(optional = false)
	private NameEvent discipline;

	@JoinColumn(name = "signal_id", referencedColumnName = "id")
	@ManyToOne(optional = true)
	private NameEvent signal;

	@Column(name = "instance_index", length = 32)
	@Size(min = 1, max = 32)
	private String instanceIndex;

	@Basic(optional = false)
	@Column(name = "name", length = 48)
	@Size(min = 9, max = 48)
	private String name;

	@Basic(optional = false)
	@Column(name = "status")
	@Enumerated(EnumType.STRING)
//	@Convert("ncStatusConverter")
	private NcNameStatus status;

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
    
    
	@Basic(optional = false)
	@NotNull
	@Column(name = "version")
	@Version
	private Integer version;

	public NcName() {
		// EMPTY
	}

	public NcName(NameEvent section, NameEvent discipline, NameEvent signal, String instanceIndex, String name,
			NcNameStatus status) {
		this.section = section;
		this.discipline = discipline;
		this.signal = signal;
		this.instanceIndex = instanceIndex;
		this.name = name;
		this.status = status;
	}

	public NameEvent getSection() {
		return section;
	}

	public void setSection(NameEvent section) {
		this.section = section;
	}

	public NameEvent getDiscipline() {
		return discipline;
	}

	public void setDiscipline(NameEvent discipline) {
		this.discipline = discipline;
	}

	public NameEvent getSignal() {
		return signal;
	}

	public void setSignal(NameEvent signal) {
		this.signal = signal;
	}

	public String getInstanceIndex() {
		return instanceIndex;
	}

	public void setInstanceIndex(String instanceIndex) {
		this.instanceIndex = instanceIndex;
	}

	public NcNameStatus getStatus() {
		return status;
	}

	public void setStatus(NcNameStatus status) {
		this.status = status;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Integer getId() {
		return id;
	}
    
    public String getNameId() {
        return nameId;
    }

    public void setNameId(String nameId) {
        this.nameId = nameId;
    }

    public Privilege getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(Privilege requestedBy) {
        this.requestedBy = requestedBy;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setProcessDate(Date processDate) {
        this.processDate = processDate;
    }

    public Privilege getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(Privilege processedBy) {
        this.processedBy = processedBy;
    }
    
    public Date getProcessDate() {
        return processDate;
    }

	public Integer getVersion() {
		return version;
	}
    
}


