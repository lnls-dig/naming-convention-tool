/*-
 * Copyright (c) 2014 European Spallation Source
 * Copyright (c) 2014 Cosylab d.d.
 *
 * This file is part of Naming Service.
 * Naming Service is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 2 of the License, or any newer version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/gpl-2.0.txt
 */

package org.openepics.names.ui.devices;


import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.util.SystemOutLogger;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartType;
import org.openepics.names.services.NamingConvention;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.services.views.DeviceRecordView;
import org.openepics.names.services.views.DeviceView;
import org.openepics.names.services.views.NamePartView;
import org.openepics.names.ui.common.OperationTreeGenerator;
import org.openepics.names.ui.common.OperationView;
import org.openepics.names.ui.common.SelectRecordManager;
import org.openepics.names.ui.common.TreeFilter;
import org.openepics.names.ui.common.TreeNodeManager;
import org.openepics.names.ui.common.UserManager;
import org.openepics.names.ui.common.ViewFactory;
import org.openepics.names.ui.export.ExcelExport;
import org.openepics.names.ui.parts.NamePartTreeBuilder;
import org.openepics.names.util.As;
import org.openepics.names.util.UnhandledCaseException;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
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

import java.io.ByteArrayInputStream;
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

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Inject private RestrictedNamePartService namePartService;
	//	@Inject private NamePartTreeBuilder namePartTreeBuilder;
	@Inject private DevicesTreeBuilder devicesTreeBuilder;
	@Inject private ViewFactory viewFactory;
	@Inject private ExcelImport excelImport;
	@Inject private ExcelExport excelExport;
	//	@Inject private NamingConvention namingConvention;
	@Inject private TreeNodeManager treeNodeManager;
	@Inject private SelectRecordManager selectRecordManager;

	private List<DeviceView> historyDeviceNames;

	//	private TreeNode sections;
	//	private TreeNode deviceTypes;
	private TreeNode viewRoot;
	private TreeNode viewDevice;
	private TreeNode deleteView;

<<<<<<< HEAD
	private TreeNode formSelectedSection;
	private TreeNode formSelectedDeviceType;
	private String formInstanceIndex = "";
	private String formAdditionalInfo = "";
	private String formDeviceName="";
	private TreeNode[] selectedNodes=new TreeNode[0];
=======
	//	private TreeNode formSelectedSection;
	//	private TreeNode formSelectedDeviceType;
	//	private String formInstanceIndex = "";
	//	private String formAdditionalInfo = "";
	//	private String formDeviceName="";
>>>>>>> NS324

	private byte[] importData;
	private String importFileName;
	private String deviceNameFilter, appliedDeviceNameFilter = "";
	private String deviceTypeFilter, appliedDeviceTypeFilter = "";
	private DevicesViewFilter displayView = DevicesViewFilter.ACTIVE;
<<<<<<< HEAD
	
=======

	private DeviceView[] selectedDevices;
	//	private DeviceView selectedDevice;

