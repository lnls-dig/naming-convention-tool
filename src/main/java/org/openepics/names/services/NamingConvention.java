package org.openepics.names.services;

import javax.annotation.Nullable;
import java.util.List;

/**
 * An interface defining the naming convention to be used by the application that includes:
 * - name validation rules
 * - name uniqueness rules
 * - form of composite names
 *
 * The used naming convention is configured through beans.xml using the CDI alternatives mechanism.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public interface NamingConvention {

    boolean isSectionNameValid(List<String> parentPath, String name);

    boolean isDeviceTypeNameValid(List<String> parentPath, String name);

    boolean isInstanceIndexValid(List<String> sectionPath, List<String> deviceTypePath, @Nullable String instanceIndex);

    String nameNormalizedForEquivalence(String name);

    String namingConventionName(List<String> sectionPath, List<String> deviceTypePath, @Nullable String instanceIndex);
}
