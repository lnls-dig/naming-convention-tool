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

	@PersistenceContext(unitName = "org.openepics.names.punit")
	private EntityManager em;

	@Override
	public NCName createNCName(NameEvent section, NameEvent discipline, NameEvent signal,
			NamingConventionEJBLocal.ESSNameConstructionMethod method) {
		if ((section.getStatus() == 'a')
				&& (section.getNameCategory().getName().equalsIgnoreCase("SUP")
						|| section.getNameCategory().getName().equalsIgnoreCase("SECT") || section.getNameCategory().getName()
						.equalsIgnoreCase("SUB"))
				&& (discipline.getStatus() == 'a')
				&& (discipline.getNameCategory().getName().equalsIgnoreCase("DSCP")
						|| discipline.getNameCategory().getName().equalsIgnoreCase("CAT")
						|| discipline.getNameCategory().getName().equalsIgnoreCase("GDEV") || discipline.getNameCategory()
						.getName().equalsIgnoreCase("SDEV"))) {
			if (signal == null) {
				if (findNCNameByReference(section, discipline, signal, null) != null)
					return null; // name already exists
				return new NCName(section, discipline, signal, null, section.getName() + "-" + discipline.getName(),
						NCNameStatus.INVALID, 1);
			} else {
				List<NCName> signalNames = getNCNamesByRef(section, discipline, signal);
				// 3 possibilities:
				// 1) The first signal
				// 2) One signal exists
				// 3) More than on signal exists
				if (signalNames.size() == 0) {
					return new NCName(section, discipline, signal, null, section.getName() + "-" + discipline.getName(),
							NCNameStatus.INVALID, 1);
				}
			}
		} else
			return null;

		return new NCName(section, discipline, signal, null, section.getName() + "-" + discipline.getName(),
				NCNameStatus.VALID, 1);
	}

	private List<NCName> getNCNamesByRef(NameEvent section, NameEvent discipline, NameEvent signal) {
		TypedQuery<NCName> query = em
				.createQuery(
						"SELECT n FROM NCName n WHERE n.section = :section AND n.discipline = :discipline AND n.signal = :signal ORDER BY n.instanceIndex ASC",
						NCName.class);
		query.setParameter("section", section).setParameter("discipline", discipline).setParameter("signal", signal);

		return query.getResultList();
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