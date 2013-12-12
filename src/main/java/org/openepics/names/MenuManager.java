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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.openepics.names.model.NameCategory;
import org.openepics.names.model.NameEvent;

/**
 * For generating menu items for Naming Categories
 * 
 * @author Vasu V <vuppala@frib.msu.org>
 */
@ManagedBean
@ViewScoped
public class MenuManager implements Serializable {

	private static final long serialVersionUID = 1L;
	@EJB
	private NamesEJBLocal namesEJB;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger("org.openepics.names");

	private List<SelectItem> categories;

	public List<SelectItem> parents;

	/**
	 * Creates a new instance of MenuManager
	 */
	public MenuManager() {
	}

	@PostConstruct
	private void init() {
		categories = new ArrayList<>();
		List<NameCategory> lcategories = namesEJB.getCategories();

		for (NameCategory cat : lcategories) {
			categories.add(new SelectItem(cat.getId(), cat.getName()));
		}

		parents = new ArrayList<>();
		List<NameEvent> names = namesEJB.getValidNames();
		for (NameEvent name : names) {
			parents.add(new SelectItem(name.getId(), name.getName()));
		}
	}

	public List<SelectItem> getCategories() {
		return categories;
	}

	public List<SelectItem> getParents() {
		return parents;
	}

}
