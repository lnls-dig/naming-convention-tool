package org.openepics.names.ui.devices;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartType;
import org.openepics.names.services.NamingConvention;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.services.views.DeviceRecordView;
import org.openepics.names.services.views.NamePartView;
import org.openepics.names.ui.common.TreeNodeManager;
import org.openepics.names.ui.parts.NamePartTreeBuilder;
import org.openepics.names.util.As;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;
import org.primefaces.model.TreeNode;
import com.google.common.collect.Lists;
/**
 * A UI controller bean for the Device wizard.
 * @author karinrathsman
 */
@ManagedBean
@ViewScoped
public class DeviceWizardController implements Serializable{
	private static final long serialVersionUID = 5971357727446485557L;
	
	@Inject private NamePartTreeBuilder namePartTreeBuilder;
	@Inject private RestrictedNamePartService namePartService;
	@Inject private TreeNodeManager treeNodeManager;
	@Inject private NamingConvention namingConvention;
	
	private TreeNode sections;
	private TreeNode deviceTypes;
	private String formInstanceIndex;
	private String formDescription;
	private String formDeviceName;
	private TreeNode formSelectedSubsection;
	private TreeNode formSelectedDeviceType;
	private TreeNode[] formSelectedSections;
	private TreeNode[] formSelectedDeviceTypes;
	final private @Nullable DeviceRecordView selectedRecord=deviceTableController().getSelectedRecord();
	private Operation operation;
	private final List<String> tabs=Lists.newArrayList("areaTab","deviceTab","instanceTab", "finishTab", "filterTab");
	
	/** 
	 * 
	 */
	public void init(){
		sections = getTreeNode(NamePartType.SECTION);
		deviceTypes =  getTreeNode(NamePartType.DEVICE_TYPE);		
		formSelectedSubsection = findSelectedTreeNode(sections);
		formSelectedDeviceType = findSelectedTreeNode(deviceTypes);
		formInstanceIndex = selectedRecord!=null? selectedRecord.getInstanceIndex():null;
		formDescription = selectedRecord!=null? selectedRecord.getDescription():null;
		formDeviceName = selectedRecord!=null? selectedRecord.getConventionName():null;		
	}
	
//	/**
//	 * @return the selectedRecord
//	 */
//	public DeviceRecordView getSelectedRecord() {
//		return selectedRecord;
//	}

	
	
//	/**
//	 * @param selectedRecord the selectedRecord to set
//	 */
//	public void setSelectedRecord(DeviceRecordView selectedRecord) {
//		this.selectedRecord = selectedRecord;
//	}

	/**
	 * @return the formSelectedSections
	 */
	public TreeNode[] getFormSelectedSections() {
		return formSelectedSections;
	}

	/**
	 * @param formSelectedSections the formSelectedSections to set
	 */
	public void setFormSelectedSections(TreeNode[] formSelectedSections) {
		this.formSelectedSections = formSelectedSections;
	}
	
	/**
	 * Prepares the add pop-up wizard
	 */
	public synchronized void prepareAddPopup() {
		operation=Operation.ADD;
		init();
		RequestContext.getCurrentInstance().reset("addDeviceNameForm");
	}

	/**
	 * Prepares the modify pop-up wizard
	 */
	public synchronized void prepareModifyPopup() {
		operation=Operation.MODIFY;	
		init();
		RequestContext.getCurrentInstance().reset("modifyDeviceNameForm");
	}
	
	/**
	 * Prepares the filter pop-up wizard
	 */
	public synchronized void prepareFilterPopup() {
		operation=Operation.FILTER;
		init();
		RequestContext.getCurrentInstance().reset("filterDeviceNameForm");
	}
		
	private static NamePartView namePartView(TreeNode treeNode){
		return treeNode!=null? (NamePartView) treeNode.getData(): null;
	}
	
	/**
	 * 
	 * @return the selected subsection as Name Part
	 */
	private NamePart selectedSubsection(){
		return formSelectedSubsection!=null? namePartView(formSelectedSubsection).getNamePart():null;
	}
	/**
	 * 
	 * @return the selected device type as Name Part
	 */
	private NamePart selectedDeviceType(){
		return formSelectedDeviceType!=null? namePartView(formSelectedDeviceType).getNamePart():null;
	}

