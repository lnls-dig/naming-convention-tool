/*-
 * Copyright (c) 2014 European Spallation Source
 * Copyright (c) 2014 Cosylab d.d.
 *
 * This file is part of Naming Service.
 * Naming Service is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 2 of the License, or any newer version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.openepics.names.services;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
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

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * A service bean managing NamePart and Device entities.
 *
 * @author Marko Kolar  
 * @author Karin Rahtsman  
 */
@Stateless
public class NamePartService {

	@Inject private NamingConvention namingConvention;
	@PersistenceContext private EntityManager em;

	private List<String> getMnemonicPath(@Nullable NamePart namePart){
		final @Nullable NamePartView namePartView = namePart != null ? view(namePart) : null;
		return namePartView != null ? namePartView.getMnemonicPath() : ImmutableList.<String>of();
	}

	/**
	 * 
	 * @param namePartType The type of the name part specifying whether the name part belongs to the Logical Area Structure or the Device Structure
	 * @param namePart The name part, null if the name part is at the root of the hierarchy
	 * @return True if the mnemonic can be null.
	 */
	public boolean isMnemonicRequired(NamePartType namePartType, @Nullable NamePart namePart) {
		final @Nullable NamePartView namePartView = namePart != null ? view(namePart) : null;
		final List<String> mnemonicPath = namePartView != null ? namePartView.getMnemonicPath() : ImmutableList.<String>of();
		return namingConvention.isMnemonicRequired(mnemonicPath, namePartType);
	}

	/**
	 * 
	 * @param namePartType The type of the name part specifying whether the name part belongs to the Logical Area Structure or the Device Structure
	 * @param namePart The parent of the name part, null if the name part is at the root of the hierarchy
	 * @return True if the mnemonic of a child name part can be null.
	 */
	public boolean isMnemonicRequiredForChild(NamePartType namePartType, NamePart namePart) {
		final @Nullable NamePartView namePartView = namePart != null ? view(namePart) : null;
		final List<String> addMnemonicPath = namePartView != null ? namePartView.getMnemonicPathWithChild("") : ImmutableList.<String>of("");
		return namingConvention.isMnemonicRequired(addMnemonicPath, namePartType);
	}

	/**
	 * 
	 * @param namePartType The type of the name part specifying whether the name part belongs to the Logical Area Structure or the Device Structure
	 * @param namePart The name part, null if the name part is at the root of the hierarchy
	 * @return The name of the name part type to be used in dialog header and menus. Example: "Modify mnemonic for namePartTypeName" where namePartTypeName can be section, subsection, discipline etc... 
	 */
	public String getNamePartTypeName(NamePartType namePartType, NamePart namePart) {
		final @Nullable NamePartView namePartView = namePart != null ? view(namePart) : null;
		final List<String> mnemonicPath = namePartView != null ? namePartView.getMnemonicPath() : ImmutableList.<String>of();
		return namingConvention.getNamePartTypeName(mnemonicPath, namePartType);
	}

	/**
	 * 
	 * @param namePartType The type of the name part specifying whether the name part belongs to the Logical Area Structure or the Device Structure
	 * @param namePart The name part, null if the name part is at the root of the hierarchy
	 * @return The name of the name part type to be used in dialog header and menus. Example: "Add mnemonic for namePartTypeName" where namePartTypeName can be section, subsection, discipline etc... 
	 */
	public String getNamePartTypeNameForChild(NamePartType namePartType, NamePart namePart) {
		final @Nullable NamePartView namePartView = namePart != null ? view(namePart) : null;
		final List<String> mnemonicPath = namePartView != null ? namePartView.getMnemonicPathWithChild("") : ImmutableList.<String>of("");
		return namingConvention.getNamePartTypeName(mnemonicPath, namePartType);
	}

	/**
	 * 
	 * @param namePartType The type of the name part specifying whether the name part belongs to the Logical Area Structure or the Device Structure
	 * @param namePart The name part, null if the name part is at the root of the hierarchy
	 * @return The mnemonic of the name part type to be used in dialogs. Example: "' Mnemonic: namePartTypeMnemonic" where namePartTypeMnemonic can be Sec, Dev  etc... 
	 */
	public String getNamePartTypeMnemonic(NamePartType namePartType, NamePart namePart) {
		final @Nullable NamePartView namePartView = namePart != null ? view(namePart) : null;
		final List<String> mnemonicPath = namePartView != null ? namePartView.getMnemonicPath() : ImmutableList.<String>of();
		return namingConvention.getNamePartTypeMnemonic(mnemonicPath, namePartType);
	}

