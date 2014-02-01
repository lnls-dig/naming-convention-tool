package org.openepics.names.ui;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.util.List;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.UserAccount;
import org.openepics.names.ui.names.NamePartView;

/**
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public class DeviceView {

    private final DeviceRevision deviceName;
    private final String conventionName;

    public DeviceView(DeviceRevision deviceName, String conventionName) {
        this.deviceName = deviceName;
        this.conventionName = conventionName;
    }

    public DeviceRevision getDevice() { return deviceName; }
    public Integer getId() { return deviceName.getId(); }
    public String getConventionName() { return conventionName; }
    public String getSectionPath() { return getNamePath(getSection()); }
    public String getDeviceTypePath() { return getNamePath(getDeviceType()); }
    public NamePartView getSection() { throw new IllegalStateException(); } // { return ViewFactory.getView(deviceName.getSection()); }
    public NamePartView getDeviceType() { throw new IllegalStateException(); } // { return ViewFactory.getView(deviceName.getDeviceType()); }
    public String getQualifier() { return deviceName.getQualifier(); }
    public String getStatus() {
        switch (deviceName.getStatus()) {
            case VALID: return "Published";
            case INVALID: return "In-Process";
            case DELETED: return "Deleted";
            default: throw new IllegalStateException();
        }
    }
    public UserAccount getRequestedBy() { return deviceName.getRequestedBy(); }

    private String getNamePath(NamePartView nameEvent) {
        final List<String> pathElementNames = Lists.newArrayList();
        for (NamePartView pathElement = nameEvent; pathElement != null; pathElement = pathElement.getParent()) {
            pathElementNames.add(0, pathElement.getFullName());
        }
        return Joiner.on(" ▸ ").join(pathElementNames);
    }
}
