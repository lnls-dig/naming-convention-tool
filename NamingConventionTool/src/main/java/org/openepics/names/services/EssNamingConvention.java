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
		if (parentPath.isEmpty() || parentPath.size() == 0 ){
			return isNameValid(name,0,16);
		} else {        	
			return isNameValid(name,1,6);
		}
	}

	@Override public boolean isDeviceTypeNameValid(List<String> parentPath, String name) {
		if(parentPath.size()==1){
			return isNameValid(name,0,16);			
		} else {
			return isNameValid(name,1,6);
		}
//			if (parentPath.size() == 2 ) {
//				return name.matches("^[a-zA-Z][a-zA-Z0-9]+$");
//			} else {
//			}
	}

	@Override public boolean isInstanceIndexValid(List<String> sectionPath, List<String> deviceTypePath, @Nullable String instanceIndex) {

		return isNameValid(instanceIndex, 0,6);
//		if(instanceIndex == null) {
//			return true;
//		} else {
//			if (!isNameLengthValid(instanceIndex,1,6)) {
//				return false;
//			} else {
//				return instanceIndex.matches("^[a-zA-Z0-9]+$");
//			}
//     }

	}

	@Override public String equivalenceClassRepresentative(String name) {
		return name.toUpperCase().replaceAll("(?<=[A-Za-z])0+", "").replace('I', '1').replace('L', '1').replace('O', '0').replace('W', 'V').replaceAll("(?<!\\d)0+(?=\\d)", "");
	}

	@Override public boolean canMnemonicsCoexist(List<String> mnemonicPath1, NamePartType mnemonicType1, List<String> mnemonicPath2, NamePartType mnemonicType2) {
//		if (mnemonicPath2.size() == 1 || mnemonicType2.equals(NamePartType.SECTION) && mnemonicPath2.size() == 2) {
			if(isDiscipline(mnemonicPath2,mnemonicType2) ||  isSection(mnemonicPath2,mnemonicType2)) {
			return false;
//		} else if (mnemonicPath1.size() == 1 || mnemonicType1.equals(NamePartType.SECTION) && mnemonicPath1.size() == 2) {
		} else if (isDiscipline(mnemonicPath1,mnemonicType1) || isSection(mnemonicPath1,mnemonicType1)){
			return false;
//		} else if (mnemonicType1.equals(NamePartType.DEVICE_TYPE) && mnemonicPath1.size() == 3 && mnemonicType2.equals(NamePartType.DEVICE_TYPE) && mnemonicPath2.size() == 3 && mnemonicPath1.get(0).equals(mnemonicPath2.get(0))) {
		} else if ((isDeviceType(mnemonicPath1,mnemonicType1) && isDeviceType(mnemonicPath2,mnemonicType2)) && getDiscipline(mnemonicPath1,mnemonicType1).equals(getDiscipline(mnemonicPath2,mnemonicType2))){
			return false;
		} else if ((isSubsection(mnemonicPath1,mnemonicType1) && isSubsection(mnemonicPath2,mnemonicType2)) && getSection(mnemonicPath1,mnemonicType1).equals(getSection(mnemonicPath2,mnemonicType2)) ){
			return false;
		} else {
			return true;
		}
	}

	@Override public String conventionName(List<String> sectionPath, List<String> deviceTypePath, @Nullable String instanceIndex) {
		if(sectionPath.size()>=3 || deviceTypePath.size()>=3){
		final String section = sectionPath.get(1);
		final String subsection = sectionPath.get(2);
		final String discipline = deviceTypePath.get(0);
		final String deviceType = deviceTypePath.get(2);
		return section + "-" + subsection + ":" + discipline + "-" + deviceType + (instanceIndex != null ? "-" + instanceIndex : "");
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
		if(deviceTypePath.size()>=3 ){
		final String discipline = deviceTypePath.get(0);
		final String deviceType = deviceTypePath.get(2);
		return discipline + "-" + deviceType;
		} else {
			return null;
		}
	}

	@Override
	public String areaName(List<String> sectionPath) {
		if(sectionPath.size()>=3 ){
		final String section = sectionPath.get(1);
		final String subsection = sectionPath.get(2);
		return section + "-" + subsection ;
		}
		else {
			return null;
		}
	}
	private boolean isAreaStructure(NamePartType namePartType){
		return(namePartType.equals(NamePartType.SECTION));
	}

	private boolean isDeviceStructure(NamePartType namePartType){
		return(namePartType.equals(NamePartType.DEVICE_TYPE));
	}
	
	private boolean isSuperSection(List<String> mnemonicPath, NamePartType mnemonicType){
		return isAreaStructure(mnemonicType) && mnemonicPath.size()==1;
	}

	private boolean isSection(List<String> mnemonicPath, NamePartType mnemonicType){
		return isAreaStructure(mnemonicType) && mnemonicPath.size()==2;
	}
	
	private boolean isSubsection(List<String> mnemonicPath, NamePartType mnemonicType){
		return isAreaStructure(mnemonicType) && mnemonicPath.size()==3;
	}
	
	private boolean isDiscipline(List<String> mnemonicPath, NamePartType mnemonicType){
		return isDeviceStructure(mnemonicType) && mnemonicPath.size()==1;
	}
	
	private boolean isCategory(List<String> mnemonicPath, NamePartType mnemonicType){
		return isDeviceStructure(mnemonicType) && mnemonicPath.size()==2;
	}

	private boolean isDeviceType(List<String> mnemonicPath, NamePartType mnemonicType){
		return isDeviceStructure(mnemonicType) && mnemonicPath.size()==3;
	}
	
	private String getSection(List<String> mnemonicPath, NamePartType mnemonicType){
		if (isAreaStructure(mnemonicType)&&mnemonicPath.size()>=2){
			return mnemonicPath.get(1);
		} else {
			return null;
		}
	}
	private String getDiscipline(List<String> mnemonicPath, NamePartType mnemonicType){
		if (isDeviceStructure(mnemonicType)&& mnemonicPath.size()>=1){
			return mnemonicPath.get(0);
		} else {
			return null;
		}
	}
}
