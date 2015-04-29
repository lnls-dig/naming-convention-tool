package org.openepics.names.services;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection.Builder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.openepics.names.model.*;
import org.openepics.names.services.views.BatchViewProvider;
import org.openepics.names.services.views.NamePartRevisionProvider;
import org.openepics.names.services.views.NamePartView;
import org.openepics.names.util.As;
import org.openepics.names.util.JpaHelper;
import org.openepics.names.util.UnhandledCaseException;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.stream.events.Comment;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * A service bean managing NamePart and Device entities.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Stateless
public class NamePartService {

    @Inject private NamingConvention namingConvention;
    @PersistenceContext private EntityManager em;

    /**
     * True if the mnemonic can be null.
     * @param namePartType the type of the name part
     * @param parent the parent of the name part, null if the name part is at the root of the hierarchy
     * @return
     */
    public boolean isMnemonicRequired(NamePartType namePartType, @Nullable NamePart namePart) {
        final @Nullable NamePartView namePartView = namePart != null ? view(namePart) : null;
        final List<String> mnemonicPath = namePartView != null ? namePartView.getMnemonicPath() : ImmutableList.<String>of();
            return namingConvention.isMnemonicRequired(mnemonicPath, namePartType);
	}
    
    /**
     * True if the mnemonic can be null for child.
     * @param namePartType the type of the name part
     * @param parent the parent of the name part, null if the name part is at the root of the hierarchy
     * @return
     */
    public boolean isMnemonicRequiredForChild(NamePartType namePartType, NamePart namePart) {
        final @Nullable NamePartView namePartView = namePart != null ? view(namePart) : null;
        final List<String> addMnemonicPath = namePartView != null ? namePartView.getMnemonicPathWithChild("") : ImmutableList.<String>of("");
        return namingConvention.isMnemonicRequired(addMnemonicPath, namePartType);
	}

    /**
     * The name of the name part type to be used in dialog header and menus. Example: "Modify mnemonic for namePartTypeName" where namePartTypeName can be section, subsection, discipline etc... 
     * @param namePartType
     * @param namePart
     * @return
     */
    public String getNamePartTypeName(NamePartType namePartType, NamePart namePart) {
        final @Nullable NamePartView namePartView = namePart != null ? view(namePart) : null;
        final List<String> mnemonicPath = namePartView != null ? namePartView.getMnemonicPath() : ImmutableList.<String>of();
        return namingConvention.getNamePartTypeName(mnemonicPath, namePartType);
    }

    /**
     * The name of the name part type to be used in dialog header and menus. Example: "Add mnemonic for namePartTypeName" where namePartTypeName can be section, subsection, discipline etc... 
     * @param namePartType
     * @param namePart
     * @return
     */
	public String getNamePartTypeNameForChild(NamePartType namePartType, NamePart namePart) {
        final @Nullable NamePartView namePartView = namePart != null ? view(namePart) : null;
        final List<String> mnemonicPath = namePartView != null ? namePartView.getMnemonicPathWithChild("") : ImmutableList.<String>of("");
        return namingConvention.getNamePartTypeName(mnemonicPath, namePartType);
	}
	
    /**
     * The mnemonic of the name part type to be used in dialogs. Example: "' Mnemonic: namePartTypeMnemonic" where namePartTypeMnemonic can be Sec, Dev  etc... 
     * @param namePartType
     * @param namePart
     * @return
     */
    public String getNamePartTypeMnemonic(NamePartType namePartType, NamePart namePart) {
        final @Nullable NamePartView namePartView = namePart != null ? view(namePart) : null;
        final List<String> mnemonicPath = namePartView != null ? namePartView.getMnemonicPath() : ImmutableList.<String>of();
        return namingConvention.getNamePartTypeMnemonic(mnemonicPath, namePartType);
    }

    /**
     * The mnemonic of the name part type to be used in dialogs. Example: "' Mnemonic: namePartTypeMnemonic" where namePartTypeMnemonic can be Sec, Dev  etc... 
     * @param namePartType
     * @param namePart
     * @return
     */
	public String getNamePartTypeMnemonicForChild(NamePartType namePartType, NamePart namePart) {
        final @Nullable NamePartView namePartView = namePart != null ? view(namePart) : null;
        final List<String> mnemonicPath = namePartView != null ? namePartView.getMnemonicPathWithChild("") : ImmutableList.<String>of("");
        return namingConvention.getNamePartTypeMnemonic(mnemonicPath, namePartType);
	}

    
    /**
     * True if the mnemonic of a name part is valid in the context of the parent.
     *
     * @param namePartType the type of the name part
     * @param parent of the name part, null if the name part is at the root of the hierarchy
     * @param mnemonic the mnemonic name of the name part to test for validity
     */
    public boolean isMnemonicValid(NamePartType namePartType, @Nullable NamePart parent, @Nullable String mnemonic) {
        final @Nullable NamePartView namePartView = parent != null ? view(parent) : null;
        final List<String> mnemonicPath = namePartView != null ? namePartView.getMnemonicPathWithChild(mnemonic) : ImmutableList.<String>of(mnemonic!=null? mnemonic: "");
        return namingConvention.isMnemonicValid(mnemonicPath, namePartType);
    }

