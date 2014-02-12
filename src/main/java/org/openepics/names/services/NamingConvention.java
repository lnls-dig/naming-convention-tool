package org.openepics.names.services;

import org.openepics.names.ui.devices.DeviceView;
import org.openepics.names.ui.parts.NamePartView;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public interface NamingConvention {

    String getNameNormalizedForEquivalence(String name);

    String getNamingConventionName(DeviceView deviceName);

    boolean isNameValid(NamePartView namePart);

    boolean isDeviceNameValid(DeviceView deviceName);
}
