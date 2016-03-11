/*
 * This software is Copyright by the Board of Trustees of Michigan
 * State University (c) Copyright 2012.
 *
 * You may use this software under the terms of the GNU public license
 *  (GPL). The terms of this license are described at:
 *       http://www.gnu.org/licenses/gpl.txt
 *
 * Contact Information:
 *   Facility for Rare Isotope Beam
 *   Michigan State University
 *   East Lansing, MI 48824-1321
 *   http://frib.msu.edu
 *
 *
 * Copyright (c) 2014 European Spallation Source
 * Copyright (c) 2014 Cosylab d.d.
*/
package org.openepics.names.ui.parts;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.openepics.names.model.*;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.services.views.NamePartView;
import org.openepics.names.services.views.NamePartView.ModifyChange;
import org.openepics.names.ui.common.*;
import org.openepics.names.util.As;
import org.openepics.names.util.Marker;
import org.openepics.names.util.UnhandledCaseException;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import java.io.Serializable;
import java.util.List;

/**
 * A UI controller bean for the Logical Area Structure and Device Category Structure screens.
 *
 * @author Vasu V  
 * @author Karin Rathsman  
 */
@ManagedBean
@ViewScoped
public class NamePartsController implements Serializable {


	@Inject private RestrictedNamePartService namePartService;
	@Inject private ViewFactory viewFactory;
	@Inject private NamePartTreeBuilder namePartTreeBuilder;
	@Inject private UserManager userManager;
	@Inject private TreeNodeManager treeNodeManager;

	private NamePartType namePartType;
	private NamePartDisplayFilter displayView = NamePartDisplayFilter.APPROVED_AND_PROPOSED;

	private List<NamePartView> historyRevisions;

	private TreeNode rootWithModifications;
	private TreeNode rootWithoutModifications;
	private TreeNode viewRoot;
	private TreeNode[] selectedNodes;
	private String type;

	private TreeNode deleteView;
	private TreeNode approveView;
	private TreeNode cancelView;

	private String formName;
	private String formMnemonic;
	private String formDescription;
	private String formComment;

