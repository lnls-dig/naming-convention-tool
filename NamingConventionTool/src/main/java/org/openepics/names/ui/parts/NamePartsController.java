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
 */
package org.openepics.names.ui.parts;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.openepics.names.model.*;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.services.views.NamePartView;
import org.openepics.names.services.views.NamePartView.Change;
import org.openepics.names.services.views.NamePartView.ModifyChange;
import org.openepics.names.ui.common.*;
import org.openepics.names.util.As;
import org.openepics.names.util.Marker;
import org.openepics.names.util.UnhandledCaseException;
import org.primefaces.context.RequestContext;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
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
 * @author Vasu V <vuppala@frib.msu.org>
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
	
	private TreeNode deleteView;
	private TreeNode approveView;
	private TreeNode cancelView;

	private String formName;
	private String formMnemonic;
	private String formDescription;
	private String formComment;

	private List<Device> affectedDevices;
	private String namePartTitle;

	@PostConstruct
	public void init() {
		formName = null;
		formMnemonic = null;
		formDescription=null;
		formComment = null;
		rootWithModifications = getRootTreeNode(true);
		rootWithoutModifications = getRootTreeNode(false);
		modifyDisplayView();
		treeNodeManager.expandCustomized(viewRoot); 
	}
	
	public void onNodeExpand(NodeExpandEvent event){
		if(event!=null && event.getTreeNode() !=null){
			treeNodeManager.expand(event.getTreeNode());
		}
	}

	public void onNodeCollapse(NodeCollapseEvent event){
		if(event!=null && event.getTreeNode() !=null){
			treeNodeManager.collapse(event.getTreeNode());    	
		}
	}

	public String getNamePartTitle(){
		return namePartTitle;
	}

	private TreeNode getRootTreeNode(boolean withModifications) {
		final @Nullable String typeParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("type");

		if (typeParam == null) {
			Marker.doNothing();
		} else if (typeParam.equals("section")) {
			namePartType = NamePartType.SECTION;
			namePartTitle = "Area Structure";		
		} else if (typeParam.equals("deviceType")) {
			namePartType = NamePartType.DEVICE_TYPE;
			namePartTitle = "Device Structure";		
		} else {
			throw new IllegalStateException();
		}
		
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

	public boolean isAddMnemonicUnique(String mnemonic) {
		final @Nullable NamePart parent = getSelectedName() != null ? getSelectedName().getNamePart() : null;
		return namePartService.isMnemonicUnique(namePartType, parent, mnemonic);
	}

	public boolean isModifyMnemonicUnique(String mnemonic) {
		final NamePartView namePart = As.notNull(getSelectedName());
		String currentMnemonic = namePart.getPendingOrElseCurrentRevision().getMnemonic();
		if (!mnemonic.equals(namePart.getPendingOrElseCurrentRevision().getMnemonic())) {
			final @Nullable NamePart parent = namePart.getParent() != null ? namePart.getParent().getNamePart() : null;
			return namePartService.isMnemonicUniqueExceptForItself(currentMnemonic,namePartType, parent, mnemonic);
		} else {
			return true;
		}
	}

	public void onAdd() {
		try {
			final @Nullable NamePartView parent = getSelectedName();
			namePartService.addNamePart(formName, formMnemonic, formDescription, namePartType, parent != null ? parent.getNamePart() : null, formComment);
			showMessage(null, FacesMessage.SEVERITY_INFO, "Success", "Your addition proposal has been submitted.");
		} finally {
			init();
		}
	}

	public void onModify() {
		try {
			namePartService.modifyNamePart(As.notNull(getSelectedName()).getNamePart(), formName, formMnemonic, formDescription, formComment);
			showMessage(null, FacesMessage.SEVERITY_INFO, "Success", "Your modification proposal has been submitted.");
		} finally {
			init();
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
			init();
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
			init();
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
			init();
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
			init();
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

	private NamePartView.ModifyChange modifyChange(Change change){
		if (change!= null && change instanceof NamePartView.ModifyChange) {
			return (NamePartView.ModifyChange)change;
		} else {
			return null;
		}
	}
	
	private String nameChange(NamePartView req){
		final ModifyChange change = modifyChange(req.getPendingChange());
		return change !=null && change.getNewName() != null ?  change.getNewName(): null;
	}
	
	private String mnemonicChange(NamePartView req){
		final ModifyChange change=modifyChange(req.getPendingChange());
		return change !=null && change.getNewMnemonic() != null ? change.getNewMnemonic(): null;
	}

	private String descriptionChange(NamePartView req){
		final ModifyChange change=modifyChange(req.getPendingChange());
		return change!= null && change.getNewDescription()!=null ? change.getNewDescription(): null;
	}
	
	public boolean isNameModified(NamePartView req){
		return nameChange(req)!=null;
	}

	public boolean isMnemonicModified(NamePartView req){
		return mnemonicChange(req)!=null;
	}
	
	public boolean isDescriptionModified(NamePartView req){
		return descriptionChange(req)!=null;
	}	
	
	public String getNewName(NamePartView req) {
		return isNameModified(req) ? nameChange(req): req.getName(); 
	}

	public String getNewMnemonic(NamePartView req) {
		return isMnemonicModified(req) ? mnemonicChange(req): req.getMnemonic();
	}

	public String getNewDescription(NamePartView req){
		return isDescriptionModified(req) ? descriptionChange(req):req.getDescription();
	}
	
	public String getOperationsNewName(OperationView<NamePartView> opReq) {
		final NamePartView req = opReq.getData();
		return  !getNewName(req).equals("")? getNewName(req): req.getName();
//		final ModifyChange change = modifyChange(req.getPendingChange());
//		final Change change = req.getPendingChange();
//		if (change instanceof NamePartView.ModifyChange && ((NamePartView.ModifyChange)change).getNewName() != null && !((NamePartView.ModifyChange) change).getNewName().equals("")) {
//			return ((NamePartView.ModifyChange)change).getNewName();
//		} else {
//			return req.getName();
//		}
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

	public String nameStyleClass(NamePartView req) {
		return req!=null ? nameStatus(req).replace(" ","-"): "";
	}

	public String nameCancelStyleClass(NamePartView req) {
		return isPending(req.getPendingChange()) ? "Cancel-"+nameStyleClass(req).toLowerCase(): "";
	}

	public boolean isPending(Change change){
		return change != null && change.getStatus() == NamePartRevisionStatus.PENDING ;
	}
	
	public String statusStyleClass(NamePartView req) {
		return req != null ? namePendingStatus(req).replace(" ", "-") : "";
	}
	
	public String nameStatus(NamePartView req) {
		final Change change = req.getPendingChange();
		if (isPending(change)) {            
			if (change instanceof NamePartView.DeleteChange) {
				return "Pending deletion";
			} else if (change instanceof NamePartView.ModifyChange) {
				return "Pending modification";
			} else if (change instanceof NamePartView.AddChange) {
				return "Proposed";
			} else {
				throw new UnhandledCaseException();
			}
		} else {
			return namePendingStatus(req);
		}
	}
	
    public String namePendingStatus(NamePartView req){
		switch (req.getPendingOrElseCurrentRevision().getStatus()) {
		case PENDING: return "In-Process";
		case CANCELLED: return "Cancelled";
		case REJECTED: return "Rejected";
		case APPROVED: return req.isDeleted() ? "Deleted" : "Approved";
		default: throw new UnhandledCaseException();
		}
    }

//	@Deprecated
//	public String nameHistoryStatus(NamePartView req) {
//		return namePendingStatus(req);
//	}
	
	@Deprecated
	public String nameViewClass(NamePartView entry) {
		switch (entry.getNameEvent().getStatus()) {
		case PENDING: return "Processing";
		case CANCELLED: return "default";
		case REJECTED: return "default";
		case APPROVED: return "Approved";
		default: throw new UnhandledCaseException();
		}
	}

	public @Nullable NamePartView getSelectedName() {
		return (selectedNodes != null && selectedNodes.length > 0) ? (NamePartView)(selectedNodes[0].getData()) : null;
	}

	public String getFormName() { return formName; }
	public void setFormName(String formName) { this.formName = formName; }

	public String getFormMnemonic() { return formMnemonic; }
	public void setFormMnemonic(String formMnemonic) { this.formMnemonic = formMnemonic; }

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

	public void prepareAddPopup() {
		formName = null;
		formMnemonic = null;
		formDescription = null;
		formComment = null;
		RequestContext.getCurrentInstance().reset("addNameForm:grid");
	}

	public void prepareModifyPopup() {
		final NamePartRevision namePartRevision = As.notNull(getSelectedName()).getPendingOrElseCurrentRevision();
		formName = namePartRevision.getName();
		formMnemonic = namePartRevision.getMnemonic();
		formDescription = namePartRevision.getDescription();
		formComment = null;
		RequestContext.getCurrentInstance().reset("ModNameForm:pgrid");
	}

	public void prepareDeletePopup() {
		final List<NamePartView> targets = linearizedTargets(deleteView);
		final List<Device> affectedDevices = Lists.newArrayList();
		for (NamePartView namePartView : targets) {
			affectedDevices.addAll(namePartService.associatedDevices(namePartView.getNamePart(), false));
		}
		this.affectedDevices = affectedDevices;
	}

	public void prepareApprovePopup() {
		final List<NamePartView> targets = linearizedTargets(approveView);
		final List<Device> affectedDevices = Lists.newArrayList();
		for (NamePartView namePartView : targets) {
			if (namePartView.getPendingChange() instanceof NamePartView.DeleteChange) {
				affectedDevices.addAll(namePartService.associatedDevices(namePartView.getNamePart(), false));
			}
		}
		this.affectedDevices = affectedDevices;
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
}
