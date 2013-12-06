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
	public NCName createNCName(NameEvent section, NameEvent discipline, NameEvent signal) {
		return new NCName(0, section, discipline, signal, null, section.getName() + "-" + discipline.getName(),
				NCNameStatus.VALID, 1);
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
		if (signal == null)
			throw new IllegalArgumentException("signal is null");
		if (instanceIndex == null)
			throw new IllegalArgumentException("instanceIndex is null");

		TypedQuery<NCName> query = em.createNamedQuery("NCName.findByName", NCName.class).setParameter("section", section)
				.setParameter("discipline", discipline).setParameter("signal", signal)
				.setParameter("instanceIndex", instanceIndex);

		return query.getSingleResult();
	}

	@Override
	public List<NCName> getActiveNames() {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isNameValid(String ncName) throws NamingConventionException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isNamePartValid(NameEvent namePart) throws NamingConventionException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isNamePartValid(String namePart, NameCategory category) throws NamingConventionException {
		// TODO Auto-generated method stub
		return false;
	}
}