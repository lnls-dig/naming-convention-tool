package org.openepics.names.services;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.openepics.names.model.*;
import org.openepics.names.services.views.NamePartRevisionProvider;
import org.openepics.names.services.views.NamePartView;
import org.openepics.names.util.As;
import org.openepics.names.util.JpaHelper;
import org.openepics.names.util.Marker;
import org.openepics.names.util.UnhandledCaseException;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * A service bean managing NamePart entities.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Stateless
public class NamePartService {

    @Inject private DeviceService deviceService;
    @Inject private NamingConvention namingConvention;
    @PersistenceContext private EntityManager em;

    public boolean isMnemonicValid(NamePartType namePartType, @Nullable NamePart parent, String mnemonic) {
        final @Nullable NamePartView parentView = parent != null ? view(parent) : null;
        final List<String> parentPath = parentView != null ? parentView.getMnemonicPath() : ImmutableList.<String>of();
        if (namePartType == NamePartType.SECTION) {
            return namingConvention.isSectionNameValid(parentPath, mnemonic);
        } else if (namePartType == NamePartType.DEVICE_TYPE) {
            return namingConvention.isDeviceTypeNameValid(parentPath, mnemonic);
        } else {
            throw new UnhandledCaseException();
        }
    }

    public boolean isMnemonicUnique(NamePartType namePartType, @Nullable NamePart parent, String mnemonic) {
        final String mnemonicEquivalenceClass = namingConvention.nameNormalizedForEquivalence(mnemonic);
        final List<NamePartRevision> siblings = em.createQuery("SELECT r FROM NamePartRevision r WHERE (r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.status = :approved) OR r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.status = :pending)) AND r.namePart.namePartType = :type AND r.deleted = FALSE AND r.parent = :parent", NamePartRevision.class).setParameter("type", namePartType).setParameter("approved", NamePartRevisionStatus.APPROVED).setParameter("pending", NamePartRevisionStatus.PENDING).setParameter("parent", parent).getResultList();
        for (NamePartRevision sibling : siblings) {
            if (namingConvention.nameNormalizedForEquivalence(sibling.getMnemonic()).equals(mnemonicEquivalenceClass)) {
                return false;
            }
        }
        return true;
    }

    public NamePartRevision addNamePart(String name, String mnemonic, NamePartType namePartType, @Nullable NamePart parent, @Nullable UserAccount user, @Nullable String comment) {
        Preconditions.checkArgument(parent == null || parent.getNamePartType() == namePartType);

        final @Nullable NamePartView parentView = parent != null ? view(parent) : null;

        if (parentView != null) {
            final NamePartRevision parentBaseRevision = parentView.getCurrentOrElsePendingRevision();
            final @Nullable NamePartRevision parentPendingRevision = parentView.getPendingRevision();
            Preconditions.checkState(!parentBaseRevision.isDeleted() && (parentPendingRevision == null || !parentPendingRevision.isDeleted()));
        }

        Preconditions.checkState(isMnemonicValid(namePartType, parent, mnemonic));
        Preconditions.checkState(isMnemonicUnique(namePartType, parent, mnemonic));

        final NamePart namePart = new NamePart(UUID.randomUUID(), namePartType);
        final NamePartRevision newRevision = new NamePartRevision(namePart, new Date(), user, comment, false, parent, name, mnemonic);

        em.persist(namePart);
        em.persist(newRevision);

        return newRevision;
    }

