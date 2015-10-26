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

package org.openepics.names.services.restricted;

import com.google.common.base.Preconditions;

import org.openepics.names.model.*;
import org.openepics.names.services.DeviceDefinition;
import org.openepics.names.services.NamePartService;
import org.openepics.names.services.SessionService;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;

import java.util.List;
import java.util.UUID;

/**
 * A gateway to a NamePartService bean that enforces user access control rules on each call. All calls from UI code should
 * go through this.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 * @author Karin Rathsman <karin.rathsman@esss.se>
 */
@Stateless
public class RestrictedNamePartService {

    @Inject private SessionService sessionService;
    @Inject private NamePartService namePartService;

    /**
     * True if the mnemonic of a name part is valid in the context of the parent.
     *
     * @param namePartType the type of the name part
     * @param parent the parent of the name part, null if the name part is at the root of the hierarchy
     * @param mnemonic the mnemonic name of the name part to test for validity
     */
    public boolean isMnemonicValid(NamePartType namePartType, @Nullable NamePart parent, String mnemonic) {
        return namePartService.isMnemonicValid(namePartType, parent, mnemonic);
    }

    /**
     * True if the mnemonic of a name part is unique when placed under the parent.
     *
     * @param namePartType the type of the name part
     * @param parent the parent of the name part, null if the name part is at the root of the hierarchy
     * @param mnemonic the mnemonic name of the name part to test for uniqueness
     */
    public boolean isMnemonicUniqueOnAdd(NamePartType namePartType, @Nullable NamePart parent, String mnemonic) {
        return namePartService.isMnemonicUniqueOnAdd(namePartType, parent, mnemonic);
    }

    /**
     * True if the mnemonic of a name part is unique when modified.
     * @param namePart the current name part
     * @param parent the parent of the name part, null if the name part is at the root of the hierarchy
     * @param mnemonic the mnemonic name of the name part to test for uniqueness
     */
    public boolean isMnemonicUniqueOnModify(NamePart namePart, @Nullable NamePart parent, String mnemonic) {
        return namePartService.isMnemonicUniqueOnModify(namePart, parent, mnemonic);
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
        return namePartService.isInstanceIndexValid(section, deviceType, instanceIndex);
    }

    /**
     * True if the device defined by the given section, device type and instance index would have a unique convention name.
     *
     * @param section the section containing the device
     * @param deviceType the device type of the device
     * @param instanceIndex the instance index of the device, null if no instance index is assigned to the device
     */
    public boolean isDeviceConventionNameUnique(NamePart section, NamePart deviceType, @Nullable String instanceIndex) {
        return namePartService.isDeviceConventionNameUnique(section, deviceType, instanceIndex);
    }

    /**
     * True if the device defined by the given section, devicetype and instance index would have a unique convention name, not taking into account itself.  
     * @param device
     * @param section
     * @param deviceType
     * @param instanceIndex
     * @return
     */
   
    public boolean isDeviceConventionNameUniqueExceptForItself( Device device,NamePart section, NamePart deviceType, @Nullable String instanceIndex){
    	return  namePartService.isDeviceConventionNameUniqueExceptForItself(device,section, deviceType, instanceIndex);
    }
    
    /**
     * Submits a proposal for addition of a new name part.
     *
     * @param name the long, descriptive name of the part. Does not need to follow a convention.
     * @param mnemonic the short, mnemonic name of the part in accordance with the naming convention
     * @param namePartType the type of the proposed name part
     * @param parent the parent of the proposed name part in the hierarchy. Null if at the top of the hierarchy.
     * @param comment the comment the user gave when submitting the proposal. Null if no comment was given.
     * @return the resulting proposed NamePart revision
     */
    public NamePartRevision addNamePart(String name, String mnemonic,@Nullable String description, NamePartType namePartType, @Nullable NamePart parent, @Nullable String comment) {
        Preconditions.checkState(sessionService.isEditor());
        return namePartService.addNamePart(name, mnemonic, description, namePartType, parent, sessionService.user(), comment);
    }

    /**
     * Submits a proposal for modification of an existing name part.
     *
     * @param namePart the name part proposed for modification
     * @param parent the new proposed parent. 
     * @param name the new long, descriptive name of the part. Does not need to follow a convention.
     * @param mnemonic the new short, mnemonic name of the part in accordance with the naming convention
     * @param comment the comment the user gave when submitting the proposal. Null if no comment was given.
     * @return the resulting proposed NamePart revision
     */
    public NamePartRevision modifyNamePart(NamePart namePart, String name, String mnemonic, @Nullable String description, @Nullable String comment) {
        Preconditions.checkState(sessionService.isEditor());
        return namePartService.modifyNamePart(namePart, name, mnemonic, description, sessionService.user(), comment);
    }
    