    /**
     * True if the mnemonic of a name part is unique. Discipline of the device structure and functional area section have to be globally unique.
     * Device type has to be unique within the context of the discipline. Functional area subsection has to be unique in the context of section. 
     * Super section and device groups can have null mnemonics and do not need to be unique. 
     * 
     * @param namePartType the type of the name part
     * @param parent the parent of the name part, null if the name part is at the root of the hierarchy
     * @param mnemonic the mnemonic name of the name part to test for uniqueness
     */
    public boolean isMnemonicUnique(NamePartType namePartType, @Nullable NamePart parent, @Nullable String mnemonic) {        
        if(mnemonic==null) {
        	return false;
        } else {
    	final String mnemonicEquivalenceClass = namingConvention.equivalenceClassRepresentative(mnemonic);
        final List<NamePartRevision> sameEqClassRevisions = em.createQuery("SELECT r FROM NamePartRevision r WHERE (r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.status = :approved) OR r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.status = :pending)) AND r.deleted = FALSE AND r.mnemonicEqClass = :mnemonicEquivalenceClass", NamePartRevision.class).setParameter("approved", NamePartRevisionStatus.APPROVED).setParameter("pending", NamePartRevisionStatus.PENDING).setParameter("mnemonicEquivalenceClass", mnemonicEquivalenceClass).getResultList();
        for (NamePartRevision sameEqClassRevision : sameEqClassRevisions) {
            if (Objects.equal(sameEqClassRevision.getParent(), parent)) {
                return false;
            }
        }           
        final List<String> newMnemonicPath = ImmutableList.<String>builder().addAll(parent == null ? ImmutableList.<String>of() : view(parent).getMnemonicPath()).add(mnemonic).build();
        for (NamePartRevision sameEqClassRevision : sameEqClassRevisions) {
            if (!namingConvention.canMnemonicsCoexist(newMnemonicPath, namePartType, view(sameEqClassRevision.getNamePart()).getMnemonicPath(), sameEqClassRevision.getNamePart().getNamePartType())) {
                return false;
            }
        }
        return true; 
        }
    }
    
    public boolean isMnemonicUniqueExceptForItself(@Nullable String currentMnemonic, NamePartType namePartType, @Nullable NamePart parent, @Nullable String mnemonic){
    	if(mnemonic==null) { 
    		return false;
    	} else if (currentMnemonic==null){
    		return isMnemonicUnique(namePartType, parent, mnemonic);
    	} else {    		
        	final String mnemonicEquivalenceClass = namingConvention.equivalenceClassRepresentative(mnemonic);
            final String currentMnemonicEqClasss = namingConvention.equivalenceClassRepresentative(currentMnemonic);
            boolean nameIsEquivalentToCurrent= mnemonicEquivalenceClass.equals(currentMnemonicEqClasss);
            return isMnemonicUnique(namePartType, parent, mnemonic)|| nameIsEquivalentToCurrent;
    	}
    }
    
    /**
     * True if the instance index of a device defined by a section and device type is valid.
     *
     * @param section the section containing the device
     * @param deviceType the device type of the device
     * @param instanceIndex the device instance index to test for validity, or null if no instance index is assigned to
     * the device, in which case this is also checked for validity
     */
    public boolean isInstanceIndexValid(NamePart section, NamePart deviceType, @Nullable String instanceIndex) {
        final NamePartView sectionView = view(section);
        final NamePartView deviceTypeView = view(deviceType);
        return namingConvention.isInstanceIndexValid(sectionView.getMnemonicPath(), deviceTypeView.getMnemonicPath(), instanceIndex);
    }

