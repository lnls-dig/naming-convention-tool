package org.openepics.names.services;

import com.google.common.base.Preconditions;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.openepics.names.model.NameCategory;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartRevisionStatus;
import org.openepics.names.model.NamePartRevisionType;
import org.openepics.names.model.Privilege;
import org.openepics.names.ui.UserManager;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Stateless
public class NamePartService {

    @Inject private UserManager userManager;
    @PersistenceContext private EntityManager em;

    public NamePartRevision addNamePart(String name, String fullName, NameCategory nameCategory, @Nullable NamePart parent, String comment) {
        Preconditions.checkState(userManager.isLoggedIn());

        final NamePart namePart = new NamePart(UUID.randomUUID().toString());
        final NamePartRevision newRevision = new NamePartRevision(namePart, NamePartRevisionType.INSERT, userManager.getUser(), new Date(), comment, nameCategory, parent, name, fullName);

        if (userManager.isEditor() && !nameCategory.isApprovalNeeded()) {
            autoApprove(newRevision);
        }

        em.persist(namePart);
        em.persist(newRevision);

        return newRevision;
    }

    public NamePartRevision modifyNamePart(NamePart namePart, String name, String fullName, String comment) {
        Preconditions.checkState(userManager.isLoggedIn());

        final NamePartRevision baseRevision = baseRevision(namePart);
        Preconditions.checkState(baseRevision.getRevisionType() != NamePartRevisionType.DELETE);

        final @Nullable NamePartRevision pendingRevision = pendingRevision(namePart);
        Preconditions.checkState(pendingRevision == null || pendingRevision.getRevisionType() != NamePartRevisionType.DELETE);

        if (pendingRevision != null) {
            pendingRevision.setStatus(NamePartRevisionStatus.CANCELLED);
            pendingRevision.setProcessedBy(userManager.getUser());
            pendingRevision.setProcessDate(new Date());
            pendingRevision.setProcessorComment(comment);
        }

        final NamePartRevisionType eventType = pendingRevision != null ? pendingRevision.getRevisionType() : NamePartRevisionType.MODIFY;
        final NamePartRevision newRevision = new NamePartRevision(namePart, eventType, userManager.getUser(), new Date(), comment, baseRevision.getNameCategory(), baseRevision.getParent(), name, fullName);

        if (userManager.isEditor() && !baseRevision.getNameCategory().isApprovalNeeded() && isOriginalCreator(userManager.getUser(), namePart)) {
            autoApprove(newRevision);
        }

        em.persist(newRevision);

        return newRevision;
    }

    public NamePartRevision deleteNamePart(NamePart namePart, String comment) {
        Preconditions.checkState(userManager.isLoggedIn());

        final NamePartRevision baseRevision = baseRevision(namePart);
        Preconditions.checkState(baseRevision.getRevisionType() != NamePartRevisionType.DELETE);

        final @Nullable NamePartRevision pendingRevision = pendingRevision(namePart);
        Preconditions.checkState(pendingRevision == null || pendingRevision.getRevisionType() != NamePartRevisionType.DELETE);

        if (pendingRevision != null) {
            pendingRevision.setStatus(NamePartRevisionStatus.CANCELLED);
            pendingRevision.setProcessedBy(userManager.getUser());
            pendingRevision.setProcessDate(new Date());
            pendingRevision.setProcessorComment(comment);
        }

        if (pendingRevision == null || pendingRevision.getRevisionType() != NamePartRevisionType.INSERT) {
            final NamePartRevision newRevision = new NamePartRevision(namePart, NamePartRevisionType.DELETE, userManager.getUser(), new Date(), comment, baseRevision.getNameCategory(), baseRevision.getParent(), baseRevision.getName(), baseRevision.getFullName());

            if (userManager.isEditor() && !baseRevision.getNameCategory().isApprovalNeeded() && isOriginalCreator(userManager.getUser(), namePart)) {
                autoApprove(newRevision);
            }

            em.persist(newRevision);
            return newRevision;
        } else {
            return pendingRevision;
        }
    }

    public List<NamePart> getApprovedNames(@Nullable NameCategory category, boolean includeDeleted) {
        throw new IllegalStateException(); // TODO
    }

    public List<NamePart> getPendingNames(@Nullable NameCategory category, boolean includeDeleted) {
        throw new IllegalStateException(); // TODO
    }

    public List<NamePart> getNamesWithChangesProposedByCurrentUser() {
        throw new IllegalStateException(); // TODO
    }

    public List<NamePartRevision> getNameEvents(NamePart namePart) {
        throw new IllegalStateException(); // TODO
    }

    public NamePartRevision cancelNamePartRequest(NamePart namePart, String comment) {
        Preconditions.checkState(userManager.isLoggedIn());

        final NamePartRevision baseRevision = baseRevision(namePart);
        Preconditions.checkState(baseRevision.getRevisionType() != NamePartRevisionType.DELETE);

        final @Nullable NamePartRevision pendingRevision = pendingRevision(namePart);
        Preconditions.checkState(pendingRevision != null);

        pendingRevision.setStatus(NamePartRevisionStatus.CANCELLED);
        pendingRevision.setProcessedBy(userManager.getUser());
        pendingRevision.setProcessDate(new Date());
        pendingRevision.setProcessorComment(comment);

        return pendingRevision;
    }

    private void autoApprove(NamePartRevision namePartEvent) {
        namePartEvent.setStatus(NamePartRevisionStatus.APPROVED);
        namePartEvent.setProcessDate(new Date());
        namePartEvent.setProcessedBy(userManager.getUser());
        namePartEvent.setProcessorComment("AUTOMATIC APPROVAL BASED ON CATEGORY SETTING");
    }

    private boolean isOriginalCreator(Privilege user, NamePart namePart) {
        throw new IllegalStateException(); // TODO
    }

    private @Nullable NamePartRevision approvedRevision(NamePart namePart) {
        throw new IllegalStateException(); // TODO
    }

    private @Nullable NamePartRevision pendingRevision(NamePart namePart) {
        throw new IllegalStateException(); // TODO
    }

    private NamePartRevision baseRevision(NamePart namePart) {
        final NamePartRevision approvedRevision = approvedRevision(namePart);
        return approvedRevision != null ? approvedRevision : pendingRevision(namePart);
    }
}
