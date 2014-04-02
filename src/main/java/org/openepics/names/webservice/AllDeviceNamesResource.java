package org.openepics.names.webservice;

import com.google.common.collect.Lists;
import org.openepics.names.model.Device;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.services.restricted.RestrictedDeviceService;
import org.openepics.names.ui.common.ViewFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

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
            final DeviceNameElement deviceData = new DeviceNameElement();
            deviceData.setUuid(deviceRevision.getDevice().getUuid());
            deviceData.setSection(viewFactory.getView(deviceRevision.getSection()).getParent().getMnemonic());
            deviceData.setSubSection(viewFactory.getView(deviceRevision.getSection()).getMnemonic());
            deviceData.setDiscipline(viewFactory.getView(deviceRevision.getDeviceType()).getParent().getParent().getMnemonic());
            deviceData.setDeviceType(viewFactory.getView(deviceRevision.getDeviceType()).getMnemonic());
            deviceData.setInstanceIndex(viewFactory.getView(deviceRevision).getInstanceIndex());
            deviceData.setName(viewFactory.getView(deviceRevision).getConventionName());
            deviceNames.add(deviceData);
        }
        
        return deviceNames;
    }
}
