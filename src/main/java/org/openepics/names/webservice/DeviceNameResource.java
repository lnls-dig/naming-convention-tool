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
import org.openepics.names.model.Device;
import org.openepics.names.model.NamePart;
import org.openepics.names.services.DeviceService;
import org.openepics.names.services.NamePartService;

/**
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Stateless
@Path("device")
public class DeviceNameResource {

    @PersistenceContext EntityManager em;
    @Inject NamePartService namePartService;
    @Inject DeviceService deviceService;

    @POST
    public Response create(@QueryParam("section_id") String sectionId, @QueryParam("device_type_id") String deviceTypeId) {
       final NamePart section = null; // TODO: namePartService.namePartWithId(sectionId);
       final NamePart deviceType = null; // TODO: namePartService.namePartWithId(deviceTypeId);
        deviceService.createDevice(section, deviceType, null, null);
        return Response.ok().build();
    }

    @DELETE
    @Path("{id}")
    public Response remove(@PathParam("id") String id) {
        try {
            final Device device = null; // TODO: deviceService.deviceWithId(id);
            deviceService.deleteDevice(device, null);
            return Response.ok().build();
        } catch (NoResultException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }
}
