package org.openepics.names.services;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.openepics.names.model.Device;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePart;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Stateless
public class DeviceService {

    @PersistenceContext private EntityManager em;

    public Device deviceWithId(String uuid) {
        throw new IllegalStateException(); // TODO
    }

    public List<Device> devices(boolean includeDeleted) {
        throw new IllegalStateException(); // TODO
    }

    public List<Device> devices() {
        return devices(false);
    }

    public List<DeviceRevision> revisions(Device device) {
        throw new IllegalStateException(); // TODO
    }

    public void createDevice(NamePart section, NamePart deviceType) {
        throw new IllegalStateException(); // TODO
    }

    public void modifyDevice(Device device, NamePart section, NamePart deviceType) {
        throw new IllegalStateException(); // TODO
    }

    public void removeDevice(Device device) {
        throw new IllegalStateException(); // TODO
    }
}
