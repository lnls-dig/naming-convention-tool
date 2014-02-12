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
package org.openepics.names.ui.names;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.ui.NamePartTreeBuilder;
import org.openepics.names.ui.ViewFactory;
import org.openepics.names.ui.names.NamePartView.Change;
import org.openepics.names.ui.names.NamePartView.DeleteChange;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 * Manages Change Requests (backing bean for request-sub.xhtml)
 *
 * @author Vasu V <vuppala@frib.msu.org>
 */
@ManagedBean
@ViewScoped
public class RequestManager implements Serializable {

    @Inject private RestrictedNamePartService namePartService;
    @Inject private ViewFactory viewFactory;
    @Inject private NamePartTreeBuilder namePartTreeBuilder;

    private List<NamePartView> historyEvents;

    private TreeNode root;
    private TreeNode[] selectedNodes;

    private TreeNode deleteView;
    private TreeNode cancelView;

    private String newCode;
    private String newDescription;
    private String newComment;

    private NamePartType namePartType;

    @PostConstruct
    public void init() {
        final @Nullable String typeParam = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("type");

        if (typeParam == null) {
            namePartType = NamePartType.SECTION;
        } else if (typeParam.equals("section")) {
            namePartType = NamePartType.SECTION;
        } else if (typeParam.equals("deviceType")) {
            namePartType = NamePartType.DEVICE_TYPE;
        } else {
            throw new IllegalStateException();
        }

        final List<NamePartRevision> approvedRevisions = ImmutableList.copyOf(Collections2.filter(namePartService.currentApprovedRevisions(true), new Predicate<NamePartRevision>() {
            @Override public boolean apply(NamePartRevision revision) { return revision.getNamePart().getNamePartType() == namePartType; }
        }));
        final List<NamePartRevision> pendingRevisions = ImmutableList.copyOf(Collections2.filter(namePartService.currentPendingRevisions(true), new Predicate<NamePartRevision>() {
            @Override public boolean apply(NamePartRevision revision) { return revision.getNamePart().getNamePartType() == namePartType; }
        }));

        root = namePartTreeBuilder.namePartApprovalTree(approvedRevisions, pendingRevisions, true);

        newCode = newDescription = newComment = null;
        selectedNodes = new TreeNode[0];
        prepareDeleteView();
        prepareCancelView();
    }

    public void onAdd() {
        try {
            final NamePartView parent = getSelectedName();
            final NamePartRevision newRequest = namePartService.addNamePart(newCode, newDescription, namePartType, parent.getNamePart(), newComment);
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
            for (TreeNode nodeToDelete : normalizedSelection()) {
                namePartService.deleteNamePart(((NamePartView)(nodeToDelete.getData())).getNamePart(), newComment);
            }
            showMessage(FacesMessage.SEVERITY_INFO, "Success", "The data you requested was successfully deleted.");
        } finally {
            init();
        }
    }

    public void onCancel() {
        try {
            namePartService.cancelChangesForNamePart(getSelectedName().getNamePart(), newComment);
            showMessage(FacesMessage.SEVERITY_INFO, "Your request has been cancelled.", "Request Number: ");
        } finally {
            init();
        }
    }

    public void onApprove() {
        // TODO
        try {
            namePartService.approveNamePartRevisions(
                    Lists.transform(null, new Function<NamePartView, NamePartRevision>() {
                        @Override public NamePartRevision apply(NamePartView namePart) { return namePart.getPendingRevision(); }
                    }), newComment);
            showMessage(FacesMessage.SEVERITY_INFO, "All selected requests were successfully approved.", " ");
        } finally {
            init();
        }
    }

