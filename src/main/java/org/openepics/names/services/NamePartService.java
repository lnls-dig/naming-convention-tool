package org.openepics.names.services;

import com.google.common.base.Preconditions;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.openepics.names.model.NameHierarchy;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartRevisionStatus;
import org.openepics.names.model.NamePartType;
import org.openepics.names.model.UserAccount;
import org.openepics.names.util.As;
import org.openepics.names.util.JpaHelper;
import org.openepics.names.util.Marker;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Stateless
public class NamePartService {

    @PersistenceContext private EntityManager em;

    public NamePart namePartWithId(String uuid) {
        throw new IllegalStateException(); // TODO
    }

    public NamePartRevision addNamePart(String name, String fullName, NamePartType nameType, @Nullable NamePart parent, @Nullable UserAccount user, String comment) {
        Preconditions.checkArgument(parent == null || parent.getNamePartType() == nameType);

        final @Nullable NamePartRevision parentBaseRevision = parent != null ? approvedOrElsePendingRevision(parent) : null;
        final @Nullable NamePartRevision parentPendingRevision = parent != null ? pendingRevision(parent) : null;
        Preconditions.checkState((parentBaseRevision == null || !parentBaseRevision.isDeleted()) && (parentPendingRevision == null || !parentPendingRevision.isDeleted()));

        final NamePart namePart = new NamePart(UUID.randomUUID().toString(), nameType);
        final NamePartRevision newRevision = new NamePartRevision(namePart, user, new Date(), comment, false, parent, name, fullName);

        em.persist(namePart);
        em.persist(newRevision);

        return newRevision;
    }

    public NamePartRevision modifyNamePart(NamePart namePart, String name, String fullName, @Nullable UserAccount user, String comment) {
        final NamePartRevision baseRevision = approvedOrElsePendingRevision(namePart);
        Preconditions.checkState(!baseRevision.isDeleted());

        final @Nullable NamePartRevision pendingRevision = pendingRevision(namePart);
        Preconditions.checkState(pendingRevision == null || !pendingRevision.isDeleted());

        if (pendingRevision != null) {
            cancelPendingRevision(pendingRevision, user);
        }

        final NamePartRevision newRevision = new NamePartRevision(namePart, user, new Date(), comment, false, baseRevision.getParent(), name, fullName);

        em.persist(newRevision);

        return newRevision;
    }

    public NamePartRevision deleteNamePart(NamePart namePart, @Nullable UserAccount user, String comment) {
        final @Nullable NamePartRevision approvedRevision = approvedRevision(namePart);
        final @Nullable NamePartRevision pendingRevision = pendingRevision(namePart);

        if ((approvedRevision == null || !approvedRevision.isDeleted()) && (pendingRevision == null || !pendingRevision.isDeleted())) {
            if (pendingRevision != null) {
                cancelPendingRevision(pendingRevision, user);
            }

            for (NamePart child : approvedAndProposedChildren(namePart)) {
                deleteChildNamePart(child, user, comment);
            }

            if (approvedRevision != null) {
                final NamePartRevision newRevision = new NamePartRevision(namePart, user, new Date(), comment, true, approvedRevision.getParent(), approvedRevision.getName(), approvedRevision.getFullName());
                em.persist(newRevision);
                return newRevision;
            } else {
                return pendingRevision;
            }
        } else {
            return approvedRevision != null ? approvedRevision : pendingRevision;
        }
    }

    public NamePartRevision cancelChangesForNamePart(NamePart namePart, @Nullable UserAccount user, String comment) {
        final @Nullable NamePartRevision approvedRevision = approvedRevision(namePart);
        final @Nullable NamePartRevision pendingRevision = pendingRevision(namePart);

        if (pendingRevision != null && pendingRevision.getStatus() == NamePartRevisionStatus.PENDING) {
            final @Nullable NamePartRevision parentApprovedRevision = approvedRevision(pendingRevision.getParent());
            final @Nullable NamePartRevision parentPendingRevision = pendingRevision(pendingRevision.getParent());
            if ((parentApprovedRevision == null || !parentApprovedRevision.isDeleted()) && (parentPendingRevision == null || !parentPendingRevision.isDeleted())) {
                pendingRevision.setStatus(NamePartRevisionStatus.CANCELLED);
                pendingRevision.setProcessedBy(user);
                pendingRevision.setProcessDate(new Date());
                pendingRevision.setProcessorComment(comment);
                if (approvedRevision == null || pendingRevision.isDeleted()) {
                    for (NamePart child : approvedAndProposedChildren(pendingRevision.getNamePart())) {
                        cancelChildNamePart(child, user);
                    }
                }
                return pendingRevision;
            } else {
                throw new IllegalStateException();
            }
        } else if (approvedRevision != null && pendingRevision == null) {
            return approvedRevision;
        } else {
            throw new IllegalStateException();
        }
    }

