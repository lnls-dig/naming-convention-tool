package org.openepics.names.webservice;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.openepics.names.model.Device;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.services.restricted.RestrictedDeviceService;
import org.openepics.names.ui.common.ViewFactory;

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
    public DeviceNameElement getAllDeviceNames(@PathParam("uuid") String uuid) {
        DeviceNameElement deviceData = new DeviceNameElement();
        for (Device device : deviceService.devices(false)) {
            if(deviceService.currentRevision(device).getDevice().getUuid().equals(uuid)) {
                DeviceRevision deviceRevision = deviceService.currentRevision(device);
                deviceData.setUuid(deviceRevision.getDevice().getUuid());
                deviceData.setSection(viewFactory.getView(deviceRevision.getSection()).getParent().getName());
                deviceData.setSubSection(viewFactory.getView(deviceRevision.getSection()).getName());
                deviceData.setDiscipline(viewFactory.getView(deviceRevision.getDeviceType()).getParent().getParent().getName());
                deviceData.setDeviceType(viewFactory.getView(deviceRevision.getDeviceType()).getName());
                deviceData.setInstanceIndex(viewFactory.getView(deviceRevision).getQualifier());
                deviceData.setName(viewFactory.getView(deviceRevision).getConventionName());
            }
        }        
        return deviceData;
    }
}
