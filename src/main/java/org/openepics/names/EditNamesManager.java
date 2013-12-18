package org.openepics.names;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.openepics.names.model.NCName;
import org.openepics.names.model.NCName.NCNameStatus;
import org.openepics.names.model.NameCategory;
import org.openepics.names.model.NameEvent;
import org.openepics.names.nc.NamingConventionEJBLocal;
import org.openepics.names.nc.NamingConventionEJBLocal.ESSNameConstructionMethod;

@ManagedBean
@ViewScoped
public class EditNamesManager implements Serializable {

	private static final long serialVersionUID = 1L;
	@EJB
	private NamingConventionEJBLocal ncEJB;
	@EJB
	private NamesEJBLocal namesEJB;
	private static final Logger logger = Logger.getLogger("org.openepics.names");

	private Integer superSectionID;
	private Integer sectionID;
	private Integer subsectionID;
	private Integer disciplineID;
	private Integer categoryID;
	private Integer genDeviceID;
	private Integer specDeviceID;

	private NCName selectedNCName;

	private List<NameEvent> superSectionNames;
	private List<NameEvent> sectionNames;
	private List<NameEvent> subsectionNames;
	private List<NameEvent> disciplineNames;
	private List<NameEvent> categoryNames;
	private List<NameEvent> genDevNames;
	private List<NameEvent> specDevNames;

	private List<NCName> allNCNames;

	public EditNamesManager() {
		// EMPTY
	}

	@PostConstruct
	public void init() {
		loadSuperSections();
		loadSections();
		loadSubsections();
		loadDisciplines();
		loadCategories();
		loadGenericDevices();
		loadSpecificDevices();
		loadAllNCNames();
	}

	public void onAdd() {
		NCName newNCName;

		try {
			logger.log(Level.INFO, "Adding NC Name");
			if (subsectionID == null || genDeviceID == null) {
				showMessage(FacesMessage.SEVERITY_ERROR, "Required field missing", " ");
			}
			NameEvent subsection = namesEJB.findEventById(subsectionID);
			NameEvent genDevice = namesEJB.findEventById(genDeviceID);
			newNCName = ncEJB.createNCNameDevice(subsection, genDevice, ESSNameConstructionMethod.ACCELERATOR);
			showMessage(FacesMessage.SEVERITY_INFO,
					"NC Name successfully added.",
					"Name: " + newNCName.getName());
		} catch (Exception e) {
			showMessage(FacesMessage.SEVERITY_ERROR, "Encountered an error",
					e.getMessage());
			System.err.println(e);
		} finally {
			init();
		}
	}

	public void onModify() {
		try {
			logger.log(Level.INFO, "Modifying NC Name");
			if (subsectionID == null || genDeviceID == null || selectedNCName == null) {
				showMessage(FacesMessage.SEVERITY_ERROR, "Required field missing", " ");
			}
			NameEvent subsection = namesEJB.findEventById(subsectionID);
			NameEvent genDevice = namesEJB.findEventById(genDeviceID);
			selectedNCName.setSection(subsection);
			selectedNCName.setDiscipline(genDevice);
		} catch (Exception e) {
			showMessage(FacesMessage.SEVERITY_ERROR, "Encountered an error",
					e.getMessage());
			System.err.println(e);
		} finally {
			init();
		}
	}

	public void onDelete() {
			selectedNCName.setStatus(NCNameStatus.DELETED);
	}

    /*
     * Used in xhtml.
     */
	public void loadSuperSections() {
		try {
			List<NameCategory> categories = namesEJB.getCategories();
			NameCategory superSectionCategory = null;
			for(NameCategory category : categories) {
				if(category.getName().equalsIgnoreCase("SUP")) {
					superSectionCategory = category;
					break;
				}
			}
			logger.log(Level.INFO, "Found Super Section category: "+superSectionCategory+" "+superSectionCategory.getId());
			superSectionNames = superSectionCategory == null ? null : namesEJB.findEventsByCategory(superSectionCategory);
			logger.log(Level.INFO, "Found supersections. Total = "+superSectionNames.size());

			sectionID = null;
			if(sectionNames != null)
				sectionNames.clear();
			subsectionID = null;
			if(subsectionNames != null)
				subsectionNames.clear();

			if(superSectionNames.size() == 1)
				superSectionID = superSectionNames.get(0).getId();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Could not load supersections.");
			System.err.println(e);
		}
	}

    /*
     * Used in XHTML.
     */
	public void loadSections() {
		if(superSectionID != null) {
			try {
				NameEvent superSection = namesEJB.findEventById(superSectionID);
				sectionNames = namesEJB.findEventsByParent(superSection);
				logger.log(Level.INFO, "Found sections. Total = "+sectionNames.size());

				subsectionID = null;
				if(subsectionNames != null)
					subsectionNames.clear();

				if(sectionNames.size() == 1)
					sectionID = sectionNames.get(0).getId();
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Could not load sections.");
				System.err.println(e);
			}
		}
	}

