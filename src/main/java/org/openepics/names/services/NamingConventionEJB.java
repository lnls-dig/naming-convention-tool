package org.openepics.names.services;

import com.google.common.base.Preconditions;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.openepics.names.ui.UserManager;
import org.openepics.names.ui.NameCategories;
import org.openepics.names.model.DeviceName;
import org.openepics.names.model.NameStatus;
import org.openepics.names.model.NameCategory;
import org.openepics.names.model.NameEvent;
import org.openepics.names.model.NameEventStatus;
import org.openepics.names.model.Privilege;

@Stateless
public class NamingConventionEJB {

    private static final Logger logger = Logger.getLogger("org.openepics.names");

    private class NameSections {
        NameEvent section;
        NameEvent disciplineOrSubsection;
        String deviceName;
    }

    @Inject
    private UserManager userManager;

    @PersistenceContext(unitName = "org.openepics.names.punit")
    private EntityManager em;
    
    public DeviceName createDeviceName(NameEvent section, NameEvent deviceType, EssNameConstructionMethod method) {
        Preconditions.checkNotNull(section);
        Preconditions.checkNotNull(deviceType);

        if (!userManager.isLoggedIn()) {
            throw new AccessControlException("You must be logged in to perform the operation.");
        }

        final NameSections nameSections = getNameSections(section, deviceType, method);
        Preconditions.checkArgument(nameSections != null, "Unable to find section.");

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
            deletedName = new DeviceName(nameToDelete.getSection(), nameToDelete.getDeviceType(), nameToDelete.getInstanceIndex(), NameStatus.DELETED);
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

    private String calcDeviceNameString(NameSections nameSections, String deviceInstanceIndex) {
        return nameSections.section.getName() + "-" + nameSections.disciplineOrSubsection.getName() + ":" + nameSections.deviceName + "-" + deviceInstanceIndex;
    }

    public DeviceName modifyDeviceName(Integer sectionId, Integer deviceTypeId, Integer selectedDeviceNameId) {
        Preconditions.checkNotNull(sectionId);
        Preconditions.checkNotNull(deviceTypeId);
        Preconditions.checkNotNull(selectedDeviceNameId);

        final NameEvent section = em.find(NameEvent.class, sectionId);
        final NameEvent deviceType = em.find(NameEvent.class, deviceTypeId);
        final DeviceName selectedDeviceName = em.find(DeviceName.class, selectedDeviceNameId);

        final DeviceName returnName = createDeviceName(section, deviceType, EssNameConstructionMethod.ACCELERATOR);
        returnName.setNameId(selectedDeviceName.getNameId());
        em.persist(returnName);

        return returnName;
    }

    private String getDeviceInstanceIndex(long namesCount) {
        return (namesCount <= 0) ? "A" : "" + ((char) (namesCount % 26 + 'A')) + (namesCount / 26);
    }

    private boolean isDeviceInstanceIndexValid(NameEvent subsection, String deviceInstanceIndex) {
        if (!subsection.getName().substring(0, 2).equals(deviceInstanceIndex.substring(0, 2))) {
            return false;
        } else {
            return Pattern.matches("[A-Za-z]\\d{0,4}", deviceInstanceIndex.substring(2));
        }
    }

    private NameSections getNameSections(NameEvent subsection, NameEvent device, EssNameConstructionMethod method) {
        if (!((subsection.getStatus() == NameEventStatus.APPROVED)
                && subsection.getNameCategory().getName().equals(NameCategories.subsection())
                && (device.getStatus() == NameEventStatus.APPROVED) && (device.getNameCategory().getName()
                .equals(NameCategories.genericDevice()) || device.getNameCategory().getName()
                .equals(NameCategories.specificDevice())))) {
            return null;
        }

        NameSections nameSections = new NameSections();

        NameEvent section;
        NameEvent genDevice;

        switch (method) {
            case ACCELERATOR:
                // find the correct section name
                section = subsection;
                while (!section.getNameCategory().getName().equals(NameCategories.section())) {
                    if (section.getParentName() == null) {
                        return null; // validation failed
                    }
                    section = section.getParentName();
                }
                nameSections.section = section;

                // find the correct discipline based on the device
                NameEvent discipline = device;
                while (!discipline.getNameCategory().getName().equals(NameCategories.discipline())) {
                    if (discipline.getParentName() == null) {
                        return null; // validation failed
                    }
                    discipline = discipline.getParentName();
                }
                nameSections.disciplineOrSubsection = discipline;

                // find the appropriate generic device name
                genDevice = device;
                while (!genDevice.getNameCategory().getName().equals(NameCategories.genericDevice())) {
                    if (genDevice.getParentName() == null) {
                        return null; // validation failed
                    }
                    genDevice = genDevice.getParentName();
                }
                nameSections.deviceName = genDevice.getName();
                break;
            case TARGET:
                section = subsection;
                while (!section.getNameCategory().getName().equals(NameCategories.section())) {
                    if (section.getParentName() == null) {
                        return null; // validation failed
                    }
                    section = section.getParentName();
                }
                nameSections.section = section;

                nameSections.disciplineOrSubsection = subsection;

                // find the appropriate generic device name
                genDevice = device;
                while (!genDevice.getNameCategory().getName().equals(NameCategories.genericDevice())) {
                    if (genDevice.getParentName() == null) {
                        return null; // validation failed
                    }
                    genDevice = genDevice.getParentName();
                }
                nameSections.deviceName = genDevice.getName();
                break;
            default:
                // all possible enum values taken care off
                return null;

        }

        return nameSections;
    }

    private long countDeviceNamesByRef(NameEvent section, NameEvent deviceType) {
        return em.createQuery("SELECT COUNT(n) FROM DeviceName n WHERE n.section = :section AND n.deviceType = :deviceType", Long.class)
                .setParameter("section", section).setParameter("deviceType", deviceType).getSingleResult().longValue();
    }

    public DeviceName findDeviceNameById(Integer id) {
        return em.createNamedQuery("DeviceName.findById", DeviceName.class).setParameter("id", id).getSingleResult();
    }

    public DeviceName findDeviceNameByReference(NameEvent section, NameEvent deviceTypeEvent, String instanceIndex) {
        Preconditions.checkNotNull(section);
        Preconditions.checkNotNull(deviceTypeEvent);
        Preconditions.checkNotNull(instanceIndex);

        return em.createNamedQuery("DeviceName.findByParts", DeviceName.class).setParameter("section", section).setParameter("deviceType", deviceTypeEvent).setParameter("instanceIndex", instanceIndex).getSingleResult();
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

    public boolean isNameValid(DeviceName deviceName) throws NamingConventionException {
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

    /**
     * Checks whether the name is composed of the actual active name parts and
     * this conforms to the naming convention. This can also be called for names
     * that have not been defined yet.
     *
     * @param deviceName
     * @return
     * @throws NamingConventionException
     */
    public boolean isNameValid(String deviceName) throws NamingConventionException {
        String[] majorParts = deviceName.split(":");

        if (majorParts.length < 2) {
            return false;
        }

        int dashIndex = majorParts[0].indexOf('-');
        // section at least one character and not all of the string
        if ((dashIndex < 1) || (dashIndex >= majorParts[0].length() - 1)) {
            return false;
        }
        String sectionName = majorParts[0].substring(0, dashIndex);
        String disciplineName = majorParts[0].substring(dashIndex + 1);

        dashIndex = majorParts[1].indexOf('-');
        if ((dashIndex < 1) || (dashIndex >= majorParts[0].length() - 1)) {
            return false;
        }
        String deviceTypeName = majorParts[1].substring(0, dashIndex);
        String deviceQntf = majorParts[1].substring(dashIndex + 1);

        // checking whether section exists, is it approved and does its category
        // equals SECT
        TypedQuery<NameEvent> sectionQ = em.createNamedQuery("NameEvent.findByName", NameEvent.class);
        sectionQ.setParameter("name", sectionName);
        NameEvent section = sectionQ.getSingleResult();
        if ((section.getStatus() != NameEventStatus.APPROVED) || !section.getNameCategory().getName().equals(NameCategories.section())) {
            return false;
        }

        // checking whether discipline exists, is it approved and does its
        // category equals DSCP
        TypedQuery<NameEvent> disciplineQ = em.createNamedQuery("NameEvent.findByName", NameEvent.class);
        disciplineQ.setParameter("name", disciplineName);
        NameEvent discipline = disciplineQ.getSingleResult();
        EssNameConstructionMethod method;
        if (discipline.getStatus() != NameEventStatus.APPROVED) {
            return false;
        } else {
            if (discipline.getNameCategory().getName().equals(NameCategories.discipline())) {
                method = EssNameConstructionMethod.ACCELERATOR;
            } else if (discipline.getNameCategory().getName().equals(NameCategories.subsection())) {
                method = EssNameConstructionMethod.TARGET;
            } else {
                return false;
            }
        }

        // checking whether device exists, is it approved and does its
        // category equals GDEV
        TypedQuery<NameEvent> deviceQ = em.createNamedQuery("NameEvent.findByName", NameEvent.class);
        deviceQ.setParameter("name", deviceTypeName);
        NameEvent genDevice = deviceQ.getSingleResult();
        if ((genDevice.getStatus() != NameEventStatus.APPROVED)
                || !genDevice.getNameCategory().getName().equals(NameCategories.genericDevice())) {
            return false;
        }

        if (method == EssNameConstructionMethod.ACCELERATOR && !isDeviceInstanceIndexValid(discipline, deviceQntf)) {
            return false;
        }

        return true;
    }

    public boolean isNamePartValid(NameEvent namePart) throws NamingConventionException {
        return namePart.getStatus() == NameEventStatus.APPROVED;
    }

    public boolean isNamePartValid(String namePart, NameCategory category) throws NamingConventionException {
        try {
            TypedQuery<NameEvent> query = em.createNamedQuery("NameEvent.findByName", NameEvent.class).setParameter("name",
                    namePart);
            NameEvent nameEvent = query.getSingleResult();
            return (nameEvent.getStatus() == NameEventStatus.APPROVED) && (nameEvent.getNameCategory().equals(category));
        } catch (NoResultException e) {
	    // if the names does not exist, check whether similar names exist
            // according to business logic

            // but first check whether the static rules apply
            // BLED-NAM-032
            for (int i = 0; i < namePart.length(); i++) {
                char c = namePart.charAt(i);
                if (!(c >= '0' && c <= '9') && !(c >= 'a' && c <= 'z') && !(c >= 'A' && c <= 'Z')) {
                    return false;
                }
            }

            // BLED-NAM-034
            if (category.getName().equals(NameCategories.section()) || category.getName().equals(NameCategories.discipline()) || category.getName().equals(NameCategories.genericDevice()) || category.getName().equals(NameCategories.specificDevice())) {
                char c = namePart.charAt(0);
                if (!(c >= 'a' && c <= 'z') && !(c >= 'A' && c <= 'Z')) {
                    return false;
                }
            }

            // now determine the categories for which the similarity must be
            // checked
            TypedQuery<NameCategory> catQuery = em.createNamedQuery("NameCategory.findByName", NameCategory.class);

            List<NameCategory> categories = new ArrayList<NameCategory>();
            if (category.getName().equals(NameCategories.section())
                    || category.getName().equals(NameCategories.discipline())
                    || category.getName().equals(NameCategories.specificDevice())) {
                catQuery.setParameter("name", NameCategories.section());
                categories.add(catQuery.getSingleResult());
                catQuery.setParameter("name", NameCategories.discipline());
                categories.add(catQuery.getSingleResult());
                catQuery.setParameter("name", NameCategories.specificDevice());
                categories.add(catQuery.getSingleResult());
            } else if (category.getName().equals(NameCategories.subsection())
                    || category.getName().equals(NameCategories.genericDevice())) {
                catQuery.setParameter("name", NameCategories.section());
                categories.add(catQuery.getSingleResult());
                catQuery.setParameter("name", NameCategories.discipline());
                categories.add(catQuery.getSingleResult());
            } else {
                categories.add(category);
            }

            // build the list of similar names
            List<String> alts = generateNameAlternatives(namePart);
            if (alts == null) {
                return false;
            }
            TypedQuery<NameEvent> similarQuery = em.createQuery(
                    "SELECT n FROM NameEvent n WHERE UPPER(n.name) IN :alternatives AND n.nameCategory IN :nameCategories",
                    NameEvent.class);
            similarQuery.setParameter("alternatives", alts).setParameter("nameCategories", categories);
            List<NameEvent> similarNames = similarQuery.getResultList();
            return !(similarNames.size() > 0);
        } catch (NonUniqueResultException e) {
            return false;
        }

    }

    /**
     * Generates name alternatives according to ESS business logic all in UPPER
     * CASE.
     *
     * @param name - the name to generate alternatives for
     * @return
     */
    private List<String> generateNameAlternatives(String name) {
        List<String> results = new ArrayList<String>();
        if (name == null || name.isEmpty()) {
            return null;
        }

        String upName = name.toUpperCase();

        boolean followZero = false;
        char c = upName.charAt(0);
        if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z'))) {
            return null;
        }
        addAlternatives(results, "", upName.charAt(0));

        for (int i = 1; i < upName.length(); i++) {
            List<String> newResults = new ArrayList<String>();
            c = upName.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z'))) {
                return null;
            }
            followZero = (c >= 'A' && c <= 'Z') || (followZero && c == '0');
            for (String prefix : results) {
                addAlternatives(newResults, prefix, c);
                if (followZero && c == '0') {
                    newResults.add(prefix);
                }
            }
            results = newResults;
        }

        return results;
    }

    private void addAlternatives(List<String> prefixes, String prefix, char c) {
        switch (c) {
            case '0':
            case 'O':
                prefixes.add(prefix + 'O');
                prefixes.add(prefix + '0');
                break;
            case 'V':
            case 'W':
                prefixes.add(prefix + 'V');
                prefixes.add(prefix + 'W');
                break;
            case 'I':
            case '1':
            case 'L':
                prefixes.add(prefix + 'I');
                prefixes.add(prefix + '1');
                prefixes.add(prefix + 'L');
                break;
            default:
                prefixes.add(prefix + c);
                break;
        }
    }

}
