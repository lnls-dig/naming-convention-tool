package org.openepics.names.ui;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import org.openepics.names.model.Device;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NameCategory;
import org.openepics.names.model.NameHierarchy;
import org.openepics.names.model.NamePart;
import org.openepics.names.services.DeviceService;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.ui.names.NamePartView;

@ManagedBean
@ViewScoped
public class EditNamesManager implements Serializable {

    @Inject private RestrictedNamePartService namePartService;
    @Inject private DeviceService deviceService;

    private DeviceView selectedDeviceName;

    private List<NamePartSelectionView> sectionLevels;
    private List<NamePartSelectionView> deviceTypeLevels;

    private List<DeviceView> allDeviceNames;
    private List<DeviceView> historyDeviceNames;

    private boolean showDeletedNames = true;

    public EditNamesManager() {}

    @PostConstruct
    public void init() {
        final NameCategory topSectionCategory = namePartService.nameHierarchy().getSectionLevels().get(0);
        final List<NamePartView> topSections =
                Lists.transform(namePartService.approvedOrPendingNames(topSectionCategory, false),
                        new Function<NamePart, NamePartView>() {
                            @Override public NamePartView apply(NamePart namePart) {
                                return ViewFactory.getView(namePart);
                            }
                        });
        sectionLevels = new ArrayList<>();
        sectionLevels.add(new NamePartSelectionView(topSections));
        for (int i = 1; i < namePartService.nameHierarchy().getSectionLevels().size(); i++) {
            sectionLevels.add(new NamePartSelectionView(new ArrayList<NamePartView>()));
        }

        final NameCategory topDeviceTypeCategory = namePartService.nameHierarchy().getDeviceTypeLevels().get(0);
        final List<NamePartView> topDeviceTypes =
                Lists.transform(namePartService.approvedOrPendingNames(topDeviceTypeCategory, false),
                        new Function<NamePart, NamePartView>() {
                            @Override public NamePartView apply(NamePart namePart) {
                                return ViewFactory.getView(namePart);
                            }
                        });
        deviceTypeLevels = new ArrayList<>();
        deviceTypeLevels.add(new NamePartSelectionView(topDeviceTypes));
        for (int i = 1; i < namePartService.nameHierarchy().getDeviceTypeLevels().size(); i++) {
            deviceTypeLevels.add(new NamePartSelectionView(new ArrayList<NamePartView>()));
        }

        loadDeviceNames();
    }

    public void onAdd() {
        // TODO solve generically and for specific + generic device
        try {
            final NamePartView subsection = sectionLevels.get(sectionLevels.size()-1).getSelected();
            final NamePartView genDevice = deviceTypeLevels.get(2).getSelected();

            if (subsection == null || genDevice == null) {
                showMessage(FacesMessage.SEVERITY_ERROR, "Error", "Required field missing");
                return;
            }
            deviceService.createDevice(subsection.getNamePart(), genDevice.getNamePart());
            showMessage(FacesMessage.SEVERITY_INFO, "Device Name successfully added.", "Name: [TODO]");
        } finally {
            init();
        }
    }

    public void onModify() {
        // TODO solve generically and for specific + generic device
        try {
            final NamePartView subsection = sectionLevels.get(sectionLevels.size()-1).getSelected();
            final NamePartView genDevice = deviceTypeLevels.get(2).getSelected();
            deviceService.modifyDevice(selectedDeviceName.getDevice().getDevice(), subsection.getNamePart(), genDevice.getNamePart());
            showMessage(FacesMessage.SEVERITY_INFO, "Device modified.", "Name: [TODO");
        } finally {
            init();
        }
    }

    public void onDelete() {
        try {
            deviceService.removeDevice(selectedDeviceName.getDevice().getDevice());
            showMessage(FacesMessage.SEVERITY_INFO, "Device successfully deleted.", "Name: [TODO]");
        } finally {
            init();
            selectedDeviceName = null;
        }
    }

    public void loadDeviceNames() {
        List<Device> allDeviceNames = deviceService.devices(showDeletedNames);
        this.allDeviceNames = allDeviceNames.isEmpty() ? null : new ArrayList<DeviceView>();
        for (Device dev : allDeviceNames) {
            this.allDeviceNames.add(ViewFactory.getView(dev));
        }
    }

