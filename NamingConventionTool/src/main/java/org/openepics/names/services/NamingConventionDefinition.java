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

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

@ManagedBean
@ApplicationScoped
public class NamingConventionDefinition  {
	private final NameDefinition superSection=new NameDefinition("Super Section","","High level area of the facility restricted to a particular use. Used for filtering and sorting purposes only. Not part of the convention names");
	private final NameDefinition section=new NameDefinition("Section","Sec", "Level 2 of Area Structure");
	private final NameDefinition subsection=new NameDefinition("Subsection","Sub","Level 3 of Area Structure"); 
	private final NameDefinition discipline=new NameDefinition("Discipline","Dis","Branch of knowledge indicating the context in which a device is used");
	private final NameDefinition deviceGroup=new NameDefinition("Device Group", "", "Introdued to allow certain device types to be grouped together in lists. Not part of the convention names. Default is Miscellaneous");
	private final NameDefinition deviceType=new NameDefinition("Device Type", "Dev", "Two devices of the same (generic) type provide the same function");
	private final NameDefinition instanceIndex=new NameDefinition("Instance Index", "Idx", "Two devices of the same generic device type provide the same function");
	private final NameDefinition deviceName=new NameDefinition("Device Name", "Sec-Sub:Dis-Dev-Idx", "Name of a single instance of a device");
	private final NameDefinition areaName=new NameDefinition("Area Name", "Sec-Sub", "Unique name of a subarea of the facility");
	private final NameDefinition deviceDefinition=new NameDefinition("Device Definition", "Dis-Dev", "Unique name of a device type at the facility");

	public class NameDefinition{
		String fullName;
		String mnemonic;
		String description;
		public NameDefinition(String fullName, String mnemonic, String description) {
			this.fullName=fullName;
			this.mnemonic=mnemonic;
			this.description=description;
		}
		public final String getFullName() {
			return fullName;
		}
		public final String getMnemonic() {
			return mnemonic;
		}
		public final String getDescription() {
			return description;
		}
	}

	/* (non-Javadoc)
	 * @see org.openepics.names.services.NamingConventionDefinition#areaStructureLevel(int)
	 */
	public NameDefinition areaStructureLevel(int level) {
		switch (level) {
		case 1: return getSuperSection();
		case 2: return getSection();
		case 3: return getSubsection();
		default:
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.openepics.names.services.NamingConventionDefinition#deviceStructureLevel(int)
	 */
	public NameDefinition deviceStructureLevel(int level) {
		switch (level) {
		case 1: return getDiscipline();
		case 2: return getDeviceGroup();
		case 3: return getDeviceType();
		default:
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.openepics.names.services.NamingConventionDefinition#getSuperSection()
	 */
	public NameDefinition getSuperSection(){
		return superSection;
	}
	
	/* (non-Javadoc)
	 * @see org.openepics.names.services.NamingConventionDefinition#getSection()
	 */
	public NameDefinition getSection(){
		return section;
	}


	/* (non-Javadoc)
	 * @see org.openepics.names.services.NamingConventionDefinition#getSubsection()
	 */
	public NameDefinition getSubsection(){
		return subsection;
	}


	/* (non-Javadoc)
	 * @see org.openepics.names.services.NamingConventionDefinition#getDiscipline()
	 */
	public NameDefinition getDiscipline(){
		return discipline;
	}

	/* (non-Javadoc)
	 * @see org.openepics.names.services.NamingConventionDefinition#getDeviceGroup()
	 */
	public NameDefinition getDeviceGroup(){
		return deviceGroup;
	}

	/* (non-Javadoc)
	 * @see org.openepics.names.services.NamingConventionDefinition#getDeviceType()
	 */
	public NameDefinition getDeviceType(){
		return deviceType;
	}

	/* (non-Javadoc)
	 * @see org.openepics.names.services.NamingConventionDefinition#getConventionName()
	 */
	public String getConventionName(){
		return deviceName.fullName;
	}	
}
