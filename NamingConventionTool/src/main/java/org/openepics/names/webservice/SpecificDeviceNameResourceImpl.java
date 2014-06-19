package org.openepics.names.webservice;

import java.util.UUID;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.openepics.names.jaxb.DeviceNameElement;
import org.openepics.names.jaxb.SpecificDeviceNameResource;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.services.views.DeviceView;
import org.openepics.names.ui.common.ViewFactory;
import org.openepics.names.util.As;

/**
 * This is implementation of {@link SpecificDeviceNameResource} interface.
 * 
 * @author Andraz Pozar <andraz.pozar@cosylab.com>
 */
@Stateless
public class SpecificDeviceNameResourceImpl implements SpecificDeviceNameResource {
    @Inject private RestrictedNamePartService namePartService;
    @Inject private ViewFactory viewFactory;

    @Override
    public DeviceNameElement getDeviceName(String reqUuid) {
        final @Nullable DeviceRevision deviceRevision = namePartService.currentDeviceRevision(UUID.fromString(reqUuid));
        if (deviceRevision != null && !deviceRevision.isDeleted()) {
            final DeviceView deviceView = viewFactory.getView(deviceRevision);
            final DeviceNameElement deviceData = new DeviceNameElement();
            deviceData.setUuid(deviceRevision.getDevice().getUuid());
            deviceData.setSection(As.notNull(deviceView.getSection().getParent()).getMnemonic());
            deviceData.setSubSection(deviceView.getSection().getMnemonic());
            deviceData.setDiscipline(As.notNull(As.notNull(deviceView.getDeviceType().getParent()).getParent()).getMnemonic());
            deviceData.setDeviceType(deviceView.getDeviceType().getMnemonic());
            deviceData.setInstanceIndex(deviceView.getInstanceIndex());
            deviceData.setName(deviceView.getConventionName());
            return deviceData;
        } else {
            return null;
        }
    }
}
