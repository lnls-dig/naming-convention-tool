package org.openepics.names.ui;

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
import javax.inject.Inject;
import org.openepics.names.services.NamesEJB;

import org.openepics.names.model.DeviceName;
import org.openepics.names.model.NameCategory;
import org.openepics.names.model.NameEvent;
import org.openepics.names.services.NamingConventionEJB;
import org.openepics.names.services.EssNameConstructionMethod;

@ManagedBean
@ViewScoped
public class EditNamesManager implements Serializable {

    private static final long serialVersionUID = 1L;
    @EJB
    private NamingConventionEJB ncEJB;
    @EJB
    private NamesEJB namesEJB;
    @Inject
    private UserManager userManager;

    private static final Logger logger = Logger.getLogger("org.openepics.names.ui.EditNamesManager");

    private Integer superSectionID;
    private Integer sectionID;
    private Integer subsectionID;
    private Integer disciplineID;
    private Integer categoryID;
    private Integer genDeviceID;
    private Integer specDeviceID;

    private DeviceName selectedDeviceName;

    private List<NameEvent> superSectionNames;
    private List<NameEvent> sectionNames;
    private List<NameEvent> subsectionNames;
    private List<NameEvent> disciplineNames;
    private List<NameEvent> categoryNames;
    private List<NameEvent> genDevNames;
    private List<NameEvent> specDevNames;

    private List<DeviceName> allDeviceNames;
    private List<DeviceName> historyDeviceNames;

    private boolean showDeletedNames = true;

    public EditNamesManager() {}

    @PostConstruct
    public void init() {
        loadSuperSections();
        loadSections();
        loadSubsections();
        loadDisciplines();
        loadCategories();
        loadGenericDevices();
        loadSpecificDevices();
        refreshDeviceNames();
        clearSelectionIds();
    }

    private void clearSelectionIds() {
        superSectionID = null;
        sectionID = null;
        subsectionID = null;
        disciplineID = null;
        categoryID = null;
        genDeviceID = null;
        specDeviceID = null;
    }

