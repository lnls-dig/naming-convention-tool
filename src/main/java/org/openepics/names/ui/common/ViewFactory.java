package org.openepics.names.ui.common;

import org.openepics.names.ui.devices.DeviceView;
import javax.annotation.Nullable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import org.openepics.names.model.Device;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.services.DeviceService;
import org.openepics.names.services.NamingConvention;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.ui.parts.NamePartView;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@ManagedBean
@ViewScoped
public class ViewFactory {

    @Inject private RestrictedNamePartService namePartService;
    @Inject private DeviceService deviceService;
    @Inject private NamingConvention namingConvention;

    public NamePartView getView(NamePart namePart) {
        return new NamePartView(namePartService, namePartService.approvedRevision(namePart), namePartService.pendingRevision(namePart));
    }

    public NamePartView getView(NamePartRevision namePartRevision) {
        return new NamePartView(namePartService, namePartRevision, null);
    }

    public NamePartView getView(@Nullable NamePartRevision approvedRevision, @Nullable NamePartRevision pendingRevision) {
        return new NamePartView(namePartService, approvedRevision, pendingRevision);
    }

    public DeviceView getView(Device device) {
        return new DeviceView(deviceService.currentRevision(device), namingConvention, this);
    }

    public DeviceView getView(DeviceRevision deviceRevision) {
        return new DeviceView(deviceRevision, namingConvention, this);
    }
}
