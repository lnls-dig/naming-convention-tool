package org.openepics.names.services;

import javax.annotation.Nullable;
import java.util.List;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public interface NamingConvention {

    String getNameNormalizedForEquivalence(String name);

    String getNamingConventionName(List<String> sectionPath, List<String> deviceTypePath, @Nullable String instanceIndex);

    boolean isSectionNameValid(List<String> parentPath, String name);

    boolean isDeviceTypeNameValid(List<String> parentPath, String name);

    boolean isInstanceIndexValid(String instanceIndex);
}
