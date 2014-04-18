package org.openepics.names.webservice;

import com.google.common.collect.Lists;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartType;
import org.openepics.names.services.NamePartService;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.services.views.BatchViewProvider;
import org.openepics.names.ui.common.ViewFactory;
import org.openepics.names.util.As;

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
    @Inject private NamePartService namePartService;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<DeviceNameElement> getAllDeviceNames() {
        final List<NamePartRevision> sectionRevisions = namePartService.currentApprovedNamePartRevisions(NamePartType.SECTION, false);
        final List<NamePartRevision> deviceTypeRevisions = namePartService.currentApprovedNamePartRevisions(NamePartType.DEVICE_TYPE, false);
        final List<DeviceRevision> deviceRevisions = namePartService.currentDeviceRevisions(false);
        final BatchViewProvider viewProvider = new BatchViewProvider(sectionRevisions, deviceTypeRevisions, deviceRevisions);

        final List<DeviceNameElement> deviceNames = Lists.newArrayList();
        
        for (DeviceRevision deviceRevision : namePartService.currentDeviceRevisions(false)) {
            final DeviceNameElement deviceData = new DeviceNameElement();
            deviceData.setUuid(deviceRevision.getDevice().getUuid());
            deviceData.setSection(As.notNull(viewProvider.view(deviceRevision.getSection()).getParent()).getMnemonic());
            deviceData.setSubSection(viewProvider.view(deviceRevision.getSection()).getMnemonic());
            deviceData.setDiscipline(As.notNull(As.notNull(viewProvider.view(deviceRevision.getDeviceType()).getParent()).getParent()).getMnemonic());
            deviceData.setDeviceType(viewProvider.view(deviceRevision.getDeviceType()).getMnemonic());
            deviceData.setInstanceIndex(viewProvider.view(deviceRevision).getInstanceIndex());
            deviceData.setName(viewProvider.view(deviceRevision).getConventionName());
            deviceNames.add(deviceData);
        }
        
        return deviceNames;
    }
}
