package org.openepics.names.services;

import com.google.common.collect.Lists;
import org.openepics.names.model.NamePartType;
import org.openepics.names.ui.devices.DeviceView;
import org.openepics.names.ui.parts.NamePartView;
import org.openepics.names.util.UnhandledCaseException;

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
    @Override public String getNamingConventionName(DeviceView deviceName) {
        final List<NamePartView> sectionPath = namePartPath(deviceName.getSection());
        final List<NamePartView> deviceTypePath = namePartPath(deviceName.getDeviceType());

        final NamePartView supersection = sectionPath.get(0);
        final NamePartView section = sectionPath.get(1);
        final NamePartView subsection = sectionPath.get(2);
        final NamePartView discipline = deviceTypePath.get(0);
        final NamePartView genericDeviceType = deviceTypePath.get(2);
        if (supersection.getMnemonic().equals("Acc")) {
            return section.getMnemonic() + "-" + discipline.getMnemonic() + ":" + genericDeviceType.getMnemonic() + "-" + subsection.getMnemonic() + deviceName.getInstanceIndex();
        } else {
            return section.getMnemonic() + "-" + subsection.getMnemonic() + ":" + discipline.getMnemonic() + "-" + deviceName.getInstanceIndex();
        }
    }

    @Override public String getNameNormalizedForEquivalence(String name) {
        return name.toUpperCase().replace('I', '1').replace('L', '1').replace('O', '0').replace('W', 'V').replaceAll("(?<!\\d)0+(?=\\d)", "");
    }

    @Override public boolean isNameValid(NamePartView namePart) {
        if (namePart.getNamePart().getNamePartType() == NamePartType.SECTION) {
            final List<NamePartView> sectionPath = namePartPath(namePart);
            final NamePartView supersection = sectionPath.get(0);
            if (supersection != null && supersection.getMnemonic().equals("Acc") && sectionPath.indexOf(namePart) == 2) {
                return namePart.getMnemonic().matches("^[0-9][0-9][1-9]$");
            } else {
                return namePart.getMnemonic().matches("^[a-zA-Z][a-zA-Z0-9]*$");
            }
        } else if (namePart.getNamePart().getNamePartType() == NamePartType.DEVICE_TYPE) {
            return namePart.getMnemonic().matches("^[a-zA-Z][a-zA-Z0-9]*$");
        } else {
            throw new UnhandledCaseException();
        }
    }

    @Override public boolean isDeviceNameValid(DeviceView deviceName) {
        return deviceName.getInstanceIndex().matches("^[a-zA-Z][a-zA-Z0-9]*$");
    }

    private List<NamePartView> namePartPath(NamePartView namePart) {
        final List<NamePartView> parts = Lists.newArrayList();
        NamePartView currentNamePart = namePart;
        do {
            parts.add(0, currentNamePart);
            currentNamePart = currentNamePart.getParent();
        } while (currentNamePart != null);

        return parts;
    }
}
