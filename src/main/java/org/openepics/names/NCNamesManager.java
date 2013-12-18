package org.openepics.names;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.openepics.names.model.NCName;
import org.openepics.names.nc.NamingConventionEJB;

@ManagedBean
@ViewScoped
public class NCNamesManager implements Serializable {

	private static final long serialVersionUID = 1L;
	@EJB
	private NamingConventionEJB ncEJB;
	private static final Logger logger = Logger.getLogger("org.openepics.names");
	
	private List<NCName> allNCNames;
	private List<NCName> activeNCNames;

	public NCNamesManager() {
		// EMPTY
	}
	
	@PostConstruct
	public void init() {
		loadAllNCNames();
		loadActiveNCNames();
	}
	
	public void loadAllNCNames() {
		try {
			allNCNames = ncEJB.getAllNCNames();
			logger.log(Level.INFO, "Found NCNames. Total = "+allNCNames.size());
		} catch(Exception e) {
			logger.log(Level.SEVERE, "Could not load any NCNames.");
			System.err.println(e);
		}
	}
	
	public void loadActiveNCNames() {
		try {
			activeNCNames = ncEJB.getActiveNames();
			logger.log(Level.INFO, "Found active NCNames. Total = "+activeNCNames.size());
		} catch(Exception e) {
			logger.log(Level.SEVERE, "Could not load any active NCNames.");
			System.err.println(e);
		}
	}

	public List<NCName> getAllNCNames() {
		return allNCNames;
	}

	public void setAllNCNames(List<NCName> allNCNames) {
		this.allNCNames = allNCNames;
	}

	public List<NCName> getActiveNCNames() {
		return activeNCNames;
	}

	public void setActiveNCNames(List<NCName> activeNCNames) {
		this.activeNCNames = activeNCNames;
	}
	
	
}
