package org.openepics.names.jaxb;

import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * This resource provides specific device name data.
 * 
 * @author Andraz Pozar <andraz.pozar@cosylab.com>
 * @author Sunil Sah <sunil.sah@cosylab.com>
 */
public interface SpecificDeviceNameResource {
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DeviceNameElement getDeviceName(@PathParam("uuid") String reqUuid);
}
