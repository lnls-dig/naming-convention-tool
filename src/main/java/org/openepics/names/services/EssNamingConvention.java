package org.openepics.names.services;

import javax.ejb.Stateless;
import org.openepics.names.model.DeviceName;
import org.openepics.names.model.NameEvent;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Stateless
public class EssNamingConvention {
    public String getNamingConventionName(DeviceName deviceName) {
        final NameEvent section = getParentForCategory(deviceName.getSection(), "SECT");
        final NameEvent subsection = getParentForCategory(deviceName.getSection(), "SUB");
        final NameEvent discipline = getParentForCategory(deviceName.getDeviceType(), "DSCP");
        return section.getName() + "-" + subsection.getName() + ":" + discipline.getName();
    }
    
    private NameEvent getParentForCategory(NameEvent nameEvent, String categoryName) {
        if (nameEvent.getNameCategory().getName().equals(categoryName)) return nameEvent;
        else return getParentForCategory(nameEvent.getParentName(), categoryName);
    }
}
