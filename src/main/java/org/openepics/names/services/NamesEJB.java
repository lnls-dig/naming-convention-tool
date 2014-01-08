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
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
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
}
