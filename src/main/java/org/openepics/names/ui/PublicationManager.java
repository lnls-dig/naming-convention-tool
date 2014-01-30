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
package org.openepics.names.ui;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import org.openepics.names.model.NameRelease;
import org.openepics.names.services.ReleaseService;

/**
 * Manages naming system releases
 *
 * @author Vasu V <vuppala@frib.msu.org>
 */
@ManagedBean
@ViewScoped
public class PublicationManager implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private ReleaseService releaseService;
    private static final Logger logger = Logger.getLogger("org.openepics.names.ui.PublicationManager");
    private List<NameRelease> releases;
    private NameRelease selectedRelease;
    private NameRelease inputRelease = new NameRelease();
    private NameRelease latestRelease;

    /**
     * Creates a new instance of PublicationManager
     */
    public PublicationManager() {
    }

    @PostConstruct
    public void init() {
        logger.log(Level.INFO, "init publication manager");
        try {
            releases = releaseService.getAllReleases();
            if (releases != null && !releases.isEmpty()) {
                latestRelease = releases.get(0); // releases are assumed in descending order of release date
            } else {
                logger.log(Level.INFO, "No release information in the database.");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not initialize Publication Manager: " + e.getMessage(), e);
        }
    }

    public void onAdd() {
        try {
            logger.log(Level.INFO, "Adding a new Release");
            if (inputRelease.getReleaseId() == null) {
                showMessage(FacesMessage.SEVERITY_ERROR, "Release ID is empty", " ");
            }
            inputRelease = releaseService.createNewRelease(inputRelease);
            showMessage(FacesMessage.SEVERITY_INFO, "A new Release was successfully published.", " ");
        } catch (Exception e) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
            logger.log(Level.SEVERE, "Error adding publication: " + e.getMessage(), e);
        } finally {
            init();
        }
    }

    //TODO: Move showMessage to a common place
    private void showMessage(FacesMessage.Severity severity, String summary, String message) {
        FacesContext context = FacesContext.getCurrentInstance();

        context.addMessage(null, new FacesMessage(severity, summary, message));
        // FacesMessage n = new FacesMessage();
    }

    public List<NameRelease> getReleases() {
        return releases;
    }

    public void setReleases(List<NameRelease> releases) {
        this.releases = releases;
    }

    public NameRelease getSelectedRelease() {
        return selectedRelease;
    }

    public void setSelectedRelease(NameRelease selectedRelease) {
        this.selectedRelease = selectedRelease;
    }

    public NameRelease getInputRelease() {
        return inputRelease;
    }

    public NameRelease getLatestRelease() {
        return latestRelease;
    }
}
