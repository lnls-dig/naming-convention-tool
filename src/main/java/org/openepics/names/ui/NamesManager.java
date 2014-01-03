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
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import org.openepics.names.services.NamesEJB;
import org.openepics.names.model.NameEvent;
import org.openepics.names.model.NameRelease;

/**
 * Manages naming events.
 *
 * @author Vasu V <vuppala@frib.msu.org>
 */
@ManagedBean
@ViewScoped
public class NamesManager implements Serializable {

	private static final long serialVersionUID = 1L;
	@EJB
	private NamesEJB namesEJB;
	@ManagedProperty(value = "#{publicationManager}")
	private PublicationManager pubManager;
	private static final Logger logger = Logger.getLogger("org.openepics.names.ui.NamesManager");
	private List<NameEvent> standardNames;
	private NameEvent selectedName;
	private List<NameEvent> filteredNames;
	private List<NameEvent> historyEvents;
	private boolean showDeletedNames = false;
	private String currentCategory;

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
				currentCategory = "%"; // ToDo: Use a better method.
			} else {
				currentCategory = category;
			}
			refreshNames();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Could not initialize NameManager.");
			System.err.println(e);
		}
	}

	public void refreshNames() {
		standardNames = namesEJB.getStandardNames(currentCategory,
				showDeletedNames);
	}

	public void findHistory() {
		try {
			if (selectedName == null) {
				showMessage(FacesMessage.SEVERITY_ERROR, "Error",
						"You must select a name first.");
				historyEvents = null;
				return;
			}
			logger.log(Level.INFO, "history ");
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

	public String nameStatus(NameEvent nreq) {
		switch (nreq.getStatus()) {
			case PROCESSING : return "In-Process";
			case CANCELLED: return "Cancelled";
			case REJECTED: return "Rejected";
			case APPROVED:
				final NameRelease latestRelease = pubManager.getLatestRelease();
				final boolean processedBeforeLatestRelease = latestRelease != null && nreq.getProcessDate() != null && nreq.getProcessDate().before(latestRelease.getReleaseDate());
				switch (nreq.getEventType()) {
					case INSERT: return processedBeforeLatestRelease ? "Published" : "Added";
					case MODIFY: return processedBeforeLatestRelease ? "Published" : "Modified";
					case DELETE: return "Deleted";
				default:
					return "unknown";
				}
			default: return "unknown";
		}
	}

	private void showMessage(FacesMessage.Severity severity, String summary,
			String message) {
		FacesContext context = FacesContext.getCurrentInstance();

		context.addMessage(null, new FacesMessage(severity, summary, message));
		FacesMessage n = new FacesMessage();

	}

	public boolean isUnderChange(NameEvent nevent) {
		return namesEJB.isUnderChange(nevent);
	}

	public void setPubManager(PublicationManager pubMgr) {
		this.pubManager = pubMgr;
	}

	public NameEvent getSelectedName() {
		return selectedName;
	}

	public void setSelectedName(NameEvent selectedName) {
		this.selectedName = selectedName;
	}

	public List<NameEvent> getFilteredNames() {
		return filteredNames;
	}

	public void setFilteredNames(List<NameEvent> filteredNames) {
		this.filteredNames = filteredNames;
	}

	public List<NameEvent> getStandardNames() {
		return standardNames;
	}

	public List<NameEvent> getHistoryEvents() {
		return historyEvents;
	}

	public boolean isShowDeletedNames() {
		return showDeletedNames;
	}

	public void setShowDeletedNames(boolean showDeletedNames) {
		this.showDeletedNames = showDeletedNames;
	}
}