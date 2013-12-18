/*
 * This software is Copyright by the Board of Trustees of Michigan
 * State University (c) Copyright 2012.
 * 
 * You may use this software under the terms of the GNU public license
 *  (GPL). The terms of this license are described at:
 *       http://www.gnu.org/licenses/gpl.txt
 * 
 * Contact Information:
 *   Facilitty for Rare Isotope Beam
 *   Michigan State University
 *   East Lansing, MI 48824-1321
 *   http://frib.msu.edu
 * 
 */
package org.openepics.names;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.openepics.names.model.NameCategory;
import org.openepics.names.model.NameEvent;
import org.openepics.names.model.NameEventStatus;
import org.openepics.names.model.NameEventType;
import org.openepics.names.model.NameRelease;
import org.openepics.names.model.Privilege;
import org.openepics.names.nc.NamingConventionEJB;

// import org.openepics.auth.japi.*;

/**
 * The process layer for Naming.
 * 
 * @author Vasu V <vuppala@frib.msu.org>
 */
@Stateless
public class NamesEJB {

	private static final Logger logger = Logger.getLogger("org.openepics.names");
	// TODO: Remove the injection. Not a good way to authorize.
	@Inject
	private UserManager userManager;
	@PersistenceContext(unitName = "org.openepics.names.punit")
	private EntityManager em;
	@EJB
	private NamingConventionEJB ncEJB;

	// private AuthServ authService = null; //* Authentication service

	/**
     * Create a new event i.e. name creation, modification, deletion etc.
     *
     * @author Vasu V <vuppala@frib.msu.org>
     */
    public NameEvent createNewEvent(String nameId, String name, String fullName, Integer nameCategoryID, Integer parentNameID, NameEventType eventType, String comment) throws Exception {
        logger.log(Level.INFO, "creating...");
        Date curdate = new Date();

        if (userManager == null) {
            throw new Exception("userManager is null. Cannot inject it");
        }

        if (!userManager.isLoggedIn()) {
            throw new Exception("You are not authorized to perform this operation.");
        }
        
        NameCategory ncat = null;
        if(nameCategoryID != null)
        	ncat = em.find(NameCategory.class, nameCategoryID);
        if (ncat == null) {
            logger.log(Level.SEVERE, "Invalid categroy: " + nameCategoryID);
            return null;
        }
        
        NameEvent parent = null;
        if(parentNameID != null)
            parent = em.find(NameEvent.class, parentNameID);
        
        NameEvent mEvent = new NameEvent(eventType, userManager.getUser(), curdate, NameEventStatus.PROCESSING, name, fullName);
        logger.log(Level.INFO, "new created:" + name + ":" + fullName);
        if (eventType == NameEventType.INSERT) {
            nameId = UUID.randomUUID().toString();
        }

        mEvent.setRequestorComment(comment);
        mEvent.setNameCategory(ncat);
        mEvent.setNameId(nameId);
        if(parent != null)
            mEvent.setParentName(parent);
        logger.log(Level.INFO, "set properties...");
        em.persist(mEvent);
        logger.log(Level.INFO, "persisted...");
        return mEvent;
    }

	/**
	 * Publish a new release of the naming system.
	 * 
	 * @author Vasu V <vuppala@frib.msu.org>
	 */
	public NameRelease createNewRelease(NameRelease newRelease) throws Exception {
		logger.log(Level.INFO, "creating release...");

		if (!userManager.isEditor()) {
			throw new Exception("You are not authorized to perform this operation.");
		}
		newRelease.setReleaseDate(new Date());
		newRelease.setReleasedBy(userManager.getUser());
		// logger.log(Level.INFO, "set properties...");
		em.persist(newRelease);
		logger.log(Level.INFO, "published new release ...");
		return newRelease;
	}

	/**
	 * Find all events that are not processed yet
	 * 
	 * @author Vasu V <vuppala@frib.msu.org>
	 */
	public List<NameEvent> getUnprocessedEvents() {
		List<NameEvent> nameEvents;

		TypedQuery<NameEvent> query = em.createQuery("SELECT n FROM NameEvent n WHERE n.status = :status", NameEvent.class).setParameter("status", NameEventStatus.PROCESSING);

		nameEvents = query.getResultList();
		logger.log(Level.INFO, "retreived pending requests: " + nameEvents.size());
		return nameEvents;
	}

	/**
	 * Get names that are approved, and are new or modified.
	 */
	public List<NameEvent> getValidNames() {
		return getStandardNames("%", false);
	}

