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
package org.openepics.names.services;

import java.util.Date;
import java.util.List;
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
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartRevisionStatus;
import org.openepics.names.model.NameHierarchy;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NameRelease;
import org.openepics.names.model.Privilege;
import org.openepics.names.ui.UserManager;

// import org.openepics.auth.japi.*;
/**
 * The process layer for Naming.
 *
 * @author Vasu V <vuppala@frib.msu.org>
 */
@Stateless
public class NamesEJB {

    private static final Logger logger = Logger.getLogger("org.openepics.names.services.NamesEJB");
    // TODO: Remove the injection. Not a good way to authorize.
    @Inject
    private UserManager userManager;
    @PersistenceContext(unitName = "org.openepics.names.punit")
    private EntityManager em;
    @EJB
    private NamingConventionEJB ncEJB;

    public NameHierarchy getNameHierarchy() {
        return em.createQuery("SELECT nameHierarchy FROM NameHierarchy nameHierarchy", NameHierarchy.class).getSingleResult();
    }

    /**
     * Publish a new release of the naming system.
     *
     * @author Vasu V <vuppala@frib.msu.org>
     */
    public NameRelease createNewRelease(NameRelease newRelease) throws Exception {
        logger.log(Level.FINER, "creating release...");

        if (!userManager.isEditor()) {
            throw new Exception("You are not authorized to perform this operation.");
        }
        newRelease.setReleaseDate(new Date());
        newRelease.setReleasedBy(userManager.getUser());
        // logger.log(Level.INFO, "set properties...");
        em.persist(newRelease);
        logger.log(Level.FINE, "published new release ...");
        return newRelease;
    }

    /**
     * Find all events that are not processed yet
     *
     * @author Vasu V <vuppala@frib.msu.org>
     */
    public List<NamePartRevision> getUnprocessedEvents() {
        List<NamePartRevision> nameEvents;

        TypedQuery<NamePartRevision> query = em.createQuery("SELECT n FROM NameEvent n WHERE n.status = :status", NamePartRevision.class).setParameter("status", NamePartRevisionStatus.PROCESSING);

        nameEvents = query.getResultList();
        logger.log(Level.INFO, "retreived pending requests: " + nameEvents.size());
        return nameEvents;
    }

    /**
     * Is name being changed?
     */
    public boolean isUnderChange(NamePartRevision nevent) {
        List<NamePartRevision> nameEvents;

        TypedQuery<NamePartRevision> query = em
                .createQuery(
                        "SELECT n FROM NameEvent n WHERE n.name = :name AND n.status != :status "
                        + "AND  n.requestDate > (SELECT MAX(r.releaseDate) FROM NameRelease r)",
                        NamePartRevision.class).setParameter("status", NamePartRevisionStatus.REJECTED).setParameter("name", nevent.getName());
        nameEvents = query.getResultList();
        logger.log(Level.FINE, "change requests: " + nameEvents.size());
        return !nameEvents.isEmpty();
    }

    /**
     * Retrieve all event.
     *
     * @author Vasu V <vuppala@frib.msu.org>
     */
    public List<NamePartRevision> getAllEvents() {
        List<NamePartRevision> nameEvents;

        TypedQuery<NamePartRevision> query = em.createQuery("SELECT n FROM NameEvent n ORDER BY n.requestDate DESC", NamePartRevision.class);

        nameEvents = query.getResultList();
        logger.log(Level.FINE, "Results for all events: " + nameEvents.size());
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
        logger.log(Level.FINE, "Results for all releases: " + releases.size());
        return releases;
    }

    /**
     * Retrieve all events of a given name.
     *
     * @param namePart
     * @return a list of name events in the descending order
     * @author Vasu V <vuppala@frib.msu.org>
     */
    public List<NamePartRevision> findEventsByName(NamePart namePart) {
        final TypedQuery<NamePartRevision> query = em.createQuery("SELECT n FROM NameEvent n WHERE n.namePart = :namePart " + "ORDER BY n.requestDate DESC", NamePartRevision.class).setParameter("namePart", namePart);
        return query.getResultList();
    }

