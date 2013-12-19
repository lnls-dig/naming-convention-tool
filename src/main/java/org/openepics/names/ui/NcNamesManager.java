package org.openepics.names.ui;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.openepics.names.model.NcName;
import org.openepics.names.services.NamingConventionEJB;

@ManagedBean
@ViewScoped
public class NcNamesManager implements Serializable {

	private static final long serialVersionUID = 1L;
	@EJB
	private NamingConventionEJB ncEJB;
	private static final Logger logger = Logger.getLogger("org.openepics.names");
	
	private List<NcName> allNcNames;
	private List<NcName> activeNcNames;

	public NcNamesManager() {
		// EMPTY
	}
	
	@PostConstruct
	public void init() {
		loadAllNcNames();
		loadActiveNcNames();
	}
	
	public void loadAllNcNames() {
		try {
			allNcNames = ncEJB.getAllNcNames();
			logger.log(Level.INFO, "Found NcNames. Total = "+allNcNames.size());
		} catch(Exception e) {
			logger.log(Level.SEVERE, "Could not load any NcNames.");
			System.err.println(e);
		}
	}
	
	public void loadActiveNcNames() {
		try {
			activeNcNames = ncEJB.getActiveNames();
			logger.log(Level.INFO, "Found active NcNames. Total = "+activeNcNames.size());
		} catch(Exception e) {
			logger.log(Level.SEVERE, "Could not load any active NcNames.");
			System.err.println(e);
		}
	}

	public List<NcName> getAllNcNames() {
		return allNcNames;
	}

	public void setAllNcNames(List<NcName> allNcNames) {
		this.allNcNames = allNcNames;
	}

	public List<NcName> getActiveNcNames() {
		return activeNcNames;
	}

	public void setActiveNcNames(List<NcName> activeNcNames) {
		this.activeNcNames = activeNcNames;
	}
	
	
}
