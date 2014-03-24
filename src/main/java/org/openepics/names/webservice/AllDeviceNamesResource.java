package org.openepics.names.webservice;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.openepics.names.model.Device;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePart;
import org.openepics.names.services.restricted.RestrictedDeviceService;
import org.openepics.names.ui.common.ViewFactory;

import com.google.common.collect.Lists;

/**
 * @author Andraz Pozar <andraz.pozar@cosylab.com>
 */
@Stateless
@Path("deviceNames")
public class AllDeviceNamesResource {
    @Inject private RestrictedDeviceService deviceService;
    @Inject private ViewFactory viewFactory;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<DeviceNameElement> getAllDeviceNames() {
        final List<DeviceNameElement> deviceNames = Lists.newArrayList();
        
        for (Device device : deviceService.devices(false)) {
            final DeviceRevision deviceRevision = deviceService.currentRevision(device);
            final String uuid = deviceRevision.getDevice().getUuid();
            final String section = viewFactory.getView(deviceRevision.getSection()).getParent().getName();
            final String subSection = viewFactory.getView(deviceRevision.getSection()).getName();
            final String discipline = viewFactory.getView(deviceRevision.getDeviceType()).getParent().getParent().getName();
            final String deviceType = viewFactory.getView(deviceRevision.getDeviceType()).getName();
            final String instanceIndex = viewFactory.getView(deviceRevision).getQualifier();
            final String name = viewFactory.getView(deviceRevision).getConventionName();
            final DeviceNameElement deviceData = new DeviceNameElement(uuid, section, subSection, discipline, deviceType, instanceIndex, name);
            deviceNames.add(deviceData);
        }
        
        return deviceNames;
    }
}