	/**
	 * 
	 * @return the selected area path as a list of string, starting from the root. 
	 */
	private List<String> selectedAreaPath(){
		return formSelectedSubsection!=null? namePartView(formSelectedSubsection).getMnemonicPath():null;
	}
	
	/**
	 * 
	 * @return the selected Device Type as a list of string, starting from the root
	 */
	private List<String> selectedDeviceTypePath(){
		return formSelectedDeviceType!=null? namePartView(formSelectedDeviceType).getMnemonicPath():null;
	}

	/**
	 * 
	 * @return true if subsection is selected
	 */
	public boolean isAreaSelected(){
		return formSelectedSubsection!=null;
	}
	
	/** 
	 * 
	 * @return true if device type is selected
	 */
	public boolean isDeviceTypeSelected(){
		return formSelectedDeviceType!=null;
	}
	
	/** 
	 * 
	 * @param instanceIndex device instance Index
 	 * @return true if the instance index is valid according to rules. 
	 */
	public boolean isInstanceIndexValid(String instanceIndex) {
		return namePartService.isInstanceIndexValid(selectedSubsection(), selectedDeviceType(), instanceIndex);
	}
	
	/** 
	 * 
	 * @param instanceIndex device instance index
	 * @return true if the instance index is unique according to naming convention rules
	 */
	public boolean isInstanceIndexUnique(@Nullable String instanceIndex) {
		switch (operation) {
		case MODIFY:
			final NamePart section = selectedSubsection();
			final NamePart deviceType = selectedDeviceType();
			boolean isSame = section.equals(selectedRecord.getSubsection().getNamePart()) && 
					deviceType.equals(selectedRecord.getDeviceType().getNamePart()) && 
					Objects.equals(instanceIndex, selectedRecord.getInstanceIndex());
			if (!(isSame)) {
				return namePartService.isDeviceConventionNameUniqueExceptForItself(selectedRecord.getDevice(),section, deviceType, instanceIndex);
			} else {
				return true;
			}						
//			break;
		default:	
			return namePartService.isDeviceConventionNameUnique(selectedSubsection(), selectedDeviceType(), instanceIndex);			
//			break;		
		}
	}
	
	
	
	public boolean isTabRendered(int tab){
		if(tab<0 || tab>tabs.size()-1 || operation==null){
			return false;
		}
		switch(operation){
		case ADD: return tab!=4 ;
		case MODIFY: return tab!=4;
		case FILTER: return tab!=2 && tab!=3;
		default: return true;
		}
	}

	public String onFlowProcess(FlowEvent event){ 
		final String oldStep= event.getOldStep();
		final String newStep=event.getNewStep();
		final int next= tabs.indexOf(newStep);
		final int prev=tabs.indexOf(oldStep);
			
		if(isTabRendered(prev) && next>prev && (operation.equals(Operation.ADD)|| operation.equals(Operation.MODIFY))){
			if(prev==0){
				if(formSelectedSubsection==null){
					showMessage(null, FacesMessage.SEVERITY_ERROR, "Validation Error:"," Please select from list");
					return oldStep;
				} 
			} else if(prev==1){
				if(formSelectedDeviceType==null) {
					showMessage(null, FacesMessage.SEVERITY_ERROR, "Validation Error:"," Please select from list");
					return oldStep;
				}
			}			
		}
		return newStep;
	}	

		
	public void setFormDeviceName(){
		final List<String> sectionPath = selectedAreaPath();
		final List<String> devicePath = selectedDeviceTypePath();
		String formDeviceName=namingConvention.conventionName(sectionPath, devicePath, getFormInstanceIndex());
		this.formDeviceName= formDeviceName != null ? formDeviceName: "";
	}

