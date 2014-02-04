package org.openepics.names.services;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.openepics.names.model.Device;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.UserAccount;

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
        if (includeDeleted)
            return em.createQuery("SELECT r.device FROM DeviceRevision r WHERE r.id = (SELECT MAX(r2.id) FROM DeviceRevision r2 WHERE r2.device = r.device)", Device.class).getResultList();
        else {
            return em.createQuery("SELECT r.device FROM DeviceRevision r WHERE r.id = (SELECT MAX(r2.id) FROM DeviceRevision r2 WHERE r2.device = r.device)", Device.class).getResultList();
        }
    }

    public List<Device> devices() {
        return devices(false);
    }

    public List<DeviceRevision> revisions(Device device) {
        return em.createQuery("SELECT r FROM DeviceRevision r WHERE r.device = :device ORDER BY r.id", DeviceRevision.class).setParameter("device", device).getResultList();
    }

    public DeviceRevision createDevice(NamePart section, NamePart deviceType, String qualifier, @Nullable UserAccount user) {
        final Device device = new Device(UUID.randomUUID().toString());
        final DeviceRevision newRevision = new DeviceRevision(device, user, new Date(), false, section, deviceType, qualifier);

        em.persist(device);
        em.persist(newRevision);

        return newRevision;
    }

    public DeviceRevision modifyDevice(Device device, NamePart section, NamePart deviceType, String qualifier, @Nullable UserAccount user) {
        final DeviceRevision currentRevision = currentRevision(device);

        final DeviceRevision newRevision = new DeviceRevision(device, user, new Date(), false, section,  deviceType, qualifier);
        em.persist(newRevision);

        return newRevision;
    }

    public DeviceRevision deleteDevice(Device device, @Nullable UserAccount user) {
        final DeviceRevision currentRevision = currentRevision(device);

        if (!currentRevision.isDeleted()) {
            final DeviceRevision newRevision = new DeviceRevision(device, user, new Date(), true, currentRevision.getSection(), currentRevision.getDeviceType(), currentRevision.getQualifier());
            em.persist(newRevision);
            return newRevision;
        } else {
            return currentRevision;
        }
    }

    public DeviceRevision currentRevision(Device device) {
        return em.createQuery("SELECT r FROM DeviceRevision r WHERE r.device = :device ORDER BY r.id DESC", DeviceRevision.class).setParameter("device", device).getResultList().get(0);
    }
}
