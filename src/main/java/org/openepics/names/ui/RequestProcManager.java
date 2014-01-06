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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import org.openepics.names.model.NameEvent;
import org.openepics.names.model.NameEventStatus;
import org.openepics.names.services.NamesEJB;
import org.openepics.names.ui.names.NameView;

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
    private static final Logger logger = Logger.getLogger("org.openepics.names.ui.RequestProcManager");
    private List<NameEvent> events;
    private List<NameEvent> selectedEvents;
    private List<NameEvent> filteredEvents;
    private List<NameEvent> historyEvents;
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
            events = namesEJB.getUnprocessedEvents();
            procComments = null;
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public void onApprove() {
        try {
            logger.log(Level.INFO, "Approving ");
            namesEJB.processEvents((NameEvent[])selectedEvents.toArray(), NameEventStatus.APPROVED, procComments);
            showMessage(FacesMessage.SEVERITY_INFO, "All selected requests were successfully approved.", " ");
        } catch (Exception e) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Encountered an error", e.getMessage());
            System.err.println(e);
        } finally {
            init();
        }
    }

    public void onReject() {
        try {
            logger.log(Level.INFO, "Rejecting ");
            namesEJB.processEvents((NameEvent[])selectedEvents.toArray(), NameEventStatus.REJECTED, procComments);
            showMessage(FacesMessage.SEVERITY_INFO, "All selected requests were successfully rejected.", " ");
        } catch (Exception e) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Encountered an error", e.getMessage());
            System.err.println(e);
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
            // logger.log(Level.INFO, "history ");
            historyEvents = new ArrayList<>();
            for (NameEvent event : selectedEvents ) {
                historyEvents.addAll(namesEJB.findEventsByName(event.getNameId()));
            }
            // showMessage(FacesMessage.SEVERITY_INFO, "Your request was successfully submitted.", "Request Number: " + newRequest.getId());
        } catch (Exception e) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Encountered an error", e.getMessage());
            System.err.println(e);
        } finally {
        }
    }

    private void showMessage(FacesMessage.Severity severity, String summary, String message) {
        FacesContext context = FacesContext.getCurrentInstance();

        context.addMessage(null, new FacesMessage(severity, summary, message));
        FacesMessage n = new FacesMessage();

    }

    public NameView[] getSelectedEvents() {
        return selectedEvents == null ? null : (NameView[])Lists.transform(selectedEvents, new Function<NameEvent, NameView>() {
            @Override public NameView apply(NameEvent nameEvent) {
                return new NameView(nameEvent, null);
            }
        }).toArray();
    }

    public void setSelectedEvents(NameView[] selectedEvents) {
        this.selectedEvents = null;
        if(selectedEvents == null) return;

        ArrayList<NameEvent> selection = new ArrayList<>();
        for(NameView event : selectedEvents)
            selection.add(event.getNameEvent());

        this.selectedEvents = selection;
    }

    public List<NameEvent> getFilteredEvents() {
        return filteredEvents;
    }

    public void setFilteredEvents(List<NameEvent> filteredEvents) {
        this.filteredEvents = filteredEvents;
    }

    public String getComments() {
        return procComments;
    }

    public void setComments(String comments) {
        this.procComments = comments;
    }

    public List<NameView> getEvents() {
        return Lists.transform(events, new Function<NameEvent, NameView>() {
            @Override public NameView apply(NameEvent nameEvent) {
                return new NameView(nameEvent, null);
            }
        });
    }

    public List<NameView> getHistoryEvents() {
        return historyEvents == null ? null : Lists.transform(historyEvents, new Function<NameEvent, NameView>() {
            @Override public NameView apply(NameEvent nameEvent) {
                return new NameView(nameEvent, null);
            }
        });
    }
}
