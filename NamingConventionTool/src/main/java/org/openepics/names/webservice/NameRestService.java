/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openepics.names.webservice;

import javax.ws.rs.core.Application;
import java.util.Set;

/**
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
