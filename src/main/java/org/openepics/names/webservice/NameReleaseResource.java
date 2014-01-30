/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openepics.names.webservice;

import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.openepics.names.model.NameRelease;
import org.openepics.names.services.ReleaseService;

/**
 *
 * @author Vasu V <vuppala@frib.msu.org>
 */
@Stateless
@Path("release")
public class NameReleaseResource {
    @Inject private ReleaseService releaseService;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
    public List<NameRelease> findAll() {
        return releaseService.getAllReleases();
    }
    /*
    public NameReleaseResource() {
        super(NameRelease.class);
    }

    @POST
    @Override
    @Consumes({"application/xml", "application/json"})
    public void create(NameRelease entity) {
        super.create(entity);
    }

    @PUT
    @Override
    @Consumes({"application/xml", "application/json"})
    public void edit(NameRelease entity) {
        super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") String id) {
        super.remove(super.find(id));
    }

    @GET
    @Path("{id}")
    @Produces({"application/xml", "application/json"})
    public NameRelease find(@PathParam("id") String id) {
        return super.find(id);
    }

    @GET
    @Override
    @Produces({"application/xml", "application/json"})
    public List<NameRelease> findAll() {
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({"application/xml", "application/json"})
    public List<NameRelease> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces("text/plain")
    public String countREST() {
        return String.valueOf(super.count());
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    */
}
