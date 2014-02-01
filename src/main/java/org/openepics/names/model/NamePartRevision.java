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

import java.util.Date;
import javax.annotation.Nullable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Vasu V <vuppala@frib.msu.org>
 */
@Entity
@Table(name = "name_part_revision")
@XmlRootElement
public class NamePartRevision extends Persistable {

    @JoinColumn(name = "name_part_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private NamePart namePart;

    @JoinColumn(name = "requested_by", referencedColumnName = "id")
    @ManyToOne(optional = true)
    private @Nullable UserAccount requestedBy;

    @Basic(optional = false)
    @NotNull
    @Column(name = "request_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date requestDate;

    @Size(max = 255)
    @Column(name = "requestor_comment")
    private @Nullable String requestorComment;

    @Column(name = "deleted")
    private boolean deleted;

    @JoinColumn(name = "name_category_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private NameCategory nameCategory;

    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    @ManyToOne(optional = true)
    private NamePart parent;

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

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "status")
    private NamePartRevisionStatus status;

    @JoinColumn(name = "processed_by", referencedColumnName = "id")
    @ManyToOne(optional = true)
    private UserAccount processedBy;

    @Column(name = "process_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date processDate;

    @Size(max = 255)
    @Column(name = "processor_comment")
    private String processorComment;

    protected NamePartRevision() {
    }

    public NamePartRevision(NamePart namePart, @Nullable UserAccount requestedBy, Date requestDate, @Nullable String requestorComment, boolean deleted, NameCategory nameCategory, NamePart parent, String name, String fullName) {
        this.namePart = namePart;
        this.requestedBy = requestedBy;
        this.requestDate = requestDate;
        this.requestorComment = requestorComment;
        this.deleted = deleted;
        this.nameCategory = nameCategory;
        this.parent = parent;
        this.name = name;
        this.fullName = fullName;
        this.status = NamePartRevisionStatus.PENDING;
        this.processedBy = null;
        this.processDate = null;
        this.processorComment = null;
    }

    public NamePart getNamePart() { return namePart; }

    public @Nullable UserAccount getRequestedBy() { return requestedBy; }

    public Date getRequestDate() { return requestDate; }

    public @Nullable String getRequestorComment() { return requestorComment; }

    public boolean isDeleted() { return deleted; }

    public NameCategory getNameCategory() { return nameCategory; }

    public NamePart getParent() { return parent; }

    public String getName() { return name; }

    public String getFullName() { return fullName; }

    public NamePartRevisionStatus getStatus() { return status; }
    public void setStatus(NamePartRevisionStatus status) { this.status = status; }

    public UserAccount getProcessedBy() { return processedBy; }
    public void setProcessedBy(UserAccount processedBy) { this.processedBy = processedBy; }

    public Date getProcessDate() { return processDate; }
    public void setProcessDate(Date processDate) { this.processDate = processDate; }

    public String getProcessorComment() { return processorComment; }
    public void setProcessorComment(String processorComment) { this.processorComment = processorComment; }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are
        // not set
        if (!(object instanceof NamePartRevision)) {
            return false;
        }
        NamePartRevision other = (NamePartRevision) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.openepics.names.NamePartRevision[ id=" + id + " ]";
    }
}
