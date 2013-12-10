package org.openepics.names.model;

import java.io.Serializable;

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
		@NamedQuery(name = "NCName.findAll", query = "SELECT n FROM NCName n"),
		@NamedQuery(name = "NCName.findById", query = "SELECT n FROM NCName n WHERE n.id = :id"),
		@NamedQuery(name = "NCName.findBySection", query = "SELECT n FROM NCName n WHERE n.section = :section"),
		@NamedQuery(name = "NCName.findByDiscipline", query = "SELECT n FROM NCName n WHERE n.discipline = :discipline"),
		@NamedQuery(name = "NCName.findBySignal", query = "SELECT n FROM NCName n WHERE n.signal = :signal"),
		@NamedQuery(name = "NCName.findByName", query = "SELECT n FROM NCName n WHERE n.name = :name"),
		@NamedQuery(name = "NCName.findByStatus", query = "SELECT n FROM NCName n WHERE n.status = :status"),
		@NamedQuery(name = "NCName.findByParts", query = "SELECT n FROM NCName n WHERE n.section = :section AND n.discipline = :device AND n.signal = :signal AND n.instanceIndex = :instanceIndex") })
public class NCName implements Serializable {
	private static final long serialVersionUID = 3745635930595784338L;

	public enum NCNameStatus {
		INVALID, VALID, DELETED;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "id")
	private Integer id;

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
	private NCNameStatus status;

	@Basic(optional = false)
	@NotNull
	@Column(name = "version")
	@Version
	private Integer version;

	public NCName() {
		// EMPTY
	}

	public NCName(NameEvent section, NameEvent discipline, NameEvent signal, String instanceIndex, String name,
			NCNameStatus status, Integer version) {
		this.section = section;
		this.discipline = discipline;
		this.signal = signal;
		this.instanceIndex = instanceIndex;
		this.name = name;
		this.status = status;
		this.version = version;
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

	public NCNameStatus getStatus() {
		return status;
	}

	public void setStatus(NCNameStatus status) {
		this.status = status;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setId(Integer id) {
		this.id = id;
	}
}
