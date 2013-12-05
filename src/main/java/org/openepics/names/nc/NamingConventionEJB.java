package org.openepics.names.nc;

import java.util.List;

import javax.ejb.Stateless;

import org.openepics.names.model.NCName;
import org.openepics.names.model.NCName.NCNameStatus;
import org.openepics.names.model.NameCategory;
import org.openepics.names.model.NameEvent;

@Stateless
public class NamingConventionEJB implements NamingConventionEJBLocal {

	@Override
	public NCName createNCName(NameEvent section, NameEvent discipline, NameEvent signal) {
		return new NCName(0, section, discipline, signal, null, section.getName()+"-"+discipline.getName(), NCNameStatus.VALID, 1);
	}

	@Override
	public NCName findNCNameById(Integer id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NCName findNCNameByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NCName findNCNameByReference(NameEvent section,
			NameEvent discipline, NameEvent signal, String instanceIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<NCName> getActiveNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<NCName> getNCNamesByStatus(NCNameStatus status) {
		// TODO Auto-generated method stub
		return null;
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