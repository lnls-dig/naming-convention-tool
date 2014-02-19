package org.openepics.names.ui.devices;

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
import org.openepics.names.ui.common.ViewFactory;
import org.openepics.names.ui.parts.NamePartTreeBuilder;
import org.openepics.names.ui.parts.NamePartView;
import org.primefaces.model.TreeNode;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

@ManagedBean
@ViewScoped
public class DevicesController implements Serializable {

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

    private String deviceQuantifier;

    public DevicesController() {}

    @PostConstruct
    public void init() {
        sections = selectedSection = null;
        deviceTypes =  selectedDeviceType = null;
        selectedDeviceName = null;
        
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
            DeviceRevision rev = deviceService.createDevice(subsection.getNamePart(), device.getNamePart(), deviceQuantifier);
            showMessage(FacesMessage.SEVERITY_INFO, "Device Name successfully added.", "Name: [TODO]" + viewFactory.getView(rev).getConventionName());
        } finally {
            init();
        }
    }

    public void onModify() {
        // TODO solve generically and for specific + generic device
        try {
            
        	final NamePartView subsection = (NamePartView)(selectedSection.getData());
            final NamePartView device = (NamePartView)(selectedDeviceType.getData());
            deviceService.modifyDevice(selectedDeviceName.getDevice().getDevice(), subsection.getNamePart(), device.getNamePart(), deviceQuantifier);
            
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

    public String getDeviceQuantifier() { return deviceQuantifier; }
    public void setDeviceQuantifier(String deviceQuantifier) { this.deviceQuantifier = deviceQuantifier; }

    public void prepareForAdd() {
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

    public void prepareForModify() {
        if (selectedDeviceName == null) {
            sections = null;
            deviceTypes = null;
            return;
        }

        final List<NamePartRevision> currentApprovedRevisions = namePartService.currentApprovedRevisions(false);

        final List<NamePartRevision> approvedSectionRevisions = ImmutableList.copyOf(Collections2.filter(currentApprovedRevisions, new Predicate<NamePartRevision>() {
            @Override public boolean apply(NamePartRevision revision) { return revision.getNamePart().getNamePartType() == NamePartType.SECTION; }
        }));
        final List<NamePartRevision> approvedDeviceTypeRevisions = ImmutableList.copyOf(Collections2.filter(currentApprovedRevisions, new Predicate<NamePartRevision>() {
            @Override public boolean apply(NamePartRevision revision) { return revision.getNamePart().getNamePartType() == NamePartType.DEVICE_TYPE; }
        }));

        final List<NamePartRevision> emptyPending = new ArrayList<>();

        sections = namePartTreeBuilder.namePartApprovalTree(approvedSectionRevisions, emptyPending, false, 2, selectedDeviceName.getSection().getNamePart());
        deviceTypes = namePartTreeBuilder.namePartApprovalTree(approvedDeviceTypeRevisions, emptyPending, false, 2, selectedDeviceName.getDeviceType().getNamePart());
        deviceQuantifier = selectedDeviceName.getQualifier();
        
        selectedSection = findSelectedTreeNode(sections);
        selectedDeviceType = findSelectedTreeNode(deviceTypes);
      
    }
    
    private TreeNode findSelectedTreeNode(TreeNode node) {
    	if (node.isSelected()) {
    		return node;
    	} else if (node.getChildCount() > 0) {
    		for (TreeNode child : node.getChildren()) {
    			TreeNode temp = findSelectedTreeNode(child);
    			if(temp != null) 
    				return temp;
    		}
    	}
    	return null;
    }

    public boolean isFormFilled() {
        return selectedDeviceType != null && selectedSection != null;
    }

    private void showMessage(FacesMessage.Severity severity, String summary, String message) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(severity, summary, message));
    }
}
