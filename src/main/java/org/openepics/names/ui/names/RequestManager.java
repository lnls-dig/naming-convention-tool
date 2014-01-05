/*
 * This software is Copyright by the Board of Trustees of Michigan
 * State University (c) Copyright 2012.
 *
 * You may use this software under the terms of the GNU public license
 *  (GPL). The terms of this license are described at:
 *       http://www.gnu.org/licenses/gpl.txt
 *
 * Contact Information:
 *   Facility for Rare Isotope Beam
 *   Michigan State University
 *   East Lansing, MI 48824-1321
 *   http://frib.msu.edu
 *
 */
package org.openepics.names.ui.names;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import org.openepics.names.model.NameCategory;
import org.openepics.names.model.NameEvent;
import org.openepics.names.model.NameEventStatus;
import org.openepics.names.model.NameEventType;
import org.openepics.names.model.NameHierarchy;
import org.openepics.names.services.NamesEJB;
import org.openepics.names.ui.names.NameView.Change;

/**
 * Manages Change Requests (backing bean for request-sub.xhtml)
 *
 * @author Vasu V <vuppala@frib.msu.org>
 */
@ManagedBean
@ViewScoped
public class RequestManager implements Serializable {

    @EJB
    private NamesEJB namesEJB;
    private static final Logger logger = Logger.getLogger("org.openepics.names.ui.RequestManager");
    private List<NameEvent> validNames;
    private NameEvent selectedName;
    private List<NameEvent> filteredNames;
    private List<NameEvent> historyEvents;
    private boolean myRequest = false; // user is looking at 'his'her' requests
    // i.e. 'option' param is 'user'
    private String option = null; // option parameter
    // Input parameters from input page
    private Integer newCategoryID;
    private Integer newParentID;
    private String newCode;
    private String newDescription;
    private String newComment;

    private List<NameEvent> parentCandidates;

    @PostConstruct
    public void init() {
        try {
            if (option == null) {
                option = (String) FacesContext.getCurrentInstance()
                        .getExternalContext().getRequestParameterMap()
                        .get("option");
            }

            if (option == null) {
                validNames = namesEJB.getValidNames();
                myRequest = false;
            } else if ("user".equals(option)) {
                validNames = namesEJB.getUserRequests();
                myRequest = true;
            }
            newCode = newDescription = newComment = null;
            newCategoryID = newParentID = null;
            selectedName = (validNames == null || validNames.size() == 0) ? null : validNames.get(0);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not initialize Request Manager.");
            System.err.println(e);
        }
    }

