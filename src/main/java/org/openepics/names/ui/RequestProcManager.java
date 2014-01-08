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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartRevisionStatus;
import org.openepics.names.services.NamePartService;
import org.openepics.names.services.NamesEJB;
import org.openepics.names.ui.names.NamePartView;

/**
 * Manages Request Processing (backing bean for request-proc.xhtml)
 *
 * @author Vasu V <vuppala@frib.msu.org>
 */
@ManagedBean(name = "requestProcManager")
@ViewScoped
public class RequestProcManager implements Serializable {

    @EJB
    private NamesEJB namesEJB;
    @Inject NamePartService namePartService;

    private static final Logger logger = Logger.getLogger("org.openepics.names.ui.RequestProcManager");
    private List<NamePartView> pendingNames;
    private List<NamePart> events;
    private List<NamePartView> selectedEvents;
    private List<NamePartView> filteredEvents;
    private List<NamePartRevision> historyEvents;
    // Input parameters
    private String procComments;

    /**
     * Creates a new instance of RequestProcManager
     */
    public RequestProcManager() {
    }

    @PostConstruct
    public void init() {
        try {
            events = namePartService.getPendingNames(null, true);
            pendingNames = events.size() > 0 ? new ArrayList<NamePartView>() : null;
            for(NamePart entry : events) {
                List<NamePartRevision> history = namePartService.getRevisions(entry);
                NamePartRevision pendingRevision = history.get(history.size() - 1);
                NamePartRevision currentRevision = history.size() > 1 ? history.get(history.size() - 2) : null;
                pendingNames.add(new NamePartView(namePartService, currentRevision, pendingRevision));
            }
            procComments = null;
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void onApprove() {
        try {
            // TODO waiting on service
            logger.log(Level.INFO, "Approving ");
            namesEJB.processEvents((NamePartRevision[])selectedEvents.toArray(), NamePartRevisionStatus.APPROVED, procComments);
            showMessage(FacesMessage.SEVERITY_INFO, "All selected requests were successfully approved.", " ");
        } catch (Exception e) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            init();
        }
    }

    public void onReject() {
        try {
            // TODO waiting on service
            logger.log(Level.INFO, "Rejecting ");
            namesEJB.processEvents((NamePartRevision[])selectedEvents.toArray(), NamePartRevisionStatus.REJECTED, procComments);
            showMessage(FacesMessage.SEVERITY_INFO, "All selected requests were successfully rejected.", " ");
        } catch (Exception e) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            init();
        }
    }

    //TODO: merge with same method in NamesManager
    public void findHistory() {
        try {
            if (selectedEvents == null || selectedEvents.isEmpty()) {
                showMessage(FacesMessage.SEVERITY_ERROR, "Error", "You must select a name first.");
                historyEvents = null;
                return;
            }
            historyEvents = new ArrayList<>();
            for (NamePartView event : selectedEvents) {
                historyEvents.addAll(namePartService.getRevisions(event.getNamePart()));
            }
        } catch (Exception e) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
        }
    }

    private void showMessage(FacesMessage.Severity severity, String summary, String message) {
        FacesContext context = FacesContext.getCurrentInstance();

        context.addMessage(null, new FacesMessage(severity, summary, message));
        FacesMessage n = new FacesMessage();

    }

    public NamePartView[] getSelectedEvents() {
        return selectedEvents == null ? null : (NamePartView[])selectedEvents.toArray();
    }

    public void setSelectedEvents(NamePartView[] selectedEvents) {
        this.selectedEvents = null;
        if(selectedEvents == null || selectedEvents.length == 0) return;

        this.selectedEvents = new ArrayList<>();
        this.selectedEvents.addAll(Arrays.asList(selectedEvents));
    }

    public List<NamePartView> getFilteredEvents() {
        return filteredEvents;
    }

    public void setFilteredEvents(List<NamePartView> filteredEvents) {
        this.filteredEvents = filteredEvents;
    }

    public String getComments() {
        return procComments;
    }

    public void setComments(String comments) {
        this.procComments = comments;
    }

    public List<NamePartView> getEvents() {
        return pendingNames;
    }

    public List<NamePartView> getHistoryEvents() {
        return historyEvents == null ? null : Lists.transform(historyEvents, new Function<NamePartRevision, NamePartView>() {
            @Override public NamePartView apply(NamePartRevision nameEvent) {
                return new NamePartView(namePartService, nameEvent, null);
            }
        });
    }
}
