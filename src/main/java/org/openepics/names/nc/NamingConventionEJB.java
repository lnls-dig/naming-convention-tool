package org.openepics.names.nc;

import java.util.List;
import java.util.regex.Pattern;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.openepics.names.model.NCName;
import org.openepics.names.model.NCName.NCNameStatus;
import org.openepics.names.model.NameCategory;
import org.openepics.names.model.NameEvent;

@Stateless
public class NamingConventionEJB implements NamingConventionEJBLocal {

	private class NameSections {
		NameEvent section;
		NameEvent disciplineOrSubsection;
		String deviceName;
	}

	@PersistenceContext(unitName = "org.openepics.names.punit")
	private EntityManager em;

	@Override
	public NCName createNCNameSignal(NameEvent subsection, NameEvent device, String deviceInstanceIndex, NameEvent signal,
			ESSNameConstructionMethod method) {
		if (subsection == null || device == null || deviceInstanceIndex == null || signal == null)
			return null;

		NameSections nameSections = getNameSections(subsection, device, method);
		if (nameSections == null)
			return null;

		if (method == ESSNameConstructionMethod.ACCELERATOR && !isDeviceInstanceIndexValid(subsection, deviceInstanceIndex))
			return null;

		return new NCName(subsection, device, signal, deviceInstanceIndex, nameSections.section.getName() + "-"
				+ nameSections.disciplineOrSubsection.getName() + ":" + nameSections.deviceName + "-" + deviceInstanceIndex
				+ ":" + validateSignalName(signal), NCNameStatus.INVALID, 1);
	}

