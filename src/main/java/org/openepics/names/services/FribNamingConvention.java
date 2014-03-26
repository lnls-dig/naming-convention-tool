package org.openepics.names.services;

import org.openepics.names.ui.devices.DeviceView;
import org.openepics.names.ui.parts.NamePartView;
import org.openepics.names.util.NotImplementedException;

import javax.ejb.Stateless;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Stateless
public class FribNamingConvention implements NamingConvention {
    @Override public String getNamingConventionName(DeviceView deviceName) {
        throw new NotImplementedException();
    }

    @Override public String getNameNormalizedForEquivalence(String name) {
        throw new NotImplementedException();
    }

    @Override public boolean isNameValid(NamePartView namePart) {
        throw new NotImplementedException();
    }

    @Override public boolean isDeviceNameValid(DeviceView deviceName) {
        throw new NotImplementedException();
    }
}
