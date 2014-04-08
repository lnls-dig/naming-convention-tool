package org.openepics.names.ui.devices;


import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartType;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.services.views.DeviceView;
import org.openepics.names.services.views.NamePartView;
import org.openepics.names.ui.common.OperationTreeGenerator;
import org.openepics.names.ui.common.OperationView;
import org.openepics.names.ui.common.TreeFilter;
import org.openepics.names.ui.common.ViewFactory;
import org.openepics.names.ui.export.ExcelExport;
import org.openepics.names.ui.parts.NamePartTreeBuilder;
import org.openepics.names.util.As;
import org.openepics.names.util.UnhandledCaseException;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * A UI controller bean for the Device Names screen.
 */
@ManagedBean
@ViewScoped
public class DevicesController implements Serializable {

    @Inject private RestrictedNamePartService namePartService;
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
    private String formInstanceIndex = "";
    private String deviceNameFilter, appliedDeviceNameFilter = "";
    private String deviceTypeFilter, appliedDeviceTypeFilter = "";

    private DevicesViewFilter displayView = DevicesViewFilter.ACTIVE;

    @PostConstruct
    public void init() {
        modifyDisplayView();
    }

    public boolean isAddInstanceIndexValid(String instanceIndex) {
        final NamePart section = As.notNull(getSelectedSection()).getNamePart();
        final NamePart deviceType = ((NamePartView) formSelectedDeviceType.getData()).getNamePart();
        return namePartService.isInstanceIndexValid(section, deviceType, instanceIndex);
    }

    public boolean isModifyInstanceIndexValid(String instanceIndex) {
        final NamePart section = ((NamePartView) formSelectedSection.getData()).getNamePart();
        final NamePart deviceType = ((NamePartView) formSelectedDeviceType.getData()).getNamePart();
        return namePartService.isInstanceIndexValid(section, deviceType, instanceIndex);
    }

    public boolean isAddInstanceIndexUnique(String instanceIndex) {
        final NamePart section = As.notNull(getSelectedSection()).getNamePart();
        final NamePart deviceType = ((NamePartView) formSelectedDeviceType.getData()).getNamePart();
        return namePartService.isDeviceConventionNameUnique(section, deviceType, instanceIndex);
    }

    public boolean isModifyInstanceIndexUnique(String instanceIndex) {
        final DeviceView deviceView = As.notNull(getSelectedDevice());
        final NamePart section = ((NamePartView) formSelectedSection.getData()).getNamePart();
        final NamePart deviceType = ((NamePartView) formSelectedDeviceType.getData()).getNamePart();
        if (!(section.equals(deviceView.getSection().getNamePart()) && deviceType.equals(deviceView.getDeviceType().getNamePart()) && Objects.equals(instanceIndex, deviceView.getInstanceIndex()))) {
            return namePartService.isDeviceConventionNameUnique(section, deviceType, instanceIndex);
        } else {
            return true;
        }
    }

    public void onAdd() {
        try {
            final NamePart subsection = As.notNull(getSelectedSection()).getNamePart();
            final NamePart deviceType = ((NamePartView) formSelectedDeviceType.getData()).getNamePart();
            final DeviceRevision rev = namePartService.addDevice(subsection, deviceType, getFormInstanceIndex());
            showMessage(null, FacesMessage.SEVERITY_INFO, "Success", "Device name has been added.");
        } finally {
            init();
        }
    }

    public void onModify() {
        try {
        	final NamePart subsection = ((NamePartView) formSelectedSection.getData()).getNamePart();
            final NamePart deviceType = ((NamePartView) formSelectedDeviceType.getData()).getNamePart();
            namePartService.modifyDevice(As.notNull(getSelectedDevice()).getDevice().getDevice(), subsection, deviceType, getFormInstanceIndex());
            showMessage(null, FacesMessage.SEVERITY_INFO, "Success", "Device name has been modified.");
        } finally {
            init();
        }
    }

    public void onDelete() {
    	try {
            final List<DeviceView> targets = linearizedTargets(deleteView);
            for (DeviceView deviceView : targets) {
            	namePartService.deleteDevice(deviceView.getDevice().getDevice());
            }
            showMessage(null, FacesMessage.SEVERITY_INFO, "Success", printedAffectedQuantity(targets.size()) + "deleted.");
        } finally {
            init();
        }
    }

    private String printedAffectedQuantity(int n) {
        return n + " device name" + (n > 1 ? "s have been " : " has been ");
    }

    private List<DeviceView> linearizedTargets(TreeNode node) {
    	final @Nullable OperationView<DeviceView> operationView = (OperationView<DeviceView>) node.getData();
        final List<DeviceView> targets = Lists.newArrayList();
        if (operationView != null && operationView.isAffected()) {
            targets.add(operationView.getData());
        }
        for (TreeNode child : node.getChildren()) {
            targets.addAll(linearizedTargets(child));
        }
        return targets;
    }


