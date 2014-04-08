package org.openepics.names.services.views;

import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.UserAccount;
import org.openepics.names.ui.common.ViewFactory;

import javax.annotation.Nullable;

/**
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public class DeviceView {

    private final ViewFactory viewFactory;

    private final DeviceRevision currentRevision;

    private @Nullable NamePartView sectionView;
    private @Nullable NamePartView deviceTypeView;

    public DeviceView(ViewFactory viewFactory, DeviceRevision currentRevision, @Nullable NamePartView sectionView, @Nullable NamePartView deviceTypeView) {
        this.currentRevision = currentRevision;
        this.viewFactory = viewFactory;
        this.sectionView = sectionView;
        this.deviceTypeView = deviceTypeView;
    }

    public DeviceRevision getDevice() { return currentRevision; }

    public Long getId() { return currentRevision.getId(); }

    public String getConventionName() {
        return currentRevision.getConventionName();
    }

    public NamePartView getSection() {
        if (sectionView == null) {
            sectionView = viewFactory.getView(currentRevision.getSection());
        }
        return sectionView;
    }

    public NamePartView getDeviceType() {
        if (deviceTypeView == null) {
            deviceTypeView = viewFactory.getView(currentRevision.getDeviceType());
        }
        return deviceTypeView;
    }

    public String getInstanceIndex() { return currentRevision.getInstanceIndex(); }

    public UserAccount getRequestedBy() { return currentRevision.getRequestedBy(); }
}
