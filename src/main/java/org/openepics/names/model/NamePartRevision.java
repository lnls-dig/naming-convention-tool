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

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.Date;

/**
 *
 * @author Vasu V <vuppala@frib.msu.org>
 */
@Entity
public class NamePartRevision extends Persistable {

    private @ManyToOne NamePart namePart;

    private @ManyToOne @Nullable UserAccount requestedBy;

    private Date requestDate;

    private @Nullable String requesterComment;

    private boolean deleted;

    private @ManyToOne @Nullable NamePart parent;

    private String name;

    private String mnemonic;

    private String mnemonicEquivalenceClass;

    @Enumerated(EnumType.STRING)
    private NamePartRevisionStatus status;

    private @ManyToOne @Nullable UserAccount processedBy;

    private @Nullable Date processDate;

    private @Nullable String processorComment;

    protected NamePartRevision() {
    }

    public NamePartRevision(NamePart namePart, @Nullable UserAccount requestedBy, Date requestDate, @Nullable String requesterComment, boolean deleted, @Nullable NamePart parent, String name, String mnemonic, String mnemonicEquivalenceClass) {
        Preconditions.checkNotNull(namePart);
        Preconditions.checkNotNull(requestDate);
        Preconditions.checkArgument(name != null && !name.isEmpty());
        Preconditions.checkArgument(mnemonic != null && !mnemonic.isEmpty());
        Preconditions.checkArgument(mnemonicEquivalenceClass != null && !mnemonicEquivalenceClass.isEmpty());
        this.namePart = namePart;
        this.requestedBy = requestedBy;
        this.requestDate = requestDate;
        this.requesterComment = requesterComment;
        this.deleted = deleted;
        this.parent = parent;
        this.name = name;
        this.mnemonic = mnemonic;
        this.mnemonicEquivalenceClass = mnemonicEquivalenceClass;
        this.status = NamePartRevisionStatus.PENDING;
        this.processedBy = null;
        this.processDate = null;
        this.processorComment = null;
    }

    public NamePart getNamePart() { return namePart; }

    public @Nullable UserAccount getRequestedBy() { return requestedBy; }

    public Date getRequestDate() { return requestDate; }

    public @Nullable String getRequesterComment() { return requesterComment; }

    public boolean isDeleted() { return deleted; }

    public @Nullable NamePart getParent() { return parent; }

    public String getName() { return name; }

    public String getMnemonic() { return mnemonic; }

    public String getMnemonicEquivalenceClass() { return mnemonicEquivalenceClass; }

    public NamePartRevisionStatus getStatus() { return status; }
    public void setStatus(NamePartRevisionStatus status) { this.status = status; }

    public @Nullable UserAccount getProcessedBy() { return processedBy; }
    public void setProcessedBy(UserAccount processedBy) { this.processedBy = processedBy; }

    public @Nullable Date getProcessDate() { return processDate; }
    public void setProcessDate(Date processDate) { this.processDate = processDate; }

    public @Nullable String getProcessorComment() { return processorComment; }
    public void setProcessorComment(String processorComment) { this.processorComment = processorComment; }
}
