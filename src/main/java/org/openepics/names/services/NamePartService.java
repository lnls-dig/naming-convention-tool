package org.openepics.names.services;

import com.google.common.base.Preconditions;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.openepics.names.model.NameCategory;
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
        final @Nullable NamePartRevision parentBaseRevision = parent != null ? approvedOrElsePendingRevision(parent) : null;
        final @Nullable NamePartRevision parentPendingRevision = parent != null ? pendingRevision(parent) : null;
        Preconditions.checkState((parentBaseRevision == null || !parentBaseRevision.isDeleted()) && (parentPendingRevision == null || !parentPendingRevision.isDeleted()));

        final NameCategory nameCategory = childNameCategory(nameType, parentBaseRevision != null ? parentBaseRevision.getNameCategory() : null);

        final NamePart namePart = new NamePart(UUID.randomUUID().toString());
        final NamePartRevision newRevision = new NamePartRevision(namePart, user, new Date(), comment, false, nameCategory, parent, name, fullName);

        em.persist(namePart);
        em.persist(newRevision);

        return newRevision;
    }

    private NameCategory childNameCategory(NamePartType nameType, @Nullable NameCategory parentCategory) {
        if (parentCategory != null) {
            return As.notNull(nameHierarchy().getSubCategory(parentCategory));
        } else {
            if (nameType == NamePartType.SECTION) {
                return nameHierarchy().getSectionLevels().get(0);
            } else if (nameType == NamePartType.DEVICE_TYPE) {
                return nameHierarchy().getDeviceTypeLevels().get(0);
            } else {
                throw new IllegalStateException();
            }
        }
    }

    public NamePartRevision modifyNamePart(NamePart namePart, String name, String fullName, @Nullable UserAccount user, String comment) {
        final NamePartRevision baseRevision = approvedOrElsePendingRevision(namePart);
        Preconditions.checkState(!baseRevision.isDeleted());

        final @Nullable NamePartRevision pendingRevision = pendingRevision(namePart);
        Preconditions.checkState(pendingRevision == null || !pendingRevision.isDeleted());

        if (pendingRevision != null) {
            cancelPendingRevision(pendingRevision, user);
        }

        final NamePartRevision newRevision = new NamePartRevision(namePart, user, new Date(), comment, false, baseRevision.getNameCategory(), baseRevision.getParent(), name, fullName);

        final NamePart parent = baseRevision.getParent();
        final NamePartRevision approvedParentRevision = approvedRevision(parent);

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
                final NamePartRevision newRevision = new NamePartRevision(namePart, user, new Date(), comment, true, approvedRevision.getNameCategory(), approvedRevision.getParent(), approvedRevision.getName(), approvedRevision.getFullName());
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
            pendingRevision.setStatus(NamePartRevisionStatus.CANCELLED);
            pendingRevision.setProcessedBy(user);
            pendingRevision.setProcessDate(new Date());
            pendingRevision.setProcessorComment(comment);

            return pendingRevision;
        } else if (approvedRevision != null && pendingRevision == null) {
            return approvedRevision;
        } else {
            throw new IllegalStateException();
        }
    }

    public void approveNamePartRevision(NamePartRevision namePartRevision, @Nullable UserAccount user) {
        if (namePartRevision.getStatus() == NamePartRevisionStatus.PENDING) {
            namePartRevision.setStatus(NamePartRevisionStatus.APPROVED);
            namePartRevision.setProcessedBy(user);
            namePartRevision.setProcessDate(new Date());
            namePartRevision.setProcessorComment(null);
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
            final NamePartRevision newRevision = new NamePartRevision(namePart, user, new Date(), comment, true, approvedRevision.getNameCategory(), approvedRevision.getParent(), approvedRevision.getName(), approvedRevision.getFullName());
            newRevision.setStatus(NamePartRevisionStatus.PENDING_PARENT);
            em.persist(newRevision);
        }
    }

    private void cancelPendingRevision(NamePartRevision pendingRevision, @Nullable UserAccount user) {
        pendingRevision.setStatus(NamePartRevisionStatus.CANCELLED);
        pendingRevision.setProcessedBy(user);
        pendingRevision.setProcessDate(new Date());
        pendingRevision.setProcessorComment(null);
    }

    private Iterable<NamePart> approvedAndProposedChildren(NamePart namePart) {
        // TODO
        throw new IllegalStateException();
    }

    public NameHierarchy nameHierarchy() {
        return em.createQuery("SELECT nameHierarchy FROM NameHierarchy nameHierarchy", NameHierarchy.class).getSingleResult();
    }

    public List<NamePart> approvedNames(boolean includeDeleted) {
        if (includeDeleted)
            return em.createQuery("SELECT r.namePart FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.status = :status)", NamePart.class).setParameter("status", NamePartRevisionStatus.APPROVED).getResultList();
        else {
            return em.createQuery("SELECT r.namePart FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.status = :status) AND r.deleted = FALSE", NamePart.class).setParameter("status", NamePartRevisionStatus.APPROVED).getResultList();
        }
    }

    public List<NamePart> approvedNames() {
        return approvedNames(false);
    }

    public List<NamePart> approvedOrPendingNames(@Nullable NameCategory category, boolean includeDeleted) {
        if (includeDeleted)
            return em.createQuery("SELECT r.namePart FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND (r2.status = :status1 OR r2.status = :status2 OR r2.status = :status3))", NamePart.class).setParameter("status1", NamePartRevisionStatus.APPROVED).setParameter("status2", NamePartRevisionStatus.PENDING).setParameter("status3", NamePartRevisionStatus.PENDING_PARENT).getResultList();
        else {
            return em.createQuery("SELECT r.namePart FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND (r2.status = :status1 OR r2.status = :status2 OR r2.status = :status3)) AND r.deleted = FALSE", NamePart.class).setParameter("status1", NamePartRevisionStatus.APPROVED).setParameter("status2", NamePartRevisionStatus.PENDING).setParameter("status3", NamePartRevisionStatus.PENDING_PARENT).getResultList();
        }
    }

    public List<NamePart> approvedOrPendingNames() {
        return approvedOrPendingNames(null, false);
    }

    public List<NamePart> namesWithChangesProposedByUser(UserAccount user) {
        return em.createQuery("SELECT r.namePart FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND (r2.status = :status1 OR r2.status = :status2) AND r2.requestedBy = :requestedBy)", NamePart.class).setParameter("status1", NamePartRevisionStatus.PENDING).setParameter("status2", NamePartRevisionStatus.REJECTED).setParameter("requestedBy", user).getResultList();
    }

    public List<NamePart> namesWithCategory(NameCategory category) {
        throw new IllegalStateException(); // TODO
    }

    public List<NamePartRevision> revisions(NamePart namePart) {
        return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.namePart = :namePart ORDER BY r.id", NamePartRevision.class).getResultList();
    }

    public List<NameCategory> nameCategories() {
        return em.createNamedQuery("SELECT nameCategory FROM NameCategory nameCategory", NameCategory.class).getResultList();
    }

    public List<NamePart> siblings(NamePart namePart) {
        throw new IllegalStateException(); // TODO
    }

    public @Nullable NamePartRevision approvedRevision(NamePart namePart) {
        return JpaHelper.getSingleResultOrNull(em.createQuery("SELECT r FROM NamePartRevision r WHERE r.namePart = :namePart AND r.status = :status ORDER BY r.id DESC LIMIT 1)", NamePartRevision.class).setParameter("status", NamePartRevisionStatus.APPROVED));
    }

    public @Nullable NamePartRevision pendingRevision(NamePart namePart) {
        final @Nullable NamePartRevision lastPendingOrApprovedRevision = JpaHelper.getSingleResultOrNull(em.createQuery("SELECT r FROM NamePartRevision r WHERE r.namePart = :namePart AND (r.status = :status1 OR r.status = :status2 OR r.status = :status3) ORDER BY r.id DESC LIMIT 1)", NamePartRevision.class).setParameter("status1", NamePartRevisionStatus.APPROVED).setParameter("status2", NamePartRevisionStatus.PENDING).setParameter("status3", NamePartRevisionStatus.PENDING_PARENT));
        if (lastPendingOrApprovedRevision == null) return null;
        else if (lastPendingOrApprovedRevision.getStatus() == NamePartRevisionStatus.PENDING || lastPendingOrApprovedRevision.getStatus() == NamePartRevisionStatus.PENDING_PARENT) return lastPendingOrApprovedRevision;
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
