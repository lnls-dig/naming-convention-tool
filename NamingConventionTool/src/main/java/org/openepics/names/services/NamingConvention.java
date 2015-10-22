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
import javax.ejb.Stateless;

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
 * @author Marko Kolar <marko.kolar@cosylab.com>
 * @author Karin Rathsman <karin.rathsman@esss.se>
 */

public interface NamingConvention {

    /**
     * True if the mnemonic is valid according to convention rules.
     *
     * @param mnemonicPath the list of mnemonics starting from the root of the hierarchy to the mnemonic for which we are testing the name
     * @param mnemonicType type of a name part specifying whether it belongs to the Logical Area Structure or the Device
     */
    boolean isMnemonicValid(List<String> mnemonicPath, NamePartType mnemonicType);
    /**
     * True if the device's instance index is valid according to convention rules, in the context of device's section
     * and device type.
     *
     * @param sectionPath the list of section names starting from the root of the hierarchy to the specific section
     * containing the device
     * @param deviceTypePath the list of device type names starting from the root of the hierarchy to the specific
     * subtype of the device
     * @param instanceIndex the device instance index to test for validity, or null if no instance index is assigned to
     * the device, in which case this is also checked for validity
     */
    boolean isInstanceIndexValid(List<String> sectionPath, List<String> deviceTypePath, @Nullable String instanceIndex);

    /**
     * The representative of the equivalence class the name belongs to. This is used to ensure uniqueness of names when
     * treating similar looking names (for example, containing 0 vs. O, 1 vs. l) as equal.
     *
     * @param name the name of which to determine the equivalence class
     */
    String equivalenceClassRepresentative(String name);
    
    /** 
     * True if two mnemonics with given mnemonic paths and name part types can coexist within the application at the same time
     * according to the convention rules.
     * 
     * @param mnemonicPath1 the list of name part mnemonics starting from the root of the hierarchy to the name part for which we are testing the mnemonic
     * @param mnemonicType1 type of a name part specifying whether it belongs to the Logical Area Structure or the Device
     * Category Structure.
     * @param mnemonicPath2 the list of name part mnemonics starting from the root of the hierarchy to the name part for which we are testing the mnemonic
     * @param mnemonicType2 type of a name part specifying whether it belongs to the Logical Area Structure or the Device
     * Category Structure.
     */
    boolean canMnemonicsCoexist(List<String> mnemonicPath1, NamePartType mnemonicType1, List<String> mnemonicPath2, NamePartType mnemonicType2);
    
    /**
     * The convention name of the device defined by it's section, device type and instance index
     *
     * @param sectionPath the list of section names starting from the root of the hierarchy to the specific section
     * containing the device
     * @param deviceTypePath the list of device type names starting from the root of the hierarchy to the specific
     * subtype of the device
     * @param instanceIndex the device instance index. Null if omitted.
     */
    String conventionName(List<String> sectionPath, List<String> deviceTypePath, @Nullable String instanceIndex);

    /**
     * The device definition of the device defined by it device type. 
     * @param deviceTypePath the list of device type names starting from the root of the hierarchy to the specific
     * subtype of the device
     * @return
     */
	String deviceDefinition(List<String> deviceTypePath);

    /**
     * The area name of the device defined by it area. 
     * @param sectionPath the list of section names starting from the root of the hierarchy to the specific subsection
     * containing the device
     * @return
     */
	String areaName(List<String> sectionPath);

	
	/**
	 * True if the mnemonic can be null, i.e, the mnemonic is not part of the name. 
	 * @param mnemonicPath
	 * @param mnemonicType
	 * @return
	 */
	boolean isMnemonicRequired(List<String> mnemonicPath, NamePartType mnemonicType);

	/**
	 * Returns the name element type name  used in e.g. dialog headers and menus. Example: 'Add new namePartTypeName' where namePartTypeName can be subsection, deviceType etc.
	 * @param sectionPath
	 * @param namePartType
	 * @return
	 */
	String getNamePartTypeName(List<String> sectionPath, NamePartType namePartType);
	
	/**
	 * Returns the name element type mnemonic  used in e.g. as watermarks in dialogs. Example: 'Add Mnemonic: namePartTypeMnemonic' where namePartTypeMnemonic can be sub, dev, etc.
	 * @param sectionPath
	 * @param namePartType
	 * @return
	 */
	String getNamePartTypeMnemonic(List<String> sectionPath, NamePartType namePartType);
	
	boolean canNamePartMove(List<String> sourcePath, NamePartType sourceNamePartType, List<String>destinationPath, NamePartType destinationNamePartType );

}
