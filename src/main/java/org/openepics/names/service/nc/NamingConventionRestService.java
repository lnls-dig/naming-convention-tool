package org.openepics.names.service.nc;

import javax.ws.rs.core.Application;
import java.util.Set;

/**
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@javax.ws.rs.ApplicationPath("/nc")
public class NamingConventionRestService extends Application {

    @Override public Set<Class<?>> getClasses() {
        final Set<Class<?>> resources = new java.util.HashSet<Class<?>>();
        resources.add(DefaultResource.class);
        return resources;
    }
}