    public NamePartRevision modifyNamePart(NamePart namePart, String name, String mnemonic, @Nullable UserAccount user, @Nullable String comment) {
        final NamePartView namePartView = view(namePart);

        final NamePartRevision baseRevision = namePartView.getCurrentOrElsePendingRevision();
        final @Nullable NamePartRevision pendingRevision = namePartView.getPendingRevision();

        Preconditions.checkState(!baseRevision.isDeleted() && (pendingRevision == null || !pendingRevision.isDeleted()));
        if (!mnemonic.equals(baseRevision.getMnemonic())) {
            Preconditions.checkState(isMnemonicValid(namePart.getNamePartType(), namePartView.getParent() != null ? namePartView.getParent().getNamePart() : null, mnemonic));
            Preconditions.checkState(isMnemonicUnique(namePart.getNamePartType(), namePartView.getParent().getNamePart(), mnemonic));
        }

        if (pendingRevision != null) {
            updateRevisionStatus(pendingRevision, NamePartRevisionStatus.CANCELLED, user, null);
        }

        final NamePartRevision newRevision = new NamePartRevision(namePart, new Date(), user, comment, false, baseRevision.getParent(), name, mnemonic);

        em.persist(newRevision);

        return newRevision;
    }

    public NamePartRevision deleteNamePart(NamePart namePart, @Nullable UserAccount user, @Nullable String comment) {
        final @Nullable NamePartRevision approvedRevision = approvedRevision(namePart);
        final @Nullable NamePartRevision pendingRevision = pendingRevision(namePart);

        if ((approvedRevision == null || !approvedRevision.isDeleted()) && (pendingRevision == null || !pendingRevision.isDeleted())) {
            if (pendingRevision != null) {
                updateRevisionStatus(pendingRevision, NamePartRevisionStatus.CANCELLED, user, null);
            }

            for (NamePart child : approvedAndProposedChildren(namePart)) {
                deleteNamePart(child, user, comment);
            }

            if (approvedRevision != null) {
                final NamePartRevision newRevision = new NamePartRevision(namePart, new Date(), user, comment, true, approvedRevision.getParent(), approvedRevision.getName(), approvedRevision.getMnemonic());
                em.persist(newRevision);
                return newRevision;
            } else {
                return pendingRevision;
            }
        } else {
            return approvedRevision != null ? approvedRevision : pendingRevision;
        }
    }

