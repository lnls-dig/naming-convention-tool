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
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.openepics.names.model.NameCategory;
import org.openepics.names.model.NameHierarchy;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartType;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.ui.ViewFactory;
import org.openepics.names.ui.names.NamePartView.Change;
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

    private List<NamePartView> filteredNames;
    private List<NamePartView> historyEvents;

    private TreeNode root;
    private TreeNode[] selectedNodes;

    private String newCode;
    private String newDescription;
    private String newComment;

    @PostConstruct
    public void init() {
        final @Nullable String typeParam = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("type");
        final @Nullable String optionParam = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("option");

        final NamePartType type;
        if (typeParam == null) {
            type = NamePartType.SECTION;
        } else if (typeParam.equals("section")) {
            type = NamePartType.SECTION;
        } else if (typeParam.equals("deviceType")) {
            type = NamePartType.DEVICE_TYPE;
        } else {
            throw new IllegalStateException();
        }

        final List<NamePartRevision> approvedRevisions = ImmutableList.copyOf(Collections2.filter(namePartService.currentApprovedRevisions(true), new Predicate<NamePartRevision>() {
            @Override public boolean apply(NamePartRevision revision) { return revision.getNamePart().getNamePartType() == type; }
        }));
        final List<NamePartRevision> pendingRevisions = ImmutableList.copyOf(Collections2.filter(namePartService.currentPendingRevisions(true), new Predicate<NamePartRevision>() {
            @Override public boolean apply(NamePartRevision revision) { return revision.getNamePart().getNamePartType() == type; }
        }));

        root = namePartApprovalTree(approvedRevisions, pendingRevisions);

        newCode = newDescription = newComment = null;
    }

    public void onModify() {
        try {
            if (getSelectedName() == null) return;
            final NamePartRevision newRequest = namePartService.modifyNamePart(getSelectedName().getNamePart(), newCode, newDescription, newComment);
            showMessage(FacesMessage.SEVERITY_INFO, "Your request was successfully submitted.", "Request Number: " + newRequest.getId());
        } finally {
            init();
        }
    }

    public void onAdd() {
        try {
            final NamePartView parent = getSelectedName();
            final NamePartType namePartType = namePartService.nameHierarchy().getSectionLevels().contains(parent.getNameEvent().getNameCategory()) ? NamePartType.SECTION : NamePartType.DEVICE_TYPE;
            final NamePartRevision newRequest = namePartService.addNamePart(newCode, newDescription, namePartType, parent.getNamePart(), newComment);
            showMessage(FacesMessage.SEVERITY_INFO, "Your request was successfully submitted.", "Request Number: " + newRequest.getId());
        } finally {
            init();
        }
    }

    /*
     * Has the selectedName been processed?
     */
    public boolean selectedEventProcessed() {
        return getSelectedName() == null ? false : getSelectedName().getPendingChange() == null;
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
        if (change instanceof NamePartView.ModifyChange)
            return ((NamePartView.ModifyChange)change).getNewName();
        else
            return req.getName();

    }

    public String getNewFullName(NamePartView req) {
        final Change change = req.getPendingChange();
        if (change instanceof NamePartView.ModifyChange)
            return ((NamePartView.ModifyChange)change).getNewFullName();
        else
            return req.getFullName();
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
        // ERROR!!!! unknown class
        else return "unknown";

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
                return "unknown";
        }
        return ret.toString();
    }

    public void onDelete() {
        try {
            if (getSelectedName() == null) return;
            final NamePartRevision newRequest = namePartService.deleteNamePart(getSelectedName().getNamePart(), newComment);
            showMessage(FacesMessage.SEVERITY_INFO, "Your request was successfully submitted.", "Request Number: " + newRequest.getId());
        } finally {
            init();
        }
    }

    public void onCancel() {
        try {
            if (getSelectedName() == null) return;
            namePartService.cancelChangesForNamePart(getSelectedName().getNamePart(), newComment);
            showMessage(FacesMessage.SEVERITY_INFO, "Your request has been cancelled.", "Request Number: ");
        } finally {
            init();
        }
    }

    private void showMessage(FacesMessage.Severity severity, String summary, String message) {
        final FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(severity, summary, message));
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

    public NamePartView getSelectedName() {
        return selectedNodes == null || selectedNodes.length < 1 ? null : (NamePartView)(selectedNodes[0].getData());
    }

    public List<NamePartView> getFilteredNames() {
        return filteredNames;
    }

    public void setFilteredNames(List<NamePartView> filteredNames) {
        this.filteredNames = filteredNames;
    }

    public String getNewCode() {
        return newCode;
    }

    public void setNewCode(String newCode) {
        this.newCode = newCode;
    }

    public String getNewDescription() {
        return newDescription;
    }

    public void setNewDescription(String newDescription) {
        this.newDescription = newDescription;
    }

    public String getNewComment() {
        return newComment;
    }

    public void setNewComment(String newComment) {
        this.newComment = newComment;
    }

    public List<NamePartView> getHistoryEvents() {
        return historyEvents;
    }

    public TreeNode getRoot() {
        return root;
    }

    public TreeNode[] getSelectedNodes() {
        return selectedNodes;
    }

    public void setSelectedNodes(TreeNode[] selectedNodes) {
        this.selectedNodes = selectedNodes;
    }

    private @Nullable NameCategory getParentCategory(NameCategory nameCategory) {
        final NameHierarchy nameHierarchy = namePartService.nameHierarchy();
        final int sectionIndex = nameHierarchy.getSectionLevels().indexOf(nameCategory);
        final int deviceTypeIndex = nameHierarchy.getDeviceTypeLevels().indexOf(nameCategory);
        if (sectionIndex >= 0) {
            return sectionIndex > 0 ? nameHierarchy.getSectionLevels().get(sectionIndex - 1) : null;
        } else {
            return deviceTypeIndex > 0 ? nameHierarchy.getDeviceTypeLevels().get(deviceTypeIndex - 1) : null;
        }
    }

    private class NamePartRevisionPair {
        private String uuid;
        private NamePartRevision approved;
        private NamePartRevision pending;

        private NamePartRevisionPair approved(NamePartRevision approved) {
            this.approved = approved;
            this.uuid = approved.getNamePart().getUuid();
            return this;
        }

        private NamePartRevisionPair pending(NamePartRevision pending) {
            this.pending = pending;
            this.uuid = pending.getNamePart().getUuid();
            return this;
        }

        private @Nullable String getParentUuid() {
            if (approved != null)
                return approved.getParent() != null ? approved.getParent().getUuid() : null;
            return pending.getParent() != null ? pending.getParent().getUuid() : null;
        }
    }

    private class NamePartRevisionTree {
        private class NamePartRevisionTreeNode {
            private final NamePartRevisionPair node;
            private final List<NamePartRevisionTreeNode> children;

            private NamePartRevisionTreeNode(NamePartRevisionPair pair) {
                node = pair;
                children = new ArrayList<>();
            }
        }

        private final NamePartRevisionTreeNode root;
        private final HashMap<String, NamePartRevisionTreeNode> inventory;

        private NamePartRevisionTree() {
            root = new NamePartRevisionTreeNode(null);
            inventory = new HashMap<>();
        }

        private boolean hasNode(NamePartRevisionPair pair) {
            return inventory.containsKey(pair.uuid);
        }

        private void addChildToParent(@Nullable String parentUuid, NamePartRevisionPair pair) {
            final NamePartRevisionTreeNode newNode = new NamePartRevisionTreeNode(pair);
            if (parentUuid != null)
                inventory.get(parentUuid).children.add(newNode);
            else
                root.children.add(newNode);
            inventory.put(pair.uuid, newNode);
        }

        private TreeNode asViewTree() {
            return asViewTree(new DefaultTreeNode("root", null), root);
        }

        private TreeNode asViewTree(TreeNode parentNode, NamePartRevisionTreeNode nprNode) {
            for (NamePartRevisionTreeNode child : nprNode.children) {
                TreeNode node = new DefaultTreeNode(viewFactory.getView(child.node.approved, child.node.pending), parentNode);
                if (child.node.pending == null || (child.node.pending != null) && !child.node.pending.isDeleted())
                    asViewTree(node, child);
            }
            return parentNode;
        }
    }

    private TreeNode namePartApprovalTree(List<NamePartRevision> approved, List<NamePartRevision> pending) {
        final Map<String, NamePartRevisionPair> completeNamePartList = new HashMap<>();

        for (NamePartRevision approvedNPR : approved)
            completeNamePartList.put(approvedNPR.getNamePart().getUuid(), new NamePartRevisionPair().approved(approvedNPR));

        for (NamePartRevision pendingNPR : pending) {
            final NamePartRevisionPair pair = completeNamePartList.get(pendingNPR.getNamePart().getUuid());
            if(pair != null)
                pair.pending(pendingNPR);
            else
                completeNamePartList.put(pendingNPR.getNamePart().getUuid(), new NamePartRevisionPair().pending(pendingNPR));
        }

        NamePartRevisionTree nprt = new NamePartRevisionTree();
        for (NamePartRevisionPair pair : completeNamePartList.values())
            addNamePartRevisionNode(nprt, pair, completeNamePartList);

        return nprt.asViewTree();
    }

    private void addNamePartRevisionNode(NamePartRevisionTree nprt, NamePartRevisionPair pair, Map<String, NamePartRevisionPair> allPairs ) {
        if(!nprt.hasNode(pair)) {
            final String parentId = pair.getParentUuid();
            if (parentId == null) {
                nprt.addChildToParent(null, pair);
            } else {
                // adding existing parent is a NOP
                addNamePartRevisionNode(nprt, allPairs.get(parentId), allPairs);
                nprt.addChildToParent(parentId, pair);
            }
        }
    }
}
