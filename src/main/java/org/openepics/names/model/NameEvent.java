/*
 * This software is Copyright by the Board of Trustees of Michigan
 * State University (c) Copyright 2012.
 * 
 * You may use this software under the terms of the GNU public license
 *  (GPL). The terms of this license are described at:
 *       http://www.gnu.org/licenses/gpl.txt
 * 
 * Contact Information:
 *   Facilitty for Rare Isotope Beam
 *   Michigan State University
 *   East Lansing, MI 48824-1321
 *   http://frib.msu.edu
 * 
 */
package org.openepics.names.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
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
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author Vasu V <vuppala@frib.msu.org>
 */
@Entity
@Table(name = "name_event")
@XmlRootElement
@NamedQueries({
		@NamedQuery(name = "NameEvent.findAll", query = "SELECT n FROM NameEvent n"),
		@NamedQuery(name = "NameEvent.findById", query = "SELECT n FROM NameEvent n WHERE n.id = :id"),
		@NamedQuery(name = "NameEvent.findByName", query = "SELECT n FROM NameEvent n WHERE n.name = :name"),
		@NamedQuery(name = "NameEvent.findByFullName", query = "SELECT n FROM NameEvent n WHERE n.fullName = :fullName"),
		@NamedQuery(name = "NameEvent.findByCategory", query = "SELECT n FROM NameEvent n WHERE n.nameCategory = :nameCategory"),
		@NamedQuery(name = "NameEvent.findByParentName", query = "SELECT n FROM NameEvent n WHERE n.parentName = :parentName"),
		@NamedQuery(name = "NameEvent.findByEventType", query = "SELECT n FROM NameEvent n WHERE n.eventType = :eventType"),
		@NamedQuery(name = "NameEvent.findByRequestedBy", query = "SELECT n FROM NameEvent n WHERE n.requestedBy = :requestedBy"),
		@NamedQuery(name = "NameEvent.findByRequestorComment", query = "SELECT n FROM NameEvent n WHERE n.requestorComment = :requestorComment"),
		@NamedQuery(name = "NameEvent.findByRequestDate", query = "SELECT n FROM NameEvent n WHERE n.requestDate = :requestDate"),
		@NamedQuery(name = "NameEvent.findByStatus", query = "SELECT n FROM NameEvent n WHERE n.status = :status"),
		@NamedQuery(name = "NameEvent.findByProcessedBy", query = "SELECT n FROM NameEvent n WHERE n.processedBy = :processedBy"),
		@NamedQuery(name = "NameEvent.findByProcessorComment", query = "SELECT n FROM NameEvent n WHERE n.processorComment = :processorComment"),
		@NamedQuery(name = "NameEvent.findByProcessDate", query = "SELECT n FROM NameEvent n WHERE n.processDate = :processDate"),
		@NamedQuery(name = "NameEvent.findByVersion", query = "SELECT n FROM NameEvent n WHERE n.version = :version") })
public class NameEvent implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "id")
	private Integer id;
	
	@Basic(optional = false)
	@Size(min = 1, max = 32)
	@NotNull
	@Column(name = "name")
	private String name;
	
	@Basic(optional = false)
	@Size(min = 1, max = 255)
	@NotNull
	@Column(name = "full_name")
	private String fullName;
	
	@JoinColumn(name = "name_category_id", referencedColumnName = "id")
	@ManyToOne(optional = false)
	private NameCategory nameCategory;
	
	@JoinColumn(name = "parent_name_id", referencedColumnName = "id")
	@ManyToOne(optional = true)
	private NameEvent parentName;
	
	@Basic(optional = false)
	@NotNull
	@Column(name = "event_type")
	private char eventType;
	
	@JoinColumn(name = "requested_by", referencedColumnName = "id")
	@ManyToOne(optional = false)
	private Privilege requestedBy;
	
	@Size(max = 255)
	@Column(name = "requestor_comment")
	private String requestorComment;
	
	@Basic(optional = false)
	@NotNull
	@Column(name = "request_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date requestDate;
	
	@Basic(optional = false)
	@NotNull
	@Size(max = 32)
	@Column(name = "status")
	private char status;
	
	@JoinColumn(name = "processed_by", referencedColumnName = "id")
	@ManyToOne(optional = true)
	private Privilege processedBy;
	
	@Size(max = 255)
	@Column(name = "processor_comment")
	private String processorComment;
	
	@Column(name = "process_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date processDate;
	
	@Basic(optional = false)
	@NotNull
	@Column(name = "version")
	@Version
	private int version;

	public NameEvent() {
		//EMPTY
	}

	public NameEvent(Integer id) {
		this.id = id;
	}

	public NameEvent(Integer id, char eventType, Privilege requestedBy,
			Date requestDate, char status, String name,
			String fullName, int version) {
		this.id = id;
		this.eventType = eventType;
		this.requestedBy = requestedBy;
		this.requestDate = requestDate;
		this.status = status;
		this.name = name;
		this.fullName = fullName;
		this.version = version;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public NameCategory getNameCategory() {
		return nameCategory;
	}

	public void setNameCategory(NameCategory nameCategory) {
		this.nameCategory = nameCategory;
	}
	
	public NameEvent getParentName() {
		return parentName;
	}
	
	public void setParentName(NameEvent parentName) {
		this.parentName = parentName;
	}

	public char getEventType() {
		return eventType;
	}

	public void setEventType(char eventType) {
		this.eventType = eventType;
	}

	public Privilege getRequestedBy() {
		return requestedBy;
	}

	public void setRequestedBy(Privilege requestedBy) {
		this.requestedBy = requestedBy;
	}

	public String getRequestorComment() {
		return requestorComment;
	}

	public void setRequestorComment(String requestorComment) {
		this.requestorComment = requestorComment;
	}

	public Date getRequestDate() {
		return requestDate;
	}

	public void setRequestDate(Date requestDate) {
		this.requestDate = requestDate;
	}

	public char getStatus() {
		return status;
	}

	public void setStatus(char status) {
		this.status = status;
	}

	public Privilege getProcessedBy() {
		return processedBy;
	}

	public void setProcessedBy(Privilege processedBy) {
		this.processedBy = processedBy;
	}

	public String getProcessorComment() {
		return processorComment;
	}

	public void setProcessorComment(String processorComment) {
		this.processorComment = processorComment;
	}

	public Date getProcessDate() {
		return processDate;
	}

	public void setProcessDate(Date processDate) {
		this.processDate = processDate;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (id != null ? id.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		//TODO: Warning - this method won't work in the case the id fields are
		// not set
		if (!(object instanceof NameEvent)) {
			return false;
		}
		NameEvent other = (NameEvent) object;
		if ((this.id == null && other.id != null)
				|| (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "org.openepics.names.NameEvent[ id=" + id + " ]";
	}

}
