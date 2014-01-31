package org.openepics.names.services.restricted;

import com.google.common.base.Preconditions;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.openepics.names.model.NameCategory;
import org.openepics.names.model.NameHierarchy;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartRevisionStatus;
import org.openepics.names.model.NamePartType;
import org.openepics.names.services.NamePartService;
import org.openepics.names.ui.UserManager;
import org.openepics.names.util.Marker;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Stateless
public class RestrictedNamePartService {

    @Inject private UserManager userManager;
    @Inject private NamePartService namePartService;

    public NamePart namePartWithId(String uuid) {
        return namePartService.namePartWithId(uuid);
    }

    public NamePartRevision addNamePart(String name, String fullName, NamePartType nameType, @Nullable NamePart parent, String comment) {
        Preconditions.checkState(userManager.isLoggedIn());
        return namePartService.addNamePart(name, fullName, nameType, parent, userManager.getUser(), comment);

//        if (userManager.isEditor() && !nameCategory.isApprovalNeeded() && (parentBaseRevision == null || parentBaseRevision.getStatus() == NamePartRevisionStatus.APPROVED)) {
//            autoApprove(newRevision);
//        }
    }

    public NamePartRevision modifyNamePart(NamePart namePart, String name, String fullName, String comment) {
        Preconditions.checkState(userManager.isLoggedIn());
        return namePartService.modifyNamePart(namePart, name, fullName, userManager.getUser(), comment);

//        if (userManager.isEditor() && !baseRevision.getNameCategory().isApprovalNeeded() && isOriginalCreator(userManager.getUser(), namePart) && approvedParentRevision != null) {
//            autoApprove(newRevision);
//        }
    }

    public NamePartRevision deleteNamePart(NamePart namePart, String comment) {
        Preconditions.checkState(userManager.isLoggedIn());
        return namePartService.deleteNamePart(namePart, userManager.getUser(), comment);

//        if (userManager.isEditor() && !approvedRevision.getNameCategory().isApprovalNeeded() && isOriginalCreator(userManager.getUser(), namePart)) {
//            autoApprove(newRevision);
//        }
    }

    public NamePartRevision cancelChangesForNamePart(NamePart namePart, String comment) {
        return namePartService.cancelChangesForNamePart(namePart, userManager.getUser(), comment);
    }

    public void approveNamePartRevision(NamePartRevision namePartRevision) {
        Preconditions.checkState(userManager.isSuperUser());

        if (namePartRevision.getStatus() == NamePartRevisionStatus.PENDING) {
            namePartRevision.setStatus(NamePartRevisionStatus.APPROVED);
            namePartRevision.setProcessedBy(userManager.getUser());
            namePartRevision.setProcessDate(new Date());
            namePartRevision.setProcessorComment(null);
        } else if (namePartRevision.getStatus() == NamePartRevisionStatus.APPROVED) {
            Marker.doNothing();
        } else {
            throw new IllegalStateException();
        }
    }

    public void rejectNamePartRevision(NamePartRevision namePartRevision, String comment) {
        Preconditions.checkState(userManager.isSuperUser());
        namePartService.rejectNamePartRevision(namePartRevision, userManager.getUser(), comment);
    }

    public void approveNamePartRevisions(List<NamePartRevision> revisions, String comment) {
        Preconditions.checkState(userManager.isSuperUser());
        namePartService.approveNamePartRevisions(revisions, userManager.getUser(), comment);
    }

    public void rejectNamePartRevisions(List<NamePartRevision> revisions, String comment) {
        Preconditions.checkState(userManager.isSuperUser());
        namePartService.rejectNamePartRevisions(revisions, userManager.getUser(), comment);
    }

    public NameHierarchy nameHierarchy() {
        return namePartService.nameHierarchy();
    }

    public List<NamePart> approvedNames(boolean includeDeleted) {
        return namePartService.approvedNames(includeDeleted);
    }

    public List<NamePart> approvedNames() {
        return namePartService.approvedNames();
    }

    public List<NamePart> approvedOrPendingNames(@Nullable NameCategory category, boolean includeDeleted) {
        return namePartService.approvedOrPendingNames(category, includeDeleted);
    }

    public List<NamePart> approvedOrPendingNames() {
        return namePartService.approvedOrPendingNames();
    }

    public List<NamePart> namesWithChangesProposedByCurrentUser() {
        return namePartService.namesWithChangesProposedByUser(userManager.getUser());
    }

    public List<NamePart> namesWithCategory(NameCategory category) {
        return namePartService.namesWithCategory(category);
    }

    public List<NamePartRevision> revisions(NamePart namePart) {
        return namePartService.revisions(namePart);
    }

    public List<NameCategory> nameCategories() {
        return namePartService.nameCategories();
    }

    public List<NamePart> siblings(NamePart namePart) {
        return namePartService.siblings(namePart);
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
}
