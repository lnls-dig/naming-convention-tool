package org.openepics.names.webservice;

import org.openepics.names.model.Device;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.services.restricted.RestrictedDeviceService;
import org.openepics.names.ui.common.ViewFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.UUID;

/**
 * @author Andraz Pozar <andraz.pozar@cosylab.com>
 */
@Stateless
@Path("deviceNames/{uuid}")
public class SpecificDeviceNameResource {
    @Inject private RestrictedDeviceService deviceService;
    @Inject private ViewFactory viewFactory;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DeviceNameElement getAllDeviceNames(@PathParam("uuid") String reqUuid) {
        for (Device device : deviceService.devices(false)) {
            if (deviceService.currentRevision(device).getDevice().getUuid().equals(UUID.fromString(reqUuid))) {
                final DeviceRevision deviceRevision = deviceService.currentRevision(device);
                final UUID uuid = deviceRevision.getDevice().getUuid();
                final String section = viewFactory.getView(deviceRevision.getSection()).getParent().getMnemonic();
                final String subSection = viewFactory.getView(deviceRevision.getSection()).getMnemonic();
                final String discipline = viewFactory.getView(deviceRevision.getDeviceType()).getParent().getParent().getMnemonic();
                final String deviceType = viewFactory.getView(deviceRevision.getDeviceType()).getMnemonic();
                final String instanceIndex = viewFactory.getView(deviceRevision).getInstanceIndex();
                final String name = viewFactory.getView(deviceRevision).getConventionName();
                return new DeviceNameElement(uuid, section, subSection, discipline, deviceType, instanceIndex, name);
            }
        }  
        return null;
    }
}
