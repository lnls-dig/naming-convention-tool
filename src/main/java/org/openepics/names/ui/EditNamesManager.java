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
import org.openepics.names.model.NameHierarchy;
import org.openepics.names.model.NamePart;
import org.openepics.names.services.restricted.RestrictedDeviceService;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.ui.names.NamePartView;

@ManagedBean
@ViewScoped
public class EditNamesManager implements Serializable {

    @Inject private RestrictedNamePartService namePartService;
    @Inject private RestrictedDeviceService deviceService;
    @Inject private ViewFactory viewFactory;

    private DeviceView selectedDeviceName;

    private List<NamePartSelectionView> sectionLevels;
    private List<NamePartSelectionView> deviceTypeLevels;

    private List<DeviceView> allDeviceNames;
    private List<DeviceView> historyDeviceNames;

    private boolean showDeletedNames = true;

    public EditNamesManager() {}

    @PostConstruct
    public void init() {
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
            deviceService.createDevice(subsection.getNamePart(), genDevice.getNamePart(), null);
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
            deviceService.modifyDevice(selectedDeviceName.getDevice().getDevice(), subsection.getNamePart(), genDevice.getNamePart(), null);
            showMessage(FacesMessage.SEVERITY_INFO, "Device modified.", "Name: [TODO]");
        } finally {
            init();
        }
    }

    public void onDelete() {
        try {
            deviceService.deleteDevice(selectedDeviceName.getDevice().getDevice());
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
         return selectedDeviceName != null ? Joiner.on(" ▸ ").join(selectedDeviceName.getSection().getNamePath()) : null;
    }

    public String getSelectedDeviceNameDisciplineString() {
        return selectedDeviceName != null ? Joiner.on(" ▸ ").join(selectedDeviceName.getDeviceType().getNamePath()) : null;
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
                                    return viewFactory.getView(f);
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
                                    return viewFactory.getView(f);
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

            final String componentName = component.getId();

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
