package org.openepics.names.services;

import org.openepics.names.model.Device;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.UserAccount;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * A service bean managing Device entities.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Stateless
public class DeviceService {

    @PersistenceContext private EntityManager em;

    public List<Device> devices(boolean includeDeleted) {
        if (includeDeleted)
            return em.createQuery("SELECT r.device FROM DeviceRevision r WHERE r.id = (SELECT MAX(r2.id) FROM DeviceRevision r2 WHERE r2.device = r.device)", Device.class).getResultList();
        else {
            return em.createQuery("SELECT r.device FROM DeviceRevision r WHERE r.id = (SELECT MAX(r2.id) FROM DeviceRevision r2 WHERE r2.device = r.device) AND r.deleted = false", Device.class).getResultList();
        }
    }

    public List<Device> devices() {
        return devices(false);
    }

    public List<DeviceRevision> currentRevisions(boolean includeDeleted) {
        if (includeDeleted)
            return em.createQuery("SELECT r FROM DeviceRevision r WHERE r.id = (SELECT MAX(r2.id) FROM DeviceRevision r2 WHERE r2.device = r.device)", DeviceRevision.class).getResultList();
        else {
            return em.createQuery("SELECT r FROM DeviceRevision r WHERE r.id = (SELECT MAX(r2.id) FROM DeviceRevision r2 WHERE r2.device = r.device) AND r.deleted = false", DeviceRevision.class).getResultList();
        }
    }

    public List<Device> devicesInSection(NamePart section) {
        return em.createQuery("SELECT r.device FROM DeviceRevision r WHERE r.id = (SELECT MAX(r2.id) FROM DeviceRevision r2 WHERE r2.device = r.device) AND r.section = :section AND r.deleted = false", Device.class).setParameter("section", section).getResultList();
    }

    public List<Device> devicesOfType(NamePart deviceType) {
        return em.createQuery("SELECT r.device FROM DeviceRevision r WHERE r.id = (SELECT MAX(r2.id) FROM DeviceRevision r2 WHERE r2.device = r.device) AND r.deviceType = :deviceType AND r.deleted = false", Device.class).setParameter("deviceType", deviceType).getResultList();
    }

    public List<DeviceRevision> revisions(Device device) {
        return em.createQuery("SELECT r FROM DeviceRevision r WHERE r.device = :device ORDER BY r.id", DeviceRevision.class).setParameter("device", device).getResultList();
    }

    public DeviceRevision createDevice(NamePart section, NamePart deviceType, @Nullable String instanceIndex, @Nullable UserAccount user) {
        final Device device = new Device(UUID.randomUUID());
        final DeviceRevision newRevision = new DeviceRevision(device, user, new Date(), false, section, deviceType, instanceIndex);

        em.persist(device);
        em.persist(newRevision);

        return newRevision;
    }

    public DeviceRevision modifyDevice(Device device, NamePart section, NamePart deviceType, @Nullable String instanceIndex, @Nullable UserAccount user) {
        final DeviceRevision currentRevision = currentRevision(device);

        final DeviceRevision newRevision = new DeviceRevision(device, user, new Date(), false, section,  deviceType, instanceIndex);
        em.persist(newRevision);

        return newRevision;
    }

    public DeviceRevision deleteDevice(Device device, @Nullable UserAccount user) {
        final DeviceRevision currentRevision = currentRevision(device);

        if (!currentRevision.isDeleted()) {
            final DeviceRevision newRevision = new DeviceRevision(device, user, new Date(), true, currentRevision.getSection(), currentRevision.getDeviceType(), currentRevision.getInstanceIndex());
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
