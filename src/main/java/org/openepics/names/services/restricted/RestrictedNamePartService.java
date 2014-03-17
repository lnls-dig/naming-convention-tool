package org.openepics.names.services.restricted;

import com.google.common.base.Preconditions;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartRevisionStatus;
import org.openepics.names.model.NamePartType;
import org.openepics.names.model.UserAccount;
import org.openepics.names.services.NamePartService;
import org.openepics.names.services.SessionService;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Stateless
public class RestrictedNamePartService {

    @Inject private SessionService sessionService;
    @Inject private NamePartService namePartService;

    public NamePartRevision addNamePart(String name, String fullName, NamePartType nameType, @Nullable NamePart parent, String comment) {
        Preconditions.checkState(sessionService.isLoggedIn());
        return namePartService.addNamePart(name, fullName, nameType, parent, sessionService.user(), comment);

//        if (sessionService.isEditor() && !nameCategory.isApprovalNeeded() && (parentBaseRevision == null || parentBaseRevision.getStatus() == NamePartRevisionStatus.APPROVED)) {
//            autoApprove(newRevision);
//        }
    }

    public NamePartRevision modifyNamePart(NamePart namePart, String name, String fullName, String comment) {
        Preconditions.checkState(sessionService.isLoggedIn());
        return namePartService.modifyNamePart(namePart, name, fullName, sessionService.user(), comment);

//        if (sessionService.isEditor() && !baseRevision.getNameCategory().isApprovalNeeded() && isOriginalCreator(sessionService.user(), namePart) && approvedParentRevision != null) {
//            autoApprove(newRevision);
//        }
    }

    public NamePartRevision deleteNamePart(NamePart namePart, String comment) {
        Preconditions.checkState(sessionService.isLoggedIn());
        return namePartService.deleteNamePart(namePart, sessionService.user(), comment);

//        if (sessionService.isEditor() && !approvedRevision.getNameCategory().isApprovalNeeded() && isOriginalCreator(sessionService.user(), namePart)) {
//            autoApprove(newRevision);
//        }
    }

    public NamePartRevision cancelChangesForNamePart(NamePart namePart, String comment) {
        return namePartService.cancelChangesForNamePart(namePart, sessionService.user(), comment, false);
    }

    public NamePartRevision rejectChangesForNamePart(NamePart namePart, String comment) {
        Preconditions.checkState(sessionService.isSuperUser());
        return namePartService.cancelChangesForNamePart(namePart, sessionService.user(), comment, true);
    }

    public void approveNamePartRevision(NamePartRevision namePartRevision, String comment) {
        Preconditions.checkState(sessionService.isSuperUser());
        namePartService.approveNamePartRevision(namePartRevision, sessionService.user(), comment);
    }

    public List<NamePart> approvedNames(boolean includeDeleted) {
        return namePartService.approvedNames(includeDeleted);
    }

    public List<NamePart> approvedNames() {
        return namePartService.approvedNames();
    }

    public List<NamePart> approvedOrPendingNames(boolean includeDeleted) {
        return namePartService.approvedOrPendingNames(includeDeleted);
    }

    public List<NamePart> approvedOrPendingNames() {
        return namePartService.approvedOrPendingNames();
    }

    public List<NamePartRevision> currentApprovedRevisions(boolean includeDeleted) {
        return namePartService.currentApprovedRevisions(includeDeleted);
    }

    public List<NamePartRevision> currentPendingRevisions(boolean includeDeleted) {
        return namePartService.currentPendingRevisions(includeDeleted);
    }

    public List<NamePart> namesWithChangesProposedByCurrentUser() {
        return namePartService.namesWithChangesProposedByUser(sessionService.user());
    }

    public List<NamePartRevision> revisions(NamePart namePart) {
        return namePartService.revisions(namePart);
    }

    public @Nullable NamePartRevision approvedRevision(NamePart namePart) {
        return namePartService.approvedRevision(namePart);
    }

    public @Nullable NamePartRevision pendingRevision(NamePart namePart) {
        return namePartService.pendingRevision(namePart);
    }

    public NamePartRevision approvedOrElsePendingRevision(NamePart namePart) {
        return namePartService.approvedOrElsePendingRevision(namePart);
    }

    public NamePartRevision pendingOrElseApprovedRevision(NamePart namePart) {
        return namePartService.pendingOrElseApprovedRevision(namePart);
    }

    private void autoApprove(NamePartRevision namePartEvent, @Nullable UserAccount user) {
        namePartEvent.setStatus(NamePartRevisionStatus.APPROVED);
        namePartEvent.setProcessDate(new Date());
        namePartEvent.setProcessedBy(user);
        namePartEvent.setProcessorComment(null);
    }

    private boolean isOriginalCreator(UserAccount user, NamePart namePart) {
        throw new IllegalStateException(); // TODO
    }
}
