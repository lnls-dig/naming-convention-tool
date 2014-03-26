package org.openepics.names.ui.devices;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.UserAccount;
import org.openepics.names.services.NamingConvention;
import org.openepics.names.ui.common.ViewFactory;
import org.openepics.names.ui.parts.NamePartView;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public class DeviceView {

    private final NamingConvention namingConvention;
    private final ViewFactory viewFactory;

    private final DeviceRevision deviceName;
    private String conventionName;

    private @Nullable NamePartView sectionView;
    private @Nullable NamePartView deviceTypeView;

    public DeviceView(DeviceRevision deviceName, NamingConvention namingConvention, ViewFactory viewFactory, @Nullable NamePartView sectionView, @Nullable NamePartView deviceTypeView) {
        this.deviceName = deviceName;
        this.namingConvention = namingConvention;
        this.viewFactory = viewFactory;
        this.sectionView = sectionView;
        this.deviceTypeView = deviceTypeView;
    }

    public DeviceRevision getDevice() { return deviceName; }

    public Long getId() { return deviceName.getId(); }

    public String getConventionName() {
        if (conventionName == null) conventionName = namingConvention.getNamingConventionName(this);
        return conventionName;
    }

    public String getSectionPath() { return getNamePath(getSection()); }

    public String getDeviceTypePath() { return getNamePath(getDeviceType()); }

    public NamePartView getSection() {
        if (sectionView == null) {
            sectionView = viewFactory.getView(deviceName.getSection());
        }
        return sectionView;
    }

    public NamePartView getDeviceType() {
        if (deviceTypeView == null) {
            deviceTypeView = viewFactory.getView(deviceName.getDeviceType());
        }
        return deviceTypeView;
    }

    public String getInstanceIndex() { return deviceName.getInstanceIndex(); }

    public UserAccount getRequestedBy() { return deviceName.getRequestedBy(); }

    private String getNamePath(NamePartView nameEvent) {
        final List<String> pathElementNames = Lists.newArrayList();
        for (NamePartView pathElement = nameEvent; pathElement != null; pathElement = pathElement.getParent()) {
            pathElementNames.add(0, pathElement.getName());
        }
        return Joiner.on(" ▸ ").join(pathElementNames);
    }
}
