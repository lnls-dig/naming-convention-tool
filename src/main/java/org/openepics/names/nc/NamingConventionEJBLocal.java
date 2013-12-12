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

	/**
	 * @param subsection
	 *            - For accelerator and target: must be a SUBSECTION
	 * 
	 * @param device
	 *            - can be either GDEV or SDEV
	 * @param signal
	 *            - can be <code>null</code>. Is <code>signal</code> is
	 *            <code>null</code>, then we are constructing a new device.
	 *            Otherwise, the we are adding a new signal to the existing
	 *            device. In this case the <code>subsection</code> must belong
	 *            to the <code>device</code>.
	 * @param method
	 * @return - returns null if this would not construct a valid device name.
	 *         Example incorrect entities were used for section, device or
	 *         signal
	 */
	NCName createNCNameSignal(NameEvent subsection, NameEvent device, String deviceInstanceIndex, NameEvent signal,
			ESSNameConstructionMethod method);

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
	NCName createNCNameDevice(NameEvent subsection, NameEvent device, ESSNameConstructionMethod method);

	NCName findNCNameById(Integer id);

	NCName findNCNameByName(String name);

	NCName findNCNameByReference(NameEvent section, NameEvent discipline, String instanceIndex, NameEvent signal);

	List<NCName> getAllNCNames();

	List<NCName> getActiveNames();

	List<NCName> getNCNamesByStatus(NCNameStatus status);

	boolean isNameValid(NCName ncName) throws NamingConventionException;

	/**
	 * Checks whether the name is composed of the actual active name parts and
	 * this conforms to the naming convention. This can also be called for names
	 * that have not been defined yet.
	 * 
	 * @param ncName
	 * @return
	 * @throws NamingConventionException
	 */
	boolean isNameValid(String ncName) throws NamingConventionException;

	boolean isNamePartValid(NameEvent namePart) throws NamingConventionException;

	boolean isNamePartValid(String namePart, NameCategory category) throws NamingConventionException;
}