	public String mnemonic(NamePartView namePartView){
		NamePartType namePartType= namePartView.getNamePart().getNamePartType();
		String mnemonic=null;
		final List<String> path = namePartView.getMnemonicPath();
		if(path.size()>=3  && namePartType.equals(NamePartType.SECTION)){
			mnemonic= namingConvention.areaName(path);
		} else if(path.size()>=3 && namePartType.equals(NamePartType.DEVICE_TYPE)){
			mnemonic = namingConvention.deviceDefinition(path);
		} 
		return mnemonic != null ? mnemonic: "";
	}

	public String formAreaName(){

		final List<String> sectionPath = ((NamePartView) formSelectedSubsection.getData()).getMnemonicPath();
		String formAreaName=namingConvention.areaName(sectionPath);
		return formAreaName != null ? formAreaName: "";
	}

	public void onAdd() {
		try {
			final DeviceRevision rev = namePartService.addDevice(selectedSubsection(), selectedDeviceType(), getFormInstanceIndex(), getFormAdditionalInfo());
			showMessage(null, FacesMessage.SEVERITY_INFO, "Success", "Device name "+rev.getConventionName()+ " has been added.");
		} finally {
//			init();
//			RequestContext.getCurrentInstance().reset("addDeviceNameForm");
		}
	}

	private void onModify() {
		try {
			final DeviceRevision rev = namePartService.modifyDevice(As.notNull(selectedRecord).getDevice(), selectedSubsection(), selectedDeviceType(), getFormInstanceIndex(), getFormAdditionalInfo());
			showMessage(null, FacesMessage.SEVERITY_INFO, "Success", "Device name has been modified as " +rev.getConventionName());
		} finally {
//			init();
//			RequestContext.getCurrentInstance().reset("modifyDeviceNameForm");
		}
	}

	public void onSubmit(){
		switch (operation) {
		case ADD:
			onAdd();
			break;
		case MODIFY:
			onModify();
			break;
		case FILTER:
			onFilter();
		default:
			break;
		}
		deviceTableController().update();
	}


	private void onFilter() {
		try {
			treeNodeManager.filterSelected(sections);
			treeNodeManager.filterSelected(deviceTypes);
			showMessage(null, FacesMessage.SEVERITY_INFO, "Success", "Filter set");
		} finally {
//			init();
//			RequestContext.getCurrentInstance().reset("modifyDeviceNameForm");
		}
		
	}

	/**
	 * 
	 * @return current bean controlling the device record table.
	 */
	private final DeviceTableController deviceTableController(){
		FacesContext facesContext=FacesContext.getCurrentInstance();
        return (DeviceTableController) facesContext.getApplication().getExpressionFactory().createValueExpression(facesContext.getELContext(), "#{deviceTableController}", Object.class).getValue(facesContext.getELContext()); 
	}
	
	/**
	 * @return the sections
	 */
	public TreeNode getSections() {
		return sections;
	}

	/**
	 * @param sections the sections to set
	 */
	public void setSections(TreeNode sections) {
		this.sections = sections;
	}

	/**
	 * @return the deviceTypes
	 */
	public TreeNode getDeviceTypes() {
		return deviceTypes;
	}

	/**
	 * @param deviceTypes the deviceTypes to set
	 */
	public void setDeviceTypes(TreeNode deviceTypes) {
		this.deviceTypes = deviceTypes;
	}

	/**
	 * @return the formInstanceIndex
	 */
	public String getFormInstanceIndex() {
		return formInstanceIndex;
	}

	/**
	 * @param formInstanceIndex the formInstanceIndex to set
	 */
	public void setFormInstanceIndex(String formInstanceIndex) { 
		this.formInstanceIndex = !formInstanceIndex.isEmpty() ? formInstanceIndex : null; 
		setFormDeviceName();
	}
	
	/**
	 * @return the formAdditionalInfo
	 */
	public String getFormAdditionalInfo() {
		return formDescription;
	}
	
	/**
	 * @param formAdditionalInfo the formAdditionalInfo to set
	 */
	public void setFormAdditionalInfo(String formAdditionalInfo){ 
		this.formDescription = !formAdditionalInfo.isEmpty() ? formAdditionalInfo : null; 
	}

