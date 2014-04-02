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
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * A revision of a NamePart entity representing its state at some point in time.
 *
 * @author Vasu V <vuppala@frib.msu.org>
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Entity
public class NamePartRevision extends Persistable {

    private @ManyToOne NamePart namePart;

    private Date requestDate;

    private @ManyToOne @Nullable UserAccount requestedBy;

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

    public NamePartRevision(NamePart namePart, Date requestDate, @Nullable UserAccount requestedBy, @Nullable String requesterComment, boolean deleted, @Nullable NamePart parent, String name, String mnemonic, String mnemonicEquivalenceClass) {
        Preconditions.checkNotNull(namePart);
        Preconditions.checkNotNull(requestDate);
        Preconditions.checkArgument(name != null && !name.isEmpty());
        Preconditions.checkArgument(mnemonic != null && !mnemonic.isEmpty());
        Preconditions.checkArgument(mnemonicEquivalenceClass != null && !mnemonicEquivalenceClass.isEmpty());
        this.namePart = namePart;
        this.requestDate = requestDate;
        this.requestedBy = requestedBy;
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

    public Date getRequestDate() { return requestDate; }

    public @Nullable UserAccount getRequestedBy() { return requestedBy; }

    public @Nullable String getRequesterComment() { return requesterComment; }

    public boolean isDeleted() { return deleted; }

    public @Nullable NamePart getParent() { return parent; }

    public String getName() { return name; }

    public String getMnemonic() { return mnemonic; }

    public String getMnemonicEquivalenceClass() { return mnemonicEquivalenceClass; }

    public NamePartRevisionStatus getStatus() { return status; }

    public @Nullable Date getProcessDate() { return processDate; }

    public @Nullable UserAccount getProcessedBy() { return processedBy; }

    public @Nullable String getProcessorComment() { return processorComment; }

    public void updateAsProcessed(NamePartRevisionStatus status, Date date, @Nullable UserAccount by, @Nullable String comment) {
        Preconditions.checkArgument(status != NamePartRevisionStatus.PENDING);
        Preconditions.checkNotNull(date);
        this.processDate = date;
        this.processedBy = by;
        this.processorComment = comment;
    }
}
