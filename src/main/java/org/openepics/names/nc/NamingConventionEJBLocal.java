package org.openepics.names.nc;

import java.util.List;

import javax.ejb.Local;

import org.openepics.names.model.NCName;
import org.openepics.names.model.NCName.NCNameStatus;
import org.openepics.names.model.NameCategory;
import org.openepics.names.model.NameEvent;

@Local
public interface NamingConventionEJBLocal {
	static enum ESSNameConstructionMethod {
		ACCELERATOR, TARGET
	}

	NCName createNCName(NameEvent section, NameEvent discipline, NameEvent signal, ESSNameConstructionMethod method);

	NCName findNCNameById(Integer id);

	NCName findNCNameByName(String name);

	NCName findNCNameByReference(NameEvent section, NameEvent discipline, NameEvent signal, Character instanceIndex);

	List<NCName> getActiveNames();

	List<NCName> getNCNamesByStatus(NCNameStatus status);

	boolean isNameValid(NCName ncName) throws NamingConventionException;

	boolean isNameValid(String ncName) throws NamingConventionException;

	boolean isNamePartValid(NameEvent namePart) throws NamingConventionException;

	boolean isNamePartValid(String namePart, NameCategory category) throws NamingConventionException;
}