    public void loadHistory() {
        historyDeviceNames = Lists.transform(namePartService.revisions(As.notNull(getSelectedDevice()).getDevice().getDevice()), new Function<DeviceRevision, DeviceView>() {
            @Override public DeviceView apply(DeviceRevision f) { return viewFactory.getView(f);}
        });
    }

    public List<DeviceView> getHistoryEvents() { return historyDeviceNames; }

    public TreeNode getSections() { return sections; }

    public TreeNode getDeviceTypes() { return deviceTypes; }

    public TreeNode getFormSelectedSection() { return formSelectedSection; }
    public void setFormSelectedSection(TreeNode formSelectedSection) { this.formSelectedSection = formSelectedSection; }

    public TreeNode getFormSelectedDeviceType() { return this.formSelectedDeviceType; }
    public void setFormSelectedDeviceType(TreeNode formSelectedDeviceType) { this.formSelectedDeviceType = formSelectedDeviceType; }

    public String getFormInstanceIndex() { return formInstanceIndex; }
    public void setFormInstanceIndex(String formInstanceIndex) { this.formInstanceIndex = !formInstanceIndex.isEmpty() ? formInstanceIndex : null; }

    public TreeNode[] getSelectedNodes() { return selectedNodes; }

    public @Nullable String getDeviceNameFilter() { return deviceNameFilter; }
    public void setDeviceNameFilter(@Nullable String deviceNameFilter) { this.deviceNameFilter = deviceNameFilter; }

    public @Nullable String getDeviceTypeFilter() { return deviceTypeFilter; }
    public void setDeviceTypeFilter(@Nullable String deviceTypeFilter) { this.deviceTypeFilter = deviceTypeFilter; }

    public DevicesViewFilter getViewFilter() { return this.displayView; }
    public void setViewFilter(DevicesViewFilter viewFilter) { this.displayView = viewFilter; }
    
    public TreeNode getViewDevice() { return viewDevice; }
    public void setViewDevice(TreeNode viewDevice) { this.viewDevice = viewDevice; }
    
    public void clearDeviceNameFilter() {
        deviceNameFilter = null;
        checkForFilterChanges();
    }
     
    public void clearDeviceTypeFilter() {
        deviceTypeFilter = null;
        checkForFilterChanges();
    }

    public void checkForFilterChanges() {
        final boolean filterHasChanged = !Objects.equals(deviceNameFilter, appliedDeviceNameFilter) || !Objects.equals(deviceTypeFilter, appliedDeviceTypeFilter);
        if (filterHasChanged) {
            appliedDeviceNameFilter = deviceNameFilter;
            appliedDeviceTypeFilter = deviceTypeFilter;
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
            throw new UnhandledCaseException();
        }
        viewDevice = filteredView(viewRoot);
        sections = deviceTypes =  formSelectedDeviceType = null;
        this.selectedNodes = new TreeNode[0];
        deleteView = deleteView(viewDevice);
    }

    public void setSelectedNodes(@Nullable TreeNode[] selectedNodes) {
    	this.selectedNodes = selectedNodes != null ? selectedNodes : new TreeNode[0];
        deleteView = deleteView(viewDevice);
    }

    public @Nullable NamePartView getSelectedSection() { return selectedNodes.length == 1 && selectedNodes[0].getData() instanceof NamePartView ? (NamePartView) selectedNodes[0].getData() : null; }
    public @Nullable DeviceView getSelectedDevice() { return selectedNodes.length == 1 && selectedNodes[0].getData() instanceof DeviceView ? (DeviceView) selectedNodes[0].getData() : null; }

    public TreeNode getDeleteView() { return deleteView; }

    public boolean canDelete() { return deleteView != null; }
    public boolean canAdd() { return getSelectedSection() != null && getSelectedSection().getLevel() == 2 && !getSelectedSection().isDeleted(); }
    public boolean canShowHistory() { return getSelectedDevice() != null; }
    public boolean canModify() { return getSelectedDevice() != null && !getSelectedDevice().getDevice().isDeleted(); }

    public void prepareAddPopup() {
        formSelectedDeviceType = null;
        formInstanceIndex = null;
        final List<NamePartRevision> approvedDeviceTypeRevisions = namePartService.currentApprovedNamePartRevisions(NamePartType.DEVICE_TYPE, false);
        deviceTypes = namePartTreeBuilder.newNamePartTree(approvedDeviceTypeRevisions, Lists.<NamePartRevision>newArrayList(), false, 2);
        RequestContext.getCurrentInstance().reset("addDeviceNameForm:grid");
    }

