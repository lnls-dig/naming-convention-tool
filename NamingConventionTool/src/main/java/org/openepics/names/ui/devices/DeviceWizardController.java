package org.openepics.names.ui.devices;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
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
import org.openepics.names.ui.devices.DeviceTableController.DevicesViewFilter;
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
	
	private String action;
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
	private Operation activeOperation;
	private final List<String> tabs=Lists.newArrayList("firstTab","areaTab","deviceTab","instanceTab", "finishTab", "filterTab");
	
		
	/** 
	 * Activates and prepares the device wizard according to the specified action 
	 * @param action string indicating the operation to be performed (add, modify or filter) 
	 */
	public void activateWizard(String action){
		setAction(action);
		sections = getTreeNode(NamePartType.SECTION);
		deviceTypes =  getTreeNode(NamePartType.DEVICE_TYPE);		
		formSelectedSubsection = findSelectedTreeNode(sections);
		formSelectedDeviceType = findSelectedTreeNode(deviceTypes);
		formInstanceIndex = selectedRecord!=null? selectedRecord.getInstanceIndex():null;
		formDescription = selectedRecord!=null? selectedRecord.getDescription():null;
		formDeviceName = selectedRecord!=null? selectedRecord.getConventionName():null;
		resetForm();
	}
		
	public void updateViewFilter(){
		sections = getTreeNode(NamePartType.SECTION);
		deviceTypes =  getTreeNode(NamePartType.DEVICE_TYPE);
		resetForm();
	}
	
	private void resetForm() {
		RequestContext.getCurrentInstance().reset(action.concat("DeviceNameForm"));
	}

	/**
	 * Inactivates the device wizard
	 */
	public void inactivateWizard(){
		sections = null;
		deviceTypes =  null;		
		formSelectedSubsection = null;
		formSelectedDeviceType = null;
		formInstanceIndex = null;
		formDescription = null;
		formDeviceName = null;
		resetForm();
		setAction(null);
	}

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
		switch (activeOperation) {
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
		if(tab<0 || tab>tabs.size()-1 || activeOperation==null){
			return false;
		}
		switch(activeOperation){
		case ADD: return tab!=0 && tab!=5 ;
		case MODIFY: return tab!=0 && tab!=5;
		case FILTER: return tab!=3 && tab!=4;
		default: return true;
		}
	}

	public String onFlowProcess(FlowEvent event){ 
		final String oldStep= event.getOldStep();
		final String newStep=event.getNewStep();
		final int next= tabs.indexOf(newStep);
		final int prev= tabs.indexOf(oldStep);
			
		if(isTabRendered(prev) && next>prev && (activeOperation.equals(Operation.ADD)|| activeOperation.equals(Operation.MODIFY))){
			if(prev==1){
				if(formSelectedSubsection==null){
					showMessage(null, FacesMessage.SEVERITY_ERROR, "Validation Error:"," Please select from list");
					return oldStep;
				} 
			} else if(prev==2){
				if(formSelectedDeviceType==null) {
					showMessage(null, FacesMessage.SEVERITY_ERROR, "Validation Error:"," Please select from list");
					return oldStep;
				}
			}			
		}
		
		return isTabRendered(next)? newStep : tabs.get(next+1);
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

	public String mnemonicStyle(NamePartView namePartView){
		if(namePartView.isDeleted()){
			return "Deleted";
		}else {
			NamePartType namePartType= namePartView.getNamePart().getNamePartType();
			return namePartType.equals(NamePartType.SECTION) ? "sec":"dev";
		}
	}
	
	public String nameStyle(NamePartView namePartView){
		if(namePartView.isDeleted()){
			return "Deleted";
		}else {
			return "Approved";
		}
	}

	
	public String formAreaName(){

		final List<String> sectionPath = ((NamePartView) formSelectedSubsection.getData()).getMnemonicPath();
		String formAreaName=namingConvention.areaName(sectionPath);
		return formAreaName != null ? formAreaName: "";
	}

	public void onSubmit(){
		try {
			switch (activeOperation) {
			case ADD:
				final DeviceRevision addrev = namePartService.addDevice(selectedSubsection(), selectedDeviceType(), getFormInstanceIndex(), getFormAdditionalInfo());
				showMessage(null, FacesMessage.SEVERITY_INFO, "Success", "Device name "+addrev.getConventionName()+ " has been added.");
				break;
			case MODIFY:
				final DeviceRevision modrev = namePartService.modifyDevice(As.notNull(selectedRecord).getDevice(), selectedSubsection(), selectedDeviceType(), getFormInstanceIndex(), getFormAdditionalInfo());
				showMessage(null, FacesMessage.SEVERITY_INFO, "Success", "Device name has been modified as " +modrev.getConventionName());
				break;
			case FILTER:
				treeNodeManager.filterSelected(sections);
				treeNodeManager.filterSelected(deviceTypes);
//				showMessage(null, FacesMessage.SEVERITY_INFO, "Success", "Filter set");
			default:
				break;
			}
		} finally {
			deviceTableController().update();
			inactivateWizard();
		}
	}

	public void onCancel(){
		inactivateWizard();
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

	
	/**
	 * 
	 * @param structure NamePartType 
	 * @return treeNode root
	 */
	private TreeNode getTreeNode(NamePartType structure){
		final List<NamePartRevision> approvedRevisions = namePartService.currentApprovedNamePartRevisions(structure, true);
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
		switch (activeOperation) {
		case FILTER:
			node=treeNodeManager.filteredNode(namePartTreeBuilder.newNamePartTree(approvedRevisions,0, null),true);		
			treeNodeManager.selectFiltered(node);
			break;
		default:
			node=treeNodeManager.filteredNode(namePartTreeBuilder.newNamePartTree(approvedRevisions,2, selectedNamePart),false,false);	
			break;
		}
		treeNodeManager.expandCustomized(node);
		return node;
	}
	
	
	private TreeNode findSelectedTreeNode(TreeNode node) {
		if(node==null) return null;
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

		
	/**
	 * @return the action
	 */
	public String getAction() {
		return action;
	}


	/**
	 * @param action the action to set
	 */
	private void setAction(String action) {
		this.action = action;
		activeOperation=action!=null? Operation.valueOf(action.toUpperCase()):null;
	}

	public enum Operation{
		FILTER, ADD, MODIFY
	}

}