	private List<Device> affectedDevices;
	private Operation operation;
	private @Nullable TreeNode formNamePartNode;
	private NamePart formNamePart;
	private @Nullable TreeNode formParentNode;
	private NamePart formParent;
	private int formNamePartLevel;
	private final List<String> tabs=Lists.newArrayList("namePartTab","parentTab","descriptionTab", "finishTab");
		
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}


	@PostConstruct
	public void init(){
		type = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("type");
		if (type == null) {
			Marker.doNothing();
		} else if (type.equals("section")) {
			namePartType = NamePartType.SECTION;	
		} else if (type.equals("deviceType")) {
			namePartType = NamePartType.DEVICE_TYPE;	
		} else {
			throw new IllegalStateException();
		}
		update();
	}
	
	/**
	 * @return the formNamePartNode
	 */
	public TreeNode getFormNamePartNode() {
		return formNamePartNode;
	}

	/**
	 * @param formNamePartNode the formNamePartNode to set
	 */
	public void setFormNamePartNode(TreeNode formNamePartNode) {
		this.formNamePartNode = formNamePartNode;
	}

	/**
	 * @return the formParentNode
	 */
	public TreeNode getFormParentNode() {
		return formParentNode;
	}

	/**
	 * @param formParentNode the formParentNode to set
	 */
	public void setFormParentNode(TreeNode formParentNode) {
		this.formParentNode = formParentNode;
	}

	public void updateNamePartForm(){
		if(formNamePartNode!=null){
			final NamePartRevision namePartRevision = ((NamePartView) formNamePartNode.getData()).getCurrentOrElsePendingRevision();
			formName = namePartRevision.getName();
			formMnemonic = namePartRevision.getMnemonic();
			formDescription =  namePartRevision.getDescription();
			formNamePart=namePartRevision.getNamePart();
			formNamePartLevel=((NamePartView) formNamePartNode.getData()).getLevel();
			if(formParentNode!=null){
				formParentNode.setSelected(false);
				formParentNode=null;
			}
			formParentNode=formNamePartNode.getParent();
			updateParentForm();
		} else {
			formName =null;
			formMnemonic = null;
			formDescription = null;
			formNamePart=null;
		}
	}
	public void updateParentForm(){
		if(formParentNode!=null && formParentNode.getData()!=null && formParentNode.getData() instanceof NamePartView){
			final NamePartRevision namePartRevision = ((NamePartView) formParentNode.getData()).getCurrentOrElsePendingRevision();
			formParent=namePartRevision.getNamePart();
		} else {
			formParent=null;
		}
	}
	

	public @Nullable NamePart getFormParent(){
		return formParent;
	}

	public @Nullable NamePart getFormNamePart(){
		return formNamePart;
	}
	
	public void onSelectNamePart(NodeSelectEvent event){
		if(formNamePartNode!=null){
			formNamePartNode.setSelected(false);
		}
		formNamePartNode=event.getTreeNode();
		formNamePartNode.setSelected(true);
		updateNamePartForm();
	}
	
	public void onUnselectNamePart(NodeUnselectEvent event){
		formNamePartNode.setSelected(false);
		formNamePartNode=null;
		updateNamePartForm();
	}
	
	public void onSelectParent(NodeSelectEvent event){
		if(formParentNode!=null){
			formParentNode.setSelected(false);
		}
		formParentNode=event.getTreeNode();
		formParentNode.setSelected(true);
		
		updateParentForm();
	}
	
	public void onUnselectParent(NodeUnselectEvent event){
		formParentNode.setSelected(false);
		formParentNode=null;
		updateParentForm();
	}
	
	public void prepareAddPopup(){
		RequestContext.getCurrentInstance().reset("AddNameForm:addName");			
		operation=Operation.ADD;
		formComment=null;
		if(formNamePartNode!=null) {
			formNamePartNode.setSelected(false);
			formNamePartNode=null;
		}
		formParentNode=getSelectedNode();
		formNamePartLevel=formParentNode!=null? ((NamePartView) formParentNode.getData()).getLevel()+1 : 0;
		updateNamePartForm();
		if(formParentNode!=null){
			formParentNode.setSelected(true);
		}
		updateParentForm();
	}
	
	boolean isNamePartSelectableInForm(NamePartView namePart){
		return namePart.getLevel()==formNamePartLevel;
	}
	
	boolean isParentSelectableInForm(NamePartView parent){
		return parent.getLevel()==formNamePartLevel-1;
	}
	
	public void prepareModifyPopup() {
		RequestContext.getCurrentInstance().reset("ModNameForm:modName");

		this.operation=Operation.MODIFY;
		formComment=null;
		if(formParentNode!=null){
		formParentNode.setSelected(false);
		}
		formParentNode=null;
		updateParentForm();
		formNamePartNode=getSelectedNode();
		formNamePartNode.setSelected(true);
		formNamePartLevel=formNamePartNode!=null? ((NamePartView) formNamePartNode.getData()).getLevel():-1;
		updateNamePartForm();
	}

	public void prepareMovePopup() {
		RequestContext.getCurrentInstance().reset("MovNameForm:movName");
		this.operation=Operation.MOVE;
		formComment=null;
		if(formParentNode!=null){
		formParentNode.setSelected(false);
		}
		formParentNode=null;
		updateParentForm();
		formNamePartNode=getSelectedNode();
		formNamePartNode.setSelected(true);
		formNamePartLevel=formNamePartNode!=null? ((NamePartView) formNamePartNode.getData()).getLevel():-1;
		updateNamePartForm();

	}

	public boolean isTabRendered(int tab){
		if(tab<0 || tab>tabs.size()-1){
			return false;
		}
		switch(operation){
		case ADD: return tab!=0 ;
		case MODIFY: return tab!=1;
		case MOVE: return tab!=2;
		default: return true;
		}
	}

	public boolean isNamePartTabRendered(){
		return isTabRendered(0); 
	}
	public boolean isParentTabRendered(){
		return isTabRendered(1); 
	}
	public boolean isDescriptionTabRendered(){
		return isTabRendered(2); 
	}


	public String onFlowProcess(FlowEvent event){ 
		final String newStep=event.getNewStep();
		final int next=newStep!=null ? tabs.indexOf(newStep):0;
		final String oldStep= next!=0? event.getOldStep(): newStep;
		final int prev=oldStep!=null? tabs.indexOf(oldStep):0;

		final int nextRendered=nextRendered(prev,next);
		boolean forward=next>=prev;

		
		if(oldStep!=null && isTabRendered(prev)){
			if(prev==0){
				if(formNamePartNode==null){
					showMessage(null, FacesMessage.SEVERITY_ERROR, "Validation Error:"," Please select from list");
					return oldStep;
				} 
			} else if(prev==1 && operation.equals(Operation.MOVE)){

				int level= formParentNode!=null ?level=((NamePartView) formParentNode.getData()).getLevel()+1:0;
				if(level!=formNamePartLevel){
					showMessage(null, FacesMessage.SEVERITY_ERROR, "Validation Error: Not Selectable level: Please select a ".concat(getNamePartTypeName()),null);
					return oldStep;
				}

			} else if (prev==2 && forward) {
				// TODO validation by separate validator.

			}			
		}

		
		if(isTabRendered(nextRendered)){
			if (nextRendered==0){
				if( formNamePartNode!=null){
					formNamePartNode.setSelected(true);
				}
				if( formParentNode!=null){
					formParentNode.setSelected(false);
				}

			} else if(nextRendered==1) {
				if( formParentNode!=null){
					formParentNode.setSelected(true);
				}	
				if( formNamePartNode!=null){
					formNamePartNode.setSelected(false);
				}

			} else if(nextRendered==2) {
				//TODO
			}
		}
		return tabs.get(nextRendered);
	}	

	private int nextRendered(int prev, int next){
		boolean forward=next>=prev;
		if(isTabRendered(next)){ 
			return next;
		}else if(forward && next+1 < tabs.size()){
			return nextRendered(prev, next+1);
		}else if(!forward && next > 0) {
			return nextRendered(prev,next-1);
		}else {
			return nextRendered(0,0);
		}
	}
		



	public void update() {
		operation=Operation.VIEW;
		rootWithModifications = getRootTreeNode(true);
		rootWithoutModifications = getRootTreeNode(false);
		modifyDisplayView();
		treeNodeManager.expandCustomized(viewRoot);
		treeNodeManager.selectCustomized(viewRoot);
	}

	public synchronized void onExpandAll(){
		treeNodeManager.expandAll(viewRoot);
	}

	public synchronized void onCollapseAll(){
		treeNodeManager.collapseAll(viewRoot);
	}

	private TreeNode getRootTreeNode(boolean withModifications) {
		final List<NamePartRevision> approvedRevisions = namePartService.currentApprovedNamePartRevisions(namePartType, true);
		final List<NamePartRevision> pendingRevisions = withModifications ? namePartService.currentPendingNamePartRevisions(namePartType, true) : Lists.<NamePartRevision>newArrayList();
		return namePartTreeBuilder.newNamePartTree(approvedRevisions, pendingRevisions, true);
	}

	public boolean isAddMnemonicValid(String mnemonic) {
		final @Nullable NamePart parent = getSelectedName() != null ? getSelectedName().getNamePart() : null;
		return namePartService.isMnemonicValid(namePartType, parent, mnemonic);
	}
	
	public boolean isModifyMnemonicValid(String mnemonic) {
		final NamePartView namePart = As.notNull(getSelectedName());
		final @Nullable NamePart parent = namePart.getParent() != null ? namePart.getParent().getNamePart() : null;
		return namePartService.isMnemonicValid(namePartType, parent, mnemonic);
	}
		
	public boolean isMnemonicRequiredForChild(){
		final NamePart parent= getSelectedName() != null ? getSelectedName().getNamePart():null;
		return namePartService.isMnemonicRequiredForChild(namePartType,parent);
	}
	
	public boolean isMnemonicRequired(){
		if(operation.equals(Operation.ADD)){
			return namePartService.isMnemonicRequiredForChild(namePartType,formParent);
		} else {
			return namePartService.isMnemonicRequired(namePartType, formNamePart);
		}
//		final @Nullable NamePart  namePart= getSelectedName() !=null? getSelectedName().getNamePart():null;
//		return namePart!=null ? namePartService.isMnemonicRequired(namePartType,namePart): true;
	}
		
	public String getNamePartTypeName() {
//		final @Nullable NamePart namePart=getSelectedName() !=null? getSelectedName().getNamePart():null;
//		return namePart !=null ? namePartService.getNamePartTypeName(namePartType,namePart): ""; 
		if(operation.equals(Operation.ADD)){
			return namePartService.getNamePartTypeNameForChild(namePartType, formParent);
		} else {
			return namePartService.getNamePartTypeName(namePartType, formNamePart);
		}
	}

	public String getNamePartTypeNameForChild() {
		final @Nullable NamePart namePart=getSelectedName() !=null? getSelectedName().getNamePart():null;
		return namePartService.getNamePartTypeNameForChild(namePartType,namePart); 
	}

	public String getNamePartTypeMnemonic() {
		if(operation.equals(Operation.ADD)){
			return namePartService.getNamePartTypeMnemonicForChild(namePartType, formParent);
		} else {
			return namePartService.getNamePartTypeMnemonic(namePartType, formNamePart);
		}
		
//		final @Nullable NamePart namePart=getSelectedName() !=null? getSelectedName().getNamePart():null;
//		return namePart !=null ? namePartService.getNamePartTypeMnemonic(namePartType,namePart): ""; 
	}

	public String getNamePartTypeMnemonicForChild() {
		final @Nullable NamePart namePart=getSelectedName() !=null? getSelectedName().getNamePart():null;
		return namePartService.getNamePartTypeMnemonicForChild(namePartType,namePart); 
	}

	
	public boolean isMnemonicRendered(NamePartView req) {
        return req!=null? namePartService.isMnemonicRequired(namePartType, req.getNamePart()): false;
    }

	
	public boolean isMnemonicUnique(String mnemonic) {
		if(operation.equals(Operation.ADD)){
			return namePartService.isMnemonicUniqueOnAdd(namePartType, formParent, mnemonic);
		} else {
			return namePartService.isMnemonicUniqueOnModify(formNamePart, formParent, mnemonic);
		}
	}
	
	public boolean isMnemonicValid(String mnemonic){
		return namePartService.isMnemonicValid(namePartType, formParent, mnemonic);
	}
