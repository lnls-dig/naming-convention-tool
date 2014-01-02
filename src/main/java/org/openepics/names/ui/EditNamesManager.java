package org.openepics.names.ui;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.openepics.names.model.DeviceName;
import org.openepics.names.model.NameCategory;
import org.openepics.names.model.NameEvent;
import org.openepics.names.model.NameHierarchy;
import org.openepics.names.services.NamesEJB;
import org.openepics.names.services.NamingConvention;
import org.openepics.names.services.NamingConventionEJB;

@ManagedBean
@ViewScoped
public class EditNamesManager implements Serializable {

    @Inject private NamingConventionEJB ncEJB;
    @Inject private NamesEJB namesEJB;
    @Inject private UserManager userManager;
    @Inject private NamingConvention namingConvention;

    private Integer superSectionID;
    private Integer sectionID;
    private Integer subsectionID;
    private Integer disciplineID;
    private Integer categoryID;
    private Integer genDeviceID;
    private Integer specDeviceID;

    private DeviceNameView selectedDeviceName;

    private List<MnemonicNameView> sectionLevels;
    private List<MnemonicNameView> deviceTypeLevels;

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

        /* init section levels
        * for init, the first level of hierarchy is filled and all other parts are empty lists
        */
        NameHierarchy nameHierarchy = namesEJB.getNameHierarchy();
        sectionLevels = new ArrayList<>(nameHierarchy.getSectionLevels().size());
        sectionLevels.add(new MnemonicNameView(namesEJB.getStandardNames(nameHierarchy.getSectionLevels().get(0).getName(), false)));
        for(int i = 1; i < nameHierarchy.getSectionLevels().size(); i++)
            sectionLevels.add(new MnemonicNameView(new ArrayList<NameEvent>()));

        /* init section levels
        * for init, the first level of hierarchy is filled and all other parts are empty lists
        */
        deviceTypeLevels = new ArrayList<>(nameHierarchy.getDeviceTypeLevels().size());
        deviceTypeLevels.add(new MnemonicNameView(namesEJB.getStandardNames(nameHierarchy.getDeviceTypeLevels().get(0).getName(), false)));
        for(int i = 1; i < nameHierarchy.getDeviceTypeLevels().size(); i++)
            deviceTypeLevels.add(new MnemonicNameView(new ArrayList<NameEvent>()));