    public List<NamePartRevision> findEventsByCategory(NameCategory category) {
        List<NamePartRevision> nameEvents;

        TypedQuery<NamePartRevision> query = em.createQuery(
                "SELECT n FROM NameEvent n WHERE n.nameCategory = :nameCategory ORDER BY n.name", NamePartRevision.class)
                .setParameter("nameCategory", category);

        nameEvents = query.getResultList();
        logger.log(Level.FINE, "Events for category " + category.getName() + nameEvents.size());
        return nameEvents;
    }

    /**
     * Find the latest event related to the given name.
     *
     * @author Vasu V <vuppala@frib.msu.org>
     */
    public NamePartRevision findLatestEvent(String name) {
        List<NamePartRevision> nameEvents;

        TypedQuery<NamePartRevision> query = em.createQuery(
                "SELECT n FROM NameEvent n WHERE n.name = :name  AND n.status != :status ORDER BY n.requestDate DESC",
                NamePartRevision.class).setParameter("status", NamePartRevisionStatus.REJECTED).setParameter("name", name);

        nameEvents = query.getResultList();
        if (nameEvents.isEmpty()) {
            return null;
        } else {
            return nameEvents.get(0);
        }
    }

    public List<NamePartRevision> findEventsByParent(NamePartRevision parent) {
        List<NamePartRevision> childEvents;

        TypedQuery<NamePartRevision> query = em.createQuery(
                "SELECT n FROM NameEvent n WHERE n.parentName = :parentName ORDER BY n.name", NamePartRevision.class);
        query.setParameter("parentName", parent);

        childEvents = query.getResultList();
        logger.log(Level.FINE, "Child events for " + parent.getName() + childEvents.size());
        return childEvents;
    }

    /**
     * Find events matching given criteria
     *
     * @param eventType an event type
     * @param eventStatus event status
     *
     * @author Vasu V <vuppala@frib.msu.org>
     */
    public List<NamePartRevision> findEvents(char eventType, char eventStatus) {
        List<NamePartRevision> nameEvents;

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

        TypedQuery<NamePartRevision> query = em.createQuery(queryStr, NamePartRevision.class);

        nameEvents = query.getResultList();
        logger.log(Level.INFO, "Search hits: " + nameEvents.size());
        return nameEvents;
    }

    /**
     * Finds a {@link NamePartRevision} with the specified ID.
     */
    public NamePartRevision findEventById(Integer id) {
        return em.find(NamePartRevision.class, id);
    }

    /**
     * Update the status of a set of events.
     *
     * @author Vasu V <vuppala@frib.msu.org>
     */
    public void processEvents(NamePartRevision[] nevents, NamePartRevisionStatus status, String comment) throws Exception {
        NamePartRevision mEvent;

        // TODO; check if user has privs to processEvents.
        if (!userManager.isEditor()) {
            throw new Exception("You are not authorized to perform this operation.");
        }
        logger.log(Level.FINE, "Processing events " + nevents.length);
        for (NamePartRevision event : nevents) {
            logger.log(Level.FINER, "Processing  " + event.getName());
            mEvent = em.find(NamePartRevision.class, event.getId(), LockModeType.OPTIMISTIC);
            mEvent.setStatus(status);
            mEvent.setProcessDate(new java.util.Date());
            mEvent.setProcessorComment(comment);
            mEvent.setProcessedBy(userManager.getUser());
            if (status == NamePartRevisionStatus.REJECTED) {
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
        NamePartRevision mEvent;

		// TODO; check if user has privs to processEvents.
        // TODO: better to merge or extend the persistence context?
        mEvent = em.find(NamePartRevision.class, eventId, LockModeType.OPTIMISTIC);

        if (mEvent == null) {
            throw new Exception("Event not found.");
        }
        if (!userManager.isEditor() && !mEvent.getRequestedBy().equals(userManager.getUser())) {
            throw new Exception("Unauthorized access");
        }
        logger.log(Level.FINE, "Processing  " + mEvent.getName());
        mEvent.setStatus(NamePartRevisionStatus.CANCELLED);
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
     * Is the current user a SuperUser?
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
        logger.log(Level.FINE, "Total number of categories: " + cats.size());

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