    /**
     * True if the device defined by the given section, device type and instance index would have a unique convention name.
     *
     * @param section the section containing the device
     * @param deviceType the device type of the device
     * @param instanceIndex the instance index of the device, null if no instance index is assigned to the device
     */
    public boolean isDeviceConventionNameUnique(NamePart section, NamePart deviceType, @Nullable String instanceIndex) {
        final String conventionName = conventionName(section, deviceType, instanceIndex);
        return isDeviceConventionNameUnique(conventionName);
    }
    /**
     * True if the device defined by the given section, devicetype and instance index would have a unique convention name, not taking into account itself.  
     * @param section
     * @param deviceType
     * @param instanceIndex
     * @param device
     * @return
     */
    
    public boolean isDeviceConventionNameUniqueExceptForItself(Device device, NamePart section, NamePart deviceType, @Nullable String instanceIndex) {
    	final String conventionName=conventionName(section, deviceType, instanceIndex);
    	final String conventionNameEqClass = namingConvention.equivalenceClassRepresentative(conventionName);
        final DeviceRevision currentRevision = currentRevision(device);
        String currentConventionNameEqClass=currentRevision.getConventionNameEqClass();
        boolean nameIsEquivalentToCurrentRevision=conventionNameEqClass.equals(currentConventionNameEqClass);
        return isDeviceConventionNameUnique(conventionName) || nameIsEquivalentToCurrentRevision;
    }

    
    /**
     * True if the device with the given convention name have a unique convention name.
     *
     * @param conventionName the device name to check
     */
    public boolean isDeviceConventionNameUnique(String conventionName) {
        final String equivalenceClass = namingConvention.equivalenceClassRepresentative(conventionName);
        return em.createQuery("SELECT r FROM DeviceRevision r WHERE r.id = (SELECT MAX(r2.id) FROM DeviceRevision r2 WHERE r2.device = r.device) AND r.deleted = false AND r.conventionNameEqClass = :conventionNameEqClass", DeviceRevision.class).setParameter("conventionNameEqClass", equivalenceClass).getResultList().isEmpty();
    }

    /**
     * Submits a proposal for addition of a new name part.
     *
     * @param name the long, descriptive name of the part. Does not need to follow a convention.
     * @param mnemonic the short, mnemonic name of the part in accordance with the naming convention
     * @param namePartType the type of the proposed name part
     * @param parent the parent of the proposed name part in the hierarchy. Null if at the top of the hierarchy.
     * @param user the user submitting the proposal. Null if the proposal is by an automated process.
     * @param comment the comment the user gave when submitting the proposal. Null if no comment was given.
     * @return the resulting proposed NamePart revision
     */
    public NamePartRevision addNamePart(String name, @Nullable String mnemonic, @Nullable String description, NamePartType namePartType, @Nullable NamePart parent, @Nullable UserAccount user, @Nullable String comment) {
        Preconditions.checkArgument(parent == null || parent.getNamePartType() == namePartType);

        final @Nullable NamePartView parentView = parent != null ? view(parent) : null;

        if (parentView != null) {
            final NamePartRevision parentBaseRevision = parentView.getCurrentOrElsePendingRevision();
            final @Nullable NamePartRevision parentPendingRevision = parentView.getPendingRevision();
            Preconditions.checkState(!parentBaseRevision.isDeleted() && (parentPendingRevision == null || !parentPendingRevision.isDeleted()));
        }

        Preconditions.checkState(isMnemonicValid(namePartType, parent, mnemonic));
        if(mnemonic!=null) Preconditions.checkState(isMnemonicUnique(namePartType, parent, mnemonic));
        final String  mnemonicEqClass = mnemonic !=null ? namingConvention.equivalenceClassRepresentative(mnemonic):null;
        final NamePart namePart = new NamePart(UUID.randomUUID(), namePartType);
        final NamePartRevision newRevision = new NamePartRevision(namePart, new Date(), user, comment, false, parent, name, mnemonic, description, mnemonicEqClass);

        em.persist(namePart);
        em.persist(newRevision);

        return newRevision;
    }

