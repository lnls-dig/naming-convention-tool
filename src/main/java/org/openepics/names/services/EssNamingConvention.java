package org.openepics.names.services;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.enterprise.inject.Alternative;
import org.openepics.names.model.DeviceName;
import org.openepics.names.model.NameEvent;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Alternative
@Stateless
public class EssNamingConvention implements NamingConvention {
    @Override public String getNamingConventionName(DeviceName deviceName) {
        final NameEvent supersection = getParentForCategory(deviceName.getSection(), "SUP");
        final NameEvent section = getParentForCategory(deviceName.getSection(), "SECT");
        final NameEvent subsection = getParentForCategory(deviceName.getSection(), "SUB");
        final NameEvent discipline = getParentForCategory(deviceName.getDeviceType(), "DSCP");
        final NameEvent genericDeviceType = getParentForCategory(deviceName.getDeviceType(), "GDEV");
        if (supersection.getName().equals("Acc")) {
            return section.getName() + "-" + discipline.getName() + ":" + genericDeviceType.getName() + "-" + subsection.getName() + deviceName.getQualifier();
        } else {
            return section.getName() + "-" + subsection.getName() + ":" + discipline.getName() + "-" + deviceName.getQualifier();
        }
    }

    @Override public String getNameNormalizedForEquivalence(String name) {
        return name.toUpperCase().replace('I', '1').replace('L', '1').replace('O', '0').replace('W', 'V').replaceAll("(?<!\\d)0+(?=\\d)", "");
    }

    @Override public boolean isNameValid(NameEvent nameEvent) {
        final @Nullable NameEvent supersection = getParentForCategory(nameEvent, "SUP");
        if (supersection != null && supersection.getName().equals("Acc") && nameEvent.getNameCategory().getName().equals("SUB")) {
            return nameEvent.getName().matches("^[0-9][0-9][1-9]$");
        } else {
            return nameEvent.getName().matches("^[a-zA-Z][a-zA-Z0-9]*$");
        }
    }

    @Override public boolean isDeviceNameValid(DeviceName deviceName) {
        return deviceName.getQualifier().matches("^[a-zA-Z][a-zA-Z0-9]*$");
    }

    private @Nullable NameEvent getParentForCategory(NameEvent nameEvent, String categoryName) {
        if (nameEvent.getNameCategory().getName().equals(categoryName)) return nameEvent;
        else if (nameEvent.getParentName() != null) return getParentForCategory(nameEvent.getParentName(), categoryName);
        else return null;
    }
}
