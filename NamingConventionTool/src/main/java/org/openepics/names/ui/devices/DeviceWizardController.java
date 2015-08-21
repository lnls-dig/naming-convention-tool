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
import org.openepics.names.services.views.DeviceView;
import org.openepics.names.services.views.NamePartView;
import org.openepics.names.ui.common.TreeNodeManager;
import org.openepics.names.ui.parts.NamePartTreeBuilder;
import org.openepics.names.util.As;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.TreeNode;

import com.google.common.collect.Lists;

@ManagedBean
@ViewScoped
public class DeviceWizardController implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5971357727446485557L;

	@Inject private NamePartTreeBuilder namePartTreeBuilder;
	@Inject private RestrictedNamePartService namePartService;
	@Inject private TreeNodeManager treeNodeManager;
	@Inject private NamingConvention namingConvention;
	@Inject private DeviceTableController selectRecordManager;

	private TreeNode sections;
	private TreeNode deviceTypes;
	private String formInstanceIndex;
	private String formAdditionalInfo;
	private String formDeviceName;
	private TreeNode formSelectedSection;
	private TreeNode formSelectedDeviceType;
	private @Nullable DeviceView deviceView;
	private @Nullable NamePart subsection;
	private @Nullable NamePart deviceType;


	@PostConstruct
	public void init(){
//		preparePopup();
	}
	
	public void modifyDisplayView() {
		sections = deviceTypes =  formSelectedDeviceType = null;
	}

	public synchronized void preparePopup() {
		@Nullable DeviceRecordView selectedRecord=selectRecordManager.getSelectedRecord();
		setDeviceView(selectedRecord!=null ? selectedRecord.getDeviceView():null);
		setSubsection(selectedRecord!=null ? selectedRecord.getSubsection().getNamePart():null);
		setDeviceType(selectedRecord!=null ? selectedRecord.getDeviceType().getNamePart():null);		
		sections = getTreeNode(getSubsection(),NamePartType.SECTION);
		deviceTypes =  getTreeNode(getDeviceType(),NamePartType.DEVICE_TYPE);		
		formSelectedSection = findSelectedTreeNode(sections);
		formSelectedDeviceType = findSelectedTreeNode(deviceTypes);
		formInstanceIndex = getDeviceView()!=null? getDeviceView().getInstanceIndex():null;
		formAdditionalInfo = getDeviceView()!=null? getDeviceView().getAdditionalInfo():null;
		formDeviceName=getDeviceView()!=null? getDeviceView().getConventionName():null;
		RequestContext.getCurrentInstance().reset("modDeviceNameForm:growl");
	}
	
	public boolean isAddInstanceIndexValid(String instanceIndex) {
		final NamePart section=((NamePartView) formSelectedSection.getData()).getNamePart();
		final NamePart deviceType = ((NamePartView) formSelectedDeviceType.getData()).getNamePart();
		return namePartService.isInstanceIndexValid(section, deviceType, instanceIndex);
	}
	
	public boolean isModifyInstanceIndexValid(String instanceIndex) {
		final NamePart section = ((NamePartView) formSelectedSection.getData()).getNamePart();
		final NamePart deviceType = ((NamePartView) formSelectedDeviceType.getData()).getNamePart();
		return namePartService.isInstanceIndexValid(section, deviceType, instanceIndex);
	}

	public boolean isAddInstanceIndexUnique(String instanceIndex) {
		final NamePart section=((NamePartView) formSelectedSection.getData()).getNamePart();
		final NamePart deviceType = ((NamePartView) formSelectedDeviceType.getData()).getNamePart();
		return namePartService.isDeviceConventionNameUnique(section, deviceType, instanceIndex);
	}

	public boolean isModifyInstanceIndexUnique(String instanceIndex) {
		final DeviceView deviceView = As.notNull(getDeviceView());
		final NamePart section = ((NamePartView) formSelectedSection.getData()).getNamePart();
		final NamePart deviceType = ((NamePartView) formSelectedDeviceType.getData()).getNamePart();
		boolean isSame = section.equals(deviceView.getSection().getNamePart()) && 
				deviceType.equals(deviceView.getDeviceType().getNamePart()) && 
				Objects.equals(instanceIndex, deviceView.getInstanceIndex());
		if (!(isSame)) {
			return namePartService.isDeviceConventionNameUniqueExceptForItself(As.notNull(getDeviceView()).getDevice().getDevice(),section, deviceType, instanceIndex);
		} else {
			return true;
		}
	}

	public String onFlowProcess(FlowEvent event){    	
		return event.getNewStep();
	}

	public void setFormDeviceName(){
		final List<String> sectionPath = ((NamePartView) formSelectedSection.getData()).getMnemonicPath();
		final List<String> devicePath = ((NamePartView) formSelectedDeviceType.getData()).getMnemonicPath();
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
		final List<String> sectionPath = ((NamePartView) formSelectedSection.getData()).getMnemonicPath();
		String formAreaName=namingConvention.areaName(sectionPath);
		return formAreaName != null ? formAreaName: "";
	}

	public void onAdd() {
		try {
			final NamePart subsection = ((NamePartView) formSelectedSection.getData()).getNamePart();
			final NamePart deviceType = ((NamePartView) formSelectedDeviceType.getData()).getNamePart();
			final DeviceRevision rev = namePartService.addDevice(subsection, deviceType, getFormInstanceIndex(), getFormAdditionalInfo());
			showMessage(null, FacesMessage.SEVERITY_INFO, "Success", "Device name "+rev.getConventionName()+ " has been added.");
		} finally {
//			init();
		}
	}

	public void onModify() {
		try {
			final NamePart subsection = ((NamePartView) formSelectedSection.getData()).getNamePart();
			final NamePart deviceType = ((NamePartView) formSelectedDeviceType.getData()).getNamePart();
			namePartService.modifyDevice(As.notNull(getDeviceView()).getDevice().getDevice(), subsection, deviceType, getFormInstanceIndex(), getFormAdditionalInfo());
			showMessage(null, FacesMessage.SEVERITY_INFO, "Success", "Device name has been modified.");
		} finally {
//			init();
		}
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
		return formAdditionalInfo;
	}
	
	/**
	 * @param formAdditionalInfo the formAdditionalInfo to set
	 */
	public void setFormAdditionalInfo(String formAdditionalInfo){ 
		this.formAdditionalInfo = !formAdditionalInfo.isEmpty() ? formAdditionalInfo : null; 
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
	public TreeNode getFormSelectedSection() {
		return formSelectedSection;
	}

	/**
	 * @param formSelectedSection the formSelectedSection to set
	 */
	public void setFormSelectedSection(TreeNode formSelectedSection) {
		this.formSelectedSection = formSelectedSection;
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
	 * @return the deviceView
	 */
	public DeviceView getDeviceView() {
		return deviceView;
	}

	/**
	 * @param deviceView the deviceView to set
	 */
	public void setDeviceView(@Nullable DeviceView deviceView) {
		this.deviceView = deviceView!=null? deviceView: null;
	}


	
	/**
	 * @return the subsection
	 */
	public NamePart getSubsection() {
		return subsection;
	}

	/**
	 * @param subsection the subsection to set
	 */
	public void setSubsection(@Nullable NamePart subsection) {
		this.subsection = subsection!=null? subsection: null;
	}

	/**
	 * @return the deviceType
	 */
	public NamePart getDeviceType() {
		return deviceType;
	}

	/**
	 * @param deviceType the deviceType to set
	 */
	public void setDeviceType(@Nullable NamePart deviceType) {
		this.deviceType = deviceType;
	}
	
	private TreeNode getTreeNode(@Nullable NamePart selectedNamePart, NamePartType structure){
		final List<NamePartRevision> approvedRevisions = namePartService.currentApprovedNamePartRevisions(structure, false);
		TreeNode node;
		if(selectedNamePart==null){
			node=namePartTreeBuilder.newNamePartTree(approvedRevisions,Lists.<NamePartRevision>newArrayList(),false,2);
		} else {
			node=namePartTreeBuilder.newNamePartTree(approvedRevisions,Lists.<NamePartRevision>newArrayList(),false,2, selectedNamePart);			
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

	
}