    public void approveNamePartRevision(NamePartRevision namePartRevision, @Nullable UserAccount user) {
        if (namePartRevision.getStatus() == NamePartRevisionStatus.PENDING) {
            final @Nullable NamePartRevision parentApprovedRevision = approvedRevision(namePartRevision.getParent());
            final @Nullable NamePartRevision parentPendingRevision = pendingRevision(namePartRevision.getParent());
            if ((parentApprovedRevision != null && !parentApprovedRevision.isDeleted()) && (parentPendingRevision == null || !parentPendingRevision.isDeleted())) {
                namePartRevision.setStatus(NamePartRevisionStatus.APPROVED);
                namePartRevision.setProcessedBy(user);
                namePartRevision.setProcessDate(new Date());
                namePartRevision.setProcessorComment(null);
                if (namePartRevision.isDeleted()) {
                    for (NamePart child : approvedAndProposedChildren(namePartRevision.getNamePart())) {
                        approveChildNamePart(child, user);
                    }
                }
            } else {
                throw new IllegalStateException();
            }
        } else if (namePartRevision.getStatus() == NamePartRevisionStatus.APPROVED) {
            Marker.doNothing();
        } else {
            throw new IllegalStateException();
        }
    }

    public void rejectNamePartRevision(NamePartRevision namePartRevision, @Nullable UserAccount user, String comment) {
        if (namePartRevision.getStatus() == NamePartRevisionStatus.PENDING) {
            namePartRevision.setStatus(NamePartRevisionStatus.REJECTED);
            namePartRevision.setProcessedBy(user);
            namePartRevision.setProcessDate(new Date());
            namePartRevision.setProcessorComment(comment);
        } else if (namePartRevision.getStatus() == NamePartRevisionStatus.REJECTED) {
            Marker.doNothing();
        } else {
            throw new IllegalStateException();
        }
    }

    public void approveNamePartRevisions(List<NamePartRevision> revisions, @Nullable UserAccount user, String comment) {
        for (NamePartRevision revision : revisions) {
            approveNamePartRevision(revision, user);
        }
    }

    public void rejectNamePartRevisions(List<NamePartRevision> revisions, @Nullable UserAccount user, String comment) {
        for (NamePartRevision revision : revisions) {
            rejectNamePartRevision(revision, user, comment);
        }
    }

    private void deleteChildNamePart(NamePart namePart, @Nullable UserAccount user, String comment) {
        final @Nullable NamePartRevision approvedRevision = approvedRevision(namePart);
        final @Nullable NamePartRevision pendingRevision = pendingRevision(namePart);

        if (pendingRevision != null) {
            cancelPendingRevision(pendingRevision, user);
        }

        for (NamePart child : approvedAndProposedChildren(namePart)) {
            deleteChildNamePart(child, user, comment);
        }

        if (approvedRevision != null) {
            final NamePartRevision newRevision = new NamePartRevision(namePart, user, new Date(), comment, true, approvedRevision.getParent(), approvedRevision.getName(), approvedRevision.getFullName());
            em.persist(newRevision);
        }
    }

    private void approveChildNamePart(NamePart namePart, @Nullable UserAccount user) {
        final NamePartRevision pendingRevision = As.notNull(pendingRevision(namePart));

        if (pendingRevision != null) {
            pendingRevision.setStatus(NamePartRevisionStatus.APPROVED);
            pendingRevision.setProcessedBy(user);
            pendingRevision.setProcessDate(new Date());
            pendingRevision.setProcessorComment(null);
        }

        for (NamePart child : approvedAndProposedChildren(namePart)) {
            approveChildNamePart(child, user);
        }
    }

    private void cancelChildNamePart(NamePart namePart, @Nullable UserAccount user) {
        final NamePartRevision pendingRevision = As.notNull(pendingRevision(namePart));

        if (pendingRevision != null) {
            pendingRevision.setStatus(NamePartRevisionStatus.CANCELLED);
            pendingRevision.setProcessedBy(user);
            pendingRevision.setProcessDate(new Date());
            pendingRevision.setProcessorComment(null);
        }

        for (NamePart child : approvedAndProposedChildren(namePart)) {
            cancelChildNamePart(child, user);
        }
    }

    private void cancelPendingRevision(NamePartRevision pendingRevision, @Nullable UserAccount user) {
        pendingRevision.setStatus(NamePartRevisionStatus.CANCELLED);
        pendingRevision.setProcessedBy(user);
        pendingRevision.setProcessDate(new Date());
        pendingRevision.setProcessorComment(null);
    }

    private Iterable<NamePart> approvedAndProposedChildren(NamePart namePart) {
        return em.createQuery("SELECT r.namePart FROM NamePartRevision r WHERE r.parent = :namePart AND r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND (r2.status = :approved OR r2.status = :pending)) AND NOT (r.status = :approved AND r.deleted = TRUE)", NamePart.class).setParameter("namePart", namePart).setParameter("approved", NamePartRevisionStatus.APPROVED).setParameter("pending", NamePartRevisionStatus.PENDING).getResultList();
    }

