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

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartType;
import org.openepics.names.model.NameRelease;
import org.openepics.names.model.UserAccount;
import org.openepics.names.services.ReleaseService;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.ui.common.UserManager;
import org.openepics.names.ui.common.ViewFactory;
import org.openepics.names.ui.parts.NamePartView.Change;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Manages Change Requests (backing bean for request-sub.xhtml)
 *
 * @author Vasu V <vuppala@frib.msu.org>
 */
@ManagedBean
@ViewScoped
public class NamePartsController implements Serializable {

    @Inject private RestrictedNamePartService namePartService;
    @Inject private ViewFactory viewFactory;
    @Inject private NamePartTreeBuilder namePartTreeBuilder;
    @Inject private ReleaseService releaseService;
    @Inject private UserManager userManager;

    private List<NamePartView> historyEvents;

    private TreeNode root;
    private TreeNode[] selectedNodes;

    private TreeNode deleteView;
    private TreeNode approveView;
    private TreeNode cancelView;
    private TreeNode modifyView;

    private String newCode;
    private String newDescription;
    private String newComment;
    private int displayView = 1;
    
    private NamePartType namePartType;

    private NameRelease latestRelease;

    @PostConstruct
    public void init() {
        modifyDisplayView();
    }
    
    private TreeNode getRootTreeNode(boolean withModifications) {
    	final @Nullable String typeParam = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("type");
    	
    	if (typeParam == null) {
            
        } else if (typeParam.equals("section")) {
            namePartType = NamePartType.SECTION;
        } else if (typeParam.equals("deviceType")) {
            namePartType = NamePartType.DEVICE_TYPE;
        } else {
            throw new IllegalStateException();
        }

        latestRelease = releaseService.getLatestRelease();

        final List<NamePartRevision> approvedRevisions = ImmutableList.copyOf(Collections2.filter(namePartService.currentApprovedRevisions(true), new Predicate<NamePartRevision>() {
            @Override public boolean apply(NamePartRevision revision) { return revision.getNamePart().getNamePartType() == namePartType; }
        }));
        final List<NamePartRevision> pendingRevisions;
        if (withModifications)
	        pendingRevisions = ImmutableList.copyOf(Collections2.filter(namePartService.currentPendingRevisions(true), new Predicate<NamePartRevision>() {
	            @Override public boolean apply(NamePartRevision revision) { return revision.getNamePart().getNamePartType() == namePartType; }
	        }));
        else {
        	pendingRevisions = Lists.newArrayList();
        }
        
        return namePartTreeBuilder.namePartApprovalTree(approvedRevisions, pendingRevisions, true);
    }

    public void onAdd() {
        try {
            final NamePartView parent = getSelectedName();
            final NamePartRevision newRequest = namePartService.addNamePart(newCode, newDescription, namePartType, parent != null ? parent.getNamePart() : null, newComment);
            showMessage(FacesMessage.SEVERITY_INFO, "Your request was successfully submitted.", "Request Number: " + newRequest.getId());
        } finally {
            init();
        }
    }

    public void onModify() {
        try {
            final NamePartRevision newRequest = namePartService.modifyNamePart(getSelectedName().getNamePart(), newCode, newDescription, newComment);
            showMessage(FacesMessage.SEVERITY_INFO, "Your request was successfully submitted.", "Request Number: " + newRequest.getId());
        } finally {
            init();
        }
    }