	/**
	 * Is name being changed?
	 */
	public boolean isUnderChange(NameEvent nevent) {
		List<NameEvent> nameEvents;

		TypedQuery<NameEvent> query = em
				.createQuery(
						"SELECT n FROM NameEvent n WHERE n.name = :name AND n.status != :status AND  n.requestDate > (SELECT MAX(r.releaseDate) FROM NameRelease r)",
						NameEvent.class).setParameter("status", NameEventStatus.REJECTED).setParameter("name", nevent.getName());
		nameEvents = query.getResultList();
		logger.log(Level.INFO, "change requests: " + nameEvents.size());
		return !nameEvents.isEmpty();
	}

	/**
	 * Get all requests of the current user
	 */
	public List<NameEvent> getUserRequests() {
		List<NameEvent> nameEvents;
		Privilege user = userManager.getUser();
		// String user = "system";

		if (user == null) {
			return null;
		}

		TypedQuery<NameEvent> query = em.createQuery("SELECT n FROM NameEvent n WHERE n.requestedBy = :user", NameEvent.class)
				.setParameter("user", user);

		nameEvents = query.getResultList();
		logger.log(Level.INFO, "Results for requests: " + nameEvents.size());
		return nameEvents;
	}

	/**
	 * Retrieve all event.
	 * 
	 * @author Vasu V <vuppala@frib.msu.org>
	 */
	public List<NameEvent> getAllEvents() {
		List<NameEvent> nameEvents;

		TypedQuery<NameEvent> query = em.createQuery("SELECT n FROM NameEvent n ORDER BY n.requestDate DESC", NameEvent.class);

		nameEvents = query.getResultList();
		logger.log(Level.INFO, "Results for all events: " + nameEvents.size());
		return nameEvents;
	}

	/**
	 * Retrieve all releases.
	 * 
	 * @author Vasu V <vuppala@frib.msu.org>
	 */
	public List<NameRelease> getAllReleases() {
		List<NameRelease> releases;

		TypedQuery<NameRelease> query = em.createQuery("SELECT n FROM NameRelease n ORDER BY n.releaseDate DESC",
				NameRelease.class);
		releases = query.getResultList();
		logger.log(Level.INFO, "Results for all events: " + releases.size());
		return releases;
	}

	/**
	 * Retrieve all events of a given name.
	 * 
	 * @author Vasu V <vuppala@frib.msu.org>
	 */
	public List<NameEvent> findEventsByName(String name) {
		List<NameEvent> nameEvents;

		TypedQuery<NameEvent> query = em.createQuery(
				"SELECT n FROM NameEvent n WHERE n.name = :name ORDER BY n.requestDate DESC", NameEvent.class).setParameter(
				"name", name);

		nameEvents = query.getResultList();
		logger.log(Level.INFO, "Events for " + name + nameEvents.size());
		return nameEvents;
	}

	public List<NameEvent> findEventsByCategory(NameCategory category) {
		List<NameEvent> nameEvents;

		TypedQuery<NameEvent> query = em.createQuery(
				"SELECT n FROM NameEvent n WHERE n.nameCategory = :nameCategory ORDER BY n.name", NameEvent.class)
				.setParameter("nameCategory", category);

		nameEvents = query.getResultList();
		logger.log(Level.INFO, "Events for category " + category.getName() + nameEvents.size());
		return nameEvents;
	}

	/**
	 * Find the latest event related to the given name.
	 * 
	 * @author Vasu V <vuppala@frib.msu.org>
	 */
	public NameEvent findLatestEvent(String name) {
		List<NameEvent> nameEvents;

		TypedQuery<NameEvent> query = em.createQuery(
				"SELECT n FROM NameEvent n WHERE n.name = :name  AND n.status != :status ORDER BY n.requestDate DESC",
				NameEvent.class).setParameter("status", NameEventStatus.REJECTED).setParameter("name", name);

		nameEvents = query.getResultList();
		if (nameEvents.isEmpty()) {
			return null;
		} else {
			return nameEvents.get(0);
		}
	}

	public List<NameEvent> findEventsByParent(NameEvent parent) {
		List<NameEvent> childEvents;

		TypedQuery<NameEvent> query = em.createQuery(
				"SELECT n FROM NameEvent n WHERE n.parentName = :parentName ORDER BY n.name", NameEvent.class).setParameter(
				"parentName", parent);

		childEvents = query.getResultList();
		logger.log(Level.INFO, "Child events for " + parent.getName() + childEvents.size());
		return childEvents;
	}