    public void onModify() {
        NameEvent newRequest;

        try {
            logger.log(Level.INFO, "Modifying ");
            newRequest = namesEJB.createNewEvent(selectedName.getNameId(), newCode, newDescription, newCategoryID, newParentID, NameEventType.MODIFY, newComment);
            showMessage(FacesMessage.SEVERITY_INFO, "Your request was successfully submitted.", "Request Number: " + newRequest.getId());
        } catch (Exception e) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Encountered an error", e.getMessage());
            System.err.println(e);
        } finally {
            init();
        }
    }

    public void onAdd() {
        NameEvent newRequest;

        try {
            logger.log(Level.INFO, "Adding...");
            if (newCode == null || newCode.isEmpty()) {
                showMessage(FacesMessage.SEVERITY_ERROR, "Code is empty", " ");
            }
            String newCategoryName = namesEJB.findEventById(newCategoryID).getName();
            newRequest = namesEJB.createNewEvent("", newCode, newDescription, newCategoryID, newParentID, NameEventType.INSERT, newComment);
            showMessage(FacesMessage.SEVERITY_INFO, "Your request was successfully submitted.", "Request Number: " + newRequest.getId());
        } catch (Exception e) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Encountered an error", e.getMessage());
            System.err.println(e);
        } finally {
            init();
        }
    }

    /*
     * Has the selectedName been processed?
     */
    public boolean selectedEventProcessed() {
        return selectedName == null ? false : selectedName.getStatus() != NameEventStatus.PROCESSING;
    }

    public String getRequestType(NameView req) {
        Change change = req.getPendingChange();
        if(change instanceof NameView.AddChange) return "Add request";
        if(change instanceof NameView.ModifiyChange) return "Modify request";
        if(change instanceof NameView.DeleteChange) return "Delete request";
        return "Unknown";
    }

    public String getNewPath(NameView req) {
        StringBuilder outputStr = new StringBuilder();
        List<String> path = req.getNamePath();
        for(int i = 0; i < path.size() - 1; i++)
            outputStr.append(path.get(i)).append(" ▸ ");
        Change change = req.getPendingChange();
        if(change instanceof NameView.ModifiyChange)
            outputStr.append(((NameView.ModifiyChange)change).getNewName());
        else
            outputStr.append(req.getName());

        return outputStr.toString();
    }

    public String getNewFullPath(NameView req) {
        StringBuilder outputStr = new StringBuilder();
        List<String> path = req.getFullNamePath();
        for(int i = 0; i < path.size() - 1; i++)
            outputStr.append(path.get(i)).append(" ▸ ");
        Change change = req.getPendingChange();
        if(change instanceof NameView.ModifiyChange)
            outputStr.append(((NameView.ModifiyChange)change).getNewFullName());
        else
            outputStr.append(req.getFullName());

        return outputStr.toString();
    }

    public boolean isModified(NameView req) {
        return req.getPendingChange() instanceof NameView.ModifiyChange;
    }

    public String getNewNameStyle(NameView req) {
        Change change = req.getPendingChange();
        if(change == null) return "";
        switch(change.getStatus()) {
            case CANCELLED: return "font-style: italic; color: #BFBFBF;";
            case REJECTED: return "font-style: italic; color: #632523; font-weight: bold;";
            case PROCESSING:
                if(change instanceof NameView.AddChange) return "color: #366092;";
                if(change instanceof NameView.ModifiyChange) return "color: #963634;";
                if(change instanceof NameView.DeleteChange) return "color: #F5A52F; text-decoration: line-through;";
                break;
        }
        return "text-decoration: blink;";
    }

    public void onDelete() {
        NameEvent newRequest;

        try {
            if (selectedName == null) {
                showMessage(FacesMessage.SEVERITY_ERROR, "Error:",
                        "You did not select any name.");
                return;
            }

            logger.log(Level.INFO, "Deleting ");
            Integer categoryID = selectedName.getNameCategory() == null ? null : selectedName.getNameCategory().getId();
            Integer parentID = selectedName.getParentName() == null ? null : selectedName.getParentName().getId();
            newRequest = namesEJB.createNewEvent(selectedName.getNameId(), selectedName.getName(), selectedName.getFullName(), categoryID, parentID, NameEventType.DELETE, newComment);
            showMessage(FacesMessage.SEVERITY_INFO,
                    "Your request was successfully submitted.",
                    "Request Number: " + newRequest.getId());
        } catch (Exception e) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Encountered an error",
                    e.getMessage());
            System.err.println(e);
        } finally {
            init();
        }
    }

    public void onCancel() {

        try {
            if (selectedName == null) {
                showMessage(FacesMessage.SEVERITY_ERROR, "Error:",
                        "You did not select any request.");
                return;
            }
            logger.log(Level.INFO, "Cancelling ");
            namesEJB.cancelRequest(selectedName.getId(), newComment);
            showMessage(FacesMessage.SEVERITY_INFO,
                    "Your request has been cancelled.", "Request Number: ");
        } catch (Exception e) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Encountered an error",
                    e.getMessage());
            System.err.println(e);
        } finally {
            init();
        }
    }

    private void showMessage(FacesMessage.Severity severity, String summary,
            String message) {
        FacesContext context = FacesContext.getCurrentInstance();

        context.addMessage(null, new FacesMessage(severity, summary, message));
        FacesMessage n = new FacesMessage();

    }

    // TODO: merge with same method in NamesManager
    public void findHistory() {
        try {
            if (selectedName == null) {
                showMessage(FacesMessage.SEVERITY_ERROR, "Error",
                        "You must select a name first.");
                historyEvents = null;
                return;
            }
            logger.log(Level.FINE, "history ");
            historyEvents = namesEJB.findEventsByName(selectedName.getNameId());
			// showMessage(FacesMessage.SEVERITY_INFO,
            // "Your request was successfully submitted.", "Request Number: " +
            // newRequest.getId());
        } catch (Exception e) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Encountered an error",
                    e.getMessage());
            System.err.println(e);
        } finally {
        }
    }

    /* --------------------------- */
    public List<NameView> getValidNames() {
        return Lists.transform(validNames, new Function<NameEvent, NameView>() {
            @Override public NameView apply(NameEvent nameEvent) {
                return new NameView(nameEvent, null);
            }
        });
    }

    public NameView getSelectedName() {
        return selectedName != null ? new NameView(selectedName, null) : null;
    }

    public void setSelectedName(NameView selectedName) {
        this.selectedName = selectedName.getNameEvent();
    }

    public List<NameEvent> getFilteredNames() {
        return filteredNames;
    }

    public void setFilteredNames(List<NameEvent> filteredNames) {
        this.filteredNames = filteredNames;
    }

    public Integer getNewCategoryID() {
        return newCategoryID;
    }

    public void setNewCategoryID(Integer newCategoryID) {
        this.newCategoryID = newCategoryID;
    }

    public Integer getNewParentID() {
        return newParentID;
    }

    public void setNewParentID(Integer newParentID) {
        this.newParentID = newParentID;
    }

    public String getNewCode() {
        return newCode;
    }

    public void setNewCode(String newCode) {
        this.newCode = newCode;
    }

    public String getNewDescription() {
        return newDescription;
    }

    public void setNewDescription(String newDescription) {
        this.newDescription = newDescription;
    }

    public String getNewComment() {
        return newComment;
    }

    public void setNewComment(String newComment) {
        this.newComment = newComment;
    }

    public boolean isMyRequest() {
        return myRequest;
    }

    public List<NameEvent> getHistoryEvents() {
        return historyEvents;
    }

    public List<NameEvent> getParentCandidates() {
        return parentCandidates;
    }

    public void setParentCandidates(List<NameEvent> parentCandidates) {
        this.parentCandidates = parentCandidates;
    }

    public void loadParentCandidates() {
        if (newCategoryID != null) {
            final NameHierarchy nameHierarchy = namesEJB.getNameHierarchy();
            final @Nullable NameCategory category = namesEJB.findCategoryById(newCategoryID);
            if (category != null) {
                final @Nullable NameCategory parentCategory = getParentCategory(category);
                setParentCandidates(parentCategory != null ? namesEJB.findEventsByCategory(parentCategory) : ImmutableList.<NameEvent>of());
            }
        }
    }

    public boolean isParentSelectable() {
        final @Nullable NameCategory category = newCategoryID != null ? namesEJB.findCategoryById(newCategoryID) : null;
        return category != null && getParentCategory(category) != null;
    }

    private @Nullable NameCategory getParentCategory(NameCategory nameCategory) {
        final NameHierarchy nameHierarchy = namesEJB.getNameHierarchy();
        final int sectionIndex = nameHierarchy.getSectionLevels().indexOf(nameCategory);
        final int deviceTypeIndex = nameHierarchy.getDeviceTypeLevels().indexOf(nameCategory);
        if (sectionIndex >= 0) {
            return sectionIndex > 0 ? nameHierarchy.getSectionLevels().get(sectionIndex - 1) : null;
        } else {
            return deviceTypeIndex > 0 ? nameHierarchy.getDeviceTypeLevels().get(deviceTypeIndex - 1) : null;
        }
    }
}
