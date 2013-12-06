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
		@NamedQuery(name = "NCName.findByDiscipline", query = "SELECT n FROM NCName n WHERE n.section = :section"),
		@NamedQuery(name = "NCName.findBySignal", query = "SELECT n FROM NCName n WHERE n.section = :section"),
		@NamedQuery(name = "NCName.findByName", query = "SELECT n FROM NCName n WHERE n.section = :section"),
		@NamedQuery(name = "NCName.findByStatus", query = "SELECT n FROM NCName n WHERE n.section = :section"),
		@NamedQuery(name = "NCName.findByParts", query = "SELECT n FROM NCName n WHERE n.section = :section AND n.discipline = :discipline AND n.signal = :signal AND n.instanceIndex = :instanceIndex") })
public class NCName implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum NCNameStatus {
		INVALID, VALID, DELETED;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "id")
	private Integer id;

	@JoinColumn(name = "FK_NC_name_section_id", referencedColumnName = "id")
	@ManyToOne(optional = false)
	@Column(name = "section_id")
	private NameEvent section;

	@JoinColumn(name = "FK_NC_name_discipline_id", referencedColumnName = "id")
	@ManyToOne(optional = false)
	@Column(name = "discipline_id")
	private NameEvent discipline;

	@JoinColumn(name = "FK_NC_name_signal_id", referencedColumnName = "id")
	@ManyToOne(optional = true)
	@Column(name = "signal_id")
	private NameEvent signal;

	@Column(name = "instance_index")
	private Character instanceIndex;

	@Basic(optional = false)
	@Column(name = "name")
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

	public NCName(Integer id) {
		this.id = id;
	}

	public NCName(Integer id, NameEvent section, NameEvent discipline, NameEvent signal, Character instanceIndex, String name,
			NCNameStatus status, Integer version) {
		this.id = id;
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

	public Character getInstanceIndex() {
		return instanceIndex;
	}

	public void setInstanceIndex(Character instanceIndex) {
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
