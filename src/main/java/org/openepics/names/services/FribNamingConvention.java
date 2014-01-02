package org.openepics.names.services;

import javax.ejb.Stateless;
import org.openepics.names.model.DeviceName;
import org.openepics.names.model.NameEvent;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Stateless
public class FribNamingConvention implements NamingConvention {
    @Override public String getNamingConventionName(DeviceName deviceName) {
        throw new IllegalStateException();
    }

    @Override public String getNameNormalizedForEquivalence(String name) {
        throw new IllegalStateException();
    }

    @Override public boolean isNameValid(NameEvent nameEvent) {
        throw new IllegalStateException();
    }

    @Override public boolean isDeviceNameValid(DeviceName deviceName) {
        throw new IllegalStateException();
    }
}
