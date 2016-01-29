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

import javax.annotation.Nullable;
import org.openepics.names.model.NamePartType;
import java.util.List;

/**
 * An interface defining the naming convention to be used by the application that includes:
 * - name validation rules
 * - name uniqueness rules
 * - form of composite names
 *
 * The used naming convention is configured through beans.xml using the CDI alternatives mechanism.
 *
 * @author Marko Kolar  
 * @author Karin Rathsman  
 */
public interface NamingConvention {

    /**
     * @param mnemonicPath the list of mnemonics starting from the root of the hierarchy to the mnemonic for which we are testing the name
     * @param mnemonicType type of a name part specifying whether it belongs to the Logical Area Structure or the Device Structure
     * @return True if the mnemonic is valid according to convention rules.
     */
    boolean isMnemonicValid(List<String> mnemonicPath, NamePartType mnemonicType);
    /**
     * @param sectionPath the list of section names starting from the root of the hierarchy to the specific section containing the device
     * @param deviceTypePath the list of device type names starting from the root of the hierarchy to the specific subtype of the device
     * @param instanceIndex the device instance index to test for validity, or null if no instance index is assigned to the device, in which case this is also checked for validity
     * @return True if the device's instance index is valid according to convention rules, in the context of device's section and device type.
     */
    boolean isInstanceIndexValid(List<String> sectionPath, List<String> deviceTypePath, @Nullable String instanceIndex);

    /**
     * @param name the name of which to determine the equivalence class
     * @return The representative of the equivalence class the name belongs to. This is used to ensure uniqueness of names when treating similar looking names (for example, containing 0 vs. O, 1 vs. l) as equal.
     */
    String equivalenceClassRepresentative(String name);
    
    /** 
     * @param mnemonicPath1 the list of name part mnemonics starting from the root of the hierarchy to the name part for which we are testing the mnemonic
     * @param mnemonicType1 type of a name part specifying whether it belongs to the Logical Area Structure or the Device Structure.
     * @param mnemonicPath2 the list of name part mnemonics starting from the root of the hierarchy to the name part for which we are testing the mnemonic
     * @param mnemonicType2 type of a name part specifying whether it belongs to the Logical Area Structure or the Device Structure.
     * @return True if two mnemonics with given mnemonic paths and name part types can coexist within the application at the same time according to the convention rules.
     */
    boolean canMnemonicsCoexist(List<String> mnemonicPath1, NamePartType mnemonicType1, List<String> mnemonicPath2, NamePartType mnemonicType2);
    
    /**
     * @param sectionPath the list of section names starting from the root of the hierarchy to the specific section containing the device
     * @param deviceTypePath the list of device type names starting from the root of the hierarchy to the specific subtype of the device
     * @param instanceIndex the device instance index. Null if omitted.
     * @return The convention name of the device defined by it's section, device type and instance index
     */
    String conventionName(List<String> sectionPath, List<String> deviceTypePath, @Nullable String instanceIndex);

    /**
     * @param deviceTypePath the list of device type names starting from the root of the hierarchy to the specific subtype of the device
     * @return The device definition of the device defined by it device type. 
     */
	String deviceDefinition(List<String> deviceTypePath);

    /**
     * @param sectionPath the list of section names starting from the root of the hierarchy to the specific subsection containing the device
     * @return The area name of the device. 
     */
	String areaName(List<String> sectionPath);

	
	/**
	 * @param mnemonicPath the list of mnemonics starting from the root of the hierarchy to the mnemonic
	 * @param mnemonicType Specifying whether the name part belongs to the Logical Area Structure or the Device Structure
	 * @return True if the mnemonic can be null, i.e, the mnemonic is not part of the name. 
	 */
	boolean isMnemonicRequired(List<String> mnemonicPath, NamePartType mnemonicType);

	/**
	 * @param menmonicPath The list of name part mnemonics starting from the root of the hierarchy
	 * @param namePartType Type of the name part specifying whether it belongs to the Logical Area Structure or the Device Structure
	 * @return The name element type name  used in e.g. dialog headers and menus. Example: 'Add new namePartTypeName' where namePartTypeName can be subsection, deviceType etc.
	 */
	String getNamePartTypeName(List<String> menmonicPath, NamePartType namePartType);
	
	/**
	 * @param sectionPath the list of name part mnemonics starting from the root of the hierarchy 
	 * @param namePartType Type of the name part specifying whether it belongs to the Logical Area Structure or the Device Structure
	 * @return The name element type mnemonic  used in e.g. as watermarks in dialogs. Example: 'Add Mnemonic: namePartTypeMnemonic' where namePartTypeMnemonic can be sub, dev, etc.
	 */
	String getNamePartTypeMnemonic(List<String> sectionPath, NamePartType namePartType);
	
	/**
	 * @param sourcePath The list of mnemonics starting from the root of the hierarchy to the parent of the name part which is to be moved. Null if the parent is root. 
	 * @param sourceNamePartType Type of the source name part specifying whether it belongs to the Logical Area Structure or the Device Structure
	 * @param destinationPath The list of mnemonics starting from the root of the hierarchy to the parent into which the name part is to be moved. Null if the parent is root.
	 * @param destinationNamePartType Type of the destination name part specifying whether it belongs to the Logical Area Structure or the Device Structure
	 * @return True if the NamePart can be moved from sourcePath to destinationPath.
	 */
	boolean canNamePartMove(List<String> sourcePath, NamePartType sourceNamePartType, List<String>destinationPath, NamePartType destinationNamePartType );

}
