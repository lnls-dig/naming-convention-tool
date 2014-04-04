package org.openepics.names.services;

import org.openepics.names.util.NotImplementedException;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.enterprise.inject.Alternative;
import java.util.List;

/**
 * An empty stub for the naming convention used by FRIB.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Alternative
@Stateless
public class FribNamingConvention implements NamingConvention {

    @Override public boolean isSectionNameValid(List<String> parentPath, String name) {
        throw new NotImplementedException();
    }

    @Override public boolean isDeviceTypeNameValid(List<String> parentPath, String name) {
        throw new NotImplementedException();
    }

    @Override public boolean isInstanceIndexValid(List<String> sectionPath, List<String> deviceTypePath, @Nullable String instanceIndex) {
        throw new NotImplementedException();
    }

    @Override public String nameNormalizedForEquivalence(String name) {
        throw new NotImplementedException();
    }

    @Override public String namingConventionName(List<String> sectionPath, List<String> deviceTypePath, @Nullable String instanceIndex) {
        throw new NotImplementedException();
    }
}