	/**
	 * @return the formDeviceName
	 */
	public String getFormDeviceName() {
		return formDeviceName;
	}

	/**
	 * @param formDeviceName the formDeviceName to set
	 */
	public void setFormDeviceName(String formDeviceName) {
		this.formDeviceName = formDeviceName;
	}

	/**
	 * @return the formSelectedSection
	 */
	public TreeNode getFormSelectedSubsection() {
		return formSelectedSubsection;
	}

	/**
	 * @param formSelectedSubsection the formSelectedSubsection to set
	 */
	public void setFormSelectedSubsection(TreeNode formSelectedSubsection) {
		this.formSelectedSubsection = formSelectedSubsection;
	}

	/**
	 * @return the formSelectedDeviceType
	 */
	public TreeNode getFormSelectedDeviceType() {
		return formSelectedDeviceType;
	}

	/**
	 * @param formSelectedDeviceType the formSelectedDeviceType to set
	 */
	public void setFormSelectedDeviceType(TreeNode formSelectedDeviceType) {
		this.formSelectedDeviceType = formSelectedDeviceType;
	}

//	/**
//	 * @return the deviceView
//	 */
//	public DeviceView getDeviceView() {
//		return deviceView;
//	}
//
//	/**
//	 * @param deviceView the deviceView to set
//	 */
//	public void setDeviceView(@Nullable DeviceView deviceView) {
//		this.deviceView = deviceView!=null? deviceView: null;
//	}


	
//	/**
//	 * @return the subsection
//	 */
//	public NamePart getSubsection() {
//		return subsection;
//	}
//
//	/**
//	 * @param subsection the subsection to set
//	 */
//	public void setSubsection(@Nullable NamePart subsection) {
//		this.subsection = subsection!=null? subsection: null;
//	}
//
//	/**
//	 * @return the deviceType
//	 */
//	public NamePart getDeviceType() {
//		return deviceType;
//	}
//
//	/**
//	 * @param deviceType the deviceType to set
//	 */
//	public void setDeviceType(@Nullable NamePart deviceType) {
//		this.deviceType = deviceType;
//	}
	
	/**
	 * 
	 * @param structure NamePartType 
	 * @return 
	 */
	private TreeNode getTreeNode(NamePartType structure){
		final List<NamePartRevision> approvedRevisions = namePartService.currentApprovedNamePartRevisions(structure, false);
		NamePart selectedNamePart=null;
		switch(structure){
		case SECTION:
			selectedNamePart=selectedRecord!=null ? selectedRecord.getSubsection().getNamePart():null;
			break;
		case DEVICE_TYPE:
			selectedNamePart=selectedRecord!=null ? selectedRecord.getDeviceType().getNamePart():null;
			break;
		}
		
		TreeNode node;
		switch (operation) {
		case ADD:
			node=namePartTreeBuilder.newNamePartTree(approvedRevisions,2, selectedNamePart);			
			break;
		case MODIFY:
			node=namePartTreeBuilder.newNamePartTree(approvedRevisions,2, selectedNamePart);		
			break;
		case FILTER:
			node=namePartTreeBuilder.newNamePartTree(approvedRevisions,0, null);		
			treeNodeManager.selectFiltered(node);
			break;
		default:
			node=null;
			break;
		}
		treeNodeManager.expandCustomized(node);
		return node;
	}
	
	
	private TreeNode findSelectedTreeNode(TreeNode node) {
		if (node.isSelected()) {
			treeNodeManager.expandParents(node);
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
	/**
	 * @return the formSelectedDeviceTypes
	 */
	public TreeNode[] getFormSelectedDeviceTypes() {
		return formSelectedDeviceTypes;
	}

	/**
	 * @param formSelectedDeviceTypes the formSelectedDeviceTypes to set
	 */
	public void setFormSelectedDeviceTypes(TreeNode[] formSelectedDeviceTypes) {
		this.formSelectedDeviceTypes = formSelectedDeviceTypes;
	}
	public enum Operation{
		FILTER, ADD, MODIFY
	}

}
