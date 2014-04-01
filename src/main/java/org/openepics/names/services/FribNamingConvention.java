package org.openepics.names.services;

import org.openepics.names.util.NotImplementedException;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import java.util.List;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Stateless
public class FribNamingConvention implements NamingConvention {

    @Override public String getNamingConventionName(List<String> sectionPath, List<String> deviceTypePath, @Nullable String instanceIndex) {
        throw new NotImplementedException();
    }

    @Override public String getNameNormalizedForEquivalence(String name) {
        throw new NotImplementedException();
    }

    @Override public boolean isSectionNameValid(List<String> parentPath, String name) {
        throw new NotImplementedException();
    }

    @Override public boolean isDeviceTypeNameValid(List<String> parentPath, String name) {
        throw new NotImplementedException();
    }

    @Override public boolean isInstanceIndexValid(String instanceIndex) {
        throw new NotImplementedException();
    }
}
