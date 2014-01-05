package org.openepics.names.ui;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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

    private DeviceNameView selectedDeviceName;

    private List<MnemonicNameView> sectionLevels;
    private List<MnemonicNameView> deviceTypeLevels;

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

        loadDeviceNames();
    }

    public void onAdd() {
        // TODO solve generically and for specific + generic device
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
            showMessage(FacesMessage.SEVERITY_INFO, "NC Name successfully added.", "Name: " +
                    namingConvention.getNamingConventionName(newDeviceName));
        } finally {
            init();
        }
    }

    public void onModify() {
        // TODO solve generically and for specific + generic device
        try {
            Integer subsectionID = sectionLevels.get(sectionLevels.size()-1).getSelectedId();
            Integer genDeviceID = deviceTypeLevels.get(2).getSelectedId();
            final DeviceName modifiedName = ncEJB.modifyDeviceName(subsectionID, genDeviceID, selectedDeviceName.getId());
            showMessage(FacesMessage.SEVERITY_INFO, "NC Name modified.", "Name: " +
                    namingConvention.getNamingConventionName(modifiedName));
        } finally {
            init();
        }
    }

    public void onDelete() {
        try {
            final DeviceName newDeviceName = ncEJB.deleteDeviceName(selectedDeviceName.getDeviceName());
            showMessage(FacesMessage.SEVERITY_INFO, "NC Name successfully deleted.", "Name: " +
                    namingConvention.getNamingConventionName(newDeviceName));
        } finally {
            init();
            selectedDeviceName = null;
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
            NameHierarchy hierarchy = namesEJB.getNameHierarchy();

            if(sectionLevels.size() != hierarchy.getSectionLevels().size())
                throw new IllegalStateException("Section levels do not match hierarchy.");

            if(deviceTypeLevels.size() != hierarchy.getDeviceTypeLevels().size())
                throw new IllegalStateException("Device type levels do not match hierarchy.");

            fillDropDowns(selectedDeviceName.getSection(), hierarchy.getSectionLevels(), sectionLevels);
            fillDropDowns(selectedDeviceName.getDeviceType(), hierarchy.getDeviceTypeLevels(), deviceTypeLevels);
        }
    }

    private void fillDropDowns(NameEvent namePart, List<NameCategory> hierarchyLevels, List<MnemonicNameView> dropdownsToFill) {
        NameEvent currentNamePart = namePart;
        for(int i = hierarchyLevels.size()-1; i >= 0; i--) {
            MnemonicNameView currentDropdown = dropdownsToFill.get(i);
            if(currentNamePart.getNameCategory().equals(hierarchyLevels.get(i))) {
                // if the current name part category equals the category a the current level
                // then we know how to fill the current drop-down and we know what element is selected
                NameEvent parent = currentNamePart.getParentName();
                if(i > 0) {
                    // not at the top of hierarchy.
                    currentDropdown.setOptions(namesEJB.findEventsByParent(parent));
                } else {
                    // at the top of the hierarchy
                    currentDropdown.setOptions(namesEJB.findEventsByCategory(currentNamePart.getNameCategory()));
                }
                currentDropdown.setSelectedId(currentNamePart.getId());
                currentNamePart = parent;
            } else {
                // the drop-down we are currently populating does not match the currently
                // selected name part, but is the selected name part the parent of the populating drop-down?
                // E.g.: name is for Generic device, but we start filling at specific device.
                if(currentNamePart.getNameCategory().equals(hierarchyLevels.get(i-1))) {
                    // we can fill this drop-down according to current name part
                    currentDropdown.setOptions(namesEJB.findEventsByParent(currentNamePart));
                    currentDropdown.setSelectedId(null);
                } else {
                    // we don not know how to fill this dropdown
                    currentDropdown.clear();
                }
            }
        }
    }

    public DeviceNameView getSelectedDeviceName() { return selectedDeviceName; }
    public void setSelectedDeviceName(DeviceNameView selectedDeviceName) { this.selectedDeviceName = selectedDeviceName; }

    public List<DeviceNameView> getAllDeviceNames() {
        return Lists.transform(allDeviceNames, new Function<DeviceName, DeviceNameView>() {
            @Override public DeviceNameView apply(DeviceName deviceName) {
                return new DeviceNameView(deviceName, namingConvention.getNamingConventionName(deviceName));
            }
        });
    }

    public List<DeviceNameView> getHistoryEvents() {
        return historyDeviceNames == null ? null : Lists.transform(historyDeviceNames, new Function<DeviceName, DeviceNameView>() {
            @Override public DeviceNameView apply(DeviceName deviceName) {
                return new DeviceNameView(deviceName, namingConvention.getNamingConventionName(deviceName));
            }
        });
    }

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
