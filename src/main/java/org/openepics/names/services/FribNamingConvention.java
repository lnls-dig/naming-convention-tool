package org.openepics.names.services;

import javax.ejb.Stateless;
import org.openepics.names.ui.devices.DeviceView;
import org.openepics.names.ui.parts.NamePartView;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Stateless
public class FribNamingConvention implements NamingConvention {
    @Override public String getNamingConventionName(DeviceView deviceName) {
        throw new IllegalStateException();
    }

    @Override public String getNameNormalizedForEquivalence(String name) {
        throw new IllegalStateException();
    }

    @Override public boolean isNameValid(NamePartView namePart) {
        throw new IllegalStateException();
    }

    @Override public boolean isDeviceNameValid(DeviceView deviceName) {
        throw new IllegalStateException();
    }
}
