package org.openepics.names.services;

import com.google.common.base.Preconditions;
import java.security.AccessControlException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.DeviceRevisionStatus;
import org.openepics.names.model.Privilege;
import org.openepics.names.ui.UserManager;

@Stateless
public class NamingConventionEJB {

    private static final Logger logger = Logger.getLogger("org.openepics.names.services.NamingConventionEJB");

    @Inject
    private UserManager userManager;

    @PersistenceContext(unitName = "org.openepics.names.punit")
    private EntityManager em;

    public DeviceRevision createDeviceName(NamePartRevision section, NamePartRevision deviceType) {
        Preconditions.checkNotNull(section);
        Preconditions.checkNotNull(deviceType);

        if (!userManager.isLoggedIn()) {
            throw new AccessControlException("You must be logged in to perform the operation.");
        }

        final long deviceInstances = countDeviceNamesByRef(section, deviceType);
        final String qualifier = getQualifier(deviceInstances);

        final DeviceRevision newDeviceName = new DeviceRevision(section, deviceType, qualifier, DeviceRevisionStatus.VALID);
        newDeviceName.setNameId(UUID.randomUUID().toString());
        newDeviceName.setRequestedBy(userManager.getUser());
        newDeviceName.setProcessedBy(userManager.getUser());
        newDeviceName.setProcessDate(new Date());
        em.persist(newDeviceName);
        return newDeviceName;
    }

    /**
     * Creates a new version of the existing name based on the name received as
     * the parameter, but the new version is deleted.
     *
     * @param nameToDelete
     * @return
     */
    public DeviceRevision deleteDeviceName(DeviceRevision nameToDelete) {
        if (!userManager.isLoggedIn()) {
            throw new AccessControlException("You must be logged in to perform this action.");
        }

        Preconditions.checkNotNull(nameToDelete);

        if (nameToDelete.getStatus() == DeviceRevisionStatus.DELETED) {
            return nameToDelete;
        }

        // Superuser can delete anything. Editors only their own NC names
        if (!userManager.isSuperUser() && nameToDelete.getRequestedBy().getId().intValue() != userManager.getUser().getId().intValue()) {
            throw new AccessControlException("You are not authorized to ");
        }

        // make new revision
        DeviceRevision deletedName = new DeviceRevision(nameToDelete.getSection(), nameToDelete.getDeviceType(), nameToDelete.getQualifier(), DeviceRevisionStatus.DELETED);
        deletedName.setNameId(nameToDelete.getNameId());
        deletedName.setRequestedBy(userManager.getUser());
        deletedName.setProcessedBy(userManager.getUser());
        deletedName.setProcessDate(new Date());
        em.persist(deletedName);

        return deletedName;
    }

    public DeviceRevision modifyDeviceName(Integer sectionId, Integer deviceTypeId, Integer selectedDeviceNameId) {
        Preconditions.checkNotNull(sectionId);
        Preconditions.checkNotNull(deviceTypeId);
        Preconditions.checkNotNull(selectedDeviceNameId);

        final NamePartRevision section = em.find(NamePartRevision.class, sectionId);
        final NamePartRevision deviceType = em.find(NamePartRevision.class, deviceTypeId);
        final DeviceRevision selectedDeviceName = em.find(DeviceRevision.class, selectedDeviceNameId);

        final DeviceRevision returnName = createDeviceName(section, deviceType);
        returnName.setNameId(selectedDeviceName.getNameId());
        em.persist(returnName);

        return returnName;
    }

    private String getQualifier(long namesCount) {
        return (namesCount <= 0) ? "A" : "" + ((char) (namesCount % 26 + 'A')) + (namesCount / 26);
    }

    private long countDeviceNamesByRef(NamePartRevision section, NamePartRevision deviceType) {
        return em.createQuery("SELECT COUNT(n) FROM DeviceName n WHERE n.section = :section AND n.deviceType = :deviceType", Long.class)
                .setParameter("section", section).setParameter("deviceType", deviceType).getSingleResult().longValue();
    }