    public void onAdd() {
        DeviceName newDeviceName;

        try {
            logger.log(Level.INFO, "Adding NC Name");
            if (subsectionID == null || genDeviceID == null) {
                showMessage(FacesMessage.SEVERITY_ERROR, "Required field missing", " ");
            }
            NameEvent subsection = namesEJB.findEventById(subsectionID);
            NameEvent genDevice = namesEJB.findEventById(genDeviceID);
            newDeviceName = ncEJB.createDeviceName(subsection, genDevice, EssNameConstructionMethod.ACCELERATOR);
            showMessage(FacesMessage.SEVERITY_INFO, "NC Name successfully added.", "Name: " + "[TODO]");
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
            DeviceName modifiedName = ncEJB.modifyDeviceName(subsectionID, genDeviceID, selectedDeviceName.getId());
            showMessage(FacesMessage.SEVERITY_INFO, "NC Name modified.", "Name: " + "[TODO]");
        } catch (Exception e) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Error",
                    e.getMessage());
            System.err.println(e);
        } finally {
            init();
        }
    }

    public void onDelete() {
        DeviceName newDeviceName;

        try {
            logger.log(Level.INFO, "Deleting NC Name");
            newDeviceName = ncEJB.deleteDeviceName(selectedDeviceName);
            showMessage(FacesMessage.SEVERITY_INFO, "NC Name successfully deleted.", "Name: " + "[TODO]");
        } catch (Exception e) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Encountered an error",
                    e.getMessage());
            System.err.println(e);
        } finally {
            init();
            selectedDeviceName = null;
        }
    }

    /*
     * Used in xhtml.
     */
    public void loadSuperSections() {
        try {
            List<NameCategory> categories = namesEJB.getCategories();
            NameCategory superSectionCategory = null;
            for (NameCategory category : categories) {
                if (category.getName().equalsIgnoreCase(NameCategories.supersection())) {
                    superSectionCategory = category;
                    break;
                }
            }
            logger.log(Level.INFO, "Found Super Section category: " + superSectionCategory + " " + superSectionCategory.getId());
            superSectionNames = superSectionCategory == null ? null : namesEJB.findEventsByCategory(superSectionCategory);
            logger.log(Level.INFO, "Found supersections. Total = " + superSectionNames.size());

            sectionID = null;
            if (sectionNames != null) {
                sectionNames.clear();
            }
            subsectionID = null;
            if (subsectionNames != null) {
                subsectionNames.clear();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not load supersections.");
            System.err.println(e);
        }
    }

    /*
     * Used in XHTML.
     */
    public void loadSections() {
        if (superSectionID != null) {
            try {
                NameEvent superSection = namesEJB.findEventById(superSectionID);
                sectionNames = namesEJB.findEventsByParent(superSection);
                logger.log(Level.INFO, "Found sections. Total = " + sectionNames.size());

                subsectionID = null;
                if (subsectionNames != null) {
                    subsectionNames.clear();
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Could not load sections.");
                System.err.println(e);
            }
        } else {
            sectionNames = null;
        }
    }

    /*
     * Used in xhtml.
     */
    public void loadSubsections() {
        if (sectionID != null) {
            try {
                NameEvent section = namesEJB.findEventById(sectionID);
                subsectionNames = namesEJB.findEventsByParent(section);
                logger.log(Level.INFO, "Found subsections. Total = " + sectionNames.size());
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Could not load subsections.");
                System.err.println(e);
            }
        } else {
            subsectionNames = null;
        }
    }

    /*
     * used in xhtml.
     */
    public void loadDisciplines() {
        try {
            List<NameCategory> categories = namesEJB.getCategories();
            NameCategory disciplineCategory = null;
            for (NameCategory category : categories) {
                if (category.getName().equalsIgnoreCase("DSCP")) {
                    disciplineCategory = category;
                    break;
                }
            }
            logger.log(Level.INFO, "Found Discipline category: " + disciplineCategory + " " + disciplineCategory.getId());
            disciplineNames = disciplineCategory == null ? null : namesEJB.findEventsByCategory(disciplineCategory);
            logger.log(Level.INFO, "Found disciplines. Total = " + disciplineNames.size());

            categoryID = null;
            if (categoryNames != null) {
                categoryNames.clear();
            }
            genDeviceID = null;
            if (genDevNames != null) {
                genDevNames.clear();
            }
            specDeviceID = null;
            if (specDevNames != null) {
                specDevNames.clear();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not load disciplines.");
            System.err.println(e);
        }
    }

    /*
     * Used in xhtml.
     */
    public void loadCategories() {
        if (disciplineID != null) {
            try {
                NameEvent discipline = namesEJB.findEventById(disciplineID);
                categoryNames = namesEJB.findEventsByParent(discipline);
                logger.log(Level.INFO, "Found categories. Total = " + categoryNames.size());

                genDeviceID = null;
                if (genDevNames != null) {
                    genDevNames.clear();
                }
                specDeviceID = null;
                if (specDevNames != null) {
                    specDevNames.clear();
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Could not load categories.");
                System.err.println(e);
            }
        } else {
            categoryNames = null;
        }
    }

    /*
     * Used in xhtml.
     */
    public void loadGenericDevices() {
        if (categoryID != null) {
            try {
                NameEvent category = namesEJB.findEventById(categoryID);
                genDevNames = namesEJB.findEventsByParent(category);
                logger.log(Level.INFO, "Found generic devices. Total = " + genDevNames.size());

                specDeviceID = null;
                if (specDevNames != null) {
                    specDevNames.clear();
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Could not load generic devices.");
                System.err.println(e);
            }
        } else {
            genDevNames = null;
        }
    }

    /*
     * Used in xhtml.
     */
    public void loadSpecificDevices() {
        if (genDeviceID != null) {
            try {
                NameEvent genDevice = namesEJB.findEventById(genDeviceID);
                specDevNames = namesEJB.findEventsByParent(genDevice);
                logger.log(Level.INFO, "Found specific devices. Total = " + specDevNames.size());
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Could not load specific devices.");
                System.err.println(e);
            }
        } else {
            specDevNames = null;
        }
    }

    public String getSelectedDeviceNameSectionString() {
        if (selectedDeviceName != null) {
            NameEvent bottomName = selectedDeviceName.getSection();
            String sectionString = "";
            boolean firstTime = true;
            while (bottomName != null) {
                if (firstTime) {
                    firstTime = false;
                } else {
                    sectionString = " - " + sectionString;
                }
                sectionString = bottomName.getFullName() + sectionString;
                bottomName = bottomName.getParentName();
            }
            return sectionString.trim();
        }
        return "No selection!";
    }

    public String getSelectedDeviceNameDisciplineString() {
        if (selectedDeviceName != null) {
            NameEvent bottomName = selectedDeviceName.getDeviceType();
            String disciplineString = "";
            boolean firstTime = true;
            while (bottomName != null) {
                if (firstTime) {
                    firstTime = false;
                } else {
                    disciplineString = " - " + disciplineString;
                }
                disciplineString = bottomName.getFullName() + disciplineString;
                bottomName = bottomName.getParentName();
            }
            return disciplineString.trim();
        }
        return "No selection!";
    }

    public void loadSelectedName() {
        if (selectedDeviceName != null) {
            Map<String, Integer> namePartMap = new HashMap<String, Integer>();

            NameEvent sectionNode = selectedDeviceName.getSection();
            while (sectionNode.getParentName() != null) {
                namePartMap.put(sectionNode.getNameCategory().getName(), sectionNode.getId());
                sectionNode = sectionNode.getParentName();
            }
            namePartMap.put(sectionNode.getNameCategory().getName(), sectionNode.getId());

            NameEvent disciplineNode = selectedDeviceName.getDeviceType();
            while (disciplineNode.getParentName() != null) {
                namePartMap.put(disciplineNode.getNameCategory().getName(), disciplineNode.getId());
                disciplineNode = disciplineNode.getParentName();
            }
            namePartMap.put(disciplineNode.getNameCategory().getName(), disciplineNode.getId());

            //Load section selections.
            loadSuperSections();
            setSuperSectionID(namePartMap.get(NameCategories.supersection()));
            loadSections();
            setSectionID(namePartMap.get(NameCategories.section()));
            loadSubsections();
            setSubsectionID(namePartMap.get(NameCategories.subsection()));

            //Load Discipline selections.
            loadDisciplines();
            setDisciplineID(namePartMap.get(NameCategories.discipline()));
            loadCategories();
            setCategoryID(namePartMap.get(NameCategories.category()));
            loadGenericDevices();
            setGenDeviceID(namePartMap.get(NameCategories.genericDevice()));
            if (namePartMap.containsKey(NameCategories.specificDevice())) {
                loadSpecificDevices();
                setSpecDeviceID(namePartMap.get(NameCategories.specificDevice()));
            }
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

    public DeviceName getSelectedDeviceName() {
        return selectedDeviceName;
    }

    public void setSelectedDeviceName(DeviceName selectedDeviceName) {
        this.selectedDeviceName = selectedDeviceName;
    }

    public List<DeviceName> getAllDeviceNames() {
        return allDeviceNames;
    }

    public void setAllDeviceNames(List<DeviceName> allDeviceNames) {
        this.allDeviceNames = allDeviceNames;
    }

    public boolean isFormFilled() {
        return superSectionID != null
                && sectionID != null
                && subsectionID != null
                && disciplineID != null
                && categoryID != null
                && genDeviceID != null;
    }

    private void showMessage(FacesMessage.Severity severity, String summary, String message) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(severity, summary, message));
    }

    public String nameStatus(DeviceName nreq) {
        switch (nreq.getStatus()) {
            case VALID:
                return "Published";
            case INVALID:
                return "In-Process";
            case DELETED:
                return "Deleted";
            default:
                return "unknown";
        }
    }

    public void findHistory() {
        try {
            if (selectedDeviceName == null) {
                showMessage(FacesMessage.SEVERITY_ERROR, "Error",
                        "You must select a name first.");
                historyDeviceNames = null;
                return;
            }
            historyDeviceNames = ncEJB.getDeviceNameHistory(selectedDeviceName.getNameId());
        } catch (Exception e) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Encountered an error",
                    e.getMessage());
            System.err.println(e);
        } finally {
            //EMPTY
        }
    }

    public List<DeviceName> getHistoryEvents() {
        return historyDeviceNames;
    }

    public boolean isSuperUser() {
        return userManager.isSuperUser();
    }

    public boolean isShowDeletedNames() {
        return showDeletedNames;
    }

    public void setShowDeletedNames(boolean showDeletedNames) {
        this.showDeletedNames = showDeletedNames;
    }

    public void refreshDeviceNames() {
        if (showDeletedNames) {
            allDeviceNames = ncEJB.getAllDeviceNames();
        } else {
            allDeviceNames = ncEJB.getExistingDeviceNames();
        }
    }
}