    /**
     * Submits a proposal for modification of an existing name part.
     *
     * @param namePart the name part proposed for modification
     * @param name the new long, descriptive name of the part. Does not need to follow a convention.
     * @param mnemonic the new short, mnemonic name of the part in accordance with the naming convention
     * @param user the user submitting the proposal. Null if the proposal is by an automated process.
     * @param comment the comment the user gave when submitting the proposal. Null if no comment was given.
     * @return the resulting proposed NamePart revision
     */
    public NamePartRevision modifyNamePart(NamePart namePart, String name, @Nullable String mnemonic, @Nullable String description, @Nullable UserAccount user, @Nullable String comment) {
        final NamePartView namePartView = view(namePart);

        final NamePartRevision baseRevision = namePartView.getCurrentOrElsePendingRevision();
        final @Nullable NamePartRevision pendingRevision = namePartView.getPendingRevision();

        Preconditions.checkState(!baseRevision.isDeleted() && (pendingRevision == null || !pendingRevision.isDeleted()));
        if (mnemonic==null) {
            Preconditions.checkState(isMnemonicValid(namePart.getNamePartType(), namePartView.getParent() != null ? namePartView.getParent().getNamePart() : null, mnemonic));
        } else if (baseRevision.getMnemonic()==null || !mnemonic.equals(baseRevision.getMnemonic())) {
            Preconditions.checkState(isMnemonicValid(namePart.getNamePartType(), namePartView.getParent() != null ? namePartView.getParent().getNamePart() : null, mnemonic));
            Preconditions.checkState(isMnemonicUniqueExceptForItself(baseRevision.getMnemonic(), namePart.getNamePartType(), namePartView.getParent() != null ? namePartView.getParent().getNamePart() : null, mnemonic));
        }

        if (pendingRevision != null) {
            updateRevisionStatus(pendingRevision, NamePartRevisionStatus.CANCELLED, user, null);
        }
        final String mnemonicEqClass = mnemonic!=null ? namingConvention.equivalenceClassRepresentative(mnemonic): null ;

        final NamePartRevision newRevision = new NamePartRevision(namePart, new Date(), user, comment, false, baseRevision.getParent(), name, mnemonic, description, mnemonicEqClass);

        em.persist(newRevision);
        
        final NamePartRevision currentRevision=namePartView.getCurrentRevision();
//        	if(newRevision.isEquivalentWith(namePartView.getCurrentRevision())) cancelChangesForNamePart(namePart, null, "Automatically cancelled: Equivalent with current approved revision", false);	

        return newRevision;
    }

