package org.openepics.names.nc;

import java.util.List;

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
	public NCName createNCName(NameEvent subsection, NameEvent device, NameEvent signal,
			NamingConventionEJBLocal.ESSNameConstructionMethod method) {
		if (subsection == null || device == null)
			return null;

		NameSections nameSections = getNameSections(subsection, device, method);
		if (nameSections == null)
			return null;

		List<NCName> deviceInstances = getNCNamesByRef(nameSections.section, nameSections.disciplineOrSubsection,
				nameSections.deviceName);
		String deviceInstanceIndex = getDeviceInstanceIndex(deviceInstances);

		if (signal == null)
			return new NCName(subsection, device, null, deviceInstanceIndex,
					nameSections.section.getName() + "-" + nameSections.disciplineOrSubsection.getName() + ":"
							+ nameSections.deviceName + "-" + deviceInstanceIndex, NCNameStatus.INVALID, 1);

		return new NCName(subsection, device, signal, deviceInstanceIndex, nameSections.section.getName() + "-"
				+ nameSections.disciplineOrSubsection.getName() + ":" + nameSections.deviceName + "-" + deviceInstanceIndex
				+ ":" + validateSignalName(signal), NCNameStatus.INVALID, 1);
	}

	private String getDeviceInstanceIndex(List<NCName> names) {
		if (names.size() == 0)
			return "A";
		return "" + ((char) (names.size() % 26 + 'A')) + (names.size() / 26);
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

	private List<NCName> getNCNamesByRef(NameEvent section, NameEvent discipline, String deviceName) {
		//@formatter:off
		TypedQuery<NCName> query = em.createQuery("SELECT n FROM NCName n " + 
														"WHERE n.section = :section " +
														"AND n.discipline = :discipline " + 
														"AND n.name = :name " + 
														"ORDER BY n.instanceIndex ASC", 
														NCName.class);
		// @formatter:on
		query.setParameter("section", section).setParameter("discipline", discipline).setParameter("name", deviceName);

		return query.getResultList();
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

	@Override
	public NCName findNCNameByReference(NameEvent section, NameEvent discipline, NameEvent signal, Character instanceIndex) {
		if (section == null)
			throw new IllegalArgumentException("section is null");
		if (discipline == null)
			throw new IllegalArgumentException("discipline is null");

		TypedQuery<NCName> query = em.createNamedQuery("NCName.findByParts", NCName.class).setParameter("section", section)
				.setParameter("discipline", discipline).setParameter("signal", signal)
				.setParameter("instanceIndex", instanceIndex);

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
		// TODO Auto-generated method stub
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