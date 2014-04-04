package org.openepics.names.services.restricted;

import com.google.common.base.Preconditions;
import org.openepics.names.model.Device;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePart;
import org.openepics.names.services.DeviceService;
import org.openepics.names.services.SessionService;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

/**
 * A gateway to a DeviceService bean that enforces user access control rules on each call. All calls from UI code should
 * go through this.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Stateless
public class RestrictedDeviceService {

    @Inject private SessionService sessionService;
    @Inject private DeviceService deviceService;

    public List<Device> devices(boolean includeDeleted) {
        return deviceService.devices(includeDeleted);
    }

    public List<Device> devices() {
        return deviceService.devices();
    }

    public List<DeviceRevision> currentRevisions(boolean includeDeleted) {
        return deviceService.currentRevisions(includeDeleted);
    }

    public List<DeviceRevision> revisions(Device device) {
        return deviceService.revisions(device);
    }
    
    public DeviceRevision currentRevision(Device device) {
        return deviceService.currentRevision(device);
    }

    public DeviceRevision createDevice(NamePart section, NamePart deviceType, @Nullable String instanceIndex) {
        Preconditions.checkState(sessionService.isEditor());
        return deviceService.createDevice(section, deviceType, instanceIndex, sessionService.user());
    }

    public DeviceRevision modifyDevice(Device device, NamePart section, NamePart deviceType, @Nullable String instanceIndex) {
        Preconditions.checkState(sessionService.isEditor());
        return deviceService.modifyDevice(device, section, deviceType, instanceIndex, sessionService.user());
    }

    public DeviceRevision deleteDevice(Device device) {
        Preconditions.checkState(sessionService.isEditor());
        return deviceService.deleteDevice(device, sessionService.user());
    }
}