    public void loadHistory() {
        if (selectedDeviceName == null) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Error", "You must select a name first.");
            historyDeviceNames = null;
            return;
        }
        historyDeviceNames = Lists.transform(deviceService.revisions(selectedDeviceName.getDevice().getDevice()),
                new Function<DeviceRevision, DeviceView>(){
                    @Override
                    public DeviceView apply(DeviceRevision f) {
                        return ViewFactory.getView(f);
                    }
                });
    }

    public void loadSelectedName() {
        if (selectedDeviceName != null) {
            NameHierarchy hierarchy = namePartService.nameHierarchy();

            if (sectionLevels.size() != hierarchy.getSectionLevels().size())
                throw new IllegalStateException("Section levels do not match hierarchy.");

            if (deviceTypeLevels.size() != hierarchy.getDeviceTypeLevels().size())
                throw new IllegalStateException("Device type levels do not match hierarchy.");

            fillDropDowns(selectedDeviceName.getSection(), hierarchy.getSectionLevels(), sectionLevels);
            fillDropDowns(selectedDeviceName.getDeviceType(), hierarchy.getDeviceTypeLevels(), deviceTypeLevels);
        }
    }

    private void fillDropDowns(NamePartView namePart, List<NameCategory> hierarchyLevels, List<NamePartSelectionView> dropdownsToFill) {
        NamePartView currentNamePart = namePart;
        for (int i = hierarchyLevels.size() - 1; i >= 0; i--) {
            NamePartSelectionView currentDropdown = dropdownsToFill.get(i);
            if (currentNamePart.getNameCategory().equals(hierarchyLevels.get(i))) {
                // if the current name part category equals the category at the current level
                // then we know how to fill the current drop-down and we know what element is selected
                final NamePartView parent = currentNamePart.getParent();
                if (i > 0) {
                    // not at the top of hierarchy.
                    currentDropdown.setOptions(parent.getChildren());
                } else {
                    // at the top of the hierarchy
                    currentDropdown.setOptions(
                            Lists.transform(namePartService.approvedOrPendingNames(currentNamePart.getNameEvent().getNameCategory(), false),
                                    new Function<NamePart, NamePartView>() {
                                        @Override public NamePartView apply(NamePart f) {
                                            return ViewFactory.getView(f);
                                        }
                            }));
                }
                currentDropdown.setSelected(currentNamePart);
                currentNamePart = parent;
            } else {
                // the drop-down we are currently populating does not match the currently
                // selected name part, but is the selected name part the parent of the populating drop-down?
                // E.g.: name is for Generic device, but we start filling at specific device.
                if (currentNamePart.getNameCategory().equals(hierarchyLevels.get(i - 1))) {
                    // we can fill this drop-down according to current name part
                    currentDropdown.setOptions(currentNamePart.getChildren());
                    currentDropdown.setSelected(null);
                } else {
                    // we don not know how to fill this dropdown
                    currentDropdown.clear();
                }
            }
        }
    }

    public DeviceView getSelectedDeviceName() { return selectedDeviceName; }
    public void setSelectedDeviceName(DeviceView selectedDeviceName) { this.selectedDeviceName = selectedDeviceName; }

    public List<DeviceView> getAllDeviceNames() { return allDeviceNames; }

    public List<DeviceView> getHistoryEvents() { return historyDeviceNames; }

    public List<NamePartSelectionView> getSectionLevels() { return sectionLevels; }

    public List<NamePartSelectionView> getDeviceTypeLevels() { return deviceTypeLevels; }

    public boolean isShowDeletedNames() { return showDeletedNames; }
    public void setShowDeletedNames(boolean showDeletedNames) { this.showDeletedNames = showDeletedNames; }

    public boolean isFormFilled() {
        boolean isFilled = true;
        for (NamePartSelectionView element : sectionLevels)
            isFilled = isFilled && (element.isSelected());

        // TODO Make this selection configurable
        for (int i = 0; i < deviceTypeLevels.size(); i++) {
            isFilled = isFilled & (deviceTypeLevels.get(i).isSelected());
            if (i>=2) break;
        }

        return isFilled;
    }

    public String getSelectedDeviceNameSectionString() {
         return Joiner.on(" ▸ ").join(selectedDeviceName.getSection().getNamePath());
    }

    public String getSelectedDeviceNameDisciplineString() {
        return Joiner.on(" ▸ ").join(selectedDeviceName.getDeviceType().getNamePath());
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
        hierarchy = namePartService.nameHierarchy();

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
        for ( int i = currentId + 1; i < maxId; i++) {
            returnIds.append(prefix);
            returnIds.append(i);
            if (i < maxId - 1) returnIds.append(' ');
        }

        return staticIds.trim().isEmpty() ? returnIds.toString() : returnIds.append(' ').append(staticIds).toString();
    }

    /**
     * Loads the next section in the hierarchy. The current level is the index in the hierarchy that the new level
     * is based on. E. g. : If current level is 0, this function prepares the data on level 1
     * @param currentLevel
     */
    public void loadNextSectionLevel(int currentLevel) {
        if (currentLevel + 1 < namePartService.nameHierarchy().getSectionLevels().size()
                && !sectionLevels.get(currentLevel).getOptions().isEmpty()
                && sectionLevels.get(currentLevel).getSelected() != null) {
            sectionLevels.get(currentLevel + 1).setOptions(
                    Lists.transform(namePartService.siblings(sectionLevels.get(currentLevel).getSelected().getNamePart()),
                            new Function<NamePart, NamePartView>() {
                                @Override
                                public NamePartView apply(NamePart f) {
                                    return ViewFactory.getView(f);
                                }
                            }));
            sectionLevels.get(currentLevel + 1).setSelected(null);

            // clear the rest of the inputs
            for (int i = currentLevel + 2; i < namePartService.nameHierarchy().getSectionLevels().size(); i++) {
                sectionLevels.get(i).clear();
            }
        }
    }

    /**
     * Loads the next device in the hierarchy. The current level is the index in the hierarchy that the new level
     * is based on. E. g. : If current level is 0, this function prepares the data on level 1
     * @param currentLevel
     */
    public void loadNextDeviceTypeLevel(int currentLevel) {
        if (currentLevel + 1 < namePartService.nameHierarchy().getDeviceTypeLevels().size()
                && !deviceTypeLevels.get(currentLevel).getOptions().isEmpty()
                && deviceTypeLevels.get(currentLevel).getSelected() != null) {
            deviceTypeLevels.get(currentLevel + 1).setOptions(
                    Lists.transform(namePartService.siblings(deviceTypeLevels.get(currentLevel).getSelected().getNamePart()),
                            new Function<NamePart, NamePartView>() {
                                @Override
                                public NamePartView apply(NamePart f) {
                                    return ViewFactory.getView(f);
                                }
                            }));
            deviceTypeLevels.get(currentLevel + 1).setSelected(null);

            // clear the rest of the inputs
            for (int i = currentLevel + 2; i < namePartService.nameHierarchy().getDeviceTypeLevels().size(); i++) {
                deviceTypeLevels.get(i).clear();
            }
        }
    }

    @FacesConverter("npsvConverter")
    public class NamePartViewConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext context, UIComponent component, String value) {
            if (value == null) return null;

            String componentName = component.getId();
            final List<NamePartSelectionView> selectionList;
            if (componentName.startsWith("sectLvl_"))
                selectionList = sectionLevels;
            else if (componentName.startsWith("devLvl_"))
                selectionList = deviceTypeLevels;
            else
                throw new IllegalStateException("Converter called on illegal UI component: " + component.getId());

            int levelIndex = Integer.parseInt(componentName.split("_")[1]);
            return selectionList.get(levelIndex).getOptions().get(Integer.parseInt(value));
        }

        @Override
        public String getAsString(FacesContext context, UIComponent component, Object value) {
            if (value == null) return null;
            if (!(value instanceof NamePartView))
                throw new IllegalStateException("Converter called on illegal UI component: " + component.getId());

            String componentName = component.getId();
            final List<NamePartSelectionView> selectionList;
            if (componentName.startsWith("sectLvl_"))
                selectionList = sectionLevels;
            else if (componentName.startsWith("devLvl_"))
                selectionList = deviceTypeLevels;
            else
                throw new IllegalStateException("Converter called on illegal UI component: " + component.getId());

            int levelIndex = Integer.parseInt(componentName.split("_")[1]);
            return Integer.toString(selectionList.get(levelIndex).getOptions().indexOf(value));
        }

    }

}
