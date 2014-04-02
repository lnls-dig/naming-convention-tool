package org.openepics.names.services.views;

import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.UserAccount;
import org.openepics.names.services.NamingConvention;
import org.openepics.names.ui.common.ViewFactory;

import javax.annotation.Nullable;

/**
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public class DeviceView {

    private final NamingConvention namingConvention;
    private final ViewFactory viewFactory;

    private final DeviceRevision currentRevision;

    private @Nullable NamePartView sectionView;
    private @Nullable NamePartView deviceTypeView;
    private @Nullable String conventionName;

    public DeviceView(ViewFactory viewFactory, NamingConvention namingConvention, DeviceRevision currentRevision, @Nullable NamePartView sectionView, @Nullable NamePartView deviceTypeView) {
        this.currentRevision = currentRevision;
        this.namingConvention = namingConvention;
        this.viewFactory = viewFactory;
        this.sectionView = sectionView;
        this.deviceTypeView = deviceTypeView;
    }

    public DeviceRevision getDevice() { return currentRevision; }

    public Long getId() { return currentRevision.getId(); }

    public String getConventionName() {
        if (conventionName == null) {
            conventionName = namingConvention.namingConventionName(getSection().getMnemonicPath(), getDeviceType().getMnemonicPath(), getInstanceIndex());
        }
        return conventionName;
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