//		final @Nullable NamePart parent = getSelectedName() != null ? getSelectedName().getNamePart() : null;
//		return namePartService.isMnemonicUnique(namePartType, parent, mnemonic);
//	}

	
	public boolean isModifyMnemonicUnique(String mnemonic) {
		final NamePartView namePart = As.notNull(getSelectedName());
//		String currentMnemonic = namePart.getPendingOrElseCurrentRevision().getMnemonic();
		if (!mnemonic.equals(namePart.getPendingOrElseCurrentRevision().getMnemonic())) {
			final @Nullable NamePart parent = namePart.getParent() != null ? namePart.getParent().getNamePart() : null;
			return namePartService.isMnemonicUniqueOnModify(namePart.getNamePart(), parent, mnemonic);
		} else {
			return true;
		}
	}

	public String getOperation(){
		switch(operation){
		case ADD: return "add";
		case MODIFY: return "modify";
		case MOVE: return "move";
		default: return null;
		}
	}
	
	public void onSubmit(){
		switch(operation){
		case ADD:  
			onAdd();
			break;
		case MODIFY: 
			onModify();
			break;
		case MOVE: 
			onMove();
			break;
		default: Marker.doNothing();
		}
	}
	
	public void onAdd() {
		try {
			namePartService.addNamePart(formName, formMnemonic, formDescription, namePartType, formParent , formComment);
			showMessage(null, FacesMessage.SEVERITY_INFO, "Success", "Your addition proposal has been submitted.");
		} finally {
			update();
			if(formParentNode!=null) {
				formParentNode.setSelected(true);
				formParentNode=null;
			}
			if(formNamePartNode!=null){
				formNamePartNode.setSelected(false);
				formNamePartNode=null;
			}
		}
	}

	public void onModify() {
		try {
			namePartService.modifyNamePart(As.notNull(formNamePart), formName, formMnemonic, formDescription, formComment);
			showMessage(null, FacesMessage.SEVERITY_INFO, "Success", "Your modification proposal has been submitted.");
		} finally {
			update();
			if(formParentNode!=null) {
				formParentNode.setSelected(false);
				formParentNode=null;
			}			
			formNamePartNode.setSelected(true);
			formNamePartNode=null;
		}
	}

	public void onMove() {
		try {
			final @Nullable NamePartView parentView = formParentNode !=null?(NamePartView) formParentNode.getData():null;
			final @Nullable NamePart parent=parentView!=null? parentView.getNamePart():null;
			final @Nullable NamePartView namePartView = formNamePartNode !=null?(NamePartView) formNamePartNode.getData():null;
			namePartService.moveNamePart(As.notNull(namePartView).getNamePart(), parent, formComment);
			showMessage(null, FacesMessage.SEVERITY_INFO, "Success", "Your move proposal has been submitted.");
		} finally {
			update();
			if(formParentNode!=null) {
				formParentNode.setSelected(false);
				formParentNode=null;
			}			
			formNamePartNode.setSelected(true);
			formNamePartNode=null;
		}
	}

	
	public void onDelete() {
		try {
			final List<NamePartView> targets = linearizedTargetsForDelete(deleteView);
			for (NamePartView namePartView : targets) {
				namePartService.deleteNamePart(namePartView.getNamePart(), formComment);
			}
			showMessage(null, FacesMessage.SEVERITY_INFO, "Success", printedAffectedQuantity(targets.size()) + "proposed for deletion.");
		} finally {
			update();
		}
	}

	public void onCancel() {
		try {
			final List<NamePartView> targets = linearizedTargets(cancelView);
			for (NamePartView namePartView : targets) {
				namePartService.cancelChangesForNamePart(namePartView.getNamePart(), formComment);
			}
			showMessage(null, FacesMessage.SEVERITY_INFO, "Success", "Proposed changes for " + printedAffectedQuantity(targets.size()) + "cancelled.");
		} finally {
			update();
		}
	}

	public void onApprove() {
		try {
			final List<NamePartView> targets = linearizedTargets(approveView);
			for (NamePartView namePartView : targets) {
				namePartService.approveNamePartRevision(namePartView.getPendingRevision(), formComment);
			}
			showMessage(null, FacesMessage.SEVERITY_INFO, "Success", "Proposed changes for " + printedAffectedQuantity(targets.size()) + "approved.");
		} finally {
			update();
		}
	}

	public void onReject() {
		try {
			final List<NamePartView> targets = linearizedTargets(cancelView);
			for (NamePartView namePartView : targets) {
				namePartService.rejectChangesForNamePart(namePartView.getNamePart(), formComment);
			}
			showMessage(null, FacesMessage.SEVERITY_INFO, "Success", "Proposed changes for " + printedAffectedQuantity(targets.size()) + "rejected.");
		} finally {
			update();
		}
	}

	private String printedAffectedQuantity(int n) {
		if (namePartType == NamePartType.SECTION) {
			return n + " section" + (n > 1 ? "s have been " : " has been ");
		} else if (namePartType == NamePartType.DEVICE_TYPE) {
			return n + " device type" + (n > 1 ? "s have been " : " has been ");
		} else {
			throw new UnhandledCaseException();
		}
	}


	@Deprecated
	public boolean isModified(NamePartView req, boolean isFullName) {
		if (req.getPendingChange() instanceof NamePartView.ModifyChange) {
			final ModifyChange modifyChange = (ModifyChange) req.getPendingChange();
			return ((isFullName && modifyChange.getNewName() != null) || !isFullName && modifyChange.getNewMnemonic() != null);
		} else {
			return false;
		}
	}

	public NamePartDisplayFilter getViewFilter() { return this.displayView; }
	public void setViewFilter(NamePartDisplayFilter viewFilter) { this.displayView = viewFilter; }

	public void modifyDisplayView() {
		if (displayView == NamePartDisplayFilter.APPROVED_AND_PROPOSED) {
			viewRoot = approvedAndProposedView(false, rootWithModifications);
		} else if (displayView == NamePartDisplayFilter.APPROVED) {
			viewRoot = approvedAndProposedView(false, rootWithoutModifications);
		} else if (displayView == NamePartDisplayFilter.PROPOSED) {
			viewRoot = onlyProposedView(rootWithModifications);
		} else if (displayView == NamePartDisplayFilter.ARCHIVED) {
			viewRoot = approvedAndProposedView(true, rootWithoutModifications);
		} else {
			throw new UnhandledCaseException();
		}

		selectedNodes = new TreeNode[0];
		updateOperationViews();		
	}


	public String operationNewStyleClass(OperationView<NamePartView> opReq, boolean modified){
		NamePartView req=opReq.getData();
		switch(operation){
		case APPROVE: return newStyleClass(req,modified);
		case CANCEL: return req.getPendingRevision() != null? "Cancel " + newStyleClass(req,modified) : newStyleClass(req,modified) ;
		case REJECT: return req.getPendingRevision() != null? "Cancel " + newStyleClass(req,modified) : newStyleClass(req,modified) ;
		case DELETE: return opReq.isAffected() ? "Delete "+ newStyleClass(req,modified) : "";
		default: throw new UnhandledCaseException();
		}
	}

	public String getOldStyleClass(){
		return "Propose-deleted";
	}

	public String operationOldStyleClass(){
		switch(operation){
		case APPROVE: return getOldStyleClass();
		case CANCEL : return "Cancel "+getOldStyleClass();
		case REJECT: return "Cancel "+getOldStyleClass();
		case DELETE: return getOldStyleClass();
		default: throw new UnhandledCaseException();
		}
	}

	public String newStyleClass(NamePartView req, boolean modified) {		
		if (req.isPendingDeletion()) {
			return getOldStyleClass();
		} else if (req.isPendingModification()&& modified || req.isProposed()) {
			return "Propose-added";
		} else if (req.isPendingModification()&& !modified){
			return "Propose-default";
		} else {
			return getStatus(req.getCurrentOrElsePendingRevision());
		}
	}

	public String getStatus(NamePartRevision rev){
		switch (rev.getStatus()) {
		case PENDING: return "Proposed";
		case CANCELLED: return "Cancelled";
		case REJECTED: return "Rejected";
		case APPROVED: return rev.isDeleted() ? "Deleted" : "Approved";
		default: throw new UnhandledCaseException();
		}
	}

	public String getStatus(NamePartView req){
		return getStatus(req.getPendingOrElseCurrentRevision());	
	}

	public String operationStatus(OperationView<NamePartView> opReq){
			if(opReq.isAffected()){
				String process=null;
				if(opReq.getData().isPendingModification()){
					process= "modification";
				} else if (opReq.getData().isPendingDeletion()){
					process= "deletion";
				} else if (opReq.getData().isProposed()){
					process= "addition";
				} 
			switch(operation){
			case APPROVE: return "Approve "+process;
			case CANCEL:  return "Cancel "+process;
			case REJECT:  return "Reject "+process;
			case DELETE:  return "Delete";
			default: throw new UnhandledCaseException();
			}
		} else {
			throw new UnhandledCaseException();
		}
	}

	public @Nullable NamePartView getSelectedName() {
		return (selectedNodes != null && selectedNodes.length > 0) ? (NamePartView)(selectedNodes[0].getData()) : null;
	}
	
	public @Nullable TreeNode getSelectedNode(){
		return (selectedNodes != null && selectedNodes.length > 0) ? selectedNodes[0] : null;
	}
	
	public String getFormName() { return formName; }
	public void setFormName(String formName) { this.formName = formName; }

	public String getFormMnemonic() { return formMnemonic !=null ? formMnemonic : ""; }
	public void setFormMnemonic(String formMnemonic) { this.formMnemonic = !formMnemonic.isEmpty()? formMnemonic:null;}

	public String getFormComment() { return formComment != null ? formComment : ""; }
	public void setFormComment(String formComment) { this.formComment = !formComment.isEmpty() ? formComment : null; }

	public String getFormDescription() { return formDescription !=null ? formDescription: "" ; }
	public void setFormDescription(String formDescription){this.formDescription =!formDescription.isEmpty() ? formDescription : null;}

	public void prepareHistoryPopup() {
		final NamePart namePart = As.notNull(getSelectedName()).getNamePart();
		historyRevisions = Lists.newArrayList(Lists.transform(namePartService.revisions(namePart), new Function<NamePartRevision, NamePartView>() {
			@Override public NamePartView apply(NamePartRevision revision) {
				return viewFactory.getView(revision);
			}
		}));
	}

	public void prepareDeletePopup() {
		final List<NamePartView> targets = linearizedTargets(deleteView);
		final List<Device> affectedDevices = Lists.newArrayList();
		for (NamePartView namePartView : targets) {
			affectedDevices.addAll(namePartService.associatedDevices(namePartView.getNamePart(), false));
		}
		this.affectedDevices = affectedDevices;
		operation=Operation.DELETE;
		formComment=null;
		RequestContext.getCurrentInstance().reset("DelNameForm:delName");			
	}

	public void prepareApprovePopup() {
		final List<NamePartView> targets = linearizedTargets(approveView);
		final List<Device> affectedDevices = Lists.newArrayList();
		for (NamePartView namePartView : targets) {
			if (namePartView.getPendingChange() instanceof NamePartView.DeleteChange) {
				affectedDevices.addAll(namePartService.associatedDevices(namePartView.getNamePart(), false));
			}
		}
		formComment=null;
		this.affectedDevices = affectedDevices;
		operation=Operation.APPROVE;
		RequestContext.getCurrentInstance().reset("ApproveForm:approveRequest");					
	}

	public void prepareRejectPopup() {
		this.affectedDevices=Lists.newArrayList();
		formComment=null;
		operation=Operation.REJECT;
		RequestContext.getCurrentInstance().reset("RejectForm:rejectRequest");			
	}

	public void prepareCancelPopup() {
		this.affectedDevices=Lists.newArrayList();
		operation=Operation.CANCEL;
		formComment=null;
		RequestContext.getCurrentInstance().reset("CancelReqForm:cancelReq");			

	}


	public int getAffectedDevicesCount() { return affectedDevices != null ? affectedDevices.size() : 0; }

	public List<NamePartView> getHistoryRevisions() { return historyRevisions; }

	public TreeNode getViewRoot() { return viewRoot != null ? viewRoot : new DefaultTreeNode(null, null);  }

	public TreeNode[] getSelectedNodes() { return selectedNodes; }
	public void setSelectedNodes(TreeNode[] selectedNodes) {
		this.selectedNodes = selectedNodes != null ? selectedNodes : new TreeNode[0];
		updateOperationViews();
	}

	public TreeNode getDeleteView() { return deleteView != null ? deleteView : new DefaultTreeNode(); }
	public TreeNode getApproveView() { return approveView != null ? approveView : new DefaultTreeNode(); }
	public TreeNode getCancelView() { return cancelView != null ? cancelView : new DefaultTreeNode(); }

	public boolean canAdd() {
		if (selectedNodes.length < 2) {
			final @Nullable NamePartView parent = getSelectedName();
			return parent == null || (!parent.getPendingOrElseCurrentRevision().isDeleted() && parent.getLevel() < 2);
		} else {
			return false;
		}
	}
	public boolean canDelete() { return deleteView != null; }
	public boolean canModify() { return getSelectedName() != null && !getSelectedName().getPendingOrElseCurrentRevision().isDeleted(); }
	public boolean canMove(){ return getSelectedName() != null && !getSelectedName().getPendingOrElseCurrentRevision().isDeleted() && getSelectedName().getLevel()>0;}
	public boolean canApprove() { return approveView != null; }
	public boolean canCancel() { return cancelView != null; }
	public boolean canShowHistory() { return selectedNodes.length == 1; }

	public void updateOperationViews() {
		deleteView = deleteView(viewRoot);
		approveView = approveView(viewRoot);
		cancelView = cancelView(viewRoot);
	}

	public boolean hasPendingComment(NamePartView req) {
		return getPendingComment(req) != null;
	}

	public @Nullable String getPendingComment(NamePartView selectedName) {
		final @Nullable NamePartRevision pendingRevision = selectedName.getPendingRevision();
		return pendingRevision != null ? pendingRevision.getRequesterComment() : null;
	}

	private List<NamePartView> linearizedTargets(TreeNode node) {
		final @Nullable OperationView<NamePartView> operationView = (OperationView<NamePartView>) node.getData();
		final List<NamePartView> targets = Lists.newArrayList();
		if (operationView != null && operationView.isAffected()) {
			targets.add(operationView.getData());
		}
		for (TreeNode child : node.getChildren()) {
			targets.addAll(linearizedTargets(child));
		}
		return targets;
	}

	private List<NamePartView> linearizedTargetsForDelete(TreeNode node) {
		final OperationView<NamePartView> operationView = (OperationView<NamePartView>) node.getData();
		final List<NamePartView> targets = Lists.newArrayList();
		if (operationView.isAffected()) {
			targets.add(operationView.getData());
		}
		if (operationView.getData() == null || !(operationView.getData().getPendingChange() instanceof NamePartView.DeleteChange)) {
			for (TreeNode child : node.getChildren()) {
				targets.addAll(linearizedTargets(child));
			}
		}
		return targets;
	}

	private @Nullable TreeNode deleteView(TreeNode node) {
		return (new OperationTreeGenerator<NamePartView>() {
			@Override protected boolean canAffect(NamePartView element) { return !(element.getPendingChange() instanceof NamePartView.DeleteChange) && (element.getCurrentRevision() == null || !(element.getCurrentRevision().isDeleted())); }
			@Override protected boolean autoSelectChildren(NamePartView element) { return !(element.getPendingChange() instanceof NamePartView.DeleteChange); }
			@Override protected boolean ignoreSelectedChildren(NamePartView element, boolean isSelected) { return element.getPendingChange() instanceof NamePartView.AddChange; }
		}).apply(node);
	}

	private @Nullable TreeNode approveView(TreeNode node) {
		return (new OperationTreeGenerator<NamePartView>() {
			@Override protected boolean canAffect(NamePartView element) { return element.getPendingChange() != null; }
			@Override protected boolean autoSelectChildren(NamePartView element) { return element.getPendingChange() instanceof NamePartView.DeleteChange; }
			@Override protected boolean ignoreSelectedChildren(NamePartView element, boolean isSelected) { return element.getPendingChange() instanceof NamePartView.AddChange && !isSelected; }
		}).apply(node);
	}

	private @Nullable TreeNode cancelView(TreeNode node) {
		return (new OperationTreeGenerator<NamePartView>() {
			@Override protected boolean canAffect(NamePartView element) { return element.getPendingChange() != null;}
			@Override protected boolean autoSelectChildren(NamePartView element) { return !(element.getPendingChange() instanceof NamePartView.ModifyChange); }
			@Override protected boolean ignoreSelectedChildren(NamePartView element, boolean isSelected) { return element.getPendingChange() instanceof NamePartView.DeleteChange; }
		}).apply(node);
	}

	private @Nullable TreeNode onlyProposedView(TreeNode node) {
		return (new TreeFilter<NamePartView>() {
			@Override protected boolean accepts(NamePartView nodeData) { return nodeData.getPendingChange() != null; }
		}).apply(node);
	}

	private @Nullable TreeNode approvedAndProposedView(final boolean withDeletions, TreeNode node) {
		return (new TreeFilter<NamePartView>() {
			@Override protected boolean accepts(NamePartView nodeData) { 
				return withDeletions || !nodeData.isDeleted(); 
			}
		}).apply(node);
	}

	private void showMessage(@Nullable String notificationChannel, FacesMessage.Severity severity, String summary, String message) {
		final FacesContext context = FacesContext.getCurrentInstance();
		context.addMessage(notificationChannel, new FacesMessage(severity, summary, message));
	}


	private enum NamePartDisplayFilter {
		APPROVED_AND_PROPOSED, APPROVED, PROPOSED, ARCHIVED
	}
	
	private enum Operation{
		VIEW, APPROVE, DELETE, REJECT, CANCEL, MODIFY, ADD, MOVE
	}
}
