package org.openepics.names.jaxb;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * This resource provides bulk device name data, and has a subresource for retrieving data of specific
 * device names.
 * 
 * @author Andraz Pozar  
 * @author Sunil Sah  
 */
@Path("deviceNames")
public interface DeviceNamesResource {

	@GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<DeviceNameElement> getAllDeviceNames();

	@Path("{uuid}")
	public SpecificDeviceNameResource getSpecificDeviceNameSubresource();
}
