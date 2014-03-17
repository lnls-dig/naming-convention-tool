package org.openepics.names.ui.devices;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartType;
import org.openepics.names.services.restricted.RestrictedDeviceService;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.ui.common.ViewFactory;
import org.openepics.names.ui.parts.NamePartTreeBuilder;
import org.openepics.names.ui.parts.NamePartView;
import org.openepics.names.ui.parts.OperationNamePartView;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

@ManagedBean
@ViewScoped
public class DevicesController implements Serializable {

    @Inject private RestrictedNamePartService namePartService;
    @Inject private RestrictedDeviceService deviceService;
    @Inject private NamePartTreeBuilder namePartTreeBuilder;
    @Inject private DevicesTreeBuilder devicesTreeBuilder;
    @Inject private ViewFactory viewFactory;

    private DeviceView selectedDeviceName;

    private List<DeviceView> historyDeviceNames;

    private TreeNode sections;
    private TreeNode deviceTypes;
    private TreeNode selectedSection;
    private TreeNode selectedDeviceType;
    private TreeNode viewRoot;
    private TreeNode[] selectedNodes;
    private TreeNode deleteView;


    private boolean showDeletedNames = true;

    private String deviceQuantifier;

    private int displayView = 2;


    @PostConstruct
    public void init() {
        modifyDisplayView();
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
            showMessage(FacesMessage.SEVERITY_INFO, "Device Name successfully added.", "Name: " + viewFactory.getView(rev).getConventionName());
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
            for (DeviceView deviceView : linearizedTargets(deleteView)) {
            	deviceService.deleteDevice(deviceView.getDevice().getDevice());
            }
            showMessage(FacesMessage.SEVERITY_INFO, "Success", "The data you requested was successfully deleted.");
        } finally {
            init();
        }
    }

    private List<DeviceView> linearizedTargets(TreeNode node) {
    	@Nullable OperationDeviceView nodeView = null;

        if (node.getData() instanceof OperationDeviceView) {
	    	nodeView = (OperationDeviceView) node.getData();
        }

        final List<DeviceView> targets = Lists.newArrayList();
        if (nodeView != null && nodeView.isAffected()) {
            targets.add(nodeView.getDeviceView());
        }
        if (nodeView == null) {
            for (TreeNode child : node.getChildren()) {
                targets.addAll(linearizedTargets(child));
            }
        }
        return targets;
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
    public void setSelectedDeviceName(DeviceView selectedDeviceName) {
    	this.selectedDeviceName = selectedDeviceName;
    }

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

    public TreeNode[] getSelectedNodes() { return selectedNodes; }

    public void setViewFilter(int filter) {
        this.displayView = filter;
    }

    public int getViewFilter() {
        return this.displayView;
    }

    public void modifyDisplayView() {
        switch (displayView) {
        case 1:
            viewRoot = devicesTreeBuilder.devicesTree(true);
            break;
        case 2:
            viewRoot = devicesTreeBuilder.devicesTree(false);
            break;
        }
        sections = selectedSection = null;
        deviceTypes =  selectedDeviceType = null;
        selectedDeviceName = null;
    }

    public void setSelectedNodes(TreeNode[] selectedNodes) {
    	this.selectedNodes = selectedNodes != null ? selectedNodes : new TreeNode[0];
        selectedDeviceName = null;
        selectedSection = null;

        try {
        	selectedDeviceName = (DeviceView) selectedNodes[0].getData();
        } catch (ClassCastException e) {
        	selectedSection = selectedNodes[0];
        }
        deleteView = deleteView(viewRoot, SelectionMode.MANUAL);
    }

    public TreeNode getViewRoot() { return viewRoot; }

    public TreeNode getDeleteView() { return deleteView; }

    public boolean canDelete() { return deleteView != null; }

    public boolean canAdd() {
        if (selectedNodes != null && selectedNodes.length == 1 && selectedNodes[0].getData() instanceof NamePartView && ((NamePartView)selectedNodes[0].getData()).getLevel() == 2) {
            return true;
        }
        return false;
    }

    public boolean canShowHistory() {
        if(selectedNodes != null && selectedNodes.length == 1 && selectedNodes[0].getData() instanceof DeviceView) {
            return true;
        }
        return false;
    }

    public boolean canModify() {
        if(selectedNodes != null && selectedNodes.length == 1 && selectedNodes[0].getData() instanceof DeviceView && !((DeviceView)selectedNodes[0].getData()).getDevice().isDeleted()) {
            return true;
        }
        return false;
    }

    public void prepareForAdd() {
        final List<NamePartRevision> currentApprovedRevisions = namePartService.currentApprovedRevisions(false);

        final List<NamePartRevision> approvedDeviceTypeRevisions = ImmutableList.copyOf(Collections2.filter(currentApprovedRevisions, new Predicate<NamePartRevision>() {
            @Override public boolean apply(NamePartRevision revision) { return revision.getNamePart().getNamePartType() == NamePartType.DEVICE_TYPE; }
        }));

        final List<NamePartRevision> emptyPending = new ArrayList<>();
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

    private enum SelectionMode { MANUAL, AUTO, DISABLED }

    private @Nullable TreeNode deleteView(TreeNode node, SelectionMode selectionMode) {
    	@Nullable NamePartView nodeView = null;
    	@Nullable DeviceView deviceNodeView = null;
    	if(node.getData() instanceof NamePartView) {
    		nodeView = (NamePartView) node.getData();
    	} else {
    		deviceNodeView = (DeviceView) node.getData();
    	}

        final SelectionMode childrenSelectionMode;
        if (selectionMode == SelectionMode.AUTO) {
            childrenSelectionMode = SelectionMode.AUTO;
        } else if (selectionMode == SelectionMode.MANUAL) {
        	 if ((nodeView != null || deviceNodeView != null) && node.isSelected()) {
                 childrenSelectionMode = SelectionMode.AUTO;
             } else {
                 childrenSelectionMode = SelectionMode.MANUAL;
             }
        } else if (selectionMode == SelectionMode.DISABLED) {
            childrenSelectionMode = SelectionMode.DISABLED;
        } else {
            throw new IllegalStateException();
        }

        final List<TreeNode> childViews = Lists.newArrayList();
        for (TreeNode child : node.getChildren()) {
            final TreeNode childView = deleteView(child, childrenSelectionMode);
            if (childView != null) {
                childViews.add(childView);
            }
        }

        final boolean affectNode = (deviceNodeView != null && !deviceNodeView.getDevice().isDeleted()) && (selectionMode == SelectionMode.AUTO || (selectionMode == SelectionMode.MANUAL && node.isSelected()));
        if (affectNode || !childViews.isEmpty()) {
        	final TreeNode result;
        	if (nodeView != null) {
        		result = new DefaultTreeNode(nodeView != null ? new OperationNamePartView(nodeView, affectNode) : null, null);
        	} else {
        		result = new DefaultTreeNode(deviceNodeView != null ? new OperationDeviceView(deviceNodeView, affectNode) : null, null);
        	}
            result.setExpanded(true);
            for (TreeNode childView : childViews) {
                childView.setParent(result);
            }
            return result;
        } else {
            return null;
        }
    }
}
