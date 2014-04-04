package org.openepics.names.webservice;

import org.openepics.names.model.Device;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.services.restricted.RestrictedNamePartService;
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
    @Inject private RestrictedNamePartService namePartService;
    @Inject private ViewFactory viewFactory;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DeviceNameElement getAllDeviceNames(@PathParam("uuid") String reqUuid) {
        for (Device device : namePartService.devices(false)) {
            if (namePartService.currentRevision(device).getDevice().getUuid().equals(UUID.fromString(reqUuid))) {
                final DeviceRevision deviceRevision = namePartService.currentRevision(device);
                final DeviceNameElement deviceData = new DeviceNameElement();
                deviceData.setUuid(deviceRevision.getDevice().getUuid());
                deviceData.setSection(viewFactory.getView(deviceRevision.getSection()).getParent().getMnemonic());
                deviceData.setSubSection(viewFactory.getView(deviceRevision.getSection()).getMnemonic());
                deviceData.setDiscipline(viewFactory.getView(deviceRevision.getDeviceType()).getParent().getParent().getMnemonic());
                deviceData.setDeviceType(viewFactory.getView(deviceRevision.getDeviceType()).getMnemonic());
                deviceData.setInstanceIndex(viewFactory.getView(deviceRevision).getInstanceIndex());
                deviceData.setName(viewFactory.getView(deviceRevision).getConventionName());
                return deviceData;
            }
        }  
        return null;
    }
}
