package org.openepics.names.ui.common;

import org.openepics.names.model.Device;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.services.DeviceService;
import org.openepics.names.services.NamingConvention;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.ui.devices.DeviceView;
import org.openepics.names.ui.parts.NamePartView;

import javax.annotation.Nullable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

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
        return new NamePartView(namePartService, namePartService.approvedRevision(namePart), namePartService.pendingRevision(namePart), null);
    }

    public NamePartView getView(NamePartRevision namePartRevision) {
        return new NamePartView(namePartService, namePartRevision, null, null);
    }

    public NamePartView getView(NamePartRevision namePartRevision, @Nullable NamePartView parentView) {
        return new NamePartView(namePartService, namePartRevision, null, parentView);
    }

    public NamePartView getView(@Nullable NamePartRevision approvedRevision, @Nullable NamePartRevision pendingRevision) {
        return new NamePartView(namePartService, approvedRevision, pendingRevision, null);
    }

    public NamePartView getView(@Nullable NamePartRevision approvedRevision, @Nullable NamePartRevision pendingRevision, @Nullable NamePartView parentView) {
        return new NamePartView(namePartService, approvedRevision, pendingRevision, parentView);
    }

    public DeviceView getView(DeviceRevision deviceRevision) {
        return new DeviceView(deviceRevision, namingConvention, this, null, null);
    }

    public DeviceView getView(DeviceRevision deviceRevision, @Nullable NamePartView sectionView, @Nullable NamePartView deviceTypeView) {
        return new DeviceView(deviceRevision, namingConvention, this, sectionView, deviceTypeView);
    }
}
