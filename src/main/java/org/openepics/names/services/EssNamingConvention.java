package org.openepics.names.services;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.enterprise.inject.Alternative;
import org.openepics.names.ui.DeviceView;
import org.openepics.names.ui.names.NamePartView;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Alternative
@Stateless
public class EssNamingConvention implements NamingConvention {
    @Override public String getNamingConventionName(DeviceView deviceName) {
        final NamePartView supersection = getParentForCategory(deviceName.getSection(), "SUP");
        final NamePartView section = getParentForCategory(deviceName.getSection(), "SECT");
        final NamePartView subsection = getParentForCategory(deviceName.getSection(), "SUB");
        final NamePartView discipline = getParentForCategory(deviceName.getDeviceType(), "DSCP");
        final NamePartView genericDeviceType = getParentForCategory(deviceName.getDeviceType(), "GDEV");
        if (supersection.getName().equals("Acc")) {
            return section.getName() + "-" + discipline.getName() + ":" + genericDeviceType.getName() + "-" + subsection.getName() + deviceName.getQualifier();
        } else {
            return section.getName() + "-" + subsection.getName() + ":" + discipline.getName() + "-" + deviceName.getQualifier();
        }
    }

    @Override public String getNameNormalizedForEquivalence(String name) {
        return name.toUpperCase().replace('I', '1').replace('L', '1').replace('O', '0').replace('W', 'V').replaceAll("(?<!\\d)0+(?=\\d)", "");
    }

    @Override public boolean isNameValid(NamePartView namePart) {
        final @Nullable NamePartView supersection = getParentForCategory(namePart, "SUP");
        if (supersection != null && supersection.getName().equals("Acc") && namePart.getNameCategory().equals("SUB")) {
            return namePart.getName().matches("^[0-9][0-9][1-9]$");
        } else {
            return namePart.getName().matches("^[a-zA-Z][a-zA-Z0-9]*$");
        }
    }

    @Override public boolean isDeviceNameValid(DeviceView deviceName) {
        return deviceName.getQualifier().matches("^[a-zA-Z][a-zA-Z0-9]*$");
    }

    private @Nullable NamePartView getParentForCategory(NamePartView namePart, String categoryName) {
        if (namePart.getNameCategory().equals(categoryName)) return namePart;
        else if (namePart.getParent() != null) return getParentForCategory(namePart.getParent(), categoryName);
        else return null;
    }
}