    public DeviceRevision findDeviceNameById(Integer id) {
        return em.createNamedQuery("DeviceName.findById", DeviceRevision.class).setParameter("id", id).getSingleResult();
    }

    public DeviceRevision findDeviceNameByReference(NamePartRevision section, NamePartRevision deviceTypeEvent, String qualifier) {
        Preconditions.checkNotNull(section);
        Preconditions.checkNotNull(deviceTypeEvent);
        Preconditions.checkNotNull(qualifier);

        return em.createNamedQuery("DeviceName.findByParts", DeviceRevision.class).setParameter("section", section).setParameter("deviceType", deviceTypeEvent).setParameter("qualifier", qualifier).getSingleResult();
    }

    /**
     * Gets all NC Names regardless of status - including deleted.
     *
     * @return
     */
    public List<DeviceRevision> getAllDeviceNames() {
        return em.createQuery("SELECT n FROM DeviceName n WHERE n.requestDate = (SELECT MAX(r.requestDate) FROM DeviceName r WHERE r.nameId = n.nameId) ORDER BY n.status, n.deviceType.id, n.section.id", DeviceRevision.class)
                .getResultList();
    }

    /**
     * Gets only the NC Names with status VALID or INVALID
     *
     * @return
     */
    public List<DeviceRevision> getExistingDeviceNames() {
        return em.createQuery("SELECT n FROM DeviceName n WHERE n.requestDate = (SELECT MAX(r.requestDate) FROM DeviceName r WHERE (r.nameId = n.nameId)) AND n.status != :status ORDER BY n.status, n.deviceType.id, n.section.id", DeviceRevision.class)
                .setParameter("status", DeviceRevisionStatus.DELETED).getResultList();
    }

    public List<DeviceRevision> getDeviceNameHistory(String deviceNameId) {
        return em.createQuery("SELECT n FROM DeviceName n WHERE n.nameId = :nameId", DeviceRevision.class).setParameter("nameId", deviceNameId).getResultList();
    }

    public List<DeviceRevision> getActiveNames() {
        return em.createQuery("SELECT n FROM DeviceName n WHERE n.requestDate = (SELECT MAX(r.requestDate) FROM DeviceName r WHERE (r.nameId = n.nameId) AND r.processDate IS NOT NULL) AND (n.status = :status) ORDER BY n.status, n.deviceType.id, n.section.id", DeviceRevision.class)
                .setParameter("status", DeviceRevisionStatus.VALID).getResultList();
    }

    public List<DeviceRevision> getDeviceNamesByStatus(DeviceRevisionStatus status) {
        return em.createNamedQuery("DeviceName.findByStatus", DeviceRevision.class).setParameter("status", status).getResultList();
    }

    public boolean isNameValid(DeviceRevision deviceName) {
        return deviceName.getStatus() == DeviceRevisionStatus.VALID;
    }

    /**
     * Sets the DeviceName indicated by the id to the valid state. The method does
     * not check if the DeviceName status is correct.
     *
     * @param id - the id of the DeviceName
     * @param modifierId - the id of the user making the change.
     * @return true if modification was successful.
     */
    public boolean setNameValid(Integer id, Integer modifierId) {
        final DeviceRevision dbName = findDeviceNameById(id);
        dbName.setStatus(DeviceRevisionStatus.VALID);
        setNameProcessed(dbName, modifierId);

        return true;
    }

    public boolean setNameProcessed(DeviceRevision nameToProcess, Integer modifierId) {
        Privilege modifier = em.find(Privilege.class, modifierId);
        Date currentDate = new Date();

        final DeviceRevision dbName;
        if (em.contains(nameToProcess)) {
            dbName = nameToProcess;
        } else {
            dbName = findDeviceNameById(nameToProcess.getId());
        }
        dbName.setProcessDate(currentDate);
        dbName.setProcessedBy(modifier);

        return true;
    }
}