	@Override
	public NCName createNCNameDevice(NameEvent subsection, NameEvent device,
			NamingConventionEJBLocal.ESSNameConstructionMethod method) {
		if (subsection == null || device == null)
			return null;

		NameSections nameSections = getNameSections(subsection, device, method);
		if (nameSections == null)
			return null;

		long deviceInstances = countNCNamesByRef(subsection, device);
		String deviceInstanceIndex = subsection.getName().substring(0, 2) + getDeviceInstanceIndex(deviceInstances);

		return new NCName(subsection, device, null, deviceInstanceIndex, nameSections.section.getName() + "-"
				+ nameSections.disciplineOrSubsection.getName() + ":" + nameSections.deviceName + "-" + deviceInstanceIndex,
				NCNameStatus.INVALID, 1);
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
			NamingConventionEJBLocal.ESSNameConstructionMethod method) {

		if (!((subsection.getStatus() == 'a') && subsection.getNameCategory().getName().equalsIgnoreCase("SUB")
				&& (device.getStatus() == 'a') && (device.getNameCategory().getName().equalsIgnoreCase("GDEV") || device
				.getNameCategory().getName().equalsIgnoreCase("SDEV"))))
			return null;

		NameSections nameSections = new NameSections();

		NameEvent section;
		NameEvent genDevice;

		switch (method) {
		case ACCELERATOR:
			// find the correct section name
			section = subsection;
			while (!section.getNameCategory().getName().equalsIgnoreCase("SECT")) {
				if (section.getParentName() == null)
					return null; // validation failed
				section = section.getParentName();
			}
			nameSections.section = section;

			// find the correct discipline based on the device
			NameEvent discipline = device;
			while (!discipline.getNameCategory().getName().equalsIgnoreCase("DSCP")) {
				if (discipline.getParentName() == null)
					return null; // validation failed
				discipline = discipline.getParentName();
			}
			nameSections.disciplineOrSubsection = discipline;

			// find the appropriate generic device name
			genDevice = device;
			while (!genDevice.getNameCategory().getName().equalsIgnoreCase("GDEV")) {
				if (genDevice.getParentName() == null)
					return null; // validation failed
				genDevice = genDevice.getParentName();
			}
			nameSections.deviceName = genDevice.getName();
			break;
		case TARGET:
			section = subsection;
			while (!section.getNameCategory().getName().equalsIgnoreCase("SECT")) {
				if (section.getParentName() == null)
					return null; // validation failed
				section = section.getParentName();
			}
			nameSections.section = section;

			nameSections.disciplineOrSubsection = subsection;

			// find the appropriate generic device name
			genDevice = device;
			while (!genDevice.getNameCategory().getName().equalsIgnoreCase("GDEV")) {
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

	private long countNCNamesByRef(NameEvent subsection, NameEvent device) {
		//@formatter:off
		TypedQuery<Long> query = em.createQuery("SELECT COUNT(n) FROM NCName n " + 
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

	@Override
	public NCName findNCNameById(Integer id) {
		TypedQuery<NCName> query = em.createNamedQuery("NCName.findById", NCName.class).setParameter("id", id);
		return query.getSingleResult();
	}

	@Override
	public NCName findNCNameByName(String name) {
		TypedQuery<NCName> query = em.createNamedQuery("NCName.findByName", NCName.class).setParameter("name", name);
		return query.getSingleResult();
	}

	// instance index cannot be null.
	@Override
	public NCName findNCNameByReference(NameEvent section, NameEvent device, String instanceIndex, NameEvent signal) {
		if (section == null)
			throw new IllegalArgumentException("section is null");
		if (device == null)
			throw new IllegalArgumentException("device is null");
		if (instanceIndex == null)
			throw new IllegalArgumentException("device quantifier is null");

		TypedQuery<NCName> query = em.createNamedQuery("NCName.findByParts", NCName.class).setParameter("section", section)
				.setParameter("device", device).setParameter("signal", signal).setParameter("instanceIndex", instanceIndex);

		return query.getSingleResult();
	}

	@Override
	public List<NCName> getActiveNames() {
		return getNCNamesByStatus(NCNameStatus.VALID);
	}

	@Override
	public List<NCName> getNCNamesByStatus(NCNameStatus status) {
		List<NCName> names;

		TypedQuery<NCName> query = em.createNamedQuery("NCName.findByStatus", NCName.class).setParameter("status", status);
		names = query.getResultList();
		// logger.log(Level.INFO, "Total number of categories: " + cats.size());

		return names;
	}

	@Override
	public boolean isNameValid(NCName ncName) throws NamingConventionException {
		return ncName.getStatus() == NCNameStatus.VALID;
	}

	@Override
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
		if ((section.getStatus() != 'a') || !section.getNameCategory().getName().equalsIgnoreCase("SECT"))
			return false;

		// checking whether discipline exists, is it approved and does its
		// category equals DSCP
		TypedQuery<NameEvent> disciplineQ = em.createNamedQuery("NameEvent.findByName", NameEvent.class);
		disciplineQ.setParameter("name", disciplineName);
		NameEvent discipline = disciplineQ.getSingleResult();
		if ((discipline.getStatus() != 'a') || !discipline.getNameCategory().getName().equalsIgnoreCase("DSCP"))
			return false;

		// checking whether device exists, is it approved and does its
		// category equals GDEV
		TypedQuery<NameEvent> deviceQ = em.createNamedQuery("NameEvent.findByName", NameEvent.class);
		deviceQ.setParameter("name", deviceName);
		NameEvent genDevice = deviceQ.getSingleResult();
		if ((genDevice.getStatus() != 'a') || !genDevice.getNameCategory().getName().equalsIgnoreCase("GDEV"))
			return false;

		// TODO what to do with device quantifier and signal

		return false;
	}

	@Override
	public boolean isNamePartValid(NameEvent namePart) throws NamingConventionException {
		return namePart.getStatus() == 'a';
	}

	@Override
	public boolean isNamePartValid(String namePart, NameCategory category) throws NamingConventionException {
		TypedQuery<NameEvent> query = em.createNamedQuery("NameEvent.findByName", NameEvent.class).setParameter("name",
				namePart);
		NameEvent nameEvent = query.getSingleResult();
		return (nameEvent.getStatus() == 'a') && (nameEvent.getNameCategory().equals(category));

	}

}