    public NameHierarchy nameHierarchy() {
        return em.createQuery("SELECT nameHierarchy FROM NameHierarchy nameHierarchy", NameHierarchy.class).getSingleResult();
    }

    public List<NamePart> approvedNames(boolean includeDeleted) {
        if (includeDeleted)
            return em.createQuery("SELECT r.namePart FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.status = :approved)", NamePart.class).setParameter("approved", NamePartRevisionStatus.APPROVED).getResultList();
        else {
            return em.createQuery("SELECT r.namePart FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.status = :approved) AND r.deleted = FALSE", NamePart.class).setParameter("approved", NamePartRevisionStatus.APPROVED).getResultList();
        }
    }

    public List<NamePart> approvedNames() {
        return approvedNames(false);
    }

    public List<NamePart> approvedOrPendingNames(boolean includeDeleted) {
        if (includeDeleted)
            return em.createQuery("SELECT r.namePart FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND (r2.status = :approved OR r2.status = :pending))", NamePart.class).setParameter("approved", NamePartRevisionStatus.APPROVED).setParameter("pending", NamePartRevisionStatus.PENDING).getResultList();
        else {
            return em.createQuery("SELECT r.namePart FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND (r2.status = :approved OR r2.status = :pending)) AND NOT (r.status = :approved AND r.deleted = TRUE)", NamePart.class).setParameter("approved", NamePartRevisionStatus.APPROVED).setParameter("pending", NamePartRevisionStatus.PENDING).getResultList();
        }
    }

    public List<NamePart> approvedOrPendingNames() {
        return approvedOrPendingNames(false);
    }

    public List<NamePartRevision> currentApprovedRevisions(boolean includeDeleted) {
        if (includeDeleted)
            return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.status = :approved)", NamePartRevision.class).setParameter("approved", NamePartRevisionStatus.APPROVED).getResultList();
        else {
            return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.status = :approved) AND r.deleted = FALSE", NamePartRevision.class).setParameter("approved", NamePartRevisionStatus.APPROVED).getResultList();
        }
    }

    public List<NamePartRevision> currentPendingRevisions(boolean includeDeleted) {
        if (includeDeleted)
            return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND (r2.status = :pending))", NamePartRevision.class).setParameter("pending", NamePartRevisionStatus.PENDING).getResultList();
        else {
            return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND (r2.status = :pending)) AND NOT (r.status = :approved AND r.deleted = TRUE)", NamePartRevision.class).setParameter("pending", NamePartRevisionStatus.PENDING).getResultList();
        }
    }

    public List<NamePart> namesWithChangesProposedByUser(UserAccount user) {
        return em.createQuery("SELECT r.namePart FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND (r2.status = :status1 OR r2.status = :status2) AND r2.requestedBy = :requestedBy)", NamePart.class).setParameter("status1", NamePartRevisionStatus.PENDING).setParameter("status2", NamePartRevisionStatus.REJECTED).setParameter("requestedBy", user).getResultList();
    }

    public List<NamePartRevision> revisions(NamePart namePart) {
        return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.namePart = :namePart ORDER BY r.id", NamePartRevision.class).setParameter("namePart", namePart).getResultList();
    }

    public @Nullable NamePartRevision approvedRevision(NamePart namePart) {
        return JpaHelper.getSingleResultOrNull(em.createQuery("SELECT r FROM NamePartRevision r WHERE r.namePart = :namePart AND r.status = :status ORDER BY r.id DESC", NamePartRevision.class).setParameter("namePart", namePart).setParameter("status", NamePartRevisionStatus.APPROVED).setMaxResults(1));
    }

    public @Nullable NamePartRevision pendingRevision(NamePart namePart) {
        final @Nullable NamePartRevision lastPendingOrApprovedRevision = JpaHelper.getSingleResultOrNull(em.createQuery("SELECT r FROM NamePartRevision r WHERE r.namePart = :namePart AND (r.status = :approved OR r.status = :pending) ORDER BY r.id DESC", NamePartRevision.class).setParameter("namePart", namePart).setParameter("approved", NamePartRevisionStatus.APPROVED).setParameter("pending", NamePartRevisionStatus.PENDING).setMaxResults(1));
        if (lastPendingOrApprovedRevision == null) return null;
        else if (lastPendingOrApprovedRevision.getStatus() == NamePartRevisionStatus.PENDING) return lastPendingOrApprovedRevision;
        else return null;
    }

    public NamePartRevision approvedOrElsePendingRevision(NamePart namePart) {
        final @Nullable NamePartRevision approvedRevision = approvedRevision(namePart);
        return approvedRevision != null ? approvedRevision : As.notNull(pendingRevision(namePart));
    }

    public NamePartRevision pendingOrElseApprovedRevision(NamePart namePart) {
        final @Nullable NamePartRevision pendingRevision = pendingRevision(namePart);
        return pendingRevision != null ? pendingRevision : As.notNull(approvedRevision(namePart));
    }
}