    public NamePartRevision moveNamePart(NamePart namePart, NamePart parent, @Nullable String comment) {
        Preconditions.checkState(sessionService.isSuperUser());
        return namePartService.moveNamePart(namePart, parent, sessionService.user(), comment);
    }


    /**
     * Submits a proposal for deletion of a name part, its children, and all associated devices.
     *
     * @param namePart the name part proposed for deletion
     * @param comment the comment the user gave when submitting the proposal. Null if no comment was given.
     * @return the resulting proposed NamePart revision
     */
    public NamePartRevision deleteNamePart(NamePart namePart, @Nullable String comment) {
        Preconditions.checkState(sessionService.isEditor());
        return namePartService.deleteNamePart(namePart, sessionService.user(), comment);
    }

    /**
     * Cancels the currently proposed modification, deletion or addition of the given name part.
     *
     * @param namePart the affected name part
     * @param comment the comment the user gave when cancelling the proposal. Null if no comment was given.
     * @return the affected NamePart revision
     */
    public NamePartRevision cancelChangesForNamePart(NamePart namePart, @Nullable String comment) {
        Preconditions.checkState(sessionService.isEditor());
        return namePartService.cancelChangesForNamePart(namePart, sessionService.user(), comment, false);
    }

    /**
     * Rejects the currently proposed modification, deletion or addition of the given name part.
     *
     * @param namePart the affected name part
     * @param comment the comment the user gave when rejecting the proposal.
     * @return the affected NamePart revision
     */
    public NamePartRevision rejectChangesForNamePart(NamePart namePart, String comment) {
        Preconditions.checkState(sessionService.isSuperUser());
        return namePartService.cancelChangesForNamePart(namePart, sessionService.user(), comment, true);
    }

    /**
     * Approves the modification, deletion or addition proposed in the given name part revision.
     *
     * @param namePartRevision the name part revision to approve
     * @param comment the comment the user gave when approving the revision. Null if no comment was given.
     * @return the affected NamePart revision
     */
    public NamePartRevision approveNamePartRevision(NamePartRevision namePartRevision, @Nullable String comment) {
        Preconditions.checkState(sessionService.isSuperUser());
        return namePartService.approveNamePartRevision(namePartRevision, sessionService.user(), comment);
    }

    /**
     * The list of devices associated by the given name part (contained under a section or of a given device type)
     *
     * @param namePart the name part
     * @param recursive true if the list should also contain devices associated by the name part's children
     * deeper down in the hierarchy
     */
    public List<Device> associatedDevices(NamePart namePart, boolean recursive) {
        return namePartService.associatedDevices(namePart, recursive);
    }

    /**
     * The list of current, most recent approved revisions of all name parts of a given type in the database.
     *
     * @param type the type of the name parts
     * @param includeDeleted true if the list should also include revisions for deleted name parts
     */
    public List<NamePartRevision> currentApprovedNamePartRevisions(NamePartType type, boolean includeDeleted) {
        return namePartService.currentApprovedNamePartRevisions(type, includeDeleted);
    }

    /**
     * The list of revisions pending approval of all name parts of the given type in the database.
     *
     * @param type the type of the name parts
     * @param includeDeleted true if the list should also include revisions for deleted name parts
     */
    public List<NamePartRevision> currentPendingNamePartRevisions(NamePartType type, boolean includeDeleted) {
        return namePartService.currentPendingNamePartRevisions(type, includeDeleted);
    }

    /**
     * The list of all revisions of the given name part, including approved, pending, canceled or rejected, starting
     * from the oldest to the latest.
     * @param namePart the name part
     */
    public List<NamePartRevision> revisions(NamePart namePart) {
        return namePartService.revisions(namePart);
    }

    /**
     * The current, most recent approved revision of the given name part. Null if no approved revision exists for this name part.
     *
     * @param namePart the name part
     */
    public @Nullable NamePartRevision approvedRevision(NamePart namePart) {
        return namePartService.approvedRevision(namePart);
    }

    /**
     * The revision of the given name part currently pending approval. Null if no revision is pending approval.
     *
     * @param namePart the name part
     */
    public @Nullable NamePartRevision pendingRevision(NamePart namePart) {
        return namePartService.pendingRevision(namePart);
    }