    public void prepareModifyPopup() {
        final List<NamePartRevision> approvedSectionRevisions = namePartService.currentApprovedNamePartRevisions(NamePartType.SECTION, false);
        final List<NamePartRevision> approvedDeviceTypeRevisions = namePartService.currentApprovedNamePartRevisions(NamePartType.DEVICE_TYPE, false);
        sections = namePartTreeBuilder.newNamePartTree(approvedSectionRevisions, Lists.<NamePartRevision>newArrayList(), false, 2, As.notNull(getSelectedDevice()).getSection().getNamePart());
        deviceTypes = namePartTreeBuilder.newNamePartTree(approvedDeviceTypeRevisions, Lists.<NamePartRevision>newArrayList(), false, 2, As.notNull(getSelectedDevice()).getDeviceType().getNamePart());

        formSelectedSection = findSelectedTreeNode(sections);
        formSelectedDeviceType = findSelectedTreeNode(deviceTypes);
        formInstanceIndex = As.notNull(getSelectedDevice()).getInstanceIndex();

        RequestContext.getCurrentInstance().reset("modDeviceNameForm:grid");
    }
    
    public void handleFileUpload(FileUploadEvent event) {
        try (InputStream inputStream = event.getFile().getInputstream()) {
            ExcelImport.ExcelImportResult importResult = excelImport.parseDeviceImportFile(inputStream);
            if (importResult instanceof ExcelImport.SuccessExcelImportResult) {
                modifyDisplayView();
                showMessage(null, FacesMessage.SEVERITY_INFO, "Import successful!", "");
            } else if (importResult instanceof ExcelImport.FailureExcelImportResult) {
                ExcelImport.FailureExcelImportResult faliureImportResult = (ExcelImport.FailureExcelImportResult) importResult;
                showMessage(null, FacesMessage.SEVERITY_ERROR, "Import failed!", "Error occurred in row " + faliureImportResult.getRowNumber() + ". " + (faliureImportResult.getNamePartType().equals(NamePartType.SECTION) ? "Logical area" : "Device category") + " part was not found in the database.");
            } else {
                throw new UnhandledCaseException();
            }                
        } catch (IOException e) {
            throw new RuntimeException();           
        }
    }
    
    public StreamedContent getDownloadableNamesTemplate() {  
        return new DefaultStreamedContent(this.getClass().getResourceAsStream("NamingImportTemplate.xlsx"), "xlsx", "NamingImportTemplate.xlsx");  
    } 
    
    public StreamedContent getAllDataExport() {  
        return new DefaultStreamedContent(excelExport.exportFile(), "xlsx", "NamingConventionExport.xlsx");
    }

    public String historyRevisionStyleClass(DeviceView req) {
        return req != null && req.getDevice().isDeleted() ? "Delete-Approved" : "";
    }

    public String sectionPath(DeviceView deviceView) {
        return Joiner.on(" ▸ ").join(deviceView.getSection().getNamePath());
    }

    public String deviceTypePath(DeviceView deviceView) {
        return Joiner.on(" ▸ ").join(deviceView.getDeviceType().getNamePath());
    }
   
    private TreeNode findSelectedTreeNode(TreeNode node) {
    	if (node.isSelected()) {
    	    return node;
    	} else {
    		for (TreeNode child : node.getChildren()) {
    			final TreeNode selectedChildNode = findSelectedTreeNode(child);
    			if (selectedChildNode != null) {
    				return selectedChildNode;
                }
    		}
            return null;
    	}
    }

    private void showMessage(@Nullable String notificationChannel, FacesMessage.Severity severity, String summary, String message) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(notificationChannel, new FacesMessage(severity, summary, message));
    }

    private @Nullable TreeNode deleteView(TreeNode node) {
        return (new OperationTreeGenerator<Object>() {
            @Override protected boolean isAffected(Object nodeView) { return nodeView instanceof DeviceView && !(((DeviceView) nodeView).getDevice().isDeleted()); }
            @Override protected boolean autoSelectChildren(Object nodeView) { return true; }
            @Override protected boolean ignoreSelectedChildren(Object nodeView, boolean isSelected) { return false; }
        }).apply(node);
    }
    
    private TreeNode filteredView(TreeNode node) {
        final @Nullable TreeNode filteredView = (new TreeFilter<Object>() {
            @Override protected boolean accepts(Object nodeData) {
                if (nodeData instanceof NamePartView) {
                    return (deviceNameFilter == null || deviceNameFilter.equals(""))  && (deviceTypeFilter == null || deviceTypeFilter.equals(""));
                } else if (nodeData instanceof DeviceView) {
                    final String name = ((DeviceView) nodeData).getConventionName().toLowerCase();
                    final String deviceType = deviceTypePath((DeviceView) nodeData).toLowerCase();
                    final boolean nameMatches = appliedDeviceNameFilter == null || name.contains(appliedDeviceNameFilter.toLowerCase());
                    final boolean deviceTypeMatches = appliedDeviceTypeFilter == null || deviceType.contains(appliedDeviceTypeFilter.toLowerCase());
                    return nameMatches && deviceTypeMatches;
                } else {
                    throw new UnhandledCaseException();
                }
            }
        }).apply(node);
        
        return filteredView != null ? filteredView : new DefaultTreeNode(null, null); 
    }
    
    private enum DevicesViewFilter {
        ACTIVE, ARCHIVED
    }
    
}
