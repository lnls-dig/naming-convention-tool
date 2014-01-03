package org.openepics.names.ui;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.util.List;
import org.openepics.names.model.DeviceName;
import org.openepics.names.model.NameEvent;
import org.openepics.names.model.Privilege;

/**
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public class DeviceNameView {

    private final DeviceName deviceName;
    private final String conventionName;

    public DeviceNameView(DeviceName deviceName, String conventionName) {
        this.deviceName = deviceName;
        this.conventionName = conventionName;
    }

    public DeviceName getDeviceName() { return deviceName; }
    public Integer getId() { return deviceName.getId(); }
    public String getConventionName() { return conventionName; }
    public String getSectionPath() { return getNamePath(getSection()); }
    public String getDeviceTypePath() { return getNamePath(getDeviceType()); }
    public NameEvent getSection() { return deviceName.getSection(); }
    public NameEvent getDeviceType() { return deviceName.getDeviceType(); }
    public String getQualifier() { return deviceName.getQualifier(); }
    public String getStatus() {
        switch (deviceName.getStatus()) {
            case VALID: return "Published";
            case INVALID: return "In-Process";
            case DELETED: return "Deleted";
            default: throw new IllegalStateException();
        }
    }
    public Privilege getRequestedBy() { return deviceName.getRequestedBy(); }

    private String getNamePath(NameEvent nameEvent) {
        final List<String> pathElements = Lists.newArrayList();
        for (NameEvent pathElement = nameEvent; pathElement != null; pathElement = pathElement.getParentName()) {
            pathElements.add(0, pathElement.getFullName());
        }
        return Joiner.on(" ▸ ").join(pathElements);
    }
}
