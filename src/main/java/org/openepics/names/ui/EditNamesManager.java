package org.openepics.names.ui;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
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
import org.openepics.names.model.Device;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartType;
import org.openepics.names.services.restricted.RestrictedDeviceService;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.ui.names.NamePartView;
import org.primefaces.model.TreeNode;

@ManagedBean
@ViewScoped
public class EditNamesManager implements Serializable {

    @Inject private RestrictedNamePartService namePartService;
    @Inject private RestrictedDeviceService deviceService;
    @Inject private NamePartTreeBuilder namePartTreeBuilder;
    @Inject private ViewFactory viewFactory;

    private DeviceView selectedDeviceName;

    private List<DeviceView> allDeviceNames;
    private List<DeviceView> historyDeviceNames;

    private TreeNode sections;
    private TreeNode deviceTypes;
    private TreeNode selectedSection;
    private TreeNode selectedDeviceType;

    private boolean showDeletedNames = true;

    public EditNamesManager() {}

    @PostConstruct
    public void init() {
        sections = selectedSection = null;
        deviceTypes =  selectedDeviceType = null;

        loadDeviceNames();
    }

    public void onAdd() {
        // TODO solve generically and for specific + generic device
        try {
            if (selectedSection == null || selectedDeviceType == null) {
                showMessage(FacesMessage.SEVERITY_ERROR, "Error", "Required field missing");
                return;
            }

            final NamePartView subsection = (NamePartView)(selectedSection.getData());
            final NamePartView device = (NamePartView)(selectedDeviceType.getData());

            if (subsection == null || device == null) {
                showMessage(FacesMessage.SEVERITY_ERROR, "Error", "Required field missing");
                return;
            }
            deviceService.createDevice(subsection.getNamePart(), device.getNamePart(), null);
            showMessage(FacesMessage.SEVERITY_INFO, "Device Name successfully added.", "Name: [TODO]");
        } finally {
            init();
        }
    }

    public void onModify() {
        // TODO solve generically and for specific + generic device
        try {
            /*
            final NamePartView subsection = sectionLevels.get(sectionLevels.size()-1).getSelected();
            final NamePartView genDevice = deviceTypeLevels.get(2).getSelected();
            deviceService.modifyDevice(selectedDeviceName.getDevice().getDevice(), subsection.getNamePart(), genDevice.getNamePart(), null);
            */
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
            this.allDeviceNames.add(viewFactory.getView(dev));
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
                        return viewFactory.getView(f);
                    }
                });
    }

    public DeviceView getSelectedDeviceName() { return selectedDeviceName; }
    public void setSelectedDeviceName(DeviceView selectedDeviceName) { this.selectedDeviceName = selectedDeviceName; }

    public List<DeviceView> getAllDeviceNames() { return allDeviceNames; }

    public List<DeviceView> getHistoryEvents() { return historyDeviceNames; }

    public boolean isShowDeletedNames() { return showDeletedNames; }
    public void setShowDeletedNames(boolean showDeletedNames) { this.showDeletedNames = showDeletedNames; }

    public TreeNode getSections() { return sections; }
    public void setSelectedSection(TreeNode selectedSection) { this.selectedSection = selectedSection; }
    public TreeNode getSelectedSection() { return this.selectedSection; }

    public TreeNode getDeviceTypes() { return deviceTypes; }
    public void setSelectedDeviceType(TreeNode selectedDeviceType) { this.selectedDeviceType = selectedDeviceType; }
    public TreeNode getSelectedDeviceType() { return this.selectedDeviceType; }

    public void prepareFormTrees() {
        final List<NamePartRevision> currentApprovedRevisions = namePartService.currentApprovedRevisions(false);

        final List<NamePartRevision> approvedSectionRevisions = ImmutableList.copyOf(Collections2.filter(currentApprovedRevisions, new Predicate<NamePartRevision>() {
            @Override public boolean apply(NamePartRevision revision) { return revision.getNamePart().getNamePartType() == NamePartType.SECTION; }
        }));
        final List<NamePartRevision> approvedDeviceTypeRevisions = ImmutableList.copyOf(Collections2.filter(currentApprovedRevisions, new Predicate<NamePartRevision>() {
            @Override public boolean apply(NamePartRevision revision) { return revision.getNamePart().getNamePartType() == NamePartType.DEVICE_TYPE; }
        }));

        final List<NamePartRevision> emptyPending = new ArrayList<>();
        sections = namePartTreeBuilder.namePartApprovalTree(approvedSectionRevisions, emptyPending, false, 2);
        deviceTypes = namePartTreeBuilder.namePartApprovalTree(approvedDeviceTypeRevisions, emptyPending, false, 2);
    }

    public boolean isFormFilled() {
        return selectedDeviceType != null && selectedSection != null;
    }

    private void showMessage(FacesMessage.Severity severity, String summary, String message) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(severity, summary, message));
    }
}
