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
import org.openepics.names.ui.common.SelectRecordManager;
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
	
	@Inject private RestrictedNamePartService namePartService;
	@Inject private TreeNodeManager treeNodeManager;
	@Inject private NamingConvention namingConvention;
	@Inject private SelectRecordManager selectRecordManager;
	
	private String action;
	private String formInstanceIndex;
	private String formDescription;
	private String formDeviceName;
	private TreeNode formSelectedSubsection;
	private TreeNode formSelectedDeviceType;
	private TreeNode[] formSelectedSections;
	private TreeNode[] formSelectedDeviceTypes;
	private TreeNode areaStructure;
	private TreeNode deviceStructure;
	private @Nullable DeviceRecordView selectedRecord;
	private Operation activeOperation;
	private DeviceTableController deviceTableController;
	private final List<String> tabs=Lists.newArrayList("areaTab","deviceTab","instanceTab", "finishTab", "filterTab");

	
		
	/** 
	 * Activates and prepares the device wizard according to the specified action 
	 * @param action string indicating the operation to be performed (add, modify or filter) 
	 */
	public void activateWizard(String action){
		setAction(action);
		deviceTableController=As.notNull(deviceTableController());
		
		switch(activeOperation){
		case ADD:
			areaStructure=deviceTableController.getFilteredAreaStructure();
			deviceStructure=deviceTableController.getFilteredDeviceStructure();
			selectedRecord=null;
//			selectRecordManager.setSelectedRecords(null);
			formSelectedSubsection = prepareTreeNode(areaStructure, null);
			formSelectedDeviceType = prepareTreeNode(deviceStructure, null);
			formInstanceIndex = null;
			formDescription = null;
			formDeviceName = null;	
		break;
		case MODIFY: 
			areaStructure=deviceTableController.getFilteredAreaStructure();
			deviceStructure=deviceTableController.getFilteredDeviceStructure();
			selectedRecord=As.notNull(deviceTableController.getSelectedRecord());
			formSelectedSubsection = As.notNull(prepareTreeNode(areaStructure,selectedRecord.getSubsection()));
			formSelectedDeviceType = As.notNull(prepareTreeNode(deviceStructure,selectedRecord.getDeviceType()));
			formInstanceIndex = selectedRecord.getInstanceIndex();
			formDescription = selectedRecord.getDescription();
			formDeviceName =  selectedRecord.getConventionName();
		break;
		default: 
			areaStructure=deviceTableController.getAreaStructure();
			deviceStructure=deviceTableController.getDeviceStructure();
			selectedRecord=null;
			formSelectedSubsection = prepareTreeNode(areaStructure, null);
			formSelectedDeviceType = prepareTreeNode(deviceStructure, null);
			formInstanceIndex = null;
			formDescription = null;
			formDeviceName = null;				
		} 
//		resetForm();
	}
		
	
	private void resetForm() {
		RequestContext.getCurrentInstance().reset(action.concat("DeviceNameForm"));
	}

	/**
	 * Inactivates the device wizard
	 */
	public void inactivateWizard(){
		formSelectedSubsection = null;
		formSelectedDeviceType = null;
		formInstanceIndex = null;
		formDescription = null;
		formDeviceName = null;
//		resetForm();
//		setAction(null);
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
		final int prev= tabs.indexOf(oldStep);
			
		if(isTabRendered(prev) && next>prev && (activeOperation.equals(Operation.ADD)|| activeOperation.equals(Operation.MODIFY))){
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
//				deviceTableController().update();

				break;
			case MODIFY:
				final DeviceRevision modrev = namePartService.modifyDevice(As.notNull(selectedRecord).getDevice(), selectedSubsection(), selectedDeviceType(), getFormInstanceIndex(), getFormAdditionalInfo());
				showMessage(null, FacesMessage.SEVERITY_INFO, "Success", "Device name has been modified as " +modrev.getConventionName());
//				deviceTableController().update();
				break;
			case FILTER:
				treeNodeManager.filterSelected(areaStructure);
				treeNodeManager.filterSelected(deviceStructure);
//				deviceTableController().updateFilter();
//				showMessage(null, FacesMessage.SEVERITY_INFO, "Success", "Filter set");
			default:
				break;
			}
		} finally {
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
		return areaStructure;
	}

//	/**
//	 * @param sections the sections to set
//	 */
//	public void setSections(TreeNode sections) {
//		this.areaStructure = sections;
//	}

	/**
	 * @return the deviceTypes
	 */
	public TreeNode getDeviceTypes() {
		return deviceStructure;
	}

//	/**
//	 * @param deviceTypes the deviceTypes to set
//	 */
//	public void setDeviceTypes(TreeNode deviceTypes) {
//		this.deviceStructure = deviceTypes;
//	}

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
	 * @param node treeNode to prepare
	 * @param view NamePartView to become selected if not null
	 * @return single selected treeNode
	 */
	private @Nullable TreeNode prepareTreeNode(TreeNode node, NamePartView view){
		unSelectAll(node);
		switch (activeOperation) {
		case FILTER:
			treeNodeManager.setSelectableLevel(node,0,true);
			treeNodeManager.selectFiltered(node);
			return null;
//			break;
		default:
			treeNodeManager.setSelectableLevel(node,3,false);
			List<TreeNode> treeNodes=nodesOf(node,view);
			TreeNode treeNode= treeNodes!=null&& treeNodes.size()==1 ?treeNodes.get(0):null; 
				if(treeNode!=null){
					treeNode.setSelected(true);
					expandParents(treeNode);
				}
			return treeNode;
		}
	}
	private void unSelectAll(TreeNode node){
		if(node!=null){
			node.setSelected(false);
			for(TreeNode child: node.getChildren()){
				unSelectAll(child);
			}
		}
	}
	
	private void expandParents(TreeNode node){
		if(node!=null && node.getParent()!=null){
			node.getParent().setExpanded(true);
			expandParents(node.getParent());
		}
	}
	
	private List<TreeNode> nodesOf(TreeNode node, NamePartView view){
		List<TreeNode> treeNodes=Lists.newArrayList();
		NamePartView namePartView= node!=null && node.getData() instanceof NamePartView? (NamePartView) node.getData():null;
		if(namePartView!=null && namePartView.equals(view)){
			treeNodes.add(node);
		}
		for (TreeNode child : node.getChildren()) {
			treeNodes.addAll(nodesOf(child,view));
		}
		return treeNodes;
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
		FILTER, ADD, MODIFY, INACTIVATE
	}

}
