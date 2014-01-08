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
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import org.openepics.names.model.NameCategory;
import org.openepics.names.services.NamePartService;
import org.primefaces.component.menuitem.MenuItem;
import org.primefaces.model.DefaultMenuModel;
import org.primefaces.model.MenuModel;

/**
 * For generating menu items for Naming Categories
 *
 * @author Vasu V <vuppala@frib.msu.org>
 */
@ManagedBean
@ViewScoped
public class MenuManager implements Serializable {

	private static final long serialVersionUID = 1L;
    @Inject private NamePartService namePartService;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger("org.openepics.names.ui.MenuManager");

	private List<NameCategory> categories;

	// private List<NamePartRevision> parents;   // TODO remove

	private MenuModel model;

	/**
	 * Creates a new instance of MenuManager
	 */
	public MenuManager() {
	}

	@PostConstruct
	private void init() {
		model = new DefaultMenuModel();

        categories = namePartService.getNameCategories();

        MenuItem item = new MenuItem();
        item.setId("__" );
		item.setValue("All");
		item.setUrl("/names.xhtml");
        item.setStyle("font-style: italic");
		model.addMenuItem(item);


		for (NameCategory cat : categories) {
			item = new MenuItem();
			item.setId("_" + cat.getId());
			item.setValue(cat.getDescription());
			item.setUrl("/names.xhtml?category=" + cat.getName());
			model.addMenuItem(item);
		}

		// parents = namesEJB.getValidNames();   // TODO remove
	}

	public List<NameCategory> getCategories() {
		return categories;
	}

    // TODO remove
	//public List<NamePartRevision> getParents() {
	//	return parents;
	//}

	public MenuModel getModel() {
		return model;
	}

}