        loadSuperSections();
        loadSections();
        loadSubsections();
        loadDisciplines();
        loadCategories();
        loadGenericDevices();
        loadSpecificDevices();
        loadDeviceNames();
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
        // TODO solve generically
        try {
            Integer subsectionID = sectionLevels.get(sectionLevels.size()-1).getSelectedId();
            Integer genDeviceID = deviceTypeLevels.get(2).getSelectedId();

            if (subsectionID == null || genDeviceID == null) {
                showMessage(FacesMessage.SEVERITY_ERROR, "Required field missing", " ");
                return;
            }
            final NameEvent subsection = namesEJB.findEventById(subsectionID);
            final NameEvent genDevice = namesEJB.findEventById(genDeviceID);
            final DeviceName newDeviceName = ncEJB.createDeviceName(subsection, genDevice);
            showMessage(FacesMessage.SEVERITY_INFO, "NC Name successfully added.", "Name: " + "[TODO]");
        } finally {
            init();
        }
    }

    public void onModify() {
        try {
            final DeviceName modifiedName = ncEJB.modifyDeviceName(subsectionID, genDeviceID, selectedDeviceName.getId());
            showMessage(FacesMessage.SEVERITY_INFO, "NC Name modified.", "Name: " + "[TODO]");
        } finally {
            init();
        }
    }

    public void onDelete() {
        try {
            final DeviceName newDeviceName = ncEJB.deleteDeviceName(selectedDeviceName.getDeviceName());
            showMessage(FacesMessage.SEVERITY_INFO, "NC Name successfully deleted.", "Name: " + "[TODO]");
        } finally {
            init();
            selectedDeviceName = null;
        }
    }

    public void loadSuperSections() {
        List<NameCategory> categories = namesEJB.getCategories();
        NameCategory superSectionCategory = null;
        for (NameCategory category : categories) {
            if (category.getName().equalsIgnoreCase(NameCategories.supersection())) {
                superSectionCategory = category;
                break;
            }
        }
        superSectionNames = superSectionCategory == null ? null : namesEJB.findEventsByCategory(superSectionCategory);

        sectionID = null;
        if (sectionNames != null) {
            sectionNames.clear();
        }
        subsectionID = null;
        if (subsectionNames != null) {
            subsectionNames.clear();
        }
    }

    public void loadSections() {
        if (superSectionID != null) {
            NameEvent superSection = namesEJB.findEventById(superSectionID);
            sectionNames = namesEJB.findEventsByParent(superSection);

            subsectionID = null;
            if (subsectionNames != null) {
                subsectionNames.clear();
            }
        } else {
            sectionNames = null;
        }
    }

    public void loadSubsections() {
        if (sectionID != null) {
            NameEvent section = namesEJB.findEventById(sectionID);
            subsectionNames = namesEJB.findEventsByParent(section);
        } else {
            subsectionNames = null;
        }
    }

    public void loadDisciplines() {
        List<NameCategory> categories = namesEJB.getCategories();
        NameCategory disciplineCategory = null;
        for (NameCategory category : categories) {
            if (category.getName().equalsIgnoreCase("DSCP")) {
                disciplineCategory = category;
                break;
            }
        }
        disciplineNames = disciplineCategory == null ? null : namesEJB.findEventsByCategory(disciplineCategory);

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
    }

    public void loadCategories() {
        if (disciplineID != null) {
            NameEvent discipline = namesEJB.findEventById(disciplineID);
            categoryNames = namesEJB.findEventsByParent(discipline);

            genDeviceID = null;
            if (genDevNames != null) {
                genDevNames.clear();
            }
            specDeviceID = null;
            if (specDevNames != null) {
                specDevNames.clear();
            }
        } else {
            categoryNames = null;
        }
    }

    public void loadGenericDevices() {
        if (categoryID != null) {
            NameEvent category = namesEJB.findEventById(categoryID);
            genDevNames = namesEJB.findEventsByParent(category);

            specDeviceID = null;
            if (specDevNames != null) {
                specDevNames.clear();
            }
        } else {
            genDevNames = null;
        }
    }

    public void loadSpecificDevices() {
        if (genDeviceID != null) {
            NameEvent genDevice = namesEJB.findEventById(genDeviceID);
            specDevNames = namesEJB.findEventsByParent(genDevice);
        } else {
            specDevNames = null;
        }
    }

    public void loadDeviceNames() {
        if (showDeletedNames) {
            allDeviceNames = ncEJB.getAllDeviceNames();
        } else {
            allDeviceNames = ncEJB.getExistingDeviceNames();
        }
    }

    public void loadHistory() {
        if (selectedDeviceName == null) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Error", "You must select a name first.");
            historyDeviceNames = null;
            return;
        }
        historyDeviceNames = ncEJB.getDeviceNameHistory(selectedDeviceName.getDeviceName().getNameId());
    }

    public void loadSelectedName() {
        if (selectedDeviceName != null) {
            Map<String, Integer> namePartMap = new HashMap<>();

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

            loadSuperSections();
            this.superSectionID = namePartMap.get(NameCategories.supersection());
            loadSections();
            this.sectionID = namePartMap.get(NameCategories.section());
            loadSubsections();
            this.subsectionID = namePartMap.get(NameCategories.subsection());

            loadDisciplines();
            this.disciplineID = namePartMap.get(NameCategories.discipline());
            loadCategories();
            this.categoryID = namePartMap.get(NameCategories.category());
            loadGenericDevices();
            this.genDeviceID = namePartMap.get(NameCategories.genericDevice());
            if (namePartMap.containsKey(NameCategories.specificDevice())) {
                loadSpecificDevices();
                this.specDeviceID = namePartMap.get(NameCategories.specificDevice());
            }
        }
    }

    public List<NameEvent> getSuperSectionNames() { return superSectionNames; }

    public List<NameEvent> getSectionNames() { return sectionNames; }

    public List<NameEvent> getSubsectionNames() { return subsectionNames; }

    public List<NameEvent> getDisciplineNames() { return disciplineNames; }

    public List<NameEvent> getCategoryNames() { return categoryNames; }

    public List<NameEvent> getGenDevNames() { return genDevNames; }

    public List<NameEvent> getSpecDevNames() { return specDevNames; }

    public Integer getSuperSectionID() { return superSectionID; }
    public void setSuperSectionID(Integer superSectionID) { this.superSectionID = superSectionID; }

    public Integer getSectionID() { return sectionID; }
    public void setSectionID(Integer sectionID) { this.sectionID = sectionID; }

    public Integer getSubsectionID() { return subsectionID; }
    public void setSubsectionID(Integer subsectionID) { this.subsectionID = subsectionID; }

    public Integer getDisciplineID() { return disciplineID; }
    public void setDisciplineID(Integer disciplineID) { this.disciplineID = disciplineID; }

    public Integer getCategoryID() { return categoryID; }
    public void setCategoryID(Integer categoryID) { this.categoryID = categoryID; }

    public Integer getGenDeviceID() { return genDeviceID; }
    public void setGenDeviceID(Integer genDeviceID) { this.genDeviceID = genDeviceID; }

    public Integer getSpecDeviceID() { return specDeviceID; }
    public void setSpecDeviceID(Integer specDeviceID) { this.specDeviceID = specDeviceID; }

    public DeviceNameView getSelectedDeviceName() { return selectedDeviceName; }
    public void setSelectedDeviceName(DeviceNameView selectedDeviceName) { this.selectedDeviceName = selectedDeviceName; }

    public List<DeviceNameView> getAllDeviceNames() {
        return Lists.transform(allDeviceNames, new Function<DeviceName, DeviceNameView>() {
            @Override public DeviceNameView apply(DeviceName deviceName) {
                return new DeviceNameView(deviceName, namingConvention.getNamingConventionName(deviceName));
            }
        });
    }

    public List<DeviceName> getHistoryEvents() { return historyDeviceNames; }

    public List<MnemonicNameView> getSectionLevels() { return sectionLevels; }

    public List<MnemonicNameView> getDeviceTypeLevels() { return deviceTypeLevels; }

    public boolean isSuperUser() { return userManager.isSuperUser(); }

    public boolean isShowDeletedNames() { return showDeletedNames; }
    public void setShowDeletedNames(boolean showDeletedNames) { this.showDeletedNames = showDeletedNames; }

    public boolean isFormFilled() {
        boolean isFilled = true;
        for(MnemonicNameView element : sectionLevels)
            isFilled = isFilled && (element.isSelected());

        // TODO Make this selection configurable
        for(int i = 0; i < deviceTypeLevels.size(); i++) {
            isFilled = isFilled & (deviceTypeLevels.get(i).isSelected());
            if(i>=2) break;
        }

        return isFilled;
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

    private void showMessage(FacesMessage.Severity severity, String summary, String message) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(severity, summary, message));
    }

    /**
     * This method returns the number of columns for the "Add new device name" dialog
     * @return
     */
    public int getNamePartsCount() {
        NameHierarchy hierarchy;
        hierarchy = namesEJB.getNameHierarchy();

        return Math.max(hierarchy.getSectionLevels().size(), hierarchy.getDeviceTypeLevels().size());
    }

    /**
     * This method generates the Ids of the select elements that need to be updated when a select element item changes.
     * @param currentId the Id (index) of the element that triggered the update
     * @param prefix
     * @param maxId
     * @param staticIds
     * @return
     */
    public String generateUpdateIds(int currentId, String prefix, int maxId, String staticIds) {
        StringBuilder returnIds = new StringBuilder();
        for( int i = currentId + 1; i < maxId; i++) {
            returnIds.append(prefix);
            returnIds.append(i);
            if(i<maxId-1) returnIds.append(' ');
        }

        return staticIds.trim().isEmpty() ? returnIds.toString() : returnIds.append(' ').append(staticIds).toString();
    }

    /**
     * Loads the next section in the hierarchy. The current level is the index in the hierarchy that the new level
     * is based on. E. g. : If current level is 0, this function prepares the data on level 1
     * @param currentLevel
     */
    public void loadNextSectionLevel(int currentLevel) {
        NameHierarchy hierarchy = namesEJB.getNameHierarchy();

        if(currentLevel + 1 < hierarchy.getSectionLevels().size() &&
                !sectionLevels.get(currentLevel).getOptions().isEmpty() &&
                sectionLevels.get(currentLevel).getSelectedId() != null) {
            NameEvent parent = namesEJB.findEventById(sectionLevels.get(currentLevel).getSelectedId());
            sectionLevels.get(currentLevel + 1).setOptions(namesEJB.findEventsByParent(parent));
            sectionLevels.get(currentLevel + 1).setSelectedId(null);

            // clear the rest of the inputs
            for(int i = currentLevel + 2; i < hierarchy.getSectionLevels().size(); i++)
                sectionLevels.get(i).clear();
        }
    }

    /**
     * Loads the next device in the hierarchy. The current level is the index in the hierarchy that the new level
     * is based on. E. g. : If current level is 0, this function prepares the data on level 1
     * @param currentLevel
     */
    public void loadNextDeviceTypeLevel(int currentLevel) {
        NameHierarchy hierarchy = namesEJB.getNameHierarchy();

        if(currentLevel + 1 < hierarchy.getDeviceTypeLevels().size() &&
                !deviceTypeLevels.get(currentLevel).getOptions().isEmpty() &&
                deviceTypeLevels.get(currentLevel).getSelectedId() != null) {
            NameEvent parent = namesEJB.findEventById(deviceTypeLevels.get(currentLevel).getSelectedId());
            deviceTypeLevels.get(currentLevel + 1).setOptions(namesEJB.findEventsByParent(parent));
            deviceTypeLevels.get(currentLevel + 1).setSelectedId(null);

            // clear the rest of the inputs
            for(int i = currentLevel + 2; i < hierarchy.getDeviceTypeLevels().size(); i++)
                deviceTypeLevels.get(i).clear();
        }
    }

}
