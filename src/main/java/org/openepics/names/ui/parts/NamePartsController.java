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
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartRevisionStatus;
import org.openepics.names.model.NamePartType;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.ui.common.*;
import org.openepics.names.ui.parts.NamePartView.Change;
import org.openepics.names.ui.parts.NamePartView.ModifyChange;
import org.openepics.names.util.Marker;
import org.openepics.names.util.UnhandledCaseException;
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
 * @author Vasu V <vuppala@frib.msu.org>
 */
@ManagedBean
@ViewScoped
public class NamePartsController implements Serializable {

    @Inject private RestrictedNamePartService namePartService;
    @Inject private ViewFactory viewFactory;
    @Inject private NamePartTreeBuilder namePartTreeBuilder;
    @Inject private UserManager userManager;

    private List<NamePartView> historyEvents;

    private TreeNode rootWithModifications;
    private TreeNode rootWithoutModifications;
    private TreeNode viewRoot;
    private TreeNode[] selectedNodes;

    private TreeNode deleteView;
    private TreeNode approveView;
    private TreeNode cancelView;


    private String newCode;
    private String newDescription;
    private String newComment;
    private NamePartDisplayFilter displayView = NamePartDisplayFilter.APPROVED_AND_PROPOSED;

    private NamePartType namePartType;

    private boolean modify;
    private boolean isDifferentThenCurrent;
    private boolean viewWithDeletions;

    @PostConstruct
    public void init() {
    	rootWithModifications = getRootTreeNode(true);
    	rootWithoutModifications = getRootTreeNode(false);
        modifyDisplayView();
    }

    private TreeNode getRootTreeNode(boolean withModifications) {
    	final @Nullable String typeParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("type");
    	
    	if (typeParam == null) {
            Marker.doNothing();
    	} else if (typeParam.equals("section")) {
            namePartType = NamePartType.SECTION;
        } else if (typeParam.equals("deviceType")) {
            namePartType = NamePartType.DEVICE_TYPE;
        } else {
            throw new IllegalStateException();
        }

        final List<NamePartRevision> approvedRevisions = namePartService.currentApprovedRevisions(namePartType, true);
        final List<NamePartRevision> pendingRevisions = withModifications ? namePartService.currentPendingRevisions(namePartType, true) : Lists.<NamePartRevision>newArrayList();
        return namePartTreeBuilder.newNamePartTree(approvedRevisions, pendingRevisions, true);
    }

    public void onAdd() {
        try {
            final NamePartView parent = getSelectedName();
            final NamePartRevision newRequest = namePartService.addNamePart(newDescription, newCode, namePartType, parent != null ? parent.getNamePart() : null, newComment);
            showMessage(FacesMessage.SEVERITY_INFO, "Your request was successfully submitted.", "Request Number: " + newRequest.getId());
        } finally {
            init();
        }
    }

    public void onModify() {
        try {
            final NamePartRevision newRequest = namePartService.modifyNamePart(getSelectedName().getNamePart(), newDescription, newCode, newComment);
            showMessage(FacesMessage.SEVERITY_INFO, "Your request was successfully submitted.", "Request Number: " + newRequest.getId());
        } finally {
            init();
        }
    }

    public void onDelete() {
        try {
            for (NamePartView namePartView : linearizedTargetsForDelete(deleteView)) {
                namePartService.deleteNamePart(namePartView.getNamePart(), newComment);
            }
            showMessage(FacesMessage.SEVERITY_INFO, "Success", "The data you requested was successfully deleted.");
        } finally {
            init();
        }
    }

    public void onCancel() {
        try {
            for (NamePartView namePartView : linearizedTargets(cancelView)) {
                namePartService.cancelChangesForNamePart(namePartView.getNamePart(), newComment);
            }
            showMessage(FacesMessage.SEVERITY_INFO, "Your request has been cancelled.", "Request Number: ");
        } finally {
            init();
        }
    }

    public void onApprove() {
        try {
            for (NamePartView namePartView : linearizedTargets(approveView)) {
                namePartService.approveNamePartRevision(namePartView.getPendingRevision(), newComment);
            }
            showMessage(FacesMessage.SEVERITY_INFO, "All selected requests were successfully approved.", " ");
        } finally {
            init();
        }
    }