	/**
	 * Find events matching given criteria
	 * 
	 * @param eventType
	 *            an event type
	 * @param eventStatus
	 *            event status
	 * 
	 * @author Vasu V <vuppala@frib.msu.org>
	 */
	public List<NameEvent> findEvents(char eventType, char eventStatus) {
		List<NameEvent> nameEvents;

		String queryStr = "SELECT n FROM NameEvent n ";
		String cons = "";

		if (eventType != 0) {
			// TODO:Bad idea! change to criteria query.
			cons += " n.eventType = '" + String.valueOf(eventType) + "' ";
		}

		if (eventStatus != 0) {
			if (!"".equals(cons)) {
				cons += " AND ";
			}
			// TODO:Bad idea! change to criteria query.
			cons += " n.status = '" + String.valueOf(eventStatus) + "' ";
		}

		if (!"".equals(cons)) {
			queryStr += "WHERE " + cons;
		}

		logger.log(Level.INFO, "Search query is: " + queryStr);

		TypedQuery<NameEvent> query = em.createQuery(queryStr, NameEvent.class);

		nameEvents = query.getResultList();
		logger.log(Level.INFO, "Search hits: " + nameEvents.size());
		return nameEvents;
	}

	/**
	 * Get name elements that have been approved.
	 * 
	 * @param category Restrict names to the given name-element category
	 * 
	 * @param includeDeleted Don't discard deleted name-elements.
	 */
	public List<NameEvent> getStandardNames(String category, boolean includeDeleted) {
		List<NameEvent> nameEvents;
		TypedQuery<NameEvent> query;

		if (includeDeleted) {
			query = em
					.createQuery(
							"SELECT n FROM NameEvent n WHERE n.nameCategory.name LIKE :categ AND n.requestDate = (SELECT MAX(r.requestDate) FROM NameEvent r WHERE r.name = n.name AND (r.status = :status1 OR r.status = :status2)) ORDER BY n.nameCategory.id, n.name",
							NameEvent.class).setParameter("status1", NameEventStatus.APPROVED).setParameter("status2", NameEventStatus.PROCESSING).setParameter("categ", category);
		} else {
			query = em
					.createQuery(
							"SELECT n FROM NameEvent n WHERE n.nameCategory.name LIKE :categ AND n.requestDate = (SELECT MAX(r.requestDate) FROM NameEvent r WHERE r.name = n.name AND (r.status = :status1 OR r.status = :status2)) AND NOT (n.eventType = :type AND n.status = :status1) ORDER BY n.nameCategory.id, n.name",
							NameEvent.class).setParameter("status1", NameEventStatus.APPROVED).setParameter("status2", NameEventStatus.PROCESSING).setParameter("type", NameEventType.DELETE).setParameter("categ", category);
		}
		// TODO: check category values
		nameEvents = query.getResultList();
		logger.log(Level.INFO, "Results for category " + category + ":" + nameEvents.size());
		return nameEvents;
	}

	/**
	 * Finds a {@link NameEvent} with the specified ID.
	 */
	public NameEvent findEventById(Integer id) {
		return em.find(NameEvent.class, id);
	}

	/**
	 * Update the status of a set of events.
	 * 
	 * @author Vasu V <vuppala@frib.msu.org>
	 */
	public void processEvents(NameEvent[] nevents, NameEventStatus status, String comment) throws Exception {
		NameEvent mEvent;

		// TODO; check if user has privs to processEvents.
		if (!userManager.isEditor()) {
			throw new Exception("You are not authorized to perform this operation.");
		}
		logger.log(Level.INFO, "Processing events " + nevents.length);
		for (NameEvent event : nevents) {
			logger.log(Level.INFO, "Processing  " + event.getName());
			mEvent = em.find(NameEvent.class, event.getId(), LockModeType.OPTIMISTIC);
			mEvent.setStatus(status);
			mEvent.setProcessDate(new java.util.Date());
			mEvent.setProcessorComment(comment);
			mEvent.setProcessedBy(userManager.getUser());
			if (status == NameEventStatus.REJECTED) {
				continue;
			}
			// TODO what's up with this?
			// if (event.getEventType() == 'i') { // initiated. ToDO: use enums.
			// mEvent.setNameId(UUID.randomUUID().toString());
			// }
		}
	}

