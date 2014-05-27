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
 */
@Alternative
@Stateless
public class EssNamingConvention implements NamingConvention {

    @Override public boolean isSectionNameValid(List<String> parentPath, String name) {
        if (!isNameLengthValid(name)) {
            return false;
        } else {
            if (parentPath.size() == 2 && parentPath.get(0).equals("Acc")) {
                return name.matches("^[0-9]*$");
            } else {
                return name.matches("^[a-zA-Z][a-zA-Z0-9]*$");
            }
        }
    }

    @Override public boolean isDeviceTypeNameValid(List<String> parentPath, String name) {
        if (!isNameLengthValid(name)) {
            return false;
        } else {
            return name.matches("^[a-zA-Z][a-zA-Z0-9]*$");
        }
    }

    @Override public boolean isInstanceIndexValid(List<String> sectionPath, List<String> deviceTypePath, @Nullable String instanceIndex) {
        if (instanceIndex == null) {
            return true;
        } else if (instanceIndex.length() > 6) {
            return false;
        } else if (sectionPath.get(0).equals("Acc")) {
            return instanceIndex.matches("^[a-zA-Z][a-zA-Z0-9]*$");
        } else {
            return instanceIndex.matches("^[a-zA-Z0-9]+$");
        }
    }

    @Override public String equivalenceClassRepresentative(String name) {
        return name.toUpperCase().replaceAll("(?<=[A-Za-z])0+", "").replace('I', '1').replace('L', '1').replace('O', '0').replace('W', 'V').replaceAll("(?<!\\d)0+(?=\\d)", "");
    }
    
    @Override public boolean canMnemonicsCoexist(List<String> newMnemonicPath, NamePartType newMnemonicType, List<String> comparableMnemonicPath, NamePartType comparableMnemonicType) {
        if (comparableMnemonicPath.size() == 1 || comparableMnemonicType.equals(NamePartType.SECTION) && comparableMnemonicPath.size() == 2) {
            return false;
        } else if (newMnemonicPath.size() == 1 || newMnemonicType.equals(NamePartType.SECTION) && newMnemonicPath.size() == 2) {
            return false;
        } else if (newMnemonicType.equals(NamePartType.DEVICE_TYPE) && newMnemonicPath.size() == 3 && comparableMnemonicType.equals(NamePartType.DEVICE_TYPE) && comparableMnemonicPath.size() == 3 && newMnemonicPath.get(0).equals(comparableMnemonicPath.get(0))) {
            return false;
        } else {
            return true;
        }
    }

    @Override public String conventionName(List<String> sectionPath, List<String> deviceTypePath, @Nullable String instanceIndex) {
        final String supersection = sectionPath.get(0);
        final String section = sectionPath.get(1);
        final String subsection = sectionPath.get(2);
        final String discipline = deviceTypePath.get(0);
        final String genericDeviceType = deviceTypePath.get(2);
        if (supersection.equals("Acc")) {
            return section + "-" + discipline + ":" + genericDeviceType + "-" + subsection + (instanceIndex != null ? instanceIndex : "");
        } else {
            return section + "-" + subsection + ":" + genericDeviceType + (instanceIndex != null ? "-" + instanceIndex : "");
        }
    }
    
    private boolean isNameLengthValid(String name) { return name.length() >= 2 && name.length() <= 6; }
}
