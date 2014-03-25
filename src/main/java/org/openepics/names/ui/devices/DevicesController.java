package org.openepics.names.ui.devices;


import java.io.IOException;
import java.io.InputStream;

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
import javax.servlet.ServletContext;

import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartType;
import org.openepics.names.services.restricted.RestrictedDeviceService;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.ui.common.OperationsTreePreview;
import org.openepics.names.ui.common.TreeViewFilter;
import org.openepics.names.ui.common.ViewFactory;
import org.openepics.names.ui.export.ExcelExport;
import org.openepics.names.ui.parts.NamePartTreeBuilder;
import org.openepics.names.ui.parts.NamePartView;
import org.openepics.names.util.As;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;
import org.primefaces.model.UploadedFile;


@ManagedBean
@ViewScoped
public class DevicesController implements Serializable {

    @Inject private RestrictedNamePartService namePartService;
    @Inject private RestrictedDeviceService deviceService;
    @Inject private NamePartTreeBuilder namePartTreeBuilder;
    @Inject private DevicesTreeBuilder devicesTreeBuilder;
    @Inject private ViewFactory viewFactory;
    @Inject private ExcelImport excelImport;
    @Inject private ExcelExport excelExport;

    private List<DeviceView> historyDeviceNames;

    private TreeNode sections;
    private TreeNode deviceTypes;
    private TreeNode viewRoot;
    private TreeNode viewDevice;
    

    private TreeNode[] selectedNodes = new TreeNode[0];
    private TreeNode deleteView;

    private TreeNode formSelectedSection;
    private TreeNode formSelectedDeviceType;
    private String deviceQuantifier;
    private String deviceNameFilter, appliedDeviceNameFilter = "";
    private String deviceTypeFilter, appliedDeviceTypeFilter = "";

    private DevicesViewFilter displayView = DevicesViewFilter.ACTIVE;

    @PostConstruct
    public void init() {
        modifyDisplayView();
    }

    public void onAdd() {
        try {
            final NamePartView subsection = As.notNull(getFormSelectedSection());
            final NamePartView deviceType = (NamePartView) formSelectedDeviceType.getData();
            final DeviceRevision rev = deviceService.createDevice(subsection.getNamePart(), deviceType.getNamePart(), deviceQuantifier);
            showMessage(FacesMessage.SEVERITY_INFO, "Device Name successfully added.", "Name: " + viewFactory.getView(rev).getConventionName());
        } finally {
            init();
        }
    }

