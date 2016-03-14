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
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import org.openepics.names.model.NamePartType;
import org.openepics.names.services.NamingConventionDefinition.NameDefinition;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import java.util.List;

/**
 * A naming convention definition used by ESS.
 *
 * @author Marko Kolar 
 * @author Karin Rathsman 
 */
@Alternative
@Stateless
public class EssNamingConvention implements NamingConvention {
	@Inject NamingConventionDefinition aliasManager;
	@Override public boolean isMnemonicValid(List<String> mnemonicPath, NamePartType mnemonicType){
		NameElement nameElement =new NameElement(mnemonicPath,mnemonicType);
		String mnemonic = nameElement.getMnemonic();
		if(nameElement.isSuperSection()){
			return mnemonic.length() == 0 || ( mnemonic.length() <=3 && mnemonic.matches("^[a-zA-Z0-9]+$") );
		} else if(nameElement.isRequired()){
			return (mnemonic.length() >= 1 && mnemonic.length() <= 6) && mnemonic.matches("^[a-zA-Z0-9]+$");
		} else { 			
			return mnemonic.length() == 0 || ( mnemonic.length() <=16 && mnemonic.matches("^[a-zA-Z0-9]+$") );
		}
	}

	@Override public boolean isMnemonicRequired(List<String> mnemonicPath, NamePartType mnemonicType){
		return (new NameElement(mnemonicPath, mnemonicType)).isRequired();
	}
	
	@Override public boolean isInstanceIndexValid(List<String> sectionPath, List<String> deviceTypePath, @Nullable String instanceIndex) {
		return isNameValid(instanceIndex, 0,6);
	}

	@Override public String equivalenceClassRepresentative(@Nullable String name) {
		if(name!=null && name.equalsIgnoreCase("W")){
			return "W";
		}else {
		return name!=null ? name.toUpperCase().replaceAll("(?<=[A-Za-z])0+", "").replace('I', '1').replace('L', '1').replace('O', '0').replace('W', 'V').replaceAll("(?<!\\d)0+(?=\\d)", ""):null;
		}
	}

	@Override public boolean canMnemonicsCoexist(List<String> mnemonicPath1, NamePartType mnemonicType1, List<String> mnemonicPath2, NamePartType mnemonicType2) {
		NameElement mnemonic1=new NameElement(mnemonicPath1,mnemonicType1);
		NameElement mnemonic2=new NameElement(mnemonicPath2,mnemonicType2);
		return mnemonic1.canCoexistWith(mnemonic2);
	}

	@Override public String conventionName(List<String> sectionPath, List<String> deviceTypePath, @Nullable String instanceIndex) {
		String areaDefinition = (new NameElement(sectionPath, NamePartType.SECTION)).getDefinition();
		String deviceDefinition= (new NameElement(deviceTypePath, NamePartType.DEVICE_TYPE)).getDefinition();
		if(areaDefinition!=null && deviceDefinition!=null){
			return areaDefinition + ":" + deviceDefinition + (instanceIndex != null && !instanceIndex.isEmpty() ? "-" + instanceIndex : "");
		}
		else {
			return null;
		}
	}

	private static String removeLeadingZeros(String string){
		return string!=null? string.replaceAll("(?<=[A-Za-z])0+", "") : null;
	}

	private boolean isNameValid(String name,int nMin, int nMax) { 
		if((name==null || name.length()==0) ){
			return nMin<=0;
		} else {
			return (name.length() >= nMin && name.length() <= nMax) && name.matches("^[a-zA-Z0-9]+$");
		}
	}
	@Override
	public String deviceDefinition(List<String> deviceTypePath) {
		return(new NameElement(deviceTypePath, NamePartType.DEVICE_TYPE)).getDefinition();
	}

	@Override
	public String areaName(List<String> sectionPath) {
		return(new NameElement(sectionPath, NamePartType.SECTION)).getDefinition();
	}

	@Override
	public String getNamePartTypeName(List<String> sectionPath, @Nullable NamePartType namePartType){
		return namePartType!=null?(new NameElement(sectionPath,namePartType)).getTypeName():null;
	}

	@Override
	public String getNamePartTypeMnemonic(List<String> sectionPath, @Nullable NamePartType namePartType){
		return namePartType!=null? (new NameElement(sectionPath,namePartType)).getTypeMnemonic():null;
	}

