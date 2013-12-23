package org.openepics.names.webservice;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.openepics.names.services.NamesEJB;
import org.openepics.names.model.DeviceName;
import org.openepics.names.model.NameEvent;
import org.openepics.names.services.NamingConventionEJB;

/**
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Stateless
@Path("device")
public class DeviceNameResource {

    @PersistenceContext
    EntityManager em;
    @Inject
    NamesEJB namesEJB;
    @Inject
    NamingConventionEJB namingConventionEJB;

    @POST
    public Response create(@QueryParam("section_id") Integer sectionId, @QueryParam("device_type_id") Integer deviceTypeId) {
        final NameEvent section = namesEJB.findEventById(sectionId);
        final NameEvent deviceType = namesEJB.findEventById(deviceTypeId);
        namingConventionEJB.createDeviceName(section, deviceType);
        return Response.ok().build();
    }

    @DELETE
    @Path("{id}")
    public Response remove(@PathParam("id") Integer id) {
        try {
            final DeviceName deviceName = namingConventionEJB.findDeviceNameById(id);
            em.remove(deviceName);
            return Response.ok().build();
        } catch (NoResultException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }
}
