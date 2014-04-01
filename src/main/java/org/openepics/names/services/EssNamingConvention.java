package org.openepics.names.services;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.enterprise.inject.Alternative;
import java.util.List;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Alternative
@Stateless
public class EssNamingConvention implements NamingConvention {
    @Override public String getNamingConventionName(List<String> sectionPath, List<String> deviceTypePath, @Nullable String instanceIndex) {
        final String supersection = sectionPath.get(0);
        final String section = sectionPath.get(1);
        final String subsection = sectionPath.get(2);
        final String discipline = deviceTypePath.get(0);
        final String genericDeviceType = deviceTypePath.get(2);
        if (supersection.equals("Acc")) {
            return section + "-" + discipline + ":" + genericDeviceType + "-" + subsection + (instanceIndex != null ? instanceIndex : "");
        } else {
            return section + "-" + subsection + ":" + discipline + "-" + (instanceIndex != null ? instanceIndex : "");
        }
    }

    @Override public String getNameNormalizedForEquivalence(String name) {
        return name.toUpperCase().replace('I', '1').replace('L', '1').replace('O', '0').replace('W', 'V').replaceAll("(?<!\\d)0+(?=\\d)", "");
    }

    @Override public boolean isSectionNameValid(List<String> parentPath, String name) {
        if (parentPath.size() == 2 && parentPath.get(0).equals("Acc")) {
            return name.matches("^([1-9][0-9][0-9])|([0-9]?[1-9][0-9])|[0-9]?[0-9]?[1-9]$");
        } else {
            return name.matches("^[a-zA-Z][a-zA-Z0-9]*$");
        }
    }

    @Override public boolean isDeviceTypeNameValid(List<String> parentPath, String name) {
        return name.matches("^[a-zA-Z][a-zA-Z0-9]*$");
    }

    @Override public boolean isInstanceIndexValid(String instanceIndex) {
        return instanceIndex.matches("^[a-zA-Z][a-zA-Z0-9]*$");
    }
}