	/**
	 * 
	 * @param namePartType The type of the name part specifying whether the name part belongs to the Logical Area Structure or the Device Structure
	 * @param namePart The name part, null if the name part is at the root of the hierarchy
	 * @return The mnemonic of the name part type to be used in dialogs. Example: "' Mnemonic: namePartTypeMnemonic" where namePartTypeMnemonic can be Sec, Dev  etc... 
	 */
	public String getNamePartTypeMnemonicForChild(NamePartType namePartType, NamePart namePart) {
		final @Nullable NamePartView namePartView = namePart != null ? view(namePart) : null;
		final List<String> mnemonicPath = namePartView != null ? namePartView.getMnemonicPathWithChild("") : ImmutableList.<String>of("");
		return namingConvention.getNamePartTypeMnemonic(mnemonicPath, namePartType);
	}


	/**
	 * 
	 * @param type The type of the name part specifying whether the name part belongs to the Logical Area Structure or the Device Structure
	 * @param parent Parent of the name part, null if the name part is at the root of the hierarchy
	 * @param mnemonic The mnemonic name of the name part to test for validity
	 * @return True if the mnemonic of a name part is valid in the context of the parent.
	 */
	public boolean isMnemonicValid(NamePartType type, @Nullable NamePart parent, @Nullable String mnemonic) {
		final @Nullable NamePartView namePartParentView = parent != null ? view(parent) : null;
		final List<String> mnemonicPath = namePartParentView != null ? namePartParentView.getMnemonicPathWithChild(mnemonic) : ImmutableList.<String>of(mnemonic!=null? mnemonic: "");
		return namingConvention.isMnemonicValid(mnemonicPath, type);
	}