    public void onReject() {
        try {
            for (NamePartView namePartView : linearizedTargets(cancelView)) {
                namePartService.rejectChangesForNamePart(namePartView.getNamePart(), newComment);
            }
            showMessage(FacesMessage.SEVERITY_INFO, "All selected requests were successfully rejected.", " ");
        } finally {
            init();
        }
    }

    public String getRequestType(NamePartView req) {
        final Change change = req.getPendingChange();
        if (change != null) {
            if (change instanceof NamePartView.AddChange) return "Add request";
            else if (change instanceof NamePartView.ModifyChange) return "Modify request";
            else if (change instanceof NamePartView.DeleteChange) return "Delete request";
            else throw new UnhandledCaseException();
        } else {
            return req.isDeleted() ? "Deleted" : "";
        }
    }

    public String getNewName(NamePartView req) {
        final Change change = req.getPendingChange();
        if (change instanceof NamePartView.ModifyChange && ((NamePartView.ModifyChange)change).getNewName() != null) {
            return ((NamePartView.ModifyChange)change).getNewName();
        } else {
            return req.getName();
        }
    }

    public String getNewMnemonic(NamePartView req) {
        final Change change = req.getPendingChange();
        if (change instanceof NamePartView.ModifyChange && ((NamePartView.ModifyChange)change).getNewMnemonic() != null) {
            return ((NamePartView.ModifyChange)change).getNewMnemonic();
        } else {
            return req.getMnemonic();
        }

    }

    public String getOperationsNewName(OperationView<NamePartView> opReq) {
        final NamePartView req = opReq.getData();
        final Change change = req.getPendingChange();
        if (change instanceof NamePartView.ModifyChange && ((NamePartView.ModifyChange)change).getNewName() != null && !((NamePartView.ModifyChange) change).getNewName().equals("")) {
            return ((NamePartView.ModifyChange)change).getNewName();
        } else {
            return req.getName();
        }
    }

