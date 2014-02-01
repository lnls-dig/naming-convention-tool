package org.openepics.names.services.restricted;

import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.openepics.names.model.Device;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePart;
import org.openepics.names.services.DeviceService;
import org.openepics.names.services.SessionService;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Stateless
public class RestrictedDeviceService {

    @Inject private SessionService sessionService;
    @Inject private DeviceService deviceService;

    public Device deviceWithId(String uuid) {
        return deviceService.deviceWithId(uuid);
    }

    public List<Device> devices(boolean includeDeleted) {
        return deviceService.devices(includeDeleted);
    }

    public List<Device> devices() {
        return deviceService.devices();
    }

    public List<DeviceRevision> revisions(Device device) {
        return deviceService.revisions(device);
    }

    public DeviceRevision createDevice(NamePart section, NamePart deviceType, String qualifier) {
        return deviceService.createDevice(section, deviceType, qualifier, sessionService.user());
    }

    public DeviceRevision modifyDevice(Device device, NamePart section, NamePart deviceType, String qualifier) {
        return deviceService.modifyDevice(device, section, deviceType, qualifier, sessionService.user());
    }

    public DeviceRevision deleteDevice(Device device) {
        return deviceService.deleteDevice(device, sessionService.user());
    }
}