    public void onModify() {
        try {
        	final NamePartView subsection = (NamePartView)(formSelectedSection.getData());
            final NamePartView deviceType = (NamePartView)(formSelectedDeviceType.getData());
            deviceService.modifyDevice(As.notNull(getSelectedDevice()).getDevice().getDevice(), subsection.getNamePart(), deviceType.getNamePart(), deviceQuantifier);
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
        historyDeviceNames = Lists.transform(deviceService.revisions(As.notNull(getSelectedDevice()).getDevice().getDevice()), new Function<DeviceRevision, DeviceView>(){
            @Override public DeviceView apply(DeviceRevision f) { return viewFactory.getView(f);}
        });
    }

    public List<DeviceView> getHistoryEvents() { return historyDeviceNames; }

    public TreeNode getSections() { return sections; }

    public TreeNode getDeviceTypes() { return deviceTypes; }
    public void setFormSelectedDeviceType(TreeNode formSelectedDeviceType) { this.formSelectedDeviceType = formSelectedDeviceType; }
    public TreeNode getFormSelectedDeviceType() { return this.formSelectedDeviceType; }

    public String getDeviceQuantifier() { return deviceQuantifier; }
    public void setDeviceQuantifier(String deviceQuantifier) { this.deviceQuantifier = deviceQuantifier; }

    public TreeNode[] getSelectedNodes() { return selectedNodes; }

    public void setViewFilter(int filter) {
        this.displayView = DevicesViewFilter.values()[filter];
    }
    
    public void setDeviceNameFilter(String filter) {
        deviceNameFilter = filter;
    }

    public String getDeviceNameFilter() {
        return deviceNameFilter;
    }

    public void setDeviceTypeFilter(String filter) {
        deviceTypeFilter = filter;
    }

    public String getDeviceTypeFilter() {
        return deviceTypeFilter;
    }

    public int getViewFilter() {
        return this.displayView.ordinal();
    }
    
    public TreeNode getViewDevice() {
        return viewDevice;
    }

    public void setViewDevice(TreeNode viewDevice) {
        this.viewDevice = viewDevice;
    }
    
    public void cleanDeviceNameFilter() {
        deviceNameFilter = "";
    }
     
    public void cleanDeviceTypeFilter() {
        deviceTypeFilter = "";
    }

    public void checkForFilterChanges() {
        final boolean filterHasChanged;
        if (deviceNameFilter.equals(appliedDeviceNameFilter) && deviceTypeFilter.equals(appliedDeviceTypeFilter)) {
            filterHasChanged = false;
        } else {
            if (!deviceNameFilter.equals(appliedDeviceNameFilter)) {
                appliedDeviceNameFilter = deviceNameFilter;
            }
            if (!deviceTypeFilter.equals(appliedDeviceTypeFilter)) {
                appliedDeviceTypeFilter = deviceTypeFilter;
            }
            filterHasChanged = true;
        }
        
        if (filterHasChanged) {        
            viewDevice = filteredView(viewRoot);
            RequestContext.getCurrentInstance().update("ManageNameForm:devicesTree");
        }        
    }

    public void modifyDisplayView() {
        if (displayView == DevicesViewFilter.ACTIVE) {
            viewRoot = devicesTreeBuilder.devicesTree(false);
        } else if (displayView == DevicesViewFilter.ARCHIVED) {
            viewRoot = devicesTreeBuilder.devicesTree(true);
        } else {
            throw new IllegalStateException();
        }
        viewDevice = filteredView(viewRoot);
        sections = deviceTypes =  formSelectedDeviceType = null;
    }

    public void setSelectedNodes(@Nullable TreeNode[] selectedNodes) {
    	this.selectedNodes = selectedNodes != null ? selectedNodes : new TreeNode[0];
        deleteView = deleteView(viewRoot);
    }

    public @Nullable DeviceView getSelectedDevice() { return selectedNodes.length == 1 && selectedNodes[0].getData() instanceof DeviceView ? (DeviceView) selectedNodes[0].getData() : null; }
    public @Nullable NamePartView getFormSelectedSection() { 
        return selectedNodes.length == 1 && selectedNodes[0].getData() instanceof NamePartView ? (NamePartView) selectedNodes[0].getData() : null; 
    }

    public TreeNode getViewRoot() { return viewRoot; }
    public TreeNode getDeleteView() { return deleteView; }

    public boolean canDelete() { return deleteView != null; }
    public boolean canAdd() { return getFormSelectedSection() != null && getFormSelectedSection().getLevel() == 2; }
    public boolean canShowHistory() { return getSelectedDevice() != null; }
    public boolean canModify() { return getSelectedDevice() != null && !getSelectedDevice().getDevice().isDeleted(); }

    public void prepareForAdd() {
        final List<NamePartRevision> approvedDeviceTypeRevisions = namePartService.currentApprovedRevisions(NamePartType.DEVICE_TYPE, false);
        final List<NamePartRevision> emptyPending = new ArrayList<>();
        deviceTypes = namePartTreeBuilder.namePartApprovalTree(approvedDeviceTypeRevisions, emptyPending, false, 2);
    }

    public void prepareForModify() {
        final List<NamePartRevision> approvedSectionRevisions = namePartService.currentApprovedRevisions(NamePartType.SECTION, false);
        final List<NamePartRevision> approvedDeviceTypeRevisions = namePartService.currentApprovedRevisions(NamePartType.DEVICE_TYPE, false);

        final List<NamePartRevision> emptyPending = new ArrayList<>();

        sections = namePartTreeBuilder.namePartApprovalTree(approvedSectionRevisions, emptyPending, false, 2, As.notNull(getSelectedDevice()).getSection().getNamePart());
        deviceTypes = namePartTreeBuilder.namePartApprovalTree(approvedDeviceTypeRevisions, emptyPending, false, 2, As.notNull(getSelectedDevice()).getDeviceType().getNamePart());
        deviceQuantifier = As.notNull(getSelectedDevice()).getQualifier();

        formSelectedSection = findSelectedTreeNode(sections);
        formSelectedDeviceType = findSelectedTreeNode(deviceTypes);
    }
    
    public void handleFileUpload(FileUploadEvent event) {
        try (InputStream inputStream = event.getFile().getInputstream()) {
            excelImport.parseDeviceImportFile(inputStream);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Import successful!", ""));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public StreamedContent getDownloadableNamesTemplate() {  
        return new DefaultStreamedContent(this.getClass().getResourceAsStream("NamingImportTemplate.xlsx"), "xlsx", "NamingImportTemplate.xlsx");  
    } 
    
    public StreamedContent getAllDataExport() {  
        return new DefaultStreamedContent(excelExport.exportFile(), "xlsx", "export.xlsx");
    } 
   
    private TreeNode findSelectedTreeNode(TreeNode node) {
    	if (node.isSelected()) {
    	    return node;
    	} else {
    		for (TreeNode child : node.getChildren()) {
    			TreeNode temp = findSelectedTreeNode(child);
    			if (temp != null) {
    				return temp;
                }
    		}
            return null;
    	}
    }

    public boolean isFormFilled() { return selectedNodes.length > 0 && selectedNodes[0].getData() instanceof NamePartView && formSelectedDeviceType != null; }

    private void showMessage(FacesMessage.Severity severity, String summary, String message) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(severity, summary, message));
    }