    public boolean isDeletePending (NamePartView req) {
    	return req.getPendingChange() instanceof NamePartView.DeleteChange;
    }

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
            viewWithDeletions = false;
            viewRoot = approvedAndProposedView(rootWithModifications);
        } else if (displayView == NamePartDisplayFilter.APPROVED) {
            viewWithDeletions = false;
            viewRoot = approvedAndProposedView(rootWithoutModifications);
        } else if (displayView == NamePartDisplayFilter.PROPOSED) {
            viewRoot = onlyProposedView(rootWithModifications);
        } else if (displayView == NamePartDisplayFilter.PROPOSED_BY_ME) {
            viewRoot = onlyProposedView(rootWithModifications); // TODO
        } else if (displayView == NamePartDisplayFilter.ARCHIVED) {
            viewWithDeletions = true;
            viewRoot = approvedAndProposedView(rootWithoutModifications);
        } else {
            throw new UnhandledCaseException();
        }
        
    	newCode = newDescription = newComment = null;
    	modify = false;
        selectedNodes = new TreeNode[0];
    	updateOperationViews();
    }

    public String getNameClass(NamePartView req, boolean isFullName) {
    	final Change change = req.getPendingChange();

        final String prefix;
        if (change == null) {
            prefix = req.isDeleted() ? "Delete" : "Insert";
        } else if (change instanceof NamePartView.AddChange) {
            prefix = "Insert";
        } else if (change instanceof NamePartView.ModifyChange) {
            final ModifyChange modifyChange = (ModifyChange) change;
            if ((isFullName && modifyChange.getNewName() != null) || !isFullName && modifyChange.getNewMnemonic() != null) {
                prefix = "Modify";
            } else {
                prefix = "Insert";
            }
        } else if (change instanceof NamePartView.DeleteChange) {
            prefix = "Delete";
        } else {
            throw new UnhandledCaseException();
        }

        final String postfix;
        if (change == null) {
            postfix = "Approved";
        } else if (change instanceof NamePartView.ModifyChange && !((isFullName && ((ModifyChange) change).getNewName() != null) || !isFullName && ((ModifyChange) change).getNewMnemonic() != null)) {
            postfix = "Approved";
        } else if (change.getStatus() == NamePartRevisionStatus.APPROVED) {
            postfix = "Approved";
        } else if (change.getStatus() == NamePartRevisionStatus.CANCELLED) {
            postfix = "Cancelled";
        } else if (change.getStatus() == NamePartRevisionStatus.PENDING) {
            postfix = "Processing";
        } else if (change.getStatus() == NamePartRevisionStatus.REJECTED) {
            postfix = "Rejected";
        } else {
            throw new IllegalStateException();
        }

        return prefix + "-" + postfix;
    }

    public String nameStatus(NamePartRevision nreq) {
        switch (nreq.getStatus()) {
            case PENDING: return "In-Process";
            case CANCELLED: return "Cancelled";
            case REJECTED: return "Rejected";
            case APPROVED: return nreq.isDeleted() ? "Deleted" : "Approved";
            default: throw new UnhandledCaseException();
        }
    }

    public String nameViewClass(NamePartView entry) {
        switch (entry.getNameEvent().getStatus()) {
            case PENDING: return "Processing";
            case CANCELLED: return "default";
            case REJECTED: return "default";
            case APPROVED: return "Approved";
            default: throw new UnhandledCaseException();
        }
    }

    public void findHistory() {
        historyEvents = getSelectedName() == null ? null : Lists.newArrayList(Lists.transform(namePartService.revisions(getSelectedName().getNamePart()), new Function<NamePartRevision, NamePartView>() {
            @Override public NamePartView apply(NamePartRevision revision) {
                return viewFactory.getView(revision);
            }
        }));
    }

    public boolean hasPendingComment(NamePartView req) {
    	return (isModified(req, true) || isModified(req, false) || isDeletePending(req)) && getPendingComment(req) != null && getPendingComment(req).length() > 0;
    }

    public List<NamePartView> findHistory(NamePartView selectedName) {
    	return Lists.newArrayList(Lists.transform(namePartService.revisions(selectedName.getNamePart()), new Function<NamePartRevision, NamePartView>() {
            @Override public NamePartView apply(NamePartRevision revision) { return viewFactory.getView(revision); }
        }));
    }

    public @Nullable NamePartView getSelectedName() {
    	return (selectedNodes != null && selectedNodes.length > 0) ? (NamePartView)(selectedNodes[0].getData()) : null;
    }

    public String getNewCode() {
    	newCode = (getSelectedName() == null || !modify) ? null : getSelectedName().getPendingOrElseCurrentRevision().getMnemonic();
    	if (modify) {
    		checkForChanges();
    	}
    	return newCode;
    }
    public void setNewCode(String newCode) { this.newCode = newCode; }

    public String getNewDescription() {
    	newDescription = (getSelectedName() == null || !modify) ? null : getSelectedName().getPendingOrElseCurrentRevision().getName();
    	return newDescription;
    }
    public void setNewDescription(String newDescription) { this.newDescription = newDescription; }

    public void showModify() { modify = true; }

    public void showAdd() { modify = false; }

    public boolean canSaveModifications() { return !isDifferentThenCurrent; }

    public void checkForChanges() {
        isDifferentThenCurrent = (!newDescription.equals(getSelectedName().getPendingOrElseCurrentRevision().getName()) || !newCode.equals(getSelectedName().getPendingOrElseCurrentRevision().getMnemonic()));
    }

    public String getNewComment() {
    	return newComment;
    }
    public void setNewComment(String newComment) { this.newComment = newComment; }

    public List<NamePartView> getHistoryEvents() { return historyEvents; }

    public TreeNode getRoot() { return rootWithModifications; }

    public TreeNode getViewRoot() { return viewRoot != null ? viewRoot : new DefaultTreeNode(null, null);  }

    public TreeNode[] getSelectedNodes() { return selectedNodes; }

    public void setSelectedNodes(TreeNode[] selectedNodes) {
        this.selectedNodes = selectedNodes != null ? selectedNodes : new TreeNode[0];
        updateOperationViews();
    }

    public TreeNode getDeleteView() { return deleteView != null ? deleteView : new DefaultTreeNode(); }
    public TreeNode getApproveView() { return approveView != null ? approveView : new DefaultTreeNode(); }
    public TreeNode getCancelView() { return cancelView != null ? cancelView : new DefaultTreeNode(); }

    public boolean canAdd() { return selectedNodes.length == 0 || (selectedNodes.length == 1 && !((NamePartView) selectedNodes[0].getData()).getPendingOrElseCurrentRevision().isDeleted()); }
    public boolean canDelete() { return deleteView != null; }
    public boolean canModify() { return selectedNodes.length == 1 && !((NamePartView) selectedNodes[0].getData()).getPendingOrElseCurrentRevision().isDeleted(); }
    public boolean canApprove() { return approveView != null; }
    public boolean canCancel() { return cancelView != null; }
    public boolean canShowHistory() { return selectedNodes.length == 1; }

    public void updateOperationViews() {
        deleteView = deleteView(viewRoot);
        approveView = approveView(viewRoot);
        cancelView = cancelView(viewRoot);
    }

    public String getPendingComment(NamePartView selectedName) {
    	List<NamePartView> historyEvents = findHistory(selectedName);
    	if (historyEvents != null && historyEvents.size() > 0) {
    		return historyEvents.get(historyEvents.size() - 1).getNameEvent().getRequesterComment();
    	} else {
    	    return null;
    	}
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
        return (new OperationsTreePreview<NamePartView>() {
            @Override protected boolean isAffected(NamePartView nodeView) { return !(nodeView.getPendingChange() instanceof NamePartView.DeleteChange); }
            @Override protected boolean autoSelectChildren(NamePartView nodeView) { return !(nodeView.getPendingChange() instanceof NamePartView.DeleteChange); }
            @Override protected boolean ignoreSelectedChildren(NamePartView nodeView, boolean isSelected) { return nodeView.getPendingChange() instanceof NamePartView.AddChange; }
        }).apply(node);
    }

    private @Nullable TreeNode approveView(TreeNode node) {
        return (new OperationsTreePreview<NamePartView>() {
            @Override protected boolean isAffected(NamePartView nodeView) { return nodeView.getPendingChange() != null; }
            @Override protected boolean autoSelectChildren(NamePartView nodeView) { return nodeView.getPendingChange() instanceof NamePartView.DeleteChange; }
            @Override protected boolean ignoreSelectedChildren(NamePartView nodeView, boolean isSelected) { return nodeView.getPendingChange() instanceof NamePartView.AddChange && !isSelected; }
        }).apply(node);
    }

    private @Nullable TreeNode cancelView(TreeNode node) {
        return (new OperationsTreePreview<NamePartView>() {
            @Override protected boolean isAffected(NamePartView nodeView) { return nodeView.getPendingChange() != null; }
            @Override protected boolean autoSelectChildren(NamePartView nodeView) { return !(nodeView.getPendingChange() instanceof NamePartView.ModifyChange); }
            @Override protected boolean ignoreSelectedChildren(NamePartView nodeView, boolean isSelected) { return nodeView.getPendingChange() instanceof NamePartView.DeleteChange; }
        }).apply(node);
    }

    private @Nullable TreeNode onlyProposedView(TreeNode node) {
        return (new TreeViewFilter<NamePartView>() {
            @Override protected boolean accepts(NamePartView nodeView) { return nodeView.getPendingChange() != null && (userManager.getUser() == null || nodeView.getPendingRevision().getRequestedBy().equals(userManager.getUser())); }
        }).apply(node);
    }

    private @Nullable TreeNode approvedAndProposedView(TreeNode node) {
        return (new TreeViewFilter<NamePartView>() {
            @Override protected boolean accepts(NamePartView nodeView) { return viewWithDeletions || !nodeView.isDeleted(); }
        }).apply(node);
    }

    private void showMessage(FacesMessage.Severity severity, String summary, String message) {
        final FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(severity, summary, message));
    }
    
    private enum NamePartDisplayFilter {
        APPROVED_AND_PROPOSED, APPROVED, PROPOSED, PROPOSED_BY_ME, ARCHIVED
    }
}
