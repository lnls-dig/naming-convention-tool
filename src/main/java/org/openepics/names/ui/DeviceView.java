package org.openepics.names.ui;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.util.List;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.UserAccount;
import org.openepics.names.services.NamingConvention;
import org.openepics.names.ui.names.NamePartView;

/**
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public class DeviceView {

    private final NamingConvention namingConvention;
    private final ViewFactory viewFactory;

    private final DeviceRevision deviceName;
    private String conventionName;

    public DeviceView(DeviceRevision deviceName, NamingConvention namingConvention, ViewFactory viewFactory) {
        this.deviceName = deviceName;
        this.namingConvention = namingConvention;
        this.viewFactory = viewFactory;
    }

    public DeviceRevision getDevice() { return deviceName; }
    public Integer getId() { return deviceName.getId(); }
    public String getConventionName() {
        if (conventionName == null) conventionName = namingConvention.getNamingConventionName(this);
        return conventionName;
    }
    public String getSectionPath() { return getNamePath(getSection()); }
    public String getDeviceTypePath() { return getNamePath(getDeviceType()); }
    public NamePartView getSection() { return viewFactory.getView(deviceName.getSection()); }
    public NamePartView getDeviceType() { return viewFactory.getView(deviceName.getDeviceType()); }
    public String getQualifier() { return deviceName.getQualifier(); }
    public UserAccount getRequestedBy() { return deviceName.getRequestedBy(); }

    private String getNamePath(NamePartView nameEvent) {
        final List<String> pathElementNames = Lists.newArrayList();
        for (NamePartView pathElement = nameEvent; pathElement != null; pathElement = pathElement.getParent()) {
            pathElementNames.add(0, pathElement.getFullName());
        }
        return Joiner.on(" ▸ ").join(pathElementNames);
    }
}
