package org.openepics.names;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.openepics.names.nc.NamingConventionEJBLocal;

@ManagedBean
@ViewScoped
public class NCNamesManager implements Serializable {

	private static final long serialVersionUID = 1L;
	@EJB
	private NamingConventionEJBLocal ncEJB;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger("org.openepics.names");

	public NCNamesManager() {
		// EMPTY
	}

	@PostConstruct
	public void init() {
		try {
			// TODO
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Could not initialize NCNamesManager.");
			System.err.println(e);
		}
	}
}