    public void batchAddDevices(Iterable<DeviceDefinition> devices) {
        Preconditions.checkState(sessionService.isEditor());
        namePartService.batchAddDevices(devices, sessionService.user());
    }

    /**
     * Adds a new device.
     *
     * @param section the section containing the device
     * @param deviceType the device type of the device
     * @param instanceIndex the instance index of the device, null if omitted
     * @param additionalInfo Additional information (description, comment etc) of the device.
     * @return current revision of the added device
     */
    public DeviceRevision addDevice(NamePart section, NamePart deviceType, @Nullable String instanceIndex, @Nullable String additionalInfo) {
        Preconditions.checkState(sessionService.isEditor());
        return namePartService.addDevice(section, deviceType, instanceIndex, additionalInfo, sessionService.user());
    }

    /**
     * Modifies the given device.
     *
     * @param device the device to modify
     * @param section the new section containing the device
     * @param deviceType the new device type of the device
     * @param instanceIndex the new instance index of the device, null no instance index is to be assigned to the device
     * @param additionalInfo Additional information (description, comment etc) of the device.
     * @return the revision of the device resulting from the modification
     */
    public DeviceRevision modifyDevice(Device device, NamePart section, NamePart deviceType, @Nullable String instanceIndex, @Nullable String additionalInfo) {
        Preconditions.checkState(sessionService.isEditor());
        return namePartService.modifyDevice(device, section, deviceType, instanceIndex, additionalInfo, sessionService.user());
    }
    

    /**
     * Deletes the given device
     *
     * @param device the device to delete
     * @return the revision of the device resulting from the deletion
     */
    public DeviceRevision deleteDevice(Device device) {
        Preconditions.checkState(sessionService.isEditor());
        return namePartService.deleteDevice(device, sessionService.user());
    }

    /**
     * The list of all devices in the database.
     *
     * @param includeDeleted true if the list should also include revisions for deleted devices
     */
    public List<DeviceRevision> obsoleteDeviceRevisions() {
        return namePartService.obsoleteDeviceRevisions();
    }
    
    /**
     * The list of current, most recent revisions of all devices in the database.
     *
     * @param includeDeleted true if the list should also include revisions for deleted devices
     */
    public List<DeviceRevision> currentDeviceRevisions(boolean includeDeleted) {
        return namePartService.currentDeviceRevisions(includeDeleted);
    }

    /**
     * The list of all revisions of the given device, starting from the oldest to the latest.
     * @param device the device
     */
    public List<DeviceRevision> revisions(Device device) {
        return namePartService.revisions(device);
    }

    /**
     * The current, most recent revision of the given device
     *
     * @param device the device
     */
    public DeviceRevision currentRevision(Device device) {
        return namePartService.currentRevision(device);
    }

    /**
     * The current, most recent revision of the device with the given UUID. Null if none found.
     *
     * @param deviceUuid the UUID of the device
     */
    public @Nullable DeviceRevision currentDeviceRevision(UUID deviceUuid) {
        return namePartService.currentDeviceRevision(deviceUuid);
    }

    public @Nullable DeviceRevision currentDeviceRevision(String deviceName){
    	return namePartService.currentDeviceRevision(deviceName);
    }
    public List<DeviceRevision> devcieRevisionsPreviouslyNamed(String deviceName){
    	return namePartService.devcieRevisionsPreviouslyNamed(deviceName);
    }
    
	public boolean isMnemonicRequiredForChild(NamePartType namePartType, NamePart namePart) {
		return namePartService.isMnemonicRequiredForChild(namePartType, namePart);
	}

	public boolean isMnemonicRequired(NamePartType namePartType, NamePart namePart) {
		return namePartService.isMnemonicRequired(namePartType, namePart);
	}

	public String getNamePartTypeName(NamePartType namePartType, NamePart namePart) {
		return namePartService.getNamePartTypeName(namePartType,namePart);
	}

	public String getNamePartTypeNameForChild(NamePartType namePartType, NamePart namePart) {
		return namePartService.getNamePartTypeNameForChild(namePartType,namePart);
	}

	public String getNamePartTypeMnemonic(NamePartType namePartType, NamePart namePart) {
		return namePartService.getNamePartTypeMnemonic(namePartType, namePart);
	}
	public String getNamePartTypeMnemonicForChild(NamePartType namePartType, NamePart namePart) {
		return namePartService.getNamePartTypeMnemonicForChild(namePartType, namePart);
	}

}
