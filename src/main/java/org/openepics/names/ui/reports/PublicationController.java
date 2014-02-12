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
package org.openepics.names.ui.reports;

import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.openepics.names.model.NameRelease;
import org.openepics.names.services.ReleaseService;

/**
 * Manages naming system releases
 *
 * @author Vasu V <vuppala@frib.msu.org>
 */
@ManagedBean
@ViewScoped
public class PublicationController implements Serializable {

    @Inject private ReleaseService releaseService;

    private List<NameRelease> releases;
    private NameRelease selectedRelease;
    private NameRelease inputRelease = new NameRelease();
    private NameRelease latestRelease;


    @PostConstruct
    public void init() {
        releases = releaseService.getAllReleases();
        if (releases != null && !releases.isEmpty()) {
            latestRelease = releases.get(0); // releases are assumed in descending order of release date
        }
    }

    public void onAdd() {
        try {
            if (inputRelease.getReleaseId() == null) {
                showMessage(FacesMessage.SEVERITY_ERROR, "Release ID is empty", " ");
            }
            inputRelease = releaseService.createNewRelease(inputRelease);
            showMessage(FacesMessage.SEVERITY_INFO, "A new Release was successfully published.", " ");
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
