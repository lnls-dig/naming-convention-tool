package org.openepics.names.services;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.enterprise.inject.Alternative;

import org.openepics.names.model.NamePartType;

import java.util.List;

/**
 * A naming convention definition used by ESS.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 * @author Karin Rathsman <karin.rathsman@esss.se>
 */
@Alternative
@Stateless
public class EssNamingConvention implements NamingConvention {

	@Override public boolean isSectionNameValid(List<String> parentPath, String name) {
		NameElement parent= new NameElement(parentPath,NamePartType.SECTION);
		if(parent.isAreaRoot()){
			return isNameValid(name,0,16);
		} else if(parent.isSuperSection()|| parent.isSection()){
			return isNameValid(name,1,6);
		} else {
		return false;
		}
	}

	@Override public boolean isDeviceTypeNameValid(List<String> parentPath, String name) {
		NameElement parent= new NameElement(parentPath,NamePartType.DEVICE_TYPE);
		if(parent.isDiscipline()){
			return isNameValid(name,0,16);
		} else if(parent.isDeviceRoot()|| parent.isDeviceGroup()){
			return isNameValid(name,1,6);
		} else {
		return false;
		}		
	}
	
	@Override public boolean isMnemonicNullable(List<String> parentPath, NamePartType mnemonicType){
		NameElement parent= new NameElement(parentPath, mnemonicType);
		if(parent.isAreaRoot()||parent.isDiscipline()){
			return true;
		} else {
			return false;
		}
	}

	@Override public boolean isInstanceIndexValid(List<String> sectionPath, List<String> deviceTypePath, @Nullable String instanceIndex) {
		return isNameValid(instanceIndex, 0,6);
	}

	@Override public String equivalenceClassRepresentative(@Nullable String name) {
		return name!=null ? name.toUpperCase().replaceAll("(?<=[A-Za-z])0+", "").replace('I', '1').replace('L', '1').replace('O', '0').replace('W', 'V').replaceAll("(?<!\\d)0+(?=\\d)", ""):null;
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

		String getDefinition() {
			if(isDeviceType()){
				return getDiscipline()+"-"+getDeviceType();
			} else if (isSubsection()){
				return getSection()+"-"+getSubsection();
			} else { 
				return null;
			}			
		}

		boolean isDeviceRoot() {
			return deviceStructure && level==0;
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
		boolean isAreaRoot() {
			return areaStructure&& level==0;
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

		boolean isNullable(){
			return isSuperSection()|| isDeviceGroup();
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

		boolean canCoexistWith(NameElement other) {
			if((isDiscipline() || isSection())&& !other.isNullable() || (other.isDiscipline()||other.isSection())&&!isNullable()){
				return false;
			} else if ((isDeviceType() && other.isDeviceType()) && getDiscipline().equals(other.getDiscipline())){
				return false;
			} else if ((isSubsection() && other.isSubsection()) && getSection().equals(other.getSection()) ){
				return false;
			} else {
				return true;
			}
		}	
	}
}