>>>>>>> NS324
	@PostConstruct
	public synchronized void init() {
		modifyDisplayView();
		@Nullable String deviceName = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("deviceName");
			try {
				@Nullable DeviceRevision deviceRevision = deviceName!=null ? namePartService.currentDeviceRevision(deviceName):null;
				@Nullable TreeNode node = deviceRevision!=null ? getNode(deviceRevision): null;
				if(node!=null) {
					expandParents(node);					
					node.setSelected(true);				
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
				
		}
	
	public String getCcdbUrl(){
		return getSelectedDeviceName()!=null? System.getProperty("names.ccdbURL").concat("?name=").concat(getSelectedDeviceName()):"";		
	}
	
	private void expandParents(@Nullable TreeNode node) {
		if(node!=null){
		node.setExpanded(true);
		expandParents(node.getParent());
		}
	}

	//	public void onNodeExpand(NodeExpandEvent event){
	//		if(event!=null && event.getTreeNode() !=null){
	//			treeNodeManager.expand(event.getTreeNode());
	//		}
	//	}
	//
	//	public void onNodeCollapse(NodeCollapseEvent event){
	//		if(event!=null && event.getTreeNode() !=null){
	//			treeNodeManager.collapse(event.getTreeNode());    	
	//		}
	//	}

	public synchronized void onExpandAll(){
		treeNodeManager.expandAll(viewDevice);
	}
	public synchronized void onCollapseAll(){
		treeNodeManager.collapseAll(viewDevice);
	}

	//	public boolean isAddInstanceIndexValid(String instanceIndex) {
	////		final NamePart section = As.notNull(getSelectedSection()).getNamePart();
	//		final NamePart section=((NamePartView) formSelectedSection.getData()).getNamePart();
	//		final NamePart deviceType = ((NamePartView) formSelectedDeviceType.getData()).getNamePart();
	//		return namePartService.isInstanceIndexValid(section, deviceType, instanceIndex);
	//	}

	//	public boolean isModifyInstanceIndexValid(String instanceIndex) {
	//		final NamePart section = ((NamePartView) formSelectedSection.getData()).getNamePart();
	//		final NamePart deviceType = ((NamePartView) formSelectedDeviceType.getData()).getNamePart();
	//		return namePartService.isInstanceIndexValid(section, deviceType, instanceIndex);
	//	}

	//	public boolean isAddInstanceIndexUnique(String instanceIndex) {
	////		final NamePart section = As.notNull(getSelectedSection()).getNamePart();
	//		final NamePart section=((NamePartView) formSelectedSection.getData()).getNamePart();
	//		final NamePart deviceType = ((NamePartView) formSelectedDeviceType.getData()).getNamePart();
	//		return namePartService.isDeviceConventionNameUnique(section, deviceType, instanceIndex);
	//	}
	//
	//	public boolean isModifyInstanceIndexUnique(String instanceIndex) {
	//		final DeviceView deviceView = As.notNull(getSelectedDevice());
	//		final NamePart section = ((NamePartView) formSelectedSection.getData()).getNamePart();
	//		final NamePart deviceType = ((NamePartView) formSelectedDeviceType.getData()).getNamePart();
	//		boolean isSame = section.equals(deviceView.getSection().getNamePart()) && 
	//				deviceType.equals(deviceView.getDeviceType().getNamePart()) && 
	//				Objects.equals(instanceIndex, deviceView.getInstanceIndex());
	//		if (!(isSame)) {
	//			return namePartService.isDeviceConventionNameUniqueExceptForItself(As.notNull(getSelectedDevice()).getDevice().getDevice(),section, deviceType, instanceIndex);
	//		} else {
	//			return true;
	//		}
	//	}
	//
	//	public String onFlowProcess(FlowEvent event){    	
	//		return event.getNewStep();
	//	}
	//
	//	public void setFormDeviceName(){
	//		final List<String> sectionPath = ((NamePartView) formSelectedSection.getData()).getMnemonicPath();
	//		final List<String> devicePath = ((NamePartView) formSelectedDeviceType.getData()).getMnemonicPath();
	//		String formDeviceName=namingConvention.conventionName(sectionPath, devicePath, getFormInstanceIndex());
	//		this.formDeviceName= formDeviceName != null ? formDeviceName: "";
	//	}
	//
	//	public String getFormDeviceName(){
	//		return formDeviceName;
	//	}
	//
	//	public String mnemonic(NamePartView namePartView){
	//		NamePartType namePartType= namePartView.getNamePart().getNamePartType();
	//		String mnemonic=null;
	//		final List<String> path = namePartView.getMnemonicPath();
	//		if(path.size()>=3  && namePartType.equals(NamePartType.SECTION)){
	//			mnemonic= namingConvention.areaName(path);
	//		} else if(path.size()>=3 && namePartType.equals(NamePartType.DEVICE_TYPE)){
	//			mnemonic = namingConvention.deviceDefinition(path);
	//		} 
	//		return mnemonic != null ? mnemonic: "";
	//	}
	//
	//	public String formAreaName(){
	//		final List<String> sectionPath = ((NamePartView) formSelectedSection.getData()).getMnemonicPath();
	//		String formAreaName=namingConvention.areaName(sectionPath);
	//		return formAreaName != null ? formAreaName: "";
	//	}
	//
	//	public void onAdd() {
	//		try {
	//			final NamePart subsection = ((NamePartView) formSelectedSection.getData()).getNamePart();
	//			final NamePart deviceType = ((NamePartView) formSelectedDeviceType.getData()).getNamePart();
	//			final DeviceRevision rev = namePartService.addDevice(subsection, deviceType, getFormInstanceIndex(), getFormAdditionalInfo());
	//			showMessage(null, FacesMessage.SEVERITY_INFO, "Success", "Device name "+rev.getConventionName()+ " has been added.");
	//		} finally {
	//			init();
	//		}
	//	}
	//
	//	public void onModify() {
	//		try {
	//			final NamePart subsection = ((NamePartView) formSelectedSection.getData()).getNamePart();
	//			final NamePart deviceType = ((NamePartView) formSelectedDeviceType.getData()).getNamePart();
	//			namePartService.modifyDevice(As.notNull(getSelectedDevice()).getDevice().getDevice(), subsection, deviceType, getFormInstanceIndex(), getFormAdditionalInfo());
	//			showMessage(null, FacesMessage.SEVERITY_INFO, "Success", "Device name has been modified.");
	//		} finally {
	//			init();
	//		}
	//	}

	public void onDelete() {
		int count=0;
		try{			
			for(DeviceRecordView record: selectRecordManager.getSelectedRecords()){

				if(!record.isDeleted()) {
					namePartService.deleteDevice(record.getDeviceView().getDevice().getDevice());
					count++;
				}
			}
			showMessage(null, FacesMessage.SEVERITY_INFO, "Success", printedAffectedQuantity(count) + "deleted.");
		} finally{
			init();
		}

//		try {
//			final List<DeviceView> targets = linearizedTargets(deleteView);
//			for (DeviceView deviceView : targets) {
//				namePartService.deleteDevice(deviceView.getDevice().getDevice());
//			}
//			showMessage(null, FacesMessage.SEVERITY_INFO, "Success", printedAffectedQuantity(targets.size()) + "deleted.");
//		} finally {
//			init();
//		}
	}

	public void onImport() {
		try (InputStream inputStream = new ByteArrayInputStream(importData)) {
			ExcelImport.ExcelImportResult importResult = excelImport.parseDeviceImportFile(inputStream);
			if (importResult instanceof ExcelImport.SuccessExcelImportResult) {
				modifyDisplayView();
				showMessage(null, FacesMessage.SEVERITY_INFO, "Import was successful!", "");
			} else if (importResult instanceof ExcelImport.CellValueFailureExcelImportResult) {
				ExcelImport.CellValueFailureExcelImportResult failureImportResult = (ExcelImport.CellValueFailureExcelImportResult) importResult;
				showMessage(null, FacesMessage.SEVERITY_ERROR, "Import failed!", "Error occurred in row " + failureImportResult.getRowNumber() + ". " + (failureImportResult.getNamePartType().equals(NamePartType.SECTION) ? "Logical area" : "Device category") + " part was not found in the database.");
			} else if (importResult instanceof ExcelImport.ColumnCountFailureExcelImportResult) {
				showMessage(null, FacesMessage.SEVERITY_ERROR, "Import failed!", "Error occurred when reading import file. Column count does not match expected value.");
			} else {
				throw new UnhandledCaseException();
			}
		} catch (IOException e) {
			throw new RuntimeException();
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

//	public void loadHistory() {
//		historyDeviceNames = Lists.transform(namePartService.revisions(As.notNull(getSelectedDevice()).getDevice().getDevice()), new Function<DeviceRevision, DeviceView>() {
//			@Override public DeviceView apply(DeviceRevision f) { return viewFactory.getView(f);}
//		});
//	}
//
//	public List<DeviceView> getHistoryEvents() { return historyDeviceNames; }

	//	public TreeNode getSections() { return sections; }

	//	public TreeNode getDeviceTypes() { return deviceTypes; }

	//	public TreeNode getFormSelectedSection() { return formSelectedSection; }
	//	public void setFormSelectedSection(TreeNode formSelectedSection) { this.formSelectedSection = formSelectedSection; }

	//	public TreeNode getFormSelectedDeviceType() { return this.formSelectedDeviceType; }
	//	public void setFormSelectedDeviceType(TreeNode formSelectedDeviceType) { this.formSelectedDeviceType = formSelectedDeviceType; }

	//	public String getFormInstanceIndex() { return formInstanceIndex; }

	//	public void setFormInstanceIndex(String formInstanceIndex) { 
	//		this.formInstanceIndex = !formInstanceIndex.isEmpty() ? formInstanceIndex : null; 
	//		setFormDeviceName();
	//	}

	//	public String getFormAdditionalInfo(){ return formAdditionalInfo;}
	//	public void setFormAdditionalInfo(String formAdditionalInfo){ this.formAdditionalInfo = !formAdditionalInfo.isEmpty() ? formAdditionalInfo : null; }

	public String getImportFileName() { return importFileName; }

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
<<<<<<< HEAD
		sections = deviceTypes =  formSelectedDeviceType = null;
		setSelectedNodes(new TreeNode[0]);
	}

	public TreeNode getNode(DeviceRevision deviceRevision){
		for(TreeNode node: getNodeList(viewDevice)){
				if(node.getData() instanceof DeviceView){
					DeviceRevision device=((DeviceView) node.getData()).getDevice();
					if(device.getConventionName().equals(deviceRevision.getConventionName())){
						return node;
					};
				}
		}
		return null;
=======
		//		sections = deviceTypes =  formSelectedDeviceType = null;
		this.selectedNodes = new TreeNode[0];
		deleteView = deleteView(viewDevice);
>>>>>>> NS324
	}

	
	
	public String cssStyle(DeviceView deviceView){
			
			if(deviceView.getDevice().isDeleted()){
				return "Deleted";
			} else {
				return "Approved";
			}
		
	}
	
	
	private List<TreeNode> getNodeList(TreeNode node) {
		final List<TreeNode> nodeList= Lists.newArrayList();
		nodeList.add(node);
		for(TreeNode child:node.getChildren()){
			nodeList.addAll(getNodeList(child));
		}
		return nodeList;
	}
	public TreeNode[] getSelectedNodes(){
		return selectedNodes;
	}
	
	public void setSelectedNodes(@Nullable TreeNode[] selectedNodes) {
		this.selectedNodes=(selectedNodes != null ? selectedNodes : new TreeNode[0]);
		deleteView = deleteView(viewDevice);
	}

<<<<<<< HEAD
	public @Nullable NamePartView getSelectedSection() { 
		return selectedNodes.length == 1 && selectedNodes[0].getData() instanceof NamePartView ? (NamePartView) selectedNodes[0].getData() : null; }
	public @Nullable DeviceView getSelectedDevice() { 
		return selectedNodes.length == 1 && selectedNodes[0].getData() instanceof DeviceView ? (DeviceView) selectedNodes[0].getData() : null; }
 
	public  String getSelectedDeviceName(){
		return getSelectedDevice()!=null? getSelectedDevice().getDevice().getConventionName():"";
	}
=======
	//public @Nullable NamePartView getSelectedSection() { return selectedNodes.length == 1 && selectedNodes[0].getData() instanceof NamePartView ? (NamePartView) selectedNodes[0].getData() : null; }
	public @Nullable DeviceView getSelectedDevice() { 
		return selectedDevices != null && selectedDevices.length==1? selectedDevices[0] : null;
		//		return selectedNodes.length == 1 && selectedNodes[0].getData() instanceof DeviceView ? (DeviceView) selectedNodes[0].getData() : null; 
		//		return selectedDevice;
	}
	public void setSelectedDevices(@Nullable DeviceView[] selectedDevices){
		this.selectedDevices=selectedDevices;
	}
	//	public void setSelectedDevice(@Nullable DeviceView selectedDevice){
	//		this.selectedDevice=selectedDevice;
	//	}

>>>>>>> NS324
	public TreeNode getDeleteView() { return deleteView; }

	public boolean canDelete() { return deleteView != null; }
	public boolean canAdd() { return true;
	//	return getSelectedSection() != null && getSelectedSection().getLevel() == 2 && !getSelectedSection().isDeleted(); 
	}
//	public boolean canShowHistory() { return getSelectedDevice() != null; }
	public boolean canModify() { return getSelectedDevice() != null && !getSelectedDevice().getDevice().isDeleted(); }

	//	public void prepareAddPopup() {

	//		final List<NamePartRevision> approvedSectionRevisions = namePartService.currentApprovedNamePartRevisions(NamePartType.SECTION, false);
	//		sections = namePartTreeBuilder.newNamePartTree(approvedSectionRevisions, Lists.<NamePartRevision>newArrayList(), false, 2, As.notNull(getSelectedSection().getNamePart()));
	//		sections = namePartTreeBuilder.newNamePartTree(approvedSectionRevisions, Lists.<NamePartRevision>newArrayList(), false, 2);
	//		formSelectedSection = findSelectedTreeNode(sections); 
	//		formSelectedDeviceType = null;
	//		formInstanceIndex = null;
	//		formAdditionalInfo=null;
	//		formDeviceName=null;
	//		final List<NamePartRevision> approvedDeviceTypeRevisions = namePartService.currentApprovedNamePartRevisions(NamePartType.DEVICE_TYPE, false);
	//		deviceTypes = namePartTreeBuilder.newNamePartTree(approvedDeviceTypeRevisions, Lists.<NamePartRevision>newArrayList(), false, 2);
	//		RequestContext.getCurrentInstance().reset("addDeviceNameForm:growl");
	//		treeNodeManager.expandCustomized(sections);
	//		treeNodeManager.expandCustomized(deviceTypes);
	//	}

	//	public void prepareModifyPopup() {
	//		final List<NamePartRevision> approvedSectionRevisions = namePartService.currentApprovedNamePartRevisions(NamePartType.SECTION, false);
	//		final List<NamePartRevision> approvedDeviceTypeRevisions = namePartService.currentApprovedNamePartRevisions(NamePartType.DEVICE_TYPE, false);
	//		sections = namePartTreeBuilder.newNamePartTree(approvedSectionRevisions, Lists.<NamePartRevision>newArrayList(), false, 2, As.notNull(getSelectedDevice()).getSection().getNamePart());
	//		deviceTypes = namePartTreeBuilder.newNamePartTree(approvedDeviceTypeRevisions, Lists.<NamePartRevision>newArrayList(), false, 2, As.notNull(getSelectedDevice()).getDeviceType().getNamePart());
	//		treeNodeManager.expandCustomized(sections);
	//		treeNodeManager.expandCustomized(deviceTypes);
	//		formSelectedSection = findSelectedTreeNode(sections);
	//		formSelectedDeviceType = findSelectedTreeNode(deviceTypes);
	//		formInstanceIndex = As.notNull(getSelectedDevice()).getInstanceIndex();
	//		formAdditionalInfo=As.notNull(getSelectedDevice()).getAdditionalInfo();
	//		formDeviceName=As.notNull(getSelectedDevice()).getConventionName();
	//		RequestContext.getCurrentInstance().reset("modDeviceNameForm:growl");
	//	}

	public void prepareImportPopup() {
		importData = null;
		importFileName = null;
	}

	public void handleFileUpload(FileUploadEvent event) {
		try (InputStream inputStream = event.getFile().getInputstream()) {
			this.importData = ByteStreams.toByteArray(inputStream);
			this.importFileName = FilenameUtils.getName(event.getFile().getFileName());
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
		return req != null && req.getDevice().isDeleted() ? "Deleted" : "Approved";
	}

	public String sectionPath(DeviceView deviceView) {
		return Joiner.on(" ▸ ").join(deviceView.getSection().getNamePath());
	}

	public String deviceTypePath(DeviceView deviceView) {
		return Joiner.on(" ▸ ").join(deviceView.getDeviceType().getNamePath());
	}

	//	private TreeNode findSelectedTreeNode(TreeNode node) {
	//		if (node.isSelected()) {
	//			return node;
	//		} else {
	//			for (TreeNode child : node.getChildren()) {
	//				final TreeNode selectedChildNode = findSelectedTreeNode(child);
	//				if (selectedChildNode != null) {
	//					return selectedChildNode;
	//				}
	//			}
	//			return null;
	//		}
	//	}

	private void showMessage(@Nullable String notificationChannel, FacesMessage.Severity severity, String summary, String message) {
		FacesContext context = FacesContext.getCurrentInstance();
		context.addMessage(notificationChannel, new FacesMessage(severity, summary, message));
	}

	private @Nullable TreeNode deleteView(TreeNode node) {
		return (new OperationTreeGenerator<Object>() {
			@Override protected boolean canAffect(Object element) { return element instanceof DeviceView && !(((DeviceView) element).getDevice().isDeleted()); }
			@Override protected boolean autoSelectChildren(Object element) { return true; }
			@Override protected boolean ignoreSelectedChildren(Object element, boolean isSelected) { return false; }
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

		treeNodeManager.expandCustomized(filteredView);
		return filteredView != null ? filteredView : new DefaultTreeNode(null, null); 
	}

	enum DevicesViewFilter {
		ACTIVE, ARCHIVED
	}

}
