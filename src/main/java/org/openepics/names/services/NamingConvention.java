package org.openepics.names.services;

import org.openepics.names.model.DeviceName;
import org.openepics.names.model.NameEvent;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public interface NamingConvention {

    String getNameNormalizedForEquivalence(String name);

    String getNamingConventionName(DeviceName deviceName);

    boolean isNameValid(NameEvent nameEvent);

    boolean isDeviceNameValid(DeviceName deviceName);
}