    /*
    * Used in xhtml.
    */
	public void loadSubsections() {
		if(sectionID != null) {
			try {
				NameEvent section = namesEJB.findEventById(sectionID);
				subsectionNames = namesEJB.findEventsByParent(section);
				logger.log(Level.INFO, "Found subsections. Total = "+sectionNames.size());

				if(subsectionNames.size() == 1)
					subsectionID = subsectionNames.get(0).getId();
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Could not load subsections.");
				System.err.println(e);
			}
		}
	}

    /*
    * used in xhtml.
    */
	public void loadDisciplines() {
		try {
			List<NameCategory> categories = namesEJB.getCategories();
			NameCategory disciplineCategory = null;
			for(NameCategory category : categories) {
				if(category.getName().equalsIgnoreCase("DSCP")) {
					disciplineCategory = category;
					break;
				}
			}
			logger.log(Level.INFO, "Found Discipline category: "+disciplineCategory+" "+disciplineCategory.getId());
			disciplineNames = disciplineCategory == null ? null : namesEJB.findEventsByCategory(disciplineCategory);
			logger.log(Level.INFO, "Found disciplines. Total = "+disciplineNames.size());

			categoryID = null;
			if(categoryNames != null)
				categoryNames.clear();
			genDeviceID = null;
			if(genDevNames != null)
				genDevNames.clear();
			specDeviceID = null;
			if(specDevNames != null)
				specDevNames.clear();

			if(disciplineNames.size() == 1)
				disciplineID = disciplineNames.get(0).getId();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Could not load disciplines.");
			System.err.println(e);
		}
	}

    /*
    * Used in xhtml.
    */
	public void loadCategories() {
		if(disciplineID != null) {
			try {
				NameEvent discipline = namesEJB.findEventById(disciplineID);
				categoryNames = namesEJB.findEventsByParent(discipline);
				logger.log(Level.INFO, "Found categories. Total = "+categoryNames.size());

				genDeviceID = null;
				if(genDevNames != null)
					genDevNames.clear();
				specDeviceID = null;
				if(specDevNames != null)
					specDevNames.clear();

				if(categoryNames.size() == 1)
					categoryID = categoryNames.get(0).getId();
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Could not load categories.");
				System.err.println(e);
			}
		}
	}

    /*
    * Used in xhtml.
    */
	public void loadGenericDevices() {
		if(categoryID != null) {
			try {
				NameEvent category = namesEJB.findEventById(categoryID);
				genDevNames = namesEJB.findEventsByParent(category);
				logger.log(Level.INFO, "Found generic devices. Total = "+genDevNames.size());

				specDeviceID = null;
				if(specDevNames != null)
					specDevNames.clear();

				if(genDevNames.size() == 1)
					genDeviceID = genDevNames.get(0).getId();
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Could not load generic devices.");
				System.err.println(e);
			}
		}
	}

    /*
    * Used in xhtml.
    */
	public void loadSpecificDevices() {
		if(genDeviceID != null) {
			try {
				NameEvent genDevice = namesEJB.findEventById(genDeviceID);
				specDevNames = namesEJB.findEventsByParent(genDevice);
				logger.log(Level.INFO, "Found specific devices. Total = "+specDevNames.size());

				if(specDevNames.size() == 1)
					specDeviceID = specDevNames.get(0).getId();
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Could not load specific devices.");
				System.err.println(e);
			}
		}
	}

	public String getSelectedNCNameSectionString() {
		if(selectedNCName != null) {
			NameEvent bottomName = selectedNCName.getSection();
			String sectionString = "";
			boolean firstTime = true;
			while(bottomName != null) {
				if(firstTime)
					firstTime = false;
				else
					sectionString = " - "+sectionString;
				sectionString = bottomName.getFullName()+sectionString;
				bottomName = bottomName.getParentName();
			}
			return sectionString.trim();
		}
		return "No selection!";
	}

	public String getSelectedNCNameDisciplineString() {
		if(selectedNCName != null) {
			NameEvent bottomName = selectedNCName.getDiscipline();
			String disciplineString = "";
			boolean firstTime = true;
			while(bottomName != null) {
				if(firstTime)
					firstTime = false;
				else
					disciplineString = " - "+disciplineString;
				disciplineString = bottomName.getFullName()+disciplineString;
				bottomName = bottomName.getParentName();
			}
			return disciplineString.trim();
		}
		return "No selection!";
	}