    private @Nullable TreeNode deleteView(TreeNode node) {
        return (new OperationsTreePreview<Object>() {
            @Override protected boolean isAffected(Object nodeView) { return nodeView instanceof DeviceView && !(((DeviceView) nodeView).getDevice().isDeleted()); }
            @Override protected boolean autoSelectChildren(Object nodeView) { return true; }
            @Override protected boolean ignoreSelectedChildren(Object nodeView, boolean isSelected) { return false; }
        }).apply(node);
    }
    
   private TreeNode filteredView(TreeNode node) {
        final @Nullable TreeNode filteredView = (new TreeViewFilter<Object>() {

            @Override
            protected boolean addToTreeView(Object nodeView) {
                if (appliedDeviceNameFilter.equals("") && appliedDeviceTypeFilter.equals("")) {
                    return true;
                } else if (!appliedDeviceNameFilter.equals("") && (nodeView instanceof DeviceView && ((DeviceView) nodeView).getConventionName().contains(appliedDeviceNameFilter) && appliedDeviceTypeFilter.equals(""))) {
                    return true;
                } else if (!appliedDeviceTypeFilter.equals("") && (nodeView instanceof DeviceView && ((DeviceView) nodeView).getDeviceTypePath().contains(appliedDeviceTypeFilter) && appliedDeviceNameFilter.equals(""))){
                    return true;
                } else if (!appliedDeviceNameFilter.equals("") && (nodeView instanceof DeviceView && ((DeviceView) nodeView).getConventionName().contains(appliedDeviceNameFilter) && !appliedDeviceTypeFilter.equals("") && (nodeView instanceof DeviceView && ((DeviceView) nodeView).getDeviceTypePath().contains(appliedDeviceTypeFilter)))) {
                    return true;
                } else {
                    return false;
                }
            }
        }).apply(node);
        
        return filteredView != null ? filteredView : new DefaultTreeNode(null, null); 
    }
    
    private enum DevicesViewFilter {
        ACTIVE, ARCHIVED
    }
    
}