	/**
	 * 
	 * @param type The type of the name part specifying whether the name part belongs to the Logical Area Structure or the Device Structure
	 * @param parent the parent of the name part, null if the name part is at the root of the hierarchy
	 * @param mnemonic the mnemonic name of the name part to test for uniqueness
	 * @return True if the mnemonic of a name part that is to be added is unique according to the naming convention rules.
	 */
	public boolean isMnemonicUniqueOnAdd(NamePartType type, @Nullable NamePart parent, @Nullable String mnemonic) {        
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
				if (!namingConvention.canMnemonicsCoexist(newMnemonicPath, type, view(sameEqClassRevision.getNamePart()).getMnemonicPath(), sameEqClassRevision.getNamePart().getNamePartType())) {
					return false;
				}
			}
			return true; 
		}
	}

	/**
	 * @param namePart The name part, null if the name part is at the root of the hierarchy
	 * @param parent the parent of the name part, null if the name part is at the 
	 * @param mnemonic the mnemonic name of the name part to be tested for uniqueness
	 * @return True if the mnemonic of a name part that is to be modify is unique according to the naming convention rules.
	 */
	public boolean isMnemonicUniqueOnModify(NamePart namePart, @Nullable NamePart parent, String mnemonic){
		NamePartType namePartType=namePart.getNamePartType();
		final List<String> newMnemonicPath = ImmutableList.<String>builder().addAll(getMnemonicPath(parent)).add(mnemonic!=null? mnemonic:"").build();
//		if(namingConvention.isMnemonicRequired(newMnemonicPath, namePartType)) {
			final String mnemonicEquivalenceClass = namingConvention.equivalenceClassRepresentative(mnemonic);    	
			final List<NamePartRevision> sameEqClassRevisions = em.createQuery("SELECT r FROM NamePartRevision r WHERE (r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.status = :approved) OR r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.status = :pending)) AND r.deleted = FALSE AND r.mnemonicEqClass = :mnemonicEquivalenceClass", NamePartRevision.class).setParameter("approved", NamePartRevisionStatus.APPROVED).setParameter("pending", NamePartRevisionStatus.PENDING).setParameter("mnemonicEquivalenceClass", mnemonicEquivalenceClass).getResultList();
			for (NamePartRevision sameEqClassRevision : sameEqClassRevisions) {
				final NamePart sameEqClassNamePart=sameEqClassRevision.getNamePart();
				if(! Objects.equal(namePart,sameEqClassNamePart)){     		
					final List<String> sameEqClassNamePartPath =getMnemonicPath(sameEqClassNamePart);
					final NamePartType sameEqClassNamePartType=sameEqClassNamePart.getNamePartType();
					if (!namingConvention.canMnemonicsCoexist(newMnemonicPath, namePartType, sameEqClassNamePartPath, sameEqClassNamePartType) ) {
						return false;
					}           
				}    	
			}			
//		}
		
		return true;
	}

	/**
	 *
	 * @param subsection The subsection containing the device
	 * @param deviceType the device type of the device
	 * @param instanceIndex the device instance index to test for validity, or null if no instance index is assigned to the device, in which case this is also checked for validity
	 * @return True if the instance index of a device defined by a section and device type is valid.
	 */
	public boolean isInstanceIndexValid(NamePart subsection, NamePart deviceType, @Nullable String instanceIndex) {
		final NamePartView subsectionView = view(subsection);
		final NamePartView deviceTypeView = view(deviceType);
		return namingConvention.isInstanceIndexValid(subsectionView.getMnemonicPath(), deviceTypeView.getMnemonicPath(), instanceIndex);
	}

	/**
	 *
	 * @param subsection The subsection containing the device
	 * @param deviceType The device type of the device
	 * @param instanceIndex The instance index of the device, null if no instance index is assigned to the device
	 * @return True if the device defined by the given subsection, device type and instance index would have a unique convention name.
	 */
	public boolean isDeviceConventionNameUnique(NamePart subsection, NamePart deviceType, @Nullable String instanceIndex) {
		final String conventionName = conventionName(subsection, deviceType, instanceIndex);
		return isDeviceConventionNameUnique(conventionName);
	}

	/**
	 * @param subsection The subsection containing the device
	 * @param deviceType The device type 
	 * @param instanceIndex The instance index to test for validity, or null if no instance index is assigned to the device, in which case this is also checked for validity
	 * @param device Instance of device
	 * @return True if the device defined by the given subsection, device type and instance index would have a unique convention name, not taking into account itself.  
	 */
	public boolean isDeviceConventionNameUniqueExceptForItself(Device device, NamePart subsection, NamePart deviceType, @Nullable String instanceIndex) {
		final String conventionName=conventionName(subsection, deviceType, instanceIndex);
		final String conventionNameEqClass = namingConvention.equivalenceClassRepresentative(conventionName);
		final DeviceRevision currentRevision = currentRevision(device);
		String currentConventionNameEqClass=currentRevision.getConventionNameEqClass();
		boolean nameIsEquivalentToCurrentRevision=conventionNameEqClass.equals(currentConventionNameEqClass);
		return isDeviceConventionNameUnique(conventionName) || nameIsEquivalentToCurrentRevision;
	}

	/**
	 *
	 * @param conventionName the device name to check
	 * @return True if the device with the given convention name have a unique convention name.
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
	 * @param description Description, comment or other relevant information for the name part
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
		if(mnemonic!=null) Preconditions.checkState(isMnemonicUniqueOnAdd(namePartType, parent, mnemonic));
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
	 * @param description Description, comment or other relevant information for the name part
	 * @param user the user submitting the proposal. Null if the proposal is by an automated process.
	 * @param comment the comment the user gave when submitting the proposal. Null if no comment was given.
	 * @return the resulting proposed NamePart revision
	 */
	public NamePartRevision modifyNamePart(NamePart namePart, String name, @Nullable String mnemonic, @Nullable String description, @Nullable UserAccount user, @Nullable String comment) {
		// namePart
		final NamePartView namePartView = view(namePart);				
		final NamePartRevision baseRevision = namePartView.getCurrentOrElsePendingRevision();
		final @Nullable NamePartRevision pendingRevision = namePartView.getPendingRevision();
		// parent
		final @Nullable NamePartView parentView=namePartView.getParent();
		final @Nullable NamePart parent = parentView!=null? parentView.getNamePart():null;
		// type
		final NamePartType namePartType=namePart.getNamePartType();
		// modification type
		boolean renaming= !Objects.equal(mnemonic,namePartView.getMnemonic());

		Preconditions.checkState(!namePartView.isDeleted() && !namePartView.isPendingDeletion(), "name part is deleted or pending deletion");
		Preconditions.checkState(name!=null && !name.isEmpty(), "full name is empty");
		if(renaming){
			Preconditions.checkState(isMnemonicValid(namePartType, parent, mnemonic), "mnemonic is not valid");

			if(isMnemonicRequiredForChild(parent, namePartType)){
				//		Preconditions.checkState(!(hasDevices(namePart)));
				Preconditions.checkState(isMnemonicUniqueOnModify(namePart, parent, mnemonic), "mnemonic is not unique");
			}
		}

		if (pendingRevision != null) {
			updateRevisionStatus(pendingRevision, NamePartRevisionStatus.CANCELLED, user, null);
		}

		final String mnemonicEqClass = mnemonic!=null ? namingConvention.equivalenceClassRepresentative(mnemonic): null ;
		final NamePartRevision newRevision = new NamePartRevision(namePart, new Date(), user, comment, false, parent, name, mnemonic, description, mnemonicEqClass);

		em.persist(newRevision);

		final NamePartRevision currentRevision=namePartView.getCurrentRevision();
		//	 	if(newRevision.isEquivalentWith(namePartView.getCurrentRevision())) cancelChangesForNamePart(namePart, null, "Automatically cancelled: Equivalent with current approved revision", false);	
		return newRevision;
	}

	/**
	 * Submits a proposal for moving an existing name part.
	 *
	 * @param namePart The name part proposed for move
	 * @param destinationParent Proposed destination parent of the name part
	 * @param user The user submitting the proposal. Null if the proposal is by an automated process.
	 * @param comment The comment or message the user gave when submitting the proposal. Null if no comment was given.
	 * @return the resulting proposed NamePart revision
	 */
	public NamePartRevision moveNamePart(NamePart namePart, @Nullable NamePart destinationParent, @Nullable UserAccount user, @Nullable String comment) {
		// namePart
		final NamePartView namePartView = view(namePart);				
		final NamePartRevision baseRevision = namePartView.getCurrentOrElsePendingRevision();
		final @Nullable NamePartRevision pendingRevision = namePartView.getPendingRevision();
		final String mnemonic=namePartView.getMnemonic();
		// source Parent
		final @Nullable NamePartView sourceParentView=namePartView.getParent();
		final @Nullable NamePart sourceParent = sourceParentView!=null? sourceParentView.getNamePart():null;

		// destinationParent
		final @Nullable NamePartView destinationParentView = destinationParent!=null? view(destinationParent):null;
		final @Nullable NamePartRevision destinationParentRevision = destinationParentView!=null? destinationParentView.getCurrentOrElsePendingRevision():null;

		// type
		final NamePartType namePartType=namePart.getNamePartType();
		// modification type
		boolean moving=  !Objects.equal(destinationParent, sourceParent);

		Preconditions.checkState(moving,"Namepart is not moving");
		Preconditions.checkState(canNamePartMove(sourceParent,destinationParent,namePartType), "NamePart cannot move into this parent level");
		Preconditions.checkState(!pendingPath(destinationParentView),"There are pending revisions in the destination parent view tree");
		Preconditions.checkState(!pendingPath(namePartView),"There are pending revisions in the name part tree");
		Preconditions.checkState(!hasDevices(namePart), "Operation affects named devices");
		Preconditions.checkState(!namePartView.isDeleted() && !namePartView.isPendingDeletion(), "name part is deleted or pending deletion");

		if(isMnemonicRequiredForChild(destinationParent, namePartType)){
			Preconditions.checkState(isMnemonicUniqueOnModify(namePart, destinationParent, mnemonic), "mnemonic is not unique");
		}

		//	if (pendingRevision != null) {
		//		updateRevisionStatus(pendingRevision, NamePartRevisionStatus.CANCELLED, user, null);
		//	}

		final String mnemonicEqClass = mnemonic!=null ? namingConvention.equivalenceClassRepresentative(mnemonic): null ;
		final NamePartRevision newRevision = new NamePartRevision(namePart, new Date(), user, comment, false, destinationParent, namePartView.getName(), mnemonic, namePartView.getDescription(), mnemonicEqClass);

		em.persist(newRevision);

		final NamePartRevision currentRevision=namePartView.getCurrentRevision();
		//	 	if(newRevision.isEquivalentWith(namePartView.getCurrentRevision())) cancelChangesForNamePart(namePart, null, "Automatically cancelled: Equivalent with current approved revision", false);	
		return newRevision;
	}

	/**
	 * 
	 * @param namePart Name Part
	 * @return True if the name part has dependent devices
	 */
	private boolean hasDevices(NamePart namePart) {
		List<String> mnemonicPath=getMnemonicPath(namePart);

		if(mnemonicPath.size()==3){
			List<Device> devices=selectDevicesFrom(namePart); 
			if(devices!=null && !devices.isEmpty()){
				return true;
			} else {
				return false;
			}
		} else {
			List<NamePart> children= approvedAndProposedChildren(namePart);
			for (NamePart child : children) {
				if(hasDevices(child)){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @param sourceParent Source parent of Name part to be moved
	 * @param destinationParent Destination Parent of Name part to be moved
	 * @param namePartType Name Part type, area or device structure
	 * @return True if a name part can be moved into another parent name part, according to the naming convention rules.
	 */
	private boolean canNamePartMove(NamePart sourceParent, NamePart destinationParent, NamePartType namePartType){
		List<String> destinationPath=getMnemonicPath(destinationParent);
		List<String> sourcePath=getMnemonicPath(sourceParent);
		NamePartType sourceType=sourceParent!=null? sourceParent.getNamePartType(): namePartType;
		NamePartType destinationType= destinationParent!=null? destinationParent.getNamePartType(): namePartType;
		return namingConvention.canNamePartMove(sourcePath, sourceType, destinationPath, destinationType);	
	}

	/**
	 * 
	 * @param namePart Name Part
	 * @param type Name Part Type specifying if the name part belongs to the Area Structure or the Device Structure
	 * @return True Mnemonic of child name part of this namepart  
	 */
	private boolean isMnemonicRequiredForChild(NamePart namePart, NamePartType type){
		final List<String> mnemonicPath = ImmutableList.<String>builder().addAll(getMnemonicPath(namePart)).add("").build();
		NamePartType mnemonicType=namePart!=null? namePart.getNamePartType():type;
		return namingConvention.isMnemonicRequired(mnemonicPath, mnemonicType);
	}

	//private List<NamePartRevision> getChildren(NamePartRevision namePartRevision){
	//	return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.namePart.parent = :parent AND r2.status = :pending OR r2.status=:approved)", NamePartRevision.class).setParameter("parent", namePartRevision.getParent()).setParameter("pending", NamePartRevisionStatus.PENDING).setParameter("approved", NamePartRevisionStatus.APPROVED).getResultList();		
	//}

	private List<Device> selectDevicesFrom(NamePart namePart){
		if(namePart.getNamePartType().equals(NamePartType.SECTION)){
			return devicesInSection(namePart);
		} else {
			return devicesOfType(namePart);
		}
	}

	/**
	 * @param namePartView View of Name Part
	 * @return True if any name parts in the same branch, from top to bottom, have any pending changes.   
	 */
	private boolean pendingPath(NamePartView namePartView){
		if(namePartView.isPendingInThisLevelAndAbove()){
			return true;
		} else {
			return hasPendingChildren(namePartView.getCurrentOrElsePendingRevision().getNamePart());
		}
	}

	/**
	 * 
	 * @param namePart Name Part in the Device Structure or Area Structure
 	 * @return True if any of the children of the name part is pending approval for being added, modified or deleted  
	 */
	private boolean hasPendingChildren(NamePart namePart) {
		for (NamePart child : approvedAndProposedChildren(namePart)) {
			if(child.getNamePartType().equals(NamePartRevisionStatus.PENDING)){
				return true;
			} else if (hasPendingChildren(child)){
				return true;
			}
		}
		return false;		
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
	 *
	 * @param namePart the name part
	 * @param recursive true if the list should also contain devices associated by the name part's children deeper down in the hierarchy
	 * @return The list of devices associated by the given name part (contained under a section or of a given device type)
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
	 *
	 * @param type the type of the name parts
	 * @param includeDeleted true if the list should also include revisions for deleted name parts
	 * @return The list of current, most recent approved revisions of all name parts of a given type in the database.
	 */
	public List<NamePartRevision> currentApprovedNamePartRevisions(NamePartType type, boolean includeDeleted) {
		if (includeDeleted)
			return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.namePart.namePartType = :type AND r2.status = :approved)", NamePartRevision.class).setParameter("type", type).setParameter("approved", NamePartRevisionStatus.APPROVED).getResultList();
		else {
			return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.namePart.namePartType = :type AND r2.status = :approved) AND r.deleted = FALSE", NamePartRevision.class).setParameter("type", type).setParameter("approved", NamePartRevisionStatus.APPROVED).getResultList();
		}
	}

	
	public List<NamePartRevision> currentApprovedNamePartRevisionRoots(NamePartType type, boolean includeDeleted) {
		if (includeDeleted)
			return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.namePart.namePartType = :type AND r2.status = :approved) AND r.parent IS NULL ORDER BY r.name", NamePartRevision.class).setParameter("type", type).setParameter("approved", NamePartRevisionStatus.APPROVED).getResultList();
		else {
			return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.namePart.namePartType = :type AND r2.status = :approved) AND r.deleted = FALSE AND r.parent IS NULL ORDER BY r.name", NamePartRevision.class).setParameter("type", type).setParameter("approved", NamePartRevisionStatus.APPROVED).getResultList();
		}
	}

	
	
	/**
	 * @return The list of all revisions
	 */
	public List<NamePartRevision> allNamePartRevisions() {
		return em.createQuery("SELECT r FROM NamePartRevision r", NamePartRevision.class).getResultList();
	}

	/**
	 *
	 * @param type the type of the name parts
	 * @param includeDeleted true if the list should also include revisions for deleted name parts
	 * @return The list of revisions pending approval of all name parts of the given type in the database.
	 */
	public List<NamePartRevision> currentPendingNamePartRevisions(NamePartType type, boolean includeDeleted) {
		if (includeDeleted)
			return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.namePart.namePartType = :type AND r2.status = :pending)", NamePartRevision.class).setParameter("type", type).setParameter("pending", NamePartRevisionStatus.PENDING).getResultList();
		else {
			return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.namePart.namePartType = :type AND r2.status = :pending) AND NOT (r.status = :approved AND r.deleted = TRUE)", NamePartRevision.class).setParameter("type", type).setParameter("pending", NamePartRevisionStatus.PENDING).getResultList();
		}
	}

	/**
	 * @param namePart the name part
	 * @return The list of all revisions of the given name part, including approved, pending, canceled or rejected, starting from the oldest to the latest.
	 */
	public List<NamePartRevision> revisions(NamePart namePart) {
		return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.namePart = :namePart ORDER BY r.id", NamePartRevision.class).setParameter("namePart", namePart).getResultList();
	}

	/**
	 *
	 * @param namePart the name part
	 * @return The current, most recent approved revision of the given name part. Null if no approved revision exists for this name part.
	 */
	public @Nullable NamePartRevision approvedRevision(NamePart namePart) {
		return JpaHelper.getSingleResultOrNull(em.createQuery("SELECT r FROM NamePartRevision r WHERE r.namePart = :namePart AND r.status = :status ORDER BY r.id DESC", NamePartRevision.class).setParameter("namePart", namePart).setParameter("status", NamePartRevisionStatus.APPROVED).setMaxResults(1));
	}

	/**
	 *
	 * @param namePart the name part
	 * @return The revision of the given name part currently pending approval. Null if no revision is pending approval.
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
				conventionName.equals(currentRevision.getConventionName()) &&
				Objects.equal(additionalInfo,currentRevision.getAdditionalInfo());
		if (!sameName) {
			Preconditions.checkState(isInstanceIndexValid(section, deviceType, instanceIndex));
			Preconditions.checkState(isDeviceConventionNameUniqueExceptForItself(device,section,deviceType,instanceIndex));
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
	 * @return The list of all revisions of obsolete devices in the database. 
	 */
	public List<DeviceRevision> obsoleteDeviceRevisions(){
		return em.createQuery("SELECT r FROM DeviceRevision r WHERE r.id <> (SELECT MAX(r2.id) FROM DeviceRevision r2 WHERE r2.device = r.device)", DeviceRevision.class).getResultList();
	}


	public List<DeviceRevision> filteredDeviceRevisions(boolean includeDeleted, List<NamePart> subsections, List<NamePart> deviceTypes){
		List<DeviceRevision> devices=Lists.newArrayList();
		for(NamePart subsection : subsections) {
			for(NamePart deviceType:deviceTypes){
				List<DeviceRevision> temporary=devicesRevisionsOf(subsection, deviceType);
				if(temporary!=null) devices.addAll(devicesRevisionsOf(subsection, deviceType));
			}
		}
		return devices;		
	}
	
	/**
	 * @return The list of current, most recent revisions of all devices in the database.
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
	 * @return The list of the latest obsolete or deleted, revisions of all devicenames in the database.
	 *
	 */
	public List<DeviceRevision> latestObsoleteDeviceRevisionsGroupedByName() {
			return em.createQuery("SELECT r FROM DeviceRevision r WHERE (r.id <> (SELECT MAX(r2.id) FROM DeviceRevision r2 WHERE r2.device = r.device) OR r.deleted = true) AND r.id= (SELECT MAX(r3.id) FROM DeviceRevision r3 WHERE r3.conventionName=r.conventionName)", DeviceRevision.class).getResultList(); 
	}

	/**
	 * @param conventionName The convention name to be searched for
	 * @return The list of the latest obsolete or deleted, revisions of all device names in the database.
	 *
	 */
	public DeviceRevision latestObsoleteDeviceRevisionNamed(String conventionName) {
			
		List<DeviceRevision> deviceRevisions=	em.createQuery("SELECT r FROM DeviceRevision r WHERE (r.id <> (SELECT MAX(r2.id) FROM DeviceRevision r2 WHERE r2.device = r.device) OR r.deleted = true) AND r.id= (SELECT MAX(r3.id) FROM DeviceRevision r3 WHERE r3.conventionName= :conventionName)", DeviceRevision.class).setParameter("conventionName", conventionName).getResultList(); 
		return deviceRevisions!=null ?deviceRevisions.get(0): null;
	}
	
	/**
	 * @return The list of all revisions of the given device, starting from the oldest to the latest.
	 * @param device the device
	 */
	public List<DeviceRevision> revisions(Device device) {
		return em.createQuery("SELECT r FROM DeviceRevision r WHERE r.device = :device ORDER BY r.id", DeviceRevision.class).setParameter("device", device).getResultList();
	}

	/**
	 * @return The current, most recent revision of the given device
	 *
	 * @param device the device
	 */
	public DeviceRevision currentRevision(Device device) {
		return em.createQuery("SELECT r FROM DeviceRevision r WHERE r.device = :device ORDER BY r.id DESC", DeviceRevision.class).setParameter("device", device).getResultList().get(0);
	}

	/**
	 * @return The current, most recent revision of the device with the given UUID. Null if none found.
	 *
	 * @param deviceUuid the UUID of the device
	 */
	public @Nullable DeviceRevision currentDeviceRevision(UUID deviceUuid) {
		return JpaHelper.getSingleResultOrNull(em.createQuery("SELECT r FROM DeviceRevision r WHERE r.device.uuid = :uuid ORDER BY r.id DESC", DeviceRevision.class).setParameter("uuid", deviceUuid.toString()).setMaxResults(1));
	}

	/**
	 * 
	 * @param deviceName device name 
	 * @return the current device revision with the given device name  
	 */
	public @Nullable DeviceRevision currentDeviceRevision(String deviceName) {
		List<DeviceRevision> deviceRevisions= em.createQuery("SELECT r FROM DeviceRevision r WHERE  r.id = (SELECT MAX(r2.id) FROM DeviceRevision r2 WHERE r2.device = r.device) AND r.conventionName = :conventionName", DeviceRevision.class).setParameter("conventionName", deviceName).getResultList();
		return !deviceRevisions.isEmpty()? deviceRevisions.get(0):null;
	}

	/**
	 * 
	 * @param deviceName the name of the device
	 * @return list of device revisions previously with the given device name
	 */
	public List<DeviceRevision> devcieRevisionsPreviouslyNamed(String deviceName){
		return em.createQuery("SELECT r FROM DeviceRevision r WHERE  r.id = (SELECT MAX(r2.id) FROM DeviceRevision r2 WHERE r2.device = r.device AND r2.conventionName = :conventionName)", DeviceRevision.class).setParameter("conventionName", deviceName).getResultList();
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

	/**
	 * @return The list of current, most recent approved revisions of all name parts of a given type in the database with a null parent.
	 *
	 * @param type the type of the name parts
	 * @param includeDeleted true if the list should also include revisions for deleted name parts
	 */
	public List<NamePartRevision> currentApprovedNamePartParentRevisions(NamePartType type, boolean includeDeleted) {
		if (includeDeleted)
			return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.namePart.namePartType = :type AND r2.status = :approved) AND r.parent = NULL", NamePartRevision.class).setParameter("type", type).setParameter("approved", NamePartRevisionStatus.APPROVED).getResultList();
		else {
			return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.namePart.namePartType = :type AND r2.status = :approved) AND r.deleted = FALSE AND r.parent IS NULL", NamePartRevision.class).setParameter("type", type).setParameter("approved", NamePartRevisionStatus.APPROVED).getResultList();
		}
	}	
	
	/**
	 * @param parent Parent revision
	 * @param includeDeleted true if the list should also include revisions for deleted name parts
	 * @return The list of current, most recent approved revisions of all name parts of a given parent revision in the database. 
	 */
	public List<NamePartRevision> currentApprovedChildrenOfNamePartRevision(NamePartRevision parent, boolean includeDeleted) {
		NamePart namePartParent=parent.getNamePart();
		if (includeDeleted)
			return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.status = :approved) AND r.parent = :parent", NamePartRevision.class).setParameter("parent", namePartParent).setParameter("approved", NamePartRevisionStatus.APPROVED).getResultList();
		else {
			return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.status = :approved) AND r.deleted = FALSE AND r.parent= :parent", NamePartRevision.class).setParameter("parent", namePartParent).setParameter("approved", NamePartRevisionStatus.APPROVED).getResultList();
		}
	}

	private NamePartView view(NamePart namePart) {
		final NamePartRevisionProvider revisionProvider = new NamePartRevisionProvider() {
			@Override public @Nullable NamePartRevision approvedRevision(NamePart namePart) { return NamePartService.this.approvedRevision(namePart); }
			@Override public @Nullable NamePartRevision pendingRevision(NamePart namePart) { return NamePartService.this.pendingRevision(namePart); }
			@Override
			public List<NamePartRevision> approvedChildrenRevisions(NamePart namePart) {
				return NamePartService.this.approvedChildrenRevisions(namePart, true);
			}
		};
		return new NamePartView(revisionProvider, approvedRevision(namePart), pendingRevision(namePart), null);
	}

	private List<Device> devicesInSection(NamePart section) {
		return em.createQuery("SELECT r.device FROM DeviceRevision r WHERE r.id = (SELECT MAX(r2.id) FROM DeviceRevision r2 WHERE r2.device = r.device) AND r.section = :section AND r.deleted = false", Device.class).setParameter("section", section).getResultList();
	}

	private List<Device> devicesOfType(NamePart deviceType) {
		return em.createQuery("SELECT r.device FROM DeviceRevision r WHERE r.id = (SELECT MAX(r2.id) FROM DeviceRevision r2 WHERE r2.device = r.device) AND r.deviceType = :deviceType AND r.deleted = false", Device.class).setParameter("deviceType", deviceType).getResultList();
	}	
	private List<DeviceRevision> devicesRevisionsOf(NamePart section, NamePart deviceType) {
		return em.createQuery("SELECT r FROM DeviceRevision r WHERE r.id = (SELECT MAX(r2.id) FROM DeviceRevision r2 WHERE r2.device = r.device) AND r.section = :section AND r.deviceType = :deviceType AND r.deleted = false", DeviceRevision.class).setParameter("section", section).setParameter("deviceType", deviceType).getResultList();
	}
	/**
	 * Query the approved name part revisions with specified name part as parent 
	 * @param namePartParent parent name part
	 * @param includeDeleted boolean flag indicating whether deleted name parts shall be included or not
	 * @return ordered list of current approved name part revisions with specified name part as parent 
	 */
	public List<NamePartRevision> approvedChildrenRevisions(NamePart namePartParent, boolean includeDeleted) {
		if (includeDeleted)
				return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.status = :approved) AND r.parent = :parent ORDER BY r.name", NamePartRevision.class).setParameter("parent", namePartParent).setParameter("approved", NamePartRevisionStatus.APPROVED).getResultList();
			else {
				return em.createQuery("SELECT r FROM NamePartRevision r WHERE r.id = (SELECT MAX(r2.id) FROM NamePartRevision r2 WHERE r2.namePart = r.namePart AND r2.status = :approved) AND r.deleted = FALSE AND r.parent= :parent ORDER BY r.name", NamePartRevision.class).setParameter("parent", namePartParent).setParameter("approved", NamePartRevisionStatus.APPROVED).getResultList();
			}
		}

	public List<DeviceRevision> deviceRevisionsIn(NamePart subsection, NamePart deviceType, boolean includeDeleted){
		if(includeDeleted){
			return em.createQuery("SELECT r FROM DeviceRevision r WHERE r.id = (SELECT MAX(r2.id) FROM DeviceRevision r2 WHERE r2.device = r.device) AND r.section = :section AND r.deviceType= :deviceType", DeviceRevision.class).setParameter("section", subsection).setParameter("deviceType", deviceType).getResultList();
		} else {
			return em.createQuery("SELECT r FROM DeviceRevision r WHERE r.id = (SELECT MAX(r2.id) FROM DeviceRevision r2 WHERE r2.device = r.device) AND r.section = :section AND r.deviceType= :deviceType AND r.deleted = FALSE", DeviceRevision.class).setParameter("section", subsection).setParameter("deviceType", deviceType).getResultList();
		}
	}	
}