	public void loadSelectedName() {
		if(selectedNCName != null) {
			Map<String, Integer> namePartMap = new HashMap<String, Integer>();

			NameEvent sectionNode = selectedNCName.getSection();
			while(sectionNode.getParentName() != null) {
				namePartMap.put(sectionNode.getNameCategory().getName(), sectionNode.getId());
				sectionNode = sectionNode.getParentName();
			}
			namePartMap.put(sectionNode.getNameCategory().getName(), sectionNode.getId());

			NameEvent disciplineNode = selectedNCName.getDiscipline();
			while(disciplineNode.getParentName() != null) {
				namePartMap.put(disciplineNode.getNameCategory().getName(), sectionNode.getId());
				sectionNode = disciplineNode.getParentName();
			}
			namePartMap.put(disciplineNode.getNameCategory().getName(), sectionNode.getId());

			init();

			for(String categoryName : namePartMap.keySet()) {
				if(categoryName.equalsIgnoreCase("SUP")) {
					superSectionID = namePartMap.get(categoryName);
				} else if(categoryName.equalsIgnoreCase("SECT")) {
					sectionID = namePartMap.get(categoryName);
				} else if(categoryName.equalsIgnoreCase("SUB")) {
					subsectionID = namePartMap.get(categoryName);
				} else if(categoryName.equalsIgnoreCase("DSCP")) {
					disciplineID = namePartMap.get(categoryName);
				} else if(categoryName.equalsIgnoreCase("CAT")) {
					categoryID = namePartMap.get(categoryName);
				} else if(categoryName.equalsIgnoreCase("GDEV")) {
					genDeviceID = namePartMap.get(categoryName);
				} else if(categoryName.equalsIgnoreCase("SDEV")) {
					specDeviceID = namePartMap.get(categoryName);
				}
			}
		}
	}

	private void loadAllNCNames() {
		setAllNCNames(ncEJB.getAllNCNames());
	}

	public List<NameEvent> getSuperSectionNames() {
		return superSectionNames;
	}

	public void setSuperSectionNames(List<NameEvent> superSectionNames) {
		this.superSectionNames = superSectionNames;
	}

	public List<NameEvent> getSectionNames() {
		return sectionNames;
	}

	public void setSectionNames(List<NameEvent> sectionNames) {
		this.sectionNames = sectionNames;
	}

	public List<NameEvent> getSubsectionNames() {
		return subsectionNames;
	}

	public void setSubsectionNames(List<NameEvent> subsectionNames) {
		this.subsectionNames = subsectionNames;
	}

	public List<NameEvent> getDisciplineNames() {
		return disciplineNames;
	}

	public void setDisciplineNames(List<NameEvent> disciplineNames) {
		this.disciplineNames = disciplineNames;
	}

	public List<NameEvent> getCategoryNames() {
		return categoryNames;
	}

	public void setCategoryNames(List<NameEvent> categoryNames) {
		this.categoryNames = categoryNames;
	}

	public List<NameEvent> getGenDevNames() {
		return genDevNames;
	}

	public void setGenDevNames(List<NameEvent> genDevNames) {
		this.genDevNames = genDevNames;
	}

	public List<NameEvent> getSpecDevNames() {
		return specDevNames;
	}

	public void setSpecDevNames(List<NameEvent> specDevNames) {
		this.specDevNames = specDevNames;
	}

	public Integer getSuperSectionID() {
		return superSectionID;
	}

	public void setSuperSectionID(Integer superSectionID) {
		this.superSectionID = superSectionID;
	}

	public Integer getSectionID() {
		return sectionID;
	}

	public void setSectionID(Integer sectionID) {
		this.sectionID = sectionID;
	}

	public Integer getSubsectionID() {
		return subsectionID;
	}

	public void setSubsectionID(Integer subsectionID) {
		this.subsectionID = subsectionID;
	}

	public Integer getDisciplineID() {
		return disciplineID;
	}

	public void setDisciplineID(Integer disciplineID) {
		this.disciplineID = disciplineID;
	}

	public Integer getCategoryID() {
		return categoryID;
	}

	public void setCategoryID(Integer categoryID) {
		this.categoryID = categoryID;
	}

	public Integer getGenDeviceID() {
		return genDeviceID;
	}

	public void setGenDeviceID(Integer genDeviceID) {
		this.genDeviceID = genDeviceID;
	}

	public Integer getSpecDeviceID() {
		return specDeviceID;
	}

	public void setSpecDeviceID(Integer specDeviceID) {
		this.specDeviceID = specDeviceID;
	}
	public NCName getSelectedNCName() {
		return selectedNCName;
	}

	public void setSelectedNCName(NCName selectedNCName) {
		this.selectedNCName = selectedNCName;
	}

	public List<NCName> getAllNCNames() {
		return allNCNames;
	}

	public void setAllNCNames(List<NCName> allNCNames) {
		this.allNCNames = allNCNames;
	}

	public boolean isSupserSectionSelected() {
		return superSectionID != null;
	}

	public boolean isSectionSelected() {
		return sectionID != null;
	}

	public boolean isSubsectionSelected() {
		return subsectionID != null;
	}
	
	public boolean isDisciplineSelected() {
		return disciplineID != null;
	}

	public boolean isCategorySelected() {
		return categoryID != null;
	}

	public boolean isGenDeviceSelected() {
		return genDeviceID != null;
	}

	private void showMessage(FacesMessage.Severity severity, String summary, String message) {
		FacesContext context = FacesContext.getCurrentInstance();
		context.addMessage(null, new FacesMessage(severity, summary, message));
	}
}
