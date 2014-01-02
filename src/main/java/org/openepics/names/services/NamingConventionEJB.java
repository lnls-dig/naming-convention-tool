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
import org.openepics.names.model.DeviceName;
import org.openepics.names.model.NameEvent;
import org.openepics.names.model.NameStatus;
import org.openepics.names.model.Privilege;
import org.openepics.names.ui.UserManager;

@Stateless
public class NamingConventionEJB {

    private static final Logger logger = Logger.getLogger("org.openepics.names");

    @Inject
    private UserManager userManager;

    @PersistenceContext(unitName = "org.openepics.names.punit")
    private EntityManager em;

    public DeviceName createDeviceName(NameEvent section, NameEvent deviceType) {
        Preconditions.checkNotNull(section);
        Preconditions.checkNotNull(deviceType);

        if (!userManager.isLoggedIn()) {
            throw new AccessControlException("You must be logged in to perform the operation.");
        }

        final long deviceInstances = countDeviceNamesByRef(section, deviceType);
        final String deviceInstanceIndex = section.getName().substring(0, 2) + getDeviceInstanceIndex(deviceInstances);

        final DeviceName newDeviceName = new DeviceName(section, deviceType, deviceInstanceIndex, NameStatus.VALID);
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
    public DeviceName deleteDeviceName(DeviceName nameToDelete) {
        if (!userManager.isLoggedIn()) {
            throw new AccessControlException("You must be logged in to perform this action.");
        }

        Preconditions.checkNotNull(nameToDelete);

        if (nameToDelete.getStatus() == NameStatus.DELETED) {
            return nameToDelete;
        }

        // Superuser can delete anything. Editors only their own NC names
        if (!userManager.isSuperUser() && nameToDelete.getRequestedBy().getId().intValue() != userManager.getUser().getId().intValue()) {
            throw new AccessControlException("You are not authorized to ");
        }

        DeviceName deletedName;
        if (nameToDelete.getStatus() == NameStatus.VALID) {
            // make new revision
            deletedName = new DeviceName(nameToDelete.getSection(), nameToDelete.getDeviceType(), nameToDelete.getQualifier(), NameStatus.DELETED);
            deletedName.setNameId(nameToDelete.getNameId());
            deletedName.setRequestedBy(userManager.getUser());
            deletedName.setProcessedBy(userManager.getUser());
            deletedName.setProcessDate(new Date());
            em.persist(deletedName);
        } else {
            // INVALID. Remove from database.
            deletedName = em.find(DeviceName.class, nameToDelete.getId());
            em.remove(deletedName);
        }

        return deletedName;
    }

    public DeviceName modifyDeviceName(Integer sectionId, Integer deviceTypeId, Integer selectedDeviceNameId) {
        Preconditions.checkNotNull(sectionId);
        Preconditions.checkNotNull(deviceTypeId);
        Preconditions.checkNotNull(selectedDeviceNameId);

        final NameEvent section = em.find(NameEvent.class, sectionId);
        final NameEvent deviceType = em.find(NameEvent.class, deviceTypeId);
        final DeviceName selectedDeviceName = em.find(DeviceName.class, selectedDeviceNameId);

        final DeviceName returnName = createDeviceName(section, deviceType);
        returnName.setNameId(selectedDeviceName.getNameId());
        em.persist(returnName);

        return returnName;
    }

    private String getDeviceInstanceIndex(long namesCount) {
        return (namesCount <= 0) ? "A" : "" + ((char) (namesCount % 26 + 'A')) + (namesCount / 26);
    }

    private long countDeviceNamesByRef(NameEvent section, NameEvent deviceType) {
        return em.createQuery("SELECT COUNT(n) FROM DeviceName n WHERE n.section = :section AND n.deviceType = :deviceType", Long.class)
                .setParameter("section", section).setParameter("deviceType", deviceType).getSingleResult().longValue();
    }

    public DeviceName findDeviceNameById(Integer id) {
        return em.createNamedQuery("DeviceName.findById", DeviceName.class).setParameter("id", id).getSingleResult();
    }

    public DeviceName findDeviceNameByReference(NameEvent section, NameEvent deviceTypeEvent, String qualifier) {
        Preconditions.checkNotNull(section);
        Preconditions.checkNotNull(deviceTypeEvent);
        Preconditions.checkNotNull(qualifier);

        return em.createNamedQuery("DeviceName.findByParts", DeviceName.class).setParameter("section", section).setParameter("deviceType", deviceTypeEvent).setParameter("qualifier", qualifier).getSingleResult();
    }

    /**
     * Gets all NC Names regardless of status - including deleted.
     *
     * @return
     */
    public List<DeviceName> getAllDeviceNames() {
        return em.createQuery("SELECT n FROM DeviceName n WHERE n.requestDate = (SELECT MAX(r.requestDate) FROM DeviceName r WHERE r.nameId = n.nameId) ORDER BY n.status, n.deviceType.id, n.section.id", DeviceName.class)
                .getResultList();
    }

    /**
     * Gets only the NC Names with status VALID or INVALID
     *
     * @return
     */
    public List<DeviceName> getExistingDeviceNames() {
        return em.createQuery("SELECT n FROM DeviceName n WHERE n.requestDate = (SELECT MAX(r.requestDate) FROM DeviceName r WHERE (r.nameId = n.nameId)) AND n.status != :status ORDER BY n.status, n.deviceType.id, n.section.id", DeviceName.class)
                .setParameter("status", NameStatus.DELETED).getResultList();
    }

    public List<DeviceName> getDeviceNameHistory(String deviceNameId) {
        return em.createQuery("SELECT n FROM DeviceName n WHERE n.nameId = :nameId", DeviceName.class).setParameter("nameId", deviceNameId).getResultList();
    }

    public List<DeviceName> getActiveNames() {
        return em.createQuery("SELECT n FROM DeviceName n WHERE n.requestDate = (SELECT MAX(r.requestDate) FROM DeviceName r WHERE (r.nameId = n.nameId) AND r.processDate IS NOT NULL) AND (n.status = :status) ORDER BY n.status, n.deviceType.id, n.section.id", DeviceName.class)
                .setParameter("status", NameStatus.VALID).getResultList();
    }

    public List<DeviceName> getDeviceNamesByStatus(NameStatus status) {
        return em.createNamedQuery("DeviceName.findByStatus", DeviceName.class).setParameter("status", status).getResultList();
    }

    public boolean isNameValid(DeviceName deviceName) {
        return deviceName.getStatus() == NameStatus.VALID;
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
        final DeviceName dbName = findDeviceNameById(id);
        dbName.setStatus(NameStatus.VALID);
        setNameProcessed(dbName, modifierId);

        return true;
    }

    public boolean setNameProcessed(DeviceName nameToProcess, Integer modifierId) {
        Privilege modifier = em.find(Privilege.class, modifierId);
        Date currentDate = new Date();

        final DeviceName dbName;
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
