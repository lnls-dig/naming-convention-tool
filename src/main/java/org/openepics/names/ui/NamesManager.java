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
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.openepics.names.model.NameCategory;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NameRelease;
import org.openepics.names.services.NamePartService;
import org.openepics.names.services.NamesEJB;
import org.openepics.names.ui.names.NamePartView;

/**
 * Manages naming events.
 *
 * @author Vasu V <vuppala@frib.msu.org>
 */
@ManagedBean
@ViewScoped
public class NamesManager implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private NamesEJB namesEJB;
    @Inject private NamePartService namePartService;
    @ManagedProperty(value = "#{publicationManager}") private PublicationManager pubManager;

    private static final Logger logger = Logger.getLogger("org.openepics.names.ui.NamesManager");
    private List<NamePartView> standardNames;
    private NamePartView selectedName;
    private List<NamePartView> filteredNames;
    private List<NamePartRevision> historyEvents;
    private boolean showDeletedNames = false;
    private NameCategory currentCategory;

    /**
     * Creates a new instance of NamesManager
     */
    public NamesManager() {
    }

    @PostConstruct
    public void init() {
        try {
            String category = (String) FacesContext.getCurrentInstance()
                    .getExternalContext().getRequestParameterMap()
                    .get("category");
            if (category == null) {
                currentCategory = null;
            } else {
                currentCategory = findCategoryByName(category);
            }
            refreshNames();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not initialize NameManager: " + e.getMessage(), e);
        }
    }

    public void refreshNames() {
        standardNames = Lists.transform(namePartService.getApprovedOrPendingNames(currentCategory, showDeletedNames), new Function<NamePart, NamePartView>() {
            @Override public NamePartView apply(NamePart namePart) {
                return ViewFactory.getView(namePart);
            }
        });
    }

    public void findHistory() {
        if (selectedName == null) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Error", "You must select a name first.");
            historyEvents = null;
        } else {
            historyEvents = namePartService.getRevisions(selectedName.getNamePart());
        }
    }

    public String nameStatus(NamePartRevision nreq) {
        switch (nreq.getStatus()) {
            case PROCESSING:
                return "In-Process";
            case CANCELLED:
                return "Cancelled";
            case REJECTED:
                return "Rejected";
            case APPROVED:
                final NameRelease latestRelease = pubManager.getLatestRelease();
                final boolean processedBeforeLatestRelease = latestRelease != null && nreq.getProcessDate() != null && nreq.getProcessDate().before(latestRelease.getReleaseDate());
                switch (nreq.getRevisionType()) {
                    case INSERT:
                        return processedBeforeLatestRelease ? "Published" : "Added";
                    case MODIFY:
                        return processedBeforeLatestRelease ? "Published" : "Modified";
                    case DELETE:
                        return "Deleted";
                    default:
                        return "unknown";
                }
            default:
                return "unknown";
        }
    }

    public String nameViewStatus(NamePartView entry) {
        switch (entry.getNameEvent().getStatus()) {
            case PROCESSING:
                return "Processing";
            case CANCELLED:
                return "Cancelled";
            case REJECTED:
                return "Rejcted";
            case APPROVED:
                if (isPublished(entry)) {
                    return "Published";
                }
                return "Approved";
            default:
                return "unknown";
        }
    }

    public String nameViewClass(NamePartView entry) {
        switch (entry.getNameEvent().getStatus()) {
            case PROCESSING:
                return "Processing";
            case CANCELLED:
            case REJECTED:
                return "default";
            case APPROVED:
                if (isPublished(entry)) {
                    return "Published";
                }
                return "Approved";
            default:
                return "unknown";
        }
    }

    public String getPath(NamePartView req) {
        return Joiner.on(" ▸ ").join(req.getNamePath());
    }

    public String getFullPath(NamePartView req) {
        return Joiner.on(" ▸ ").join(req.getFullNamePath());
    }

    public boolean isPublished(NamePartView entry) {
        if (entry.getNameEvent().getProcessDate() == null) {
            return false;
        }

        List<NameRelease> releases = namesEJB.getAllReleases();
        return (releases.size() > 0 && !releases.get(0).getReleaseDate().before(entry.getNameEvent().getProcessDate()));
    }

    private void showMessage(FacesMessage.Severity severity, String summary,
            String message) {
        FacesContext context = FacesContext.getCurrentInstance();

        context.addMessage(null, new FacesMessage(severity, summary, message));
        // FacesMessage n = new FacesMessage();
    }

    // TODO no usage found
    // public boolean isUnderChange(NamePartRevision nevent) {
    //     return namesEJB.isUnderChange(nevent);
    // }

    public void setPubManager(PublicationManager pubMgr) {
        this.pubManager = pubMgr;
    }

    public NamePartView getSelectedName() {
        return selectedName;
    }

    public void setSelectedName(NamePartView selectedName) {
        this.selectedName = selectedName;
    }

    public List<NamePartView> getFilteredNames() {
        return filteredNames;
    }

    public void setFilteredNames(List<NamePartView> filteredNames) {
        this.filteredNames = filteredNames;
    }

    public List<NamePartView> getStandardNames() {
        return standardNames;
    }

    public List<NamePartView> getHistoryEvents() {
        return historyEvents == null ? null : Lists.transform(historyEvents, new Function<NamePartRevision, NamePartView>() {
            @Override public NamePartView apply(NamePartRevision namePartRevision) {
                return ViewFactory.getView(namePartRevision);
            }
        });
    }

    public boolean isShowDeletedNames() {
        return showDeletedNames;
    }

    public void setShowDeletedNames(boolean showDeletedNames) {
        this.showDeletedNames = showDeletedNames;
    }

    private NameCategory findCategoryByName(String name) {
        List<NameCategory> categories = namePartService.getNameCategories();
        for (NameCategory category : categories)
            if (category.getDescription().equalsIgnoreCase(name)) return category;
        return null;
    }
}
