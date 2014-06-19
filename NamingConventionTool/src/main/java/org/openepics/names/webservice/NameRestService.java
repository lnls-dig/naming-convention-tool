package org.openepics.names.webservice;

import java.util.Set;

import javax.ws.rs.core.Application;

/**
 * This represents the JAX-RS application which hosts all REST resources of the naming tool.
 *
 * @author Andraz Pozar <andraz.pozar@cosylab.com>
 */
@javax.ws.rs.ApplicationPath("/rest")
public class NameRestService extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return getRestResourceClasses();
    }

    private Set<Class<?>> getRestResourceClasses() {
        Set<Class<?>> resources = new java.util.HashSet<Class<?>>();
        return resources;
    }
}
