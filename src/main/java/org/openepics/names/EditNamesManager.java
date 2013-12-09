package org.openepics.names;

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

import org.openepics.names.model.NCName;
import org.openepics.names.model.NameCategory;
import org.openepics.names.model.NameEvent;
import org.openepics.names.nc.NamingConventionEJBLocal;

@ManagedBean
@ViewScoped
public class EditNamesManager implements Serializable {

	private static final long serialVersionUID = 1L;
	@EJB
	private NamingConventionEJBLocal ncEJB;
	@EJB
	private NamesEJBLocal namesEJB;
	private static final Logger logger = Logger.getLogger("org.openepics.names");
	
	private NameEvent superSection;
	private NameEvent section;
	private NameEvent subsection;
	private NameEvent discipline;
	private NameEvent category;
	private NameEvent genDevice;
	
	private List<NameEvent> superSectionNames;
	private List<NameEvent> sectionNames;
	private List<NameEvent> subsectionNames;
	private List<NameEvent> disciplineNames;
	private List<NameEvent> categoryNames;
	private List<NameEvent> genDevNames;

	public EditNamesManager() {
		// EMPTY
	}

	@PostConstruct
	public void init() {
		loadSuperSections();
		loadDisciplines();
	}
	
	public void onAdd() {
		NCName newNCName;

		try {
			logger.log(Level.INFO, "Adding NC Name");
			if (superSection == null || section == null || subsection == null || discipline == null || category == null || genDevice == null) {
				showMessage(FacesMessage.SEVERITY_ERROR, "Required field missing", " ");
			}
			newNCName = ncEJB.createNCName(section, discipline, null);
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
	
	public void loadSuperSections() {
		try {
			List<NameCategory> categories = namesEJB.getCategories();
			NameCategory superSectionCategory = null;
			for(NameCategory category : categories) {
				if(category.getName().equalsIgnoreCase("Sup")) {
					superSectionCategory = category;
					break;
				}
			}
			logger.log(Level.INFO, "Found Super Section category: "+superSectionCategory+" "+superSectionCategory.getId());
			superSectionNames = superSectionCategory == null ? null : namesEJB.findEventsByCategory(superSectionCategory);
			logger.log(Level.INFO, "Found supersections. Total = "+superSectionNames.size());
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Could not initialize NCNamesManager.");
			System.err.println(e);
		}
	}
	
	public void loadSections() {
		try {
			sectionNames = namesEJB.findEventsByParent(superSection);
			logger.log(Level.INFO, "Found sections. Total = "+sectionNames.size());
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Could not initialize NCNamesManager.");
			System.err.println(e);
		}
	}
	
	public void loadSubsections() {
		try {
			subsectionNames = namesEJB.findEventsByParent(section);
			logger.log(Level.INFO, "Found subsections. Total = "+sectionNames.size());
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Could not initialize NCNamesManager.");
			System.err.println(e);
		}
	}
	
	public void loadDisciplines() {
		try {
			List<NameCategory> categories = namesEJB.getCategories();
			NameCategory disciplineCategory = null;
			for(NameCategory category : categories) {
				if(category.getName().equalsIgnoreCase("Dsc")) {
					disciplineCategory = category;
					break;
				}
			}
			logger.log(Level.INFO, "Found Discipline category: "+disciplineCategory+" "+disciplineCategory.getId());
			disciplineNames = disciplineCategory == null ? null : namesEJB.findEventsByCategory(disciplineCategory);
			logger.log(Level.INFO, "Found disciplines. Total = "+disciplineNames.size());
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Could not initialize NCNamesManager.");
			System.err.println(e);
		}
	}
	
	public void loadCategories() {
		try {
			categoryNames = namesEJB.findEventsByParent(discipline);
			logger.log(Level.INFO, "Found categories. Total = "+sectionNames.size());
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Could not initialize NCNamesManager.");
			System.err.println(e);
		}
	}
	
	public void loadGenericDevices() {
		try {
			genDevNames = namesEJB.findEventsByParent(genDevice);
			logger.log(Level.INFO, "Found generic devices. Total = "+genDevNames.size());
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Could not initialize NCNamesManager.");
			System.err.println(e);
		}
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

	public NameEvent getSuperSection() {
		return superSection;
	}

	public void setSuperSection(NameEvent superSection) {
		this.superSection = superSection;
	}

	public NameEvent getSection() {
		return section;
	}

	public void setSection(NameEvent section) {
		this.section = section;
	}

	public NameEvent getSubsection() {
		return subsection;
	}

	public void setSubsection(NameEvent subsection) {
		this.subsection = subsection;
	}

	public NameEvent getDiscipline() {
		return discipline;
	}

	public void setDiscipline(NameEvent discipline) {
		this.discipline = discipline;
	}

	public NameEvent getCategory() {
		return category;
	}

	public void setCategory(NameEvent category) {
		this.category = category;
	}

	public NameEvent getGenDevice() {
		return genDevice;
	}

	public void setGenDevice(NameEvent genDevice) {
		this.genDevice = genDevice;
	}
	
	public boolean isSupserSectionSelected() {
		return superSection != null;
	}
	
	public boolean isSectionSelected() {
		return section != null;
	}
	
	public boolean isDisciplineSelected() {
		return discipline != null;
	}
	
	public boolean isCategorySelected() {
		return category != null;
	}

	private void showMessage(FacesMessage.Severity severity, String summary, String message) {
		FacesContext context = FacesContext.getCurrentInstance();
		context.addMessage(null, new FacesMessage(severity, summary, message));
	}
}