    public void onDelete() {
        try {
            for (NamePartView namePartView : linearizedTargets(deleteView)) {
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
        // TODO after checking the parts-request-proc.xhtml, remove everything related to 'Change'
        final Change change = req.getPendingChange();
        if (change instanceof NamePartView.AddChange) return "Add request";
        else if (change instanceof NamePartView.ModifyChange) return "Modify request";
        else if (change instanceof NamePartView.DeleteChange) return "Delete request";
        else if (req.isDeleted()) return "Deleted" ;
        else return "";
    }

    public String getNewName(NamePartView req) {
        final Change change = req.getPendingChange();
        if (change instanceof NamePartView.ModifyChange) {
            return ((NamePartView.ModifyChange)change).getNewName();
        } else {
            return req.getName();
        }

    }

    public String getNewFullName(NamePartView req) {
        final Change change = req.getPendingChange();
        if (change instanceof NamePartView.ModifyChange) {
            return ((NamePartView.ModifyChange)change).getNewFullName();
        } else {
            return req.getFullName();
        }
    }

    public boolean isModified(NamePartView req) {
        return req.getPendingChange() instanceof NamePartView.ModifyChange;
    }
    
    public void setViewFilter(int filter) {
    	this.displayView = filter;
    }
    
    public int getViewFilter() {
    	return this.displayView;
    }
    
    public void modifyDisplayView() {
    	switch (displayView) {
    	case 1:
    		root = viewAll(getRootTreeNode(true), false);
    		break;
    	case 2:
    		root = viewAll(getRootTreeNode(false), false);
    		break;
		case 3:
			root = viewOnlyModified(getRootTreeNode(true), null);
			break;
		case 4:
			root = viewOnlyModified(getRootTreeNode(true), userManager.getUser());
			break;
		case 5:
			root = viewAll(getRootTreeNode(false), true);
    		break;
		}
    	newCode = newDescription = newComment = null;
        selectedNodes = new TreeNode[0];
    	updateOperationViews();
    }

    public String getNameClass(NamePartView req) {
        final Change change = req.getPendingChange();
        if (change == null) {
            if (req.isDeleted()) return "Delete-Approved";
            return "Insert-Approved";
        }

        StringBuilder ret = new StringBuilder();
        if (change instanceof NamePartView.AddChange) ret.append("Insert-");
        else if (change instanceof NamePartView.ModifyChange) ret.append("Modify-");
        else if (change instanceof NamePartView.DeleteChange) ret.append("Delete-");
        else throw new IllegalStateException();

        switch (change.getStatus()) {
            case APPROVED:
                ret.append("Approved");
                break;
            case CANCELLED:
                ret.append("Cancelled");
                break;
            case PENDING:
                ret.append("Processing");
                break;
            case REJECTED:
                ret.append("Rejected");
                break;
            default:
                throw new IllegalStateException();
        }
        return ret.toString();
    }

    public String nameStatus(NamePartRevision nreq) {
        switch (nreq.getStatus()) {
            case PENDING:
                return "In-Process";
            case CANCELLED:
                return "Cancelled";
            case REJECTED:
                return "Rejected";
            case APPROVED:
                if (nreq.isDeleted()) {
                    return "Deleted";
                } else {
                    return isPublished(nreq) ? "Published" : "Unpublished";
                }
            default:
                throw new IllegalStateException();
        }
    }

    public String nameViewClass(NamePartView entry) {
        switch (entry.getNameEvent().getStatus()) {
            case PENDING: return "Processing";
            case CANCELLED: return "default";
            case REJECTED: return "default";
            case APPROVED: return isPublished(entry.getNameEvent()) ? "Published" : "Approved";
            default: throw new IllegalStateException();
        }
    }

    private boolean isPublished(NamePartRevision namePartRevision) {
        final Date processDate = namePartRevision.getProcessDate();
        if (processDate != null) {
            return latestRelease != null && latestRelease.getReleaseDate().before(processDate);
        } else {
            return false;
        }
    }

    public void findHistory() {
        if (getSelectedName() == null) {
            historyEvents = null;
            return;
        }
        historyEvents = Lists.newArrayList(Lists.transform(namePartService.revisions(getSelectedName().getNamePart()), new Function<NamePartRevision, NamePartView>() {
            @Override public NamePartView apply(NamePartRevision revision) {
                return viewFactory.getView(revision);
            }
        }));
    }

    public @Nullable NamePartView getSelectedName() {
        return selectedNodes.length < 1 ? null : (NamePartView)(selectedNodes[0].getData());
    }

    public String getNewCode() { return newCode; }
    public void setNewCode(String newCode) { this.newCode = newCode; }

    public String getNewDescription() { return newDescription; }
    public void setNewDescription(String newDescription) { this.newDescription = newDescription; }

    public String getNewComment() { return newComment; }
    public void setNewComment(String newComment) { this.newComment = newComment; }

    public List<NamePartView> getHistoryEvents() { return historyEvents; }

    public TreeNode getRoot() { return root; }
    
    public TreeNode[] getSelectedNodes() { return selectedNodes; }
    public void setSelectedNodes(TreeNode[] selectedNodes) {
        this.selectedNodes = selectedNodes != null ? selectedNodes : new TreeNode[0];
        updateOperationViews();
    }

    public TreeNode getDeleteView() { return deleteView; }

    public TreeNode getApproveView() { return approveView; }

    public TreeNode getCancelView() { return cancelView; }
    
    public TreeNode getModifyView() { return modifyView; }

    public boolean canAdd() { return selectedNodes.length == 0 || (selectedNodes.length == 1 && !((NamePartView) selectedNodes[0].getData()).getPendingOrElseCurrentRevision().isDeleted()); }

    public boolean canDelete() { return deleteView != null; }

    public boolean canModify() { return selectedNodes.length == 1 && !((NamePartView) selectedNodes[0].getData()).getPendingOrElseCurrentRevision().isDeleted(); }

    public boolean canApprove() { return approveView != null; }

    public boolean canCancel() { return cancelView != null; }

    public boolean canShowHistory() { return selectedNodes.length == 1; }

    public void updateOperationViews() {
        deleteView = deleteView(root, SelectionMode.MANUAL);
        approveView = approveView(root, SelectionMode.MANUAL);
        cancelView = cancelView(root, SelectionMode.MANUAL);
    }

    private enum SelectionMode { MANUAL, AUTO, DISABLED }

    private List<NamePartView> linearizedTargets(TreeNode node) {
        final @Nullable OperationNamePartView nodeView = (OperationNamePartView) node.getData();
        final List<NamePartView> targets = Lists.newArrayList();
        if (nodeView != null && nodeView.isAffected()) {
            targets.add(nodeView.getNamePartView());
        }
        if (nodeView == null || !(nodeView.getNamePartView().getPendingChange() instanceof NamePartView.DeleteChange)) {
            for (TreeNode child : node.getChildren()) {
                targets.addAll(linearizedTargets(child));
            }
        }
        return targets;
    }

    private @Nullable TreeNode deleteView(TreeNode node, SelectionMode selectionMode) {
        final @Nullable NamePartView nodeView = (NamePartView) node.getData();

        final SelectionMode childrenSelectionMode;
        if (selectionMode == SelectionMode.AUTO) {
            childrenSelectionMode = SelectionMode.AUTO;
        } else if (selectionMode == SelectionMode.MANUAL) {
            if (nodeView != null && node.isSelected() && !(nodeView.getPendingChange() instanceof NamePartView.DeleteChange)) {
                childrenSelectionMode = SelectionMode.AUTO;
            } else if (nodeView != null && nodeView.getPendingChange() instanceof NamePartView.AddChange) {
                childrenSelectionMode = SelectionMode.DISABLED;
            } else {
                childrenSelectionMode = SelectionMode.MANUAL;
            }
        } else if (selectionMode == SelectionMode.DISABLED) {
            childrenSelectionMode = SelectionMode.DISABLED;
        } else {
            throw new IllegalStateException();
        }

        final List<TreeNode> childViews = Lists.newArrayList();
        for (TreeNode child : node.getChildren()) {
            final TreeNode childView = deleteView(child, childrenSelectionMode);
            if (childView != null) {
                childViews.add(childView);
            }
        }

        final boolean affectNode = nodeView != null && (selectionMode == SelectionMode.AUTO || (selectionMode == SelectionMode.MANUAL && node.isSelected())) && !(nodeView.getPendingChange() instanceof NamePartView.DeleteChange);
        if (affectNode || !childViews.isEmpty()) {
            final TreeNode result = new DefaultTreeNode(nodeView != null ? new OperationNamePartView(nodeView, affectNode) : null, null);
            result.setExpanded(true);
            for (TreeNode childView : childViews) {
                childView.setParent(result);
            }
            return result;
        } else {
            return null;
        }
    }
    
    private @Nullable TreeNode approveView(TreeNode node, SelectionMode selectionMode) {
        final @Nullable NamePartView nodeView = (NamePartView) node.getData();
      
        final SelectionMode childrenSelectionMode;
        if (selectionMode == SelectionMode.AUTO) {
            childrenSelectionMode = SelectionMode.AUTO;
        } else if (selectionMode == SelectionMode.MANUAL) {
            if (nodeView != null && node.isSelected() && (nodeView.getPendingChange() instanceof NamePartView.DeleteChange)) {
                childrenSelectionMode = SelectionMode.AUTO;
            } else if (nodeView != null && nodeView.getPendingChange() instanceof NamePartView.AddChange && !node.isSelected()) {
                childrenSelectionMode = SelectionMode.DISABLED;
            } else {
                childrenSelectionMode = SelectionMode.MANUAL;
            }
        } else if (selectionMode == SelectionMode.DISABLED) {
            childrenSelectionMode = SelectionMode.DISABLED;
        } else {
            throw new IllegalStateException();
        }

        final List<TreeNode> childViews = Lists.newArrayList();
        for (TreeNode child : node.getChildren()) {
            final TreeNode childView = approveView(child, childrenSelectionMode);
            if (childView != null) {
                childViews.add(childView);
            }
        }

        final boolean affectNode = nodeView != null && (selectionMode == SelectionMode.AUTO || (selectionMode == SelectionMode.MANUAL && node.isSelected())) && (nodeView.getPendingChange() != null);
        if (affectNode || !childViews.isEmpty()) {
            final TreeNode result = new DefaultTreeNode(nodeView != null ? new OperationNamePartView(nodeView, affectNode) : null, null);
            result.setExpanded(true);
            for (TreeNode childView : childViews) {
                childView.setParent(result);
            }
            return result;
        } else {
            return null;
        }
    }

    private @Nullable TreeNode cancelView(TreeNode node, SelectionMode selectionMode) {
        final @Nullable NamePartView nodeView = (NamePartView) node.getData();

        final SelectionMode childrenSelectionMode;
        if (selectionMode == SelectionMode.AUTO) {
            childrenSelectionMode = SelectionMode.AUTO;
        } else if (selectionMode == SelectionMode.MANUAL) {
            if (nodeView != null && node.isSelected() && !(nodeView.getPendingChange() instanceof NamePartView.ModifyChange)) {
                childrenSelectionMode = SelectionMode.AUTO;
            } else if (nodeView != null && nodeView.getPendingChange() instanceof NamePartView.DeleteChange) {
                childrenSelectionMode = SelectionMode.DISABLED;
            } else {
                childrenSelectionMode = SelectionMode.MANUAL;
            }
        } else if (selectionMode == SelectionMode.DISABLED) {
            childrenSelectionMode = SelectionMode.DISABLED;
        } else {
            throw new IllegalStateException();
        }

        final List<TreeNode> childViews = Lists.newArrayList();
        for (TreeNode child : node.getChildren()) {
            final TreeNode childView = cancelView(child, childrenSelectionMode);
            if (childView != null) {
                childViews.add(childView);
            }
        }
        
        final boolean affectNode = nodeView != null && (selectionMode == SelectionMode.AUTO || (selectionMode == SelectionMode.MANUAL && node.isSelected())) && (nodeView.getPendingChange() != null);
        if (affectNode || !childViews.isEmpty() ) {
            final TreeNode result = new DefaultTreeNode(nodeView != null ? new OperationNamePartView(nodeView, affectNode) : null, null);
            result.setExpanded(true);
            for (TreeNode childView : childViews) {
                childView.setParent(result);
            }
            return result;
        } else {
            return null;
        }
    }
    
    private @Nullable TreeNode viewOnlyModified(TreeNode node, UserAccount user) {
    	
    	final List<TreeNode> modifiedChildren = Lists.newArrayList();
    	
    	if (node.getChildCount() > 0) {
    		for (TreeNode child : node.getChildren()) {
    			TreeNode temp = viewOnlyModified(child, user);
    			if (temp != null) {
    				modifiedChildren.add(temp);
    			} 
    		}
    	} else {
    		final @Nullable NamePartView nodeView = (NamePartView) node.getData();
    		if(user == null) {
	    		if (nodeView != null && nodeView.getPendingChange() != null) {
	    			return node;
	    		} else { 
	    			return null;
	    		}
    		} else {
    			if (nodeView != null && nodeView.getPendingChange() != null && nodeView.getPendingRevision().getRequestedBy().equals(user)) {
	    			return node;
	    		} else { 
	    			return null;
	    		}
    		}
    	}
    	final List<TreeNode> children = Lists.newArrayList();
    	children.addAll(node.getChildren());
    	if (children.size() > 0) {
    		for (TreeNode child : children) {
    			if (!modifiedChildren.contains(child)) {
    				node.getChildren().remove(child);
    			}
    		}
    	}
    	if (modifiedChildren.size() > 0) {
    		return node;
    	} else {
    		return null;
    	}
	}
    
    private @Nullable TreeNode viewAll (TreeNode node, boolean withDeletetions) {
    	final List<TreeNode> nonDeletedChildren = Lists.newArrayList();
    	
    	if (node.getChildCount() > 0) {
    		for (TreeNode child : node.getChildren()) {
    			TreeNode temp = viewAll(child, withDeletetions);
    			if (temp != null) {
    				nonDeletedChildren.add(temp);
    			} 
    		}
    	} else {
    		final @Nullable NamePartView nodeView = (NamePartView) node.getData();
    		if (!withDeletetions) {
	    		if (nodeView != null && !nodeView.isDeleted()) {
	    			return node;
	    		} else { 
	    			return null;
	    		}
    		} else if (withDeletetions) {
    			if (nodeView != null) {
	    			return node;
	    		} else { 
	    			return null;
	    		}
    		} else {
    			throw new IllegalStateException();
    		}
    	}
    	final List<TreeNode> children = Lists.newArrayList();
    	children.addAll(node.getChildren());
    	if (children.size() > 0) {
    		for (TreeNode child : children) {
    			if (!nonDeletedChildren.contains(child)) {
    				node.getChildren().remove(child);
    			}
    		}
    	}
    	if (nonDeletedChildren.size() > 0) {
    		return node;
    	} else {
    		return null;
    	}
    }
    	

    private void showMessage(FacesMessage.Severity severity, String summary, String message) {
        final FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(severity, summary, message));
    }
}