	/**
	 * Cancel a change request
	 * 
	 * @author Vasu V <vuppala@frib.msu.org>
	 */
	public void cancelRequest(int eventId, String comment) throws Exception {
		NameEvent mEvent;

		// TODO; check if user has privs to processEvents.
		// TODO: better to merge or extend the persistence context?
		mEvent = em.find(NameEvent.class, eventId, LockModeType.OPTIMISTIC);

		if (mEvent == null) {
			throw new Exception("Event not found.");
		}
		if (!userManager.isEditor() && !mEvent.getRequestedBy().equals(userManager.getUser())) {
			throw new Exception("Unauthorized access");
		}
		logger.log(Level.INFO, "Processing  " + mEvent.getName());
		mEvent.setStatus(NameEventStatus.CANCELLED);
		mEvent.setProcessDate(new java.util.Date());
		mEvent.setProcessorComment(comment);
		mEvent.setProcessedBy(userManager.getUser());

	}

	/**
	 * Is the current user an Editor?
	 * 
	 * @author Vasu V <vuppala@frib.msu.org>
	 */
	public boolean isEditor(Privilege user) {
		if (user != null) {
			return "E".equalsIgnoreCase(user.getOperation()) || "S".equalsIgnoreCase(user.getOperation());
		} else {
			return false;
		}
	}
	
	/**
	 * Is the current user an Editor?
	 */
	public boolean isSuperUser(Privilege user) {
		if (user != null) {
			return "S".equalsIgnoreCase(user.getOperation());
		} else {
			return false;
		}
	}

	/**
	 * Retrieve all the name categories
	 * 
	 * @author Vasu V <vuppala@frib.msu.org>
	 */
	public List<NameCategory> getCategories() {
		List<NameCategory> cats;

		TypedQuery<NameCategory> query = em.createNamedQuery("NameCategory.findAll", NameCategory.class);
		cats = query.getResultList();
		logger.log(Level.INFO, "Total number of categories: " + cats.size());

		return cats;
	}
	
	/**
	 * Finds a {@link NameCategory} with the specified ID.
	 */
	public NameCategory findCategoryById(Integer categoryID) {
		return em.find(NameCategory.class, categoryID);
	}

	/*
	 * private int validate(String ticket) throws Exception { findAuthService();
	 * 
	 * if (authService == null) { logger.log(Level.WARNING,
	 * "Cannot access Auth Service."); return -1; //TODO: This is not good. Use
	 * exceptions. }
	 * 
	 * MultivaluedMap<String, String> params = new MultivaluedMapImpl();
	 * AuthResponse resp;
	 * 
	 * params.add("ticket", ticket);
	 * 
	 * resp = authService.validate(params);
	 * 
	 * return resp.getStatus(); }
	 * 
	 * @Override public AuthResponse authenticate (String userid, String
	 * password) throws Exception { AuthResponse response; findAuthService();
	 * 
	 * if (authService == null) { logger.log(Level.WARNING,
	 * "Cannot access Auth Service."); return null; //TODO: Use exceptions. }
	 * 
	 * // RequestContext context = RequestContext.getCurrentInstance(); //
	 * AuthServ auth = new
	 * AuthServ("http://qa01.hlc.nscl.msu.edu:8080/auth/rs/v0");
	 * MultivaluedMap<String, String> params = new MultivaluedMapImpl();
	 * 
	 * params.add("user", userid); params.add("password", password); response =
	 * authService.authenticate(params); password = "xxxxxxxx"; //TODO implement
	 * a better way destroy the password (from JVM)
	 * 
	 * return response; }
	 * 
	 * private void findAuthService() throws Exception { Properties prop =
	 * getProperties("AuthDomain"); // defined via JNDI String serviceURL;
	 * 
	 * if (prop == null || !"true".equals(prop.getProperty("Enabled"))) {
	 * authService = null; logger.log(Level.INFO,
	 * "Auth Domain not enabled or defined"); return; }
	 * 
	 * if (authService == null) { serviceURL = prop.getProperty("ServiceURL");
	 * if (serviceURL == null || serviceURL.isEmpty()) {
	 * logger.log(Level.SEVERE, "ServiceURL not set"); authService = null; }
	 * else { authService = new AuthServ(serviceURL); } } }
	 * 
	 * private Properties getProperties(String jndiName) throws Exception {
	 * Properties properties;
	 * 
	 * InitialContext context = new InitialContext(); properties = (Properties)
	 * context.lookup(jndiName); context.close();
	 * 
	 * if (properties == null) { logger.log(Level.SEVERE,
	 * "Error occurred while getting properties from JNDI."); }
	 * 
	 * return properties; }
	 */
}