    public void onReject() {
        // TODO
        try {
            namePartService.rejectNamePartRevisions(
                    Lists.transform(null, new Function<NamePartView, NamePartRevision>() {
                        @Override public NamePartRevision apply(NamePartView namePart) { return namePart.getPendingRevision(); }
                    }), newComment);
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
        if (this.selectedNodes != selectedNodes) {
            this.selectedNodes = selectedNodes;
            prepareDeleteView();
            prepareCancelView();
        }
    }

    public TreeNode getDeleteView() { return deleteView; }

    public TreeNode getCancelView() { return cancelView; }

    public boolean canAdd() { return selectedNodes.length == 1 && !((NamePartView) selectedNodes[0].getData()).getPendingOrElseCurrentRevision().isDeleted(); }

    public boolean canDelete() { return deleteView.getChildCount() > 0; }

    public boolean canModify() { return selectedNodes.length == 1 && !((NamePartView) selectedNodes[0].getData()).getPendingOrElseCurrentRevision().isDeleted(); }

    public boolean canCancel() { return cancelView != null; }

    public void prepareDeleteView() {
        final List<TreeNode> normalizedSelection = normalizedSelection();
        final HashMap<String, TreeNode> processed = new HashMap<>();
        deleteView = new DefaultTreeNode("root", null);
        for (TreeNode selected : normalizedSelection) {
            addToDeleteView(selected, processed);
        }
    }

    public void prepareCancelView() {
        cancelView = cancelView(root, false);
    }

    /**
     * A list of all delete candidates which do not have an ancestor selected as well or are not already proposed
     * for deletion.
     * @return List of TreeNode.
     */
    private List<TreeNode> normalizedSelection() {
        final List<TreeNode> normalizedSelection = new ArrayList<>();
        for (TreeNode node : selectedNodes) {
            final Change change = ((NamePartView)node.getData()).getPendingChange();
            if (!(change instanceof DeleteChange) && !isAncestorSelected(node)) normalizedSelection.add(node);
        }
        return normalizedSelection;
    }

    private boolean isAncestorSelected(TreeNode node) {
        TreeNode parent = node.getParent();
        while (parent != null) {
            if (isSelected(parent)) return true;
            parent = parent.getParent();
        }
        return false;
    }

    private boolean isSelected(TreeNode node) {
        for (TreeNode searchNode : selectedNodes) {
            if (node == searchNode) return true;
        }
        return false;
    }

    private TreeNode addToDeleteView(TreeNode mainTableNode, HashMap<String, TreeNode> processedDialogNodes) {
        final TreeNode mainTableNodeParent = mainTableNode.getParent();

        final TreeNode dialogParent;

        if (!(mainTableNodeParent.getData() instanceof NamePartView)) {
            dialogParent = deleteView;
        } else {
            final String parentUuid = ((NamePartView)(mainTableNodeParent.getData())).getNamePart().getUuid();
            dialogParent = processedDialogNodes.containsKey(parentUuid) ? processedDialogNodes.get(parentUuid) : addToDeleteView(mainTableNodeParent, processedDialogNodes);
        }

        NamePartView npv = (NamePartView)(mainTableNode.getData());
        final TreeNode dialogDeleteNode = new DefaultTreeNode(new DeleteNamePartView(npv.getName(), npv.getFullName(), mainTableNode.isSelected()), dialogParent);
        dialogDeleteNode.setExpanded(true);
        processedDialogNodes.put(npv.getNamePart().getUuid(), dialogDeleteNode);

        return dialogDeleteNode;
    }

    private void showMessage(FacesMessage.Severity severity, String summary, String message) {
        final FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(severity, summary, message));
    }

    private @Nullable TreeNode cancelView(TreeNode node, boolean cancelAny) {
        final @Nullable NamePartView nodeView = (NamePartView) node.getData();
        final boolean cancelNode = nodeView != null && (cancelAny || isSelected(node)) && (nodeView.getPendingChange() != null);
        final boolean childrenCancelAny = cancelAny || (nodeView != null && isSelected(node) && !(nodeView.getPendingChange() instanceof NamePartView.ModifyChange));

        final List<TreeNode> childViews = Lists.newArrayList();
        for (TreeNode child : node.getChildren()) {
            final TreeNode childView = cancelView(child, childrenCancelAny);
            if (childView != null) {
                childViews.add(childView);
            }
        }

        if (cancelNode || !childViews.isEmpty()) {
            final TreeNode result = new DefaultTreeNode(nodeView != null ? new DeleteNamePartView(nodeView.getName(), nodeView.getFullName(), cancelNode) : null, null);
            result.setExpanded(true);
            for (TreeNode childView : childViews) {
                childView.setParent(result);
            }
            return result;
        } else {
            return null;
        }
    }
}