    /**
     * Submits a proposal for deletion of a name part, its children, and all associated devices.
     *
     * @param namePart the name part proposed for deletion
     * @param user the user submitting the proposal. Null if the proposal is by an automated process.
     * @param comment the comment the user gave when submitting the proposal. Null if no comment was given.
     * @return the resulting proposed NamePart revision
     */
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
                final NamePartRevision newRevision = new NamePartRevision(namePart, new Date(), user, comment, true, approvedRevision.getParent(), approvedRevision.getName(), approvedRevision.getMnemonic(), approvedRevision.getDescription(), approvedRevision.getMnemonicEqClass());
                em.persist(newRevision);
                return newRevision;
            } else {
                return pendingRevision;
            }
        } else {
            return approvedRevision != null ? approvedRevision : pendingRevision;
        }
    }

    /**
     * Cancels or rejects the currently proposed modification, deletion or addition of the given name part.
     *
     * @param namePart the affected name part
     * @param user the user canceling or rejecting the proposal. Null if done by an automated process.
     * @param comment the comment the user gave when canceling or rejecting the proposal. Null if no comment was given.
     * @param markAsRejected true if this is a rejection by the administrator and not a cancel by the original submitter
     * of the proposal
     * @return the affected NamePart revision
     */
    public NamePartRevision cancelChangesForNamePart(NamePart namePart, @Nullable UserAccount user, @Nullable String comment, boolean markAsRejected) {
        final @Nullable NamePartRevision approvedRevision = approvedRevision(namePart);
        final @Nullable NamePartRevision pendingRevision = pendingRevision(namePart);

        if (pendingRevision != null && pendingRevision.getStatus() == NamePartRevisionStatus.PENDING) {
            if (canCancelChild(pendingRevision.getParent())) {
                updateRevisionStatus(pendingRevision, markAsRejected ? NamePartRevisionStatus.REJECTED : NamePartRevisionStatus.CANCELLED, user, comment);
                if (approvedRevision == null || pendingRevision.isDeleted()) {
                    for (NamePart child : approvedAndProposedChildren(pendingRevision.getNamePart())) {
                        cancelChildNamePart(child, user, comment, markAsRejected);
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

    /**
     * Approves the modification, deletion or addition proposed in the given name part revision.
     *
     * @param namePartRevision the name part revision to approve
     * @param user the user approving the revision. Null if done by an automated process.
     * @param comment the comment the user gave when approving the revision. Null if no comment was given.
     * @return the affected NamePart revision
     */
    public NamePartRevision approveNamePartRevision(NamePartRevision namePartRevision, @Nullable UserAccount user, @Nullable String comment) {
        namePartRevision = emAttached(namePartRevision);
        if (namePartRevision.getStatus() == NamePartRevisionStatus.PENDING) {
            if (canApproveChild(namePartRevision.getParent())) {
                updateRevisionStatus(namePartRevision, NamePartRevisionStatus.APPROVED, user, comment);

                if (namePartRevision.isDeleted()) {
                    for (Device device : associatedDevices(namePartRevision.getNamePart(), false)) {
                        deleteDevice(device, user);
                    }
                } else {
                    for (Device device : associatedDevices(namePartRevision.getNamePart(), true)) {
                        final DeviceRevision currentDeviceRevision = currentRevision(device);
                        modifyDevice(device, currentDeviceRevision.getSection(), currentDeviceRevision.getDeviceType(), currentDeviceRevision.getInstanceIndex(), currentDeviceRevision.getAdditionalInfo(), user);
                    }
                }

                if (namePartRevision.isDeleted()) {
                    for (NamePart child : approvedAndProposedChildren(namePartRevision.getNamePart())) {
                        approveChildNamePartForDelete(child, user);
                    }
                }

                return namePartRevision;
            } else {
                throw new IllegalStateException();
            }
        } else if (namePartRevision.getStatus() == NamePartRevisionStatus.APPROVED) {
            return namePartRevision;
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * The list of devices associated by the given name part (contained under a section or of a given device type)
     *
     * @param namePart the name part
     * @param recursive true if the list should also contain devices associated by the name part's children
     * deeper down in the hierarchy
     */
    public List<Device> associatedDevices(NamePart namePart, boolean recursive) {
        final List<Device> associatedDevices = Lists.newArrayList();
        if (namePart.getNamePartType() == NamePartType.SECTION) {
            associatedDevices.addAll(devicesInSection(namePart));
        } else if (namePart.getNamePartType() == NamePartType.DEVICE_TYPE) {
            associatedDevices.addAll(devicesOfType(namePart));
        } else {
            throw new UnhandledCaseException();
        }
        if (recursive) {
            for (NamePart child : approvedChildren(namePart)) {
                associatedDevices.addAll(associatedDevices(child, true));
            }
        }
        return associatedDevices;
    }

    /**
     * The list of current, most recent approved revisions of all name parts of a given type in the database.
     *
     * @param type the type of the name parts
     * @param includeDeleted true if the list should also include revisions for deleted name parts
     */
    public List<NamePartRevision> currentApprovedNamePartRevisions(NamePartType type, boolean includeDeleted) {
        if (includeDeleted)
            return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.namePart.namePartType = :type AND r2.status = :approved)", NamePartRevision.class).setParameter("type", type).setParameter("approved", NamePartRevisionStatus.APPROVED).getResultList();
        else {
            return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.namePart.namePartType = :type AND r2.status = :approved) AND r.deleted = FALSE", NamePartRevision.class).setParameter("type", type).setParameter("approved", NamePartRevisionStatus.APPROVED).getResultList();
        }
    }
    
    /**
     * The list of all revisions
     */
    public List<NamePartRevision> allNamePartRevisions() {
        return em.createQuery("SELECT r FROM NamePartRevision r", NamePartRevision.class).getResultList();
    }

    /**
     * The list of revisions pending approval of all name parts of the given type in the database.
     *
     * @param type the type of the name parts
     * @param includeDeleted true if the list should also include revisions for deleted name parts
     */
    public List<NamePartRevision> currentPendingNamePartRevisions(NamePartType type, boolean includeDeleted) {
        if (includeDeleted)
            return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.namePart.namePartType = :type AND r2.status = :pending)", NamePartRevision.class).setParameter("type", type).setParameter("pending", NamePartRevisionStatus.PENDING).getResultList();
        else {
            return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.namePart.namePartType = :type AND r2.status = :pending) AND NOT (r.status = :approved AND r.deleted = TRUE)", NamePartRevision.class).setParameter("type", type).setParameter("pending", NamePartRevisionStatus.PENDING).getResultList();
        }
    }

    /**
     * The list of all revisions of the given name part, including approved, pending, canceled or rejected, starting
     * from the oldest to the latest.
     * @param namePart the name part
     */
    public List<NamePartRevision> revisions(NamePart namePart) {
        return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.namePart = :namePart ORDER BY r.id", NamePartRevision.class).setParameter("namePart", namePart).getResultList();
    }

    /**
     * The current, most recent approved revision of the given name part. Null if no approved revision exists for this name part.
     *
     * @param namePart the name part
     */
    public @Nullable NamePartRevision approvedRevision(NamePart namePart) {
        return JpaHelper.getSingleResultOrNull(em.createQuery("SELECT r FROM NamePartRevision r WHERE r.namePart = :namePart AND r.status = :status ORDER BY r.id DESC", NamePartRevision.class).setParameter("namePart", namePart).setParameter("status", NamePartRevisionStatus.APPROVED).setMaxResults(1));
    }

    /**
     * The revision of the given name part currently pending approval. Null if no revision is pending approval.
     *
     * @param namePart the name part
     */
    public @Nullable NamePartRevision pendingRevision(NamePart namePart) {
        final @Nullable NamePartRevision lastPendingOrApprovedRevision = JpaHelper.getSingleResultOrNull(em.createQuery("SELECT r FROM NamePartRevision r WHERE r.namePart = :namePart AND (r.status = :approved OR r.status = :pending) ORDER BY r.id DESC", NamePartRevision.class).setParameter("namePart", namePart).setParameter("approved", NamePartRevisionStatus.APPROVED).setParameter("pending", NamePartRevisionStatus.PENDING).setMaxResults(1));
        if (lastPendingOrApprovedRevision == null) return null;
        else if (lastPendingOrApprovedRevision.getStatus() == NamePartRevisionStatus.PENDING) return lastPendingOrApprovedRevision;
        else return null;
    }

    /**
     * Adds a batch of devices. Similar to addDevice, but with better performance when adding a large number of devices.
     *
     * @param devices the devices to add
     * @param user the user adding the devices. Null if done by an automated process.
     */
    public void batchAddDevices(Iterable<DeviceDefinition> devices, @Nullable UserAccount user) {
        final BatchViewProvider batchViewProvider = new BatchViewProvider(currentApprovedNamePartRevisions(NamePartType.SECTION, false), currentApprovedNamePartRevisions(NamePartType.DEVICE_TYPE, false), ImmutableList.<DeviceRevision>of());
        for (DeviceDefinition device : devices) {
            final NamePartView sectionView = batchViewProvider.view(device.section());
            final NamePartView deviceTypeView = batchViewProvider.view(device.deviceType());
            Preconditions.checkArgument(!sectionView.isDeleted());
            Preconditions.checkArgument(!deviceTypeView.isDeleted());

            final String conventionName = namingConvention.conventionName(sectionView.getMnemonicPath(), deviceTypeView.getMnemonicPath(), device.instanceIndex());
            final String conventionNameEqClass = namingConvention.equivalenceClassRepresentative(conventionName);

            Preconditions.checkState(namingConvention.isInstanceIndexValid(sectionView.getMnemonicPath(), deviceTypeView.getMnemonicPath(), device.instanceIndex()));
            Preconditions.checkState(isDeviceConventionNameUnique(conventionName));

            final Device deviceEntity = new Device(UUID.randomUUID());
            final DeviceRevision newRevision = new DeviceRevision(deviceEntity, new Date(), user, false, device.section(), device.deviceType(), device.instanceIndex(), conventionName, conventionNameEqClass, device.additionalInfo());

            em.persist(deviceEntity);
            em.persist(newRevision);
        }
    }

    /**
     * Adds a new device.
     *
     * @param section the section containing the device
     * @param deviceType the device type of the device
     * @param instanceIndex the instance index of the device, null if omitted
     * @param additionalInfo Additional information (description, comment etc) of the device.
     * @param user the user adding the device. Null if done by an automated process.
     * @return current revision of the added device
     */
    public DeviceRevision addDevice(NamePart section, NamePart deviceType, @Nullable String instanceIndex,@Nullable String additionalInfo, @Nullable UserAccount user) {
        final NamePartRevision sectionRevision = As.notNull(approvedRevision(section));
        final NamePartRevision deviceTypeRevision = As.notNull(approvedRevision(deviceType));
        Preconditions.checkArgument(!sectionRevision.isDeleted());
        Preconditions.checkArgument(!deviceTypeRevision.isDeleted());

        final String conventionName = conventionName(section, deviceType, instanceIndex);
        final String conventionNameEqClass = namingConvention.equivalenceClassRepresentative(conventionName);

        Preconditions.checkState(isInstanceIndexValid(section, deviceType, instanceIndex));
        Preconditions.checkState(isDeviceConventionNameUnique(section, deviceType, instanceIndex));
        final Device device = new Device(UUID.randomUUID());
        final DeviceRevision newRevision = new DeviceRevision(device, new Date(), user, false, section, deviceType, instanceIndex, conventionName, conventionNameEqClass, additionalInfo);
               
        em.persist(device);
        em.persist(newRevision);

        return newRevision;
    }

    /**
     * Modifies the given device.
     *
     * @param device the device to modify
     * @param section the new section containing the device
     * @param deviceType the new device type of the device
     * @param instanceIndex the new instance index of the device, null no instance index is to be assigned to the device
     * @param additionalInfo Additional information (description, comment etc) of the device
     * @param user the user modifying the device. Null if done by an automated process.
     * @return the revision of the device resulting from the modification
     */
    public DeviceRevision modifyDevice(Device device, NamePart section, NamePart deviceType, @Nullable String instanceIndex,@Nullable String additionalInfo, @Nullable UserAccount user) {
        final DeviceRevision currentRevision = currentRevision(device);
        Preconditions.checkArgument(!currentRevision.isDeleted());
        device.getUuid();

        final NamePartRevision sectionRevision = As.notNull(approvedRevision(section));
        final NamePartRevision deviceTypeRevision = As.notNull(approvedRevision(deviceType));
        Preconditions.checkArgument(!sectionRevision.isDeleted());
        Preconditions.checkArgument(!deviceTypeRevision.isDeleted());

        final String conventionName = conventionName(section, deviceType, instanceIndex);
        final String conventionNameEqClass = namingConvention.equivalenceClassRepresentative(conventionName);
        
        final boolean sameName=
        		section.equals(currentRevision.getSection()) && 
        		deviceType.equals(currentRevision.getDeviceType()) && 
        		Objects.equal(instanceIndex, currentRevision.getInstanceIndex()) && 
        		conventionName.equals(currentRevision.getConventionName());
        if (!sameName) {
            Preconditions.checkState(isInstanceIndexValid(section, deviceType, instanceIndex));
            Preconditions.checkState(isDeviceConventionNameUniqueExceptForItself(device,section,deviceType,instanceIndex));
        	final DeviceRevision newRevision = new DeviceRevision(device, new Date(), user, false, section, deviceType, instanceIndex, conventionName, conventionNameEqClass, additionalInfo);
            em.persist(newRevision);
            return newRevision;

        } else if(sameName && ! additionalInfo.equals(currentRevision.getAdditionalInfo()) ) {
        	final DeviceRevision newRevision = new DeviceRevision(device, new Date(), user, false, section, deviceType, instanceIndex, conventionName, conventionNameEqClass, additionalInfo);
            em.persist(newRevision);
            return newRevision;
        } else {   	
        	return currentRevision;
            //TODO: Throw exception if no changes was made. 
        }
    }

 
	/**
     * Deletes the given device
     *
     * @param device the device to delete
     * @param user the user deleting the device. Null if done by an automated process.
     * @return the revision of the device resulting from the deletion
     */
    public DeviceRevision deleteDevice(Device device, @Nullable UserAccount user) {
        final DeviceRevision currentRevision = currentRevision(device);

        if (!currentRevision.isDeleted()) {
            final DeviceRevision newRevision = new DeviceRevision(device, new Date(), user, true, currentRevision.getSection(), currentRevision.getDeviceType(), currentRevision.getInstanceIndex(), currentRevision.getConventionName(), currentRevision.getConventionNameEqClass(), currentRevision.getAdditionalInfo());
            em.persist(newRevision);
            return newRevision;
        } else {
            return currentRevision;
        }
    }

    /**
     * The list of current, most recent revisions of all devices in the database.
     *
     * @param includeDeleted true if the list should also include revisions for deleted devices
     */
    public List<DeviceRevision> currentDeviceRevisions(boolean includeDeleted) {
        if (includeDeleted)
            return em.createQuery("SELECT r FROM DeviceRevision r WHERE r.id = (SELECT MAX(r2.id) FROM DeviceRevision r2 WHERE r2.device = r.device)", DeviceRevision.class).getResultList();
        else {
            return em.createQuery("SELECT r FROM DeviceRevision r WHERE r.id = (SELECT MAX(r2.id) FROM DeviceRevision r2 WHERE r2.device = r.device) AND r.deleted = false", DeviceRevision.class).getResultList();
        }
    }

    /**
     * The list of all revisions of the given device, starting from the oldest to the latest.
     * @param device the device
     */
    public List<DeviceRevision> revisions(Device device) {
        return em.createQuery("SELECT r FROM DeviceRevision r WHERE r.device = :device ORDER BY r.id", DeviceRevision.class).setParameter("device", device).getResultList();
    }

    /**
     * The current, most recent revision of the given device
     *
     * @param device the device
     */
    public DeviceRevision currentRevision(Device device) {
        return em.createQuery("SELECT r FROM DeviceRevision r WHERE r.device = :device ORDER BY r.id DESC", DeviceRevision.class).setParameter("device", device).getResultList().get(0);
    }

    /**
     * The current, most recent revision of the device with the given UUID. Null if none found.
     *
     * @param deviceUuid the UUID of the device
     */
    public @Nullable DeviceRevision currentDeviceRevision(UUID deviceUuid) {
        return JpaHelper.getSingleResultOrNull(em.createQuery("SELECT r FROM DeviceRevision r WHERE r.device.uuid = :uuid ORDER BY r.id DESC", DeviceRevision.class).setParameter("uuid", deviceUuid.toString()).setMaxResults(1));
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

    private boolean canApproveChild(@Nullable NamePart parent) {
        if (parent != null) {
            final @Nullable NamePartRevision parentApprovedRevision = approvedRevision(parent);
            return (parentApprovedRevision != null && !parentApprovedRevision.isDeleted());
        } else {
            return true;
        }
    }

    private void approveChildNamePartForDelete(NamePart namePart, @Nullable UserAccount user) {
        final NamePartRevision pendingRevision = As.notNull(pendingRevision(namePart));

        updateRevisionStatus(pendingRevision, NamePartRevisionStatus.APPROVED, user, null);
        for (Device device : associatedDevices(namePart, false)) {
            deleteDevice(device, user);
        }

        for (NamePart child : approvedAndProposedChildren(namePart)) {
            approveChildNamePartForDelete(child, user);
        }
    }

    private void cancelChildNamePart(NamePart namePart, @Nullable UserAccount user, @Nullable String comment, boolean markAsRejected) {
        final NamePartRevision pendingRevision = As.notNull(pendingRevision(namePart));

        if (pendingRevision != null) {
            updateRevisionStatus(pendingRevision, markAsRejected ? NamePartRevisionStatus.REJECTED : NamePartRevisionStatus.CANCELLED, user, comment);
        }

        for (NamePart child : approvedAndProposedChildren(namePart)) {
            cancelChildNamePart(child, user, comment, markAsRejected);
        }
    }

    private void updateRevisionStatus(NamePartRevision pendingRevision, NamePartRevisionStatus newStatus, @Nullable UserAccount user, @Nullable String comment) {
        pendingRevision.updateAsProcessed(newStatus, new Date(), user, comment);
    }

    private List<NamePart> approvedChildren(NamePart namePart) {
        return em.createQuery("SELECT r.namePart FROM NamePartRevision r WHERE r.parent = :namePart AND r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND (r2.status = :approved)) AND NOT (r.status = :approved AND r.deleted = TRUE)", NamePart.class).setParameter("namePart", namePart).setParameter("approved", NamePartRevisionStatus.APPROVED).getResultList();
    }

    private List<NamePart> approvedAndProposedChildren(NamePart namePart) {
        return em.createQuery("SELECT r.namePart FROM NamePartRevision r WHERE r.parent = :namePart AND r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND (r2.status = :approved OR r2.status = :pending)) AND NOT (r.status = :approved AND r.deleted = TRUE)", NamePart.class).setParameter("namePart", namePart).setParameter("approved", NamePartRevisionStatus.APPROVED).setParameter("pending", NamePartRevisionStatus.PENDING).getResultList();
    }

    private String conventionName(NamePart section, NamePart deviceType, @Nullable String instanceIndex) {
        final NamePartView sectionView = view(section);
        final NamePartView deviceTypeView = view(deviceType);

        return namingConvention.conventionName(sectionView.getMnemonicPath(), deviceTypeView.getMnemonicPath(), instanceIndex);
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

    private List<Device> devicesInSection(NamePart section) {
        return em.createQuery("SELECT r.device FROM DeviceRevision r WHERE r.id = (SELECT MAX(r2.id) FROM DeviceRevision r2 WHERE r2.device = r.device) AND r.section = :section AND r.deleted = false", Device.class).setParameter("section", section).getResultList();
    }

    private List<Device> devicesOfType(NamePart deviceType) {
        return em.createQuery("SELECT r.device FROM DeviceRevision r WHERE r.id = (SELECT MAX(r2.id) FROM DeviceRevision r2 WHERE r2.device = r.device) AND r.deviceType = :deviceType AND r.deleted = false", Device.class).setParameter("deviceType", deviceType).getResultList();
    }

	


}
