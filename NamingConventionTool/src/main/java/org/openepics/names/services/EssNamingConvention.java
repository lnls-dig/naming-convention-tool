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
		if (!isNameLengthValid(name,1,6)) {
			return false;
		} else {        	
			return name.matches("^[a-zA-Z0-9]+$");
		}
	}

	@Override public boolean isDeviceTypeNameValid(List<String> parentPath, String name) {
		if (!isNameLengthValid(name,1,6)) {
			return false;
		} else {
			if (parentPath.size() == 2 ) {
				return name.matches("^[a-zA-Z][a-zA-Z0-9]+$");
			} else {
				return name.matches("^[a-zA-Z0-9]+$");
			}

		}
	}

	@Override public boolean isInstanceIndexValid(List<String> sectionPath, List<String> deviceTypePath, @Nullable String instanceIndex) {
		if(instanceIndex == null) {
			return true;
		} else {
			if (!isNameLengthValid(instanceIndex,1,6)) {
				return false;
			} else {
				return instanceIndex.matches("^[a-zA-Z0-9]+$");
			}
		}
	}

	@Override public String equivalenceClassRepresentative(String name) {
		return name.toUpperCase().replaceAll("(?<=[A-Za-z])0+", "").replace('I', '1').replace('L', '1').replace('O', '0').replace('W', 'V').replaceAll("(?<!\\d)0+(?=\\d)", "");
	}

	@Override public boolean canMnemonicsCoexist(List<String> mnemonicPath1, NamePartType mnemonicType1, List<String> mnemonicPath2, NamePartType mnemonicType2) {
		if (mnemonicPath2.size() == 1 || mnemonicType2.equals(NamePartType.SECTION) && mnemonicPath2.size() == 2) {
			return false;
		} else if (mnemonicPath1.size() == 1 || mnemonicType1.equals(NamePartType.SECTION) && mnemonicPath1.size() == 2) {
			return false;
		} else if (mnemonicType1.equals(NamePartType.DEVICE_TYPE) && mnemonicPath1.size() == 3 && mnemonicType2.equals(NamePartType.DEVICE_TYPE) && mnemonicPath2.size() == 3 && mnemonicPath1.get(0).equals(mnemonicPath2.get(0))) {
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

	private boolean isNameLengthValid(String name,int nMin, int nMax) { 
		return name.length() >= nMin && name.length() <= nMax; 
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
}
