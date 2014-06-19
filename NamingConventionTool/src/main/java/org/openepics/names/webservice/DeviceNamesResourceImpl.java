package org.openepics.names.webservice;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.openepics.names.jaxb.DeviceNameElement;
import org.openepics.names.jaxb.SpecificDeviceNameResource;
import org.openepics.names.jaxb.DeviceNamesResource;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartType;
import org.openepics.names.services.NamePartService;
import org.openepics.names.services.views.BatchViewProvider;
import org.openepics.names.util.As;

import com.google.common.collect.Lists;

/**
 * This is implementation of {@link DeviceNamesResource} interface.
 * 
 * @author Andraz Pozar <andraz.pozar@cosylab.com>
 */
@Stateless
public class DeviceNamesResourceImpl implements DeviceNamesResource {
    @Inject private NamePartService namePartService;
    @Inject private SpecificDeviceNameResource deviceNameResource;

	@Override
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

	@Override
	public SpecificDeviceNameResource getSpecificDeviceNameSubresource() {
		return deviceNameResource;
	}
}