    public NamePartRevision cancelChangesForNamePart(NamePart namePart, @Nullable UserAccount user, @Nullable String comment, boolean markAsRejected) {
        final @Nullable NamePartRevision approvedRevision = approvedRevision(namePart);
        final @Nullable NamePartRevision pendingRevision = pendingRevision(namePart);

        if (pendingRevision != null && pendingRevision.getStatus() == NamePartRevisionStatus.PENDING) {
            if (canCancelChild(pendingRevision.getParent())) {
                updateRevisionStatus(pendingRevision, markAsRejected ? NamePartRevisionStatus.REJECTED : NamePartRevisionStatus.CANCELLED, user, comment);
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

    private boolean canCancelChild(@Nullable NamePart parent) {
        if (parent != null) {
            final @Nullable NamePartRevision parentApprovedRevision = approvedRevision(parent);
            final @Nullable NamePartRevision parentPendingRevision = pendingRevision(parent);
            return (parentApprovedRevision == null || !parentApprovedRevision.isDeleted()) && (parentPendingRevision == null || !parentPendingRevision.isDeleted());
        } else {
            return true;
        }
    }

    public void approveNamePartRevision(NamePartRevision namePartRevision, @Nullable UserAccount user, @Nullable String comment) {
        namePartRevision = emAttached(namePartRevision);
        if (namePartRevision.getStatus() == NamePartRevisionStatus.PENDING) {
            if (canApproveChild(namePartRevision.getParent())) {
                updateRevisionStatus(namePartRevision, NamePartRevisionStatus.APPROVED, user, comment);
                if (namePartRevision.isDeleted()) {
                    for (Device device : associatedDevices(namePartRevision.getNamePart())) {
                        deviceService.deleteDevice(device, user);
                    }
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

    public List<Device> associatedDevices(NamePart namePart) {
        if (namePart.getNamePartType() == NamePartType.SECTION) {
            return deviceService.devicesInSection(namePart);
        } else if (namePart.getNamePartType() == NamePartType.DEVICE_TYPE) {
            return deviceService.devicesOfType(namePart);
        } else {
            throw new UnhandledCaseException();
        }
    }

    private boolean canApproveChild(@Nullable NamePart parent) {
        if (parent != null) {
            final @Nullable NamePartRevision parentApprovedRevision = approvedRevision(parent);
            return (parentApprovedRevision != null && !parentApprovedRevision.isDeleted());
        } else {
            return true;
        }
    }

    private void approveChildNamePart(NamePart namePart, @Nullable UserAccount user) {
        final NamePartRevision pendingRevision = As.notNull(pendingRevision(namePart));

        updateRevisionStatus(pendingRevision, NamePartRevisionStatus.APPROVED, user, null);
        for (Device device : associatedDevices(namePart)) {
            deviceService.deleteDevice(device, user);
        }

        for (NamePart child : approvedAndProposedChildren(namePart)) {
            approveChildNamePart(child, user);
        }
    }

    private void cancelChildNamePart(NamePart namePart, @Nullable UserAccount user) {
        final NamePartRevision pendingRevision = As.notNull(pendingRevision(namePart));

        if (pendingRevision != null) {
            updateRevisionStatus(pendingRevision, NamePartRevisionStatus.CANCELLED, user, null);
        }

        for (NamePart child : approvedAndProposedChildren(namePart)) {
            cancelChildNamePart(child, user);
        }
    }

    private void updateRevisionStatus(NamePartRevision pendingRevision, NamePartRevisionStatus newStatus, @Nullable UserAccount user, @Nullable String comment) {
        pendingRevision.updateAsProcessed(newStatus, new Date(), user, comment);
    }

    private List<NamePart> approvedAndProposedChildren(NamePart namePart) {
        return em.createQuery("SELECT r.namePart FROM NamePartRevision r WHERE r.parent = :namePart AND r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND (r2.status = :approved OR r2.status = :pending)) AND NOT (r.status = :approved AND r.deleted = TRUE)", NamePart.class).setParameter("namePart", namePart).setParameter("approved", NamePartRevisionStatus.APPROVED).setParameter("pending", NamePartRevisionStatus.PENDING).getResultList();
    }

    public List<NamePartRevision> currentApprovedRevisions(NamePartType type, boolean includeDeleted) {
        if (includeDeleted)
            return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.namePart.namePartType = :type AND r2.status = :approved)", NamePartRevision.class).setParameter("type", type).setParameter("approved", NamePartRevisionStatus.APPROVED).getResultList();
        else {
            return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.namePart.namePartType = :type AND r2.status = :approved) AND r.deleted = FALSE", NamePartRevision.class).setParameter("type", type).setParameter("approved", NamePartRevisionStatus.APPROVED).getResultList();
        }
    }

    public List<NamePartRevision> currentPendingRevisions(NamePartType type, boolean includeDeleted) {
        if (includeDeleted)
            return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.namePart.namePartType = :type AND r2.status = :pending)", NamePartRevision.class).setParameter("type", type).setParameter("pending", NamePartRevisionStatus.PENDING).getResultList();
        else {
            return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.namePart.namePartType = :type AND r2.status = :pending) AND NOT (r.status = :approved AND r.deleted = TRUE)", NamePartRevision.class).setParameter("type", type).setParameter("pending", NamePartRevisionStatus.PENDING).getResultList();
        }
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

    private NamePartRevision emAttached(NamePartRevision namePartRevision) {
        if (!em.contains(namePartRevision)) {
            return As.notNull(em.find(NamePartRevision.class, namePartRevision.getId()));
        } else {
            return namePartRevision;
        }
    }

    private NamePartView view(NamePart namePart) {
        final NamePartRevisionProvider revisionProvider = new NamePartRevisionProvider() {
            @Override public @Nullable NamePartRevision approvedRevision(NamePart namePart) { return NamePartService.this.approvedRevision(namePart); }
            @Override public @Nullable NamePartRevision pendingRevision(NamePart namePart) { return NamePartService.this.pendingRevision(namePart); }
        };
        return new NamePartView(revisionProvider, approvedRevision(namePart), pendingRevision(namePart), null);
    }
}