	@Override
	public boolean canNamePartMove(List<String> sourcePath, NamePartType sourceNamePartType, List<String> destinationPath, NamePartType destinationNamePartType) {
		
		return (new NameElement(sourcePath,sourceNamePartType).canMoveTo(new NameElement(destinationPath, destinationNamePartType)));
	}

	
	private class NameElement {
		List<String> path;
		boolean areaStructure;
		boolean deviceStructure;
		Integer level;				

		NameElement(List<String> path, NamePartType type){
			this.path=path;
			this.areaStructure=type.equals(NamePartType.SECTION);
			this.deviceStructure=type.equals(NamePartType.DEVICE_TYPE);
			this.level= !( path==null || path.isEmpty())  ? path.size(): 0;
		}

		public boolean canMoveTo(NameElement nameElement) {
			return(this.getTypeMnemonic().equals(nameElement.getTypeMnemonic()));
		}

		String getDefinition() {
			if(isDeviceType()){
				return getDiscipline()+"-"+getDeviceType();
			} else if (isSubsection()){
	
				if(!isOffsite()){
				return getSection()+"-"+getSubsection();
				}else{
				return getSuperSection()+"-"+getSection()+"-"+getSubsection();
				}
			} else { 
				return null;
			}			
		}

		NameDefinition getNameDefinition(){
			if(deviceStructure){
				return aliasManager.deviceStructureLevel(level);
			} else if(areaStructure){
				return aliasManager.areaStructureLevel(level);
			}else{
				return null;
			}
		}
		String getTypeName(){			
			return getNameDefinition()!=null? getNameDefinition().getFullName(): "";
		}

		String getTypeMnemonic(){
			return getNameDefinition()!=null? getNameDefinition().getMnemonic(): "";
		}

		boolean isDiscipline() {
			return deviceStructure&& level==1;
		}
		
		boolean isDeviceGroup() {
			return deviceStructure && level==2;
		}
		
		boolean isDeviceType() {
			return deviceStructure && level==3;
		}
		
		boolean isSuperSection() {
			return areaStructure&& level==1;
		}
		
		boolean isSection() {
			return areaStructure && level==2;
		}
		
		boolean isSubsection() {
			return areaStructure && level==3;
		}
		
		boolean isRequired(){
			return !(isSuperSection()|| isDeviceGroup());
		}
		
		boolean isReserved(){
			return isDiscipline() || isSection();
		}

		boolean isOffsite(){
			return areaStructure && !getSuperSection().isEmpty();
		}
		
		String getSuperSection(){
			return isSuperSection()|| isSection() || isSubsection() ? path.get(0): null;
		}
		
		String getSection(){
			return isSection() || isSubsection() ? path.get(1): null;
		}

		String getDiscipline(){
			return isDiscipline() || isDeviceGroup() || isDeviceType() ? path.get(0): null;
		}

		String getSubsection() {
			return isSubsection() ? path.get(2): null;
		}

		String getDeviceType() {
			return isDeviceType() ? path.get(2): null;
		}

		String getMnemonic() {
			return level>0 ? path.get(level-1): null;
		}
		String getMnemonicCompare(){
			return getMnemonic()!=null || getMnemonic().isEmpty() ? getMnemonic().toUpperCase():null;
		}
		boolean mnemonicEquals(NameElement other){
			return getMnemonicCompare()!=null && other.getMnemonicCompare()!=null ? getMnemonicCompare().equals(other.getMnemonicCompare()):false;
		}

		boolean canCoexistWith(NameElement other) {
			boolean sameSectionDifferentSuperSection= isSection() && other.isSection() && ! getSuperSection().equals(other.getSuperSection());
			boolean sameOffsiteSuperSection=isSuperSection() && other.isSuperSection() && isOffsite();
			if (sameOffsiteSuperSection){
				return false;
			} else if (isReserved() && other.isReserved()&& !sameSectionDifferentSuperSection){ 
				return false;
			} else if ((isDeviceType()||isDiscipline()) && (other.isDeviceType()||other.isDiscipline()) && getDiscipline().equals(other.getDiscipline())){
				return false;
			} else if ((isSubsection()||isSection()) && (other.isSubsection()||other.isSection()) && getSection().equals(other.getSection()) && !sameSectionDifferentSuperSection){
				return false;
			} else if ((isReserved() && other.isRequired() || other.isReserved() && isRequired() ) && mnemonicEquals(other) &&!sameSectionDifferentSuperSection){
				return false;
			} else {
				return true;
			}
		}	
	}

}
