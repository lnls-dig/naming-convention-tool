package org.openepics.names.services;

import java.security.AccessControlException;
import java.security.InvalidParameterException;
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
import org.openepics.names.model.NcName;
import org.openepics.names.model.NcNameStatus;
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

	/**
	 * @param subsection
	 *            For accelerator and target: must be a SUBSECTION
	 * 
	 * @param device
	 *            can be either GDEV or SDEV
	 * 
	 * @param deviceInstanceIndex
	 *            this is selecting or determining the actual device instance
	 *            that the signal is belonging to.
	 * 
	 * @param signal
	 *            We are adding a new signal to the existing device or reserving
	 *            a signal name for a device which does not exist yet.
	 * 
	 * @param method
	 * @return - returns null if this would not construct a valid device name.
	 *         Example incorrect entities were used for section, device or
	 *         signal
	 */
	public NcName createNcNameSignal(NameEvent subsection, NameEvent device, String deviceInstanceIndex, NameEvent signal,
			EssNameConstructionMethod method) {
		if (subsection == null || device == null || deviceInstanceIndex == null || signal == null)
			throw new InvalidParameterException("Subsection or device not specified.");

        if (!userManager.isLoggedIn()) 
            throw new AccessControlException("You must be logged in to perform the operation.");
        
		NameSections nameSections = getNameSections(subsection, device, method);
		if (nameSections == null)
			throw new InvalidParameterException("Unable to find section.");

		if (method == EssNameConstructionMethod.ACCELERATOR && !isDeviceInstanceIndexValid(subsection, deviceInstanceIndex))
			throw new InvalidParameterException("Device instance index invalid.");

		NcName newNcName = new NcName(subsection, device, signal, deviceInstanceIndex, 
                calcNcNameSignal(nameSections, deviceInstanceIndex, validateSignalName(signal)), NcNameStatus.VALID);
        newNcName.setNameId(UUID.randomUUID().toString());
        newNcName.setRequestedBy(userManager.getUser());
        newNcName.setProcessedBy(userManager.getUser());
        newNcName.setProcessDate(new Date());
		em.persist(newNcName);
		return newNcName;
	}

	/**
     * Creates a new version of the existing name based on the name received as the parameter, but the new version is deleted.
     * @param nameToDelete
     * @return 
     */
    public NcName deleteNcName(NcName nameToDelete) {
        if(!userManager.isLoggedIn())
            throw new AccessControlException("You must be logged in to perform this action.");
        if(nameToDelete == null)
            throw new InvalidParameterException("Parameter nameToDelete must not be null");
        if(nameToDelete.getSection() == null || nameToDelete.getDiscipline() == null)
            throw new InvalidParameterException("The Entity NcName has invalid menmonic references.");
        if(nameToDelete.getInstanceIndex() == null || nameToDelete.getInstanceIndex().isEmpty())
            throw new InvalidParameterException("Invalid instance index.");
        if(nameToDelete.getName() == null || nameToDelete.getName().isEmpty())
            throw new InvalidParameterException("No NC name specified.");
        
        if(nameToDelete.getStatus() == NcNameStatus.DELETED)
            return nameToDelete;
        
        // Superuser can delete anything. Editors only their own NC names
        if(!userManager.isSuperUser() && 
                nameToDelete.getRequestedBy().getId().intValue() != userManager.getUser().getId().intValue())
            throw new AccessControlException("You are not authorized to ");
        
        NcName deletedName = null;
        if(nameToDelete.getStatus() == NcNameStatus.VALID) {
            // make new revision
            deletedName = new NcName(nameToDelete.getSection(), nameToDelete.getDiscipline(), nameToDelete.getSignal(),
                    nameToDelete.getInstanceIndex(), nameToDelete.getName(), NcNameStatus.DELETED);
            deletedName.setNameId(nameToDelete.getNameId());
            deletedName.setRequestedBy(userManager.getUser());
            deletedName.setProcessedBy(userManager.getUser());
            deletedName.setProcessDate(new Date());
            em.persist(deletedName);
        } else {
            // INVALID. Remove from database.
            deletedName = em.find(NcName.class, nameToDelete.getId());
            em.remove(deletedName);
        }
        
        return deletedName;
    }
    
	/**
	 * @param subsection
	 *            - For accelerator and target: must be a SUBSECTION
	 * 
	 * @param device
	 *            - can be either GDEV or SDEV
	 * @param method
	 * @return - returns null if this would not construct a valid device name.
	 *         Example incorrect entities were used for section, device or
	 *         signal
	 */
	public NcName createNcNameDevice(NameEvent subsection, NameEvent device,
			EssNameConstructionMethod method) {
		if (subsection == null || device == null)
			throw new InvalidParameterException("Subsection or device not specified.");

        if (!userManager.isLoggedIn()) 
            throw new AccessControlException("You must be logged in to perform the operation.");
        
		NameSections nameSections = getNameSections(subsection, device, method);
		if (nameSections == null)
			throw new InvalidParameterException("Unable to find section.");

		long deviceInstances = countNcNamesByRef(subsection, device);
		String deviceInstanceIndex = subsection.getName().substring(0, 2) + getDeviceInstanceIndex(deviceInstances);
        
        logger.info("Creating NcName device");
		NcName newNcName = new NcName(subsection, device, null, deviceInstanceIndex, 
                calcNcNameDevice(nameSections, deviceInstanceIndex), NcNameStatus.VALID);
        newNcName.setNameId(UUID.randomUUID().toString());
        newNcName.setRequestedBy(userManager.getUser());
        newNcName.setProcessedBy(userManager.getUser());
        newNcName.setProcessDate(new Date());
		em.persist(newNcName);
		return newNcName;
	}

    private String calcNcNameDevice(NameSections nameSections, String deviceInstanceIndex) {
        return nameSections.section.getName() + "-" + nameSections.disciplineOrSubsection.getName() +
                ":" + nameSections.deviceName + "-" + deviceInstanceIndex;
    }
    
    private String calcNcNameSignal(NameSections nameSections, String deviceInstanceIndex, String signal) {
        return nameSections.section.getName() + "-" + nameSections.disciplineOrSubsection.getName() + 
                ":" + nameSections.deviceName + "-" + deviceInstanceIndex + ":" + signal;
    }
    
    public NcName modifyNcName(Integer subsectionId, Integer genDeviceId, Integer selectedNcNameId) {
		if (subsectionId == null || genDeviceId == null || selectedNcNameId == null) 
            throw new InvalidParameterException("Error in selected name.");
        
        NcName returnName;
        NcName selectedNcName = em.find(NcName.class, selectedNcNameId);
		NameEvent subsection = em.find(NameEvent.class, subsectionId);
		NameEvent genDevice = em.find(NameEvent.class, genDeviceId);
        
        // TODO: fill drop-down menus with correct values.
        
        // TODO: Handle correct construction method
        returnName = createNcNameDevice(subsection, genDevice, EssNameConstructionMethod.ACCELERATOR);
        returnName.setNameId(selectedNcName.getNameId());
        em.persist(returnName);
        
        return returnName;
    }
    
	private String getDeviceInstanceIndex(long namesCount) {
		if (namesCount <= 0)
			return "A";
		return "" + ((char) (namesCount % 26 + 'A')) + (namesCount / 26);
	}

	private boolean isDeviceInstanceIndexValid(NameEvent subsection, String deviceInstanceIndex) {
		if (!subsection.getName().substring(0, 2).equals(deviceInstanceIndex.substring(0, 2)))
			return false;

		return Pattern.matches("[A-Za-z]\\d{0,4}", deviceInstanceIndex.substring(2));
	}

	private NameSections getNameSections(NameEvent subsection, NameEvent device,
			EssNameConstructionMethod method) {

		if (!((subsection.getStatus() == NameEventStatus.APPROVED)
				&& subsection.getNameCategory().getName().equals(NameCategories.subsection())
				&& (device.getStatus() == NameEventStatus.APPROVED) && (device.getNameCategory().getName()
				.equals(NameCategories.genericDevice()) || device.getNameCategory().getName()
				.equals(NameCategories.specificDevice()))))
			return null;

		NameSections nameSections = new NameSections();

		NameEvent section;
		NameEvent genDevice;

		switch (method) {
		case ACCELERATOR:
			// find the correct section name
			section = subsection;
			while (!section.getNameCategory().getName().equals(NameCategories.section())) {
				if (section.getParentName() == null)
					return null; // validation failed
				section = section.getParentName();
			}
			nameSections.section = section;

			// find the correct discipline based on the device
			NameEvent discipline = device;
			while (!discipline.getNameCategory().getName().equals(NameCategories.discipline())) {
				if (discipline.getParentName() == null)
					return null; // validation failed
				discipline = discipline.getParentName();
			}
			nameSections.disciplineOrSubsection = discipline;

			// find the appropriate generic device name
			genDevice = device;
			while (!genDevice.getNameCategory().getName().equals(NameCategories.genericDevice())) {
				if (genDevice.getParentName() == null)
					return null; // validation failed
				genDevice = genDevice.getParentName();
			}
			nameSections.deviceName = genDevice.getName();
			break;
		case TARGET:
			section = subsection;
			while (!section.getNameCategory().getName().equals(NameCategories.section())) {
				if (section.getParentName() == null)
					return null; // validation failed
				section = section.getParentName();
			}
			nameSections.section = section;

			nameSections.disciplineOrSubsection = subsection;

			// find the appropriate generic device name
			genDevice = device;
			while (!genDevice.getNameCategory().getName().equals(NameCategories.genericDevice())) {
				if (genDevice.getParentName() == null)
					return null; // validation failed
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

	private long countNcNamesByRef(NameEvent subsection, NameEvent device) {
		//@formatter:off
		TypedQuery<Long> query = em.createQuery("SELECT COUNT(n) FROM NcName n " + 
														"WHERE n.section = :section " +
														"AND n.discipline = :device ", 
														Long.class);
		// @formatter:on
		query.setParameter("section", subsection).setParameter("device", device);
		return query.getSingleResult().longValue();
	}

	private String validateSignalName(NameEvent signal) {
		if (signal == null)
			return null;
		return signal.getName();
	}

	public NcName findNcNameById(Integer id) {
		TypedQuery<NcName> query = em.createNamedQuery("NcName.findById", NcName.class).setParameter("id", id);
		return query.getSingleResult();
	}

	public NcName findNcNameByName(String name) {
		TypedQuery<NcName> query = em.createNamedQuery("NcName.findByName", NcName.class).setParameter("name", name);
		return query.getSingleResult();
	}

	// instance index cannot be null.
	public NcName findNcNameByReference(NameEvent section, NameEvent device, String instanceIndex, NameEvent signal) {
		if (section == null)
			throw new IllegalArgumentException("section is null");
		if (device == null)
			throw new IllegalArgumentException("device is null");
		if (instanceIndex == null)
			throw new IllegalArgumentException("device quantifier is null");

		TypedQuery<NcName> query = em.createNamedQuery("NcName.findByParts", NcName.class).setParameter("section", section)
				.setParameter("device", device).setParameter("signal", signal).setParameter("instanceIndex", instanceIndex);

		return query.getSingleResult();
	}

	/**
     * Gets all NC Names regardless of status - including deleted.
     * @return 
     */
	public List<NcName> getAllNcNames() {
		List<NcName> ncNames;

		TypedQuery<NcName> query = em.createQuery(
							"SELECT n FROM NcName n WHERE n.requestDate = "
                                    + "(SELECT MAX(r.requestDate) FROM NcName r WHERE r.nameId = n.nameId) "
                                    + "ORDER BY n.status, n.discipline.id, n.section.id, n.name",
							NcName.class);
		ncNames = query.getResultList();
		logger.info("Total number of unique NcNames: " + ncNames.size());

		return ncNames;
	}
    
	/**
     * Gets only the NC Names with status VALID or INVALID
     * @return 
     */
    public List<NcName> getExistingNcNames() {
		List<NcName> ncNames;
        
		TypedQuery<NcName> query;
        query = em.createQuery(
                "SELECT n FROM NcName n WHERE n.requestDate = "
                        + "(SELECT MAX(r.requestDate) FROM NcName r WHERE (r.nameId = n.nameId))"
                        + " AND n.status != :status "
                        + "ORDER BY n.status, n.discipline.id, n.section.id, n.name",
                NcName.class).setParameter("status", NcNameStatus.DELETED);
		ncNames = query.getResultList();
        return ncNames;
    }

    public List<NcName> getNcNameHistory(String ncNameId) {
		List<NcName> ncNames;
        
		TypedQuery<NcName> query;
        query = em.createQuery("SELECT n FROM NcName n WHERE n.nameId = :nameId", NcName.class).
                setParameter("nameId", ncNameId);
		ncNames = query.getResultList();
        return ncNames;
        
    }
    
	public List<NcName> getActiveNames() {
		List<NcName> ncNames;
        
		TypedQuery<NcName> query;
        query = em.createQuery(
                "SELECT n FROM NcName n WHERE n.requestDate = "
                        + "(SELECT MAX(r.requestDate) FROM NcName r WHERE (r.nameId = n.nameId) AND r.processDate IS NOT NULL) "
                        + "AND (n.status = :status) "
                        + "ORDER BY n.status, n.discipline.id, n.section.id, n.name",
                NcName.class).setParameter("status", NcNameStatus.VALID);
		ncNames = query.getResultList();
        return ncNames;
	}

	public List<NcName> getNcNamesByStatus(NcNameStatus status) {
		List<NcName> names;

		TypedQuery<NcName> query = em.createNamedQuery("NcName.findByStatus", NcName.class).setParameter("status", status);
		names = query.getResultList();
		// logger.log(Level.INFO, "Total number of categories: " + cats.size());

		return names;
	}

	public boolean isNameValid(NcName ncName) throws NamingConventionException {
		return ncName.getStatus() == NcNameStatus.VALID;
	}
    
	/**
     * Sets the NcName indicated by the id to the valid state. The method does not check if the NcName status is correct.
     * @param id - the id of the NcName
     * @param modifierId - the id of the user making the change.
     * @return true if modification was successful.
     */
    public boolean setNameValid(Integer id, Integer modifierId) {
        NcName dbName = findNcNameById(id);
        dbName.setStatus(NcNameStatus.VALID);
        setNameProcessed(dbName, modifierId);
        
        return true;
    }
    
    public boolean setNameProcessed(NcName nameToProcess, Integer modifierId) {
        NcName dbName;
        
        Privilege modifier = em.find(Privilege.class, modifierId);
        Date currentDate = new Date();
        if(em.contains(nameToProcess))
            dbName = nameToProcess;
        else
            dbName = findNcNameById(nameToProcess.getId());
        dbName.setProcessDate(currentDate);
        dbName.setProcessedBy(modifier);
        
        return true;
    }

	/**
	 * Checks whether the name is composed of the actual active name parts and
	 * this conforms to the naming convention. This can also be called for names
	 * that have not been defined yet.
	 * 
	 * @param ncName
	 * @return
	 * @throws NamingConventionException
	 */
	public boolean isNameValid(String ncName) throws NamingConventionException {
		String[] majorParts = ncName.split(":");

		if (majorParts.length < 2)
			return false;

		int dashIndex = majorParts[0].indexOf('-');
		// section at least one character and not all of the string
		if ((dashIndex < 1) || (dashIndex >= majorParts[0].length() - 1))
			return false;
		String sectionName = majorParts[0].substring(0, dashIndex);
		String disciplineName = majorParts[0].substring(dashIndex + 1);

		dashIndex = majorParts[1].indexOf('-');
		if ((dashIndex < 1) || (dashIndex >= majorParts[0].length() - 1))
			return false;
		String deviceName = majorParts[1].substring(0, dashIndex);
		String deviceQntf = majorParts[1].substring(dashIndex + 1);

		// checking whether section exists, is it approved and does its category
		// equals SECT
		TypedQuery<NameEvent> sectionQ = em.createNamedQuery("NameEvent.findByName", NameEvent.class);
		sectionQ.setParameter("name", sectionName);
		NameEvent section = sectionQ.getSingleResult();
		if ((section.getStatus() != NameEventStatus.APPROVED) || !section.getNameCategory().getName().equals(NameCategories.section()))
			return false;

		// checking whether discipline exists, is it approved and does its
		// category equals DSCP
		TypedQuery<NameEvent> disciplineQ = em.createNamedQuery("NameEvent.findByName", NameEvent.class);
		disciplineQ.setParameter("name", disciplineName);
		NameEvent discipline = disciplineQ.getSingleResult();
		EssNameConstructionMethod method;
		if (discipline.getStatus() != NameEventStatus.APPROVED)
			return false;
		else {
			if (discipline.getNameCategory().getName().equals(NameCategories.discipline()))
				method = EssNameConstructionMethod.ACCELERATOR;
			else if (discipline.getNameCategory().getName().equals(NameCategories.subsection()))
				method = EssNameConstructionMethod.TARGET;
			else
				return false;
		}

		// checking whether device exists, is it approved and does its
		// category equals GDEV
		TypedQuery<NameEvent> deviceQ = em.createNamedQuery("NameEvent.findByName", NameEvent.class);
		deviceQ.setParameter("name", deviceName);
		NameEvent genDevice = deviceQ.getSingleResult();
		if ((genDevice.getStatus() != NameEventStatus.APPROVED)
				|| !genDevice.getNameCategory().getName().equals(NameCategories.genericDevice()))
			return false;

		if (method == EssNameConstructionMethod.ACCELERATOR && !isDeviceInstanceIndexValid(discipline, deviceQntf))
			return false;

		// TODO insert signal verification here once it is defined. For now
		// reaching this point means name is valid.
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
				if (!(c >= '0' && c <= '9') && !(c >= 'a' && c <= 'z') && !(c >= 'A' && c <= 'Z'))
					return false;
			}

			// BLED-NAM-034
			if (category.getName().equals(NameCategories.section())
					||
					// TODO D-type subsection missing
					category.getName().equals(NameCategories.discipline())
					|| category.getName().equals(NameCategories.genericDevice())
					|| category.getName().equals(NameCategories.specificDevice()) ||
					// TODO A-type section qualifier
					category.getName().equals(NameCategories.signalType())) {
				char c = namePart.charAt(0);
				if (!(c >= 'a' && c <= 'z') && !(c >= 'A' && c <= 'Z'))
					return false;
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
			if (alts == null)
				return false;
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
	 * @param name
	 *            - the name to generate alternatives for
	 * @return
	 */
	private List<String> generateNameAlternatives(String name) {
		List<String> results = new ArrayList<String>();
		if (name == null || name.isEmpty())
			return null;

		String upName = name.toUpperCase();

		boolean followZero = false;
		char c = upName.charAt(0);
		if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z')))
			return null;
		addAlternatives(results, "", upName.charAt(0));

		for (int i = 1; i < upName.length(); i++) {
			List<String> newResults = new ArrayList<String>();
			c = upName.charAt(i);
			if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z')))
				return null;
			followZero = (c >= 'A' && c <= 'Z') || (followZero && c == '0');
			for (String prefix : results) {
				addAlternatives(newResults, prefix, c);
				if (followZero && c == '0')
					newResults.add(prefix);
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
