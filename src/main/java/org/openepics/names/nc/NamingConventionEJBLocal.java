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
	public NCName createNCNameSignal(NameEvent subsection, NameEvent device, String deviceInstanceIndex, NameEvent signal,
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
	public NCName createNCNameDevice(NameEvent subsection, NameEvent device, ESSNameConstructionMethod method);

	public NCName findNCNameById(Integer id);

	public NCName findNCNameByName(String name);

	public NCName findNCNameByReference(NameEvent section, NameEvent discipline, String instanceIndex, NameEvent signal);

    /**
     * Gets all NC Names regardless of status - including deleted.
     * @return 
     */
	public List<NCName> getAllNCNames();
    
    /**
     * Gets only the NC Names with status VALID or INVALID.
     * @return 
     */
    public List<NCName> getExistingNCNames();

	public List<NCName> getActiveNames();

	public List<NCName> getNCNamesByStatus(NCNameStatus status);

	public boolean isNameValid(NCName ncName) throws NamingConventionException;

	/**
	 * Checks whether the name is composed of the actual active name parts and
	 * this conforms to the naming convention. This can also be called for names
	 * that have not been defined yet.
	 * 
	 * @param ncName
	 * @return
	 * @throws NamingConventionException
	 */
	public boolean isNameValid(String ncName) throws NamingConventionException;

	public boolean isNamePartValid(NameEvent namePart) throws NamingConventionException;

	public boolean isNamePartValid(String namePart, NameCategory category) throws NamingConventionException;
}
