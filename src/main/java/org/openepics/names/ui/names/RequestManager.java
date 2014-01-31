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
import com.google.common.base.Joiner;
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
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartType;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.ui.ViewFactory;
import org.openepics.names.ui.names.NamePartView.Change;

/**
 * Manages Change Requests (backing bean for request-sub.xhtml)
 *
 * @author Vasu V <vuppala@frib.msu.org>
 */
@ManagedBean
@ViewScoped
public class RequestManager implements Serializable {

    @Inject private RestrictedNamePartService namePartService;

    private List<NamePartView> validNames;
    private NamePartView selectedName;
    private List<NamePartView> filteredNames;
    private List<NamePartView> historyEvents;
    private boolean myRequest = false;

    // Input parameters from input page
    private NameCategory category;
    private String newParentID;
    private String newCode;
    private String newDescription;
    private String newComment;

    private List<NamePartView> parentCandidates;

    @PostConstruct
    public void init() {
        final String option = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("option");

        final List<NamePart> validNameParts;
        if (option == null) {
            validNameParts = namePartService.approvedOrPendingNames();
            myRequest = false;
        } else if ("user".equals(option)) {
            validNameParts = namePartService.namesWithChangesProposedByCurrentUser();
            myRequest = true;
        } else {
            validNameParts = Lists.newArrayList();
        }

        validNames = Lists.newArrayList(Lists.transform(validNameParts, new Function<NamePart, NamePartView>() {
            @Override public NamePartView apply(NamePart namePart) {
                return ViewFactory.getView(namePart);
            }
        }));

        newCode = newDescription = newComment = null;
        category = null;
        newParentID = null;
        selectedName = (validNames == null || validNames.size() == 0) ? null : validNames.get(0);
    }

    public void onModify() {
        try {
            final NamePartRevision newRequest = namePartService.modifyNamePart(selectedName.getNamePart(), newCode, newDescription, newComment);
            showMessage(FacesMessage.SEVERITY_INFO, "Your request was successfully submitted.", "Request Number: " + newRequest.getId());
        } finally {
            init();
        }
    }

    public void onAdd() {
        try {
            final NamePartType namePartType = namePartService.nameHierarchy().getSectionLevels().contains(category) ? NamePartType.SECTION : NamePartType.DEVICE_TYPE;
            final NamePartRevision newRequest = namePartService.addNamePart(newCode, newDescription, namePartType, namePartService.namePartWithId(newParentID), newComment);
            showMessage(FacesMessage.SEVERITY_INFO, "Your request was successfully submitted.", "Request Number: " + newRequest.getId());
        } finally {
            init();
        }
    }

    /*
     * Has the selectedName been processed?
     */
    public boolean selectedEventProcessed() {
        return selectedName == null ? false : selectedName.getPendingChange() == null;
    }

    public String getRequestType(NamePartView req) {
        final Change change = req.getPendingChange();
        if (change instanceof NamePartView.AddChange) return "Add request";
        else if (change instanceof NamePartView.ModifyChange) return "Modify request";
        else if (change instanceof NamePartView.DeleteChange) return "Delete request";
        else throw new IllegalStateException();
    }

    public String getNewPath(NamePartView req) {
        StringBuilder outputStr = new StringBuilder();

        Joiner.on(" ▸ ").appendTo(outputStr, req.getNamePath().subList(0, req.getNamePath().size() - 1));
        if(outputStr.length() > 0) outputStr.append(" ▸ ");

        Change change = req.getPendingChange();
        if(change instanceof NamePartView.ModifyChange)
            outputStr.append(((NamePartView.ModifyChange)change).getNewName());
        else
            outputStr.append(req.getName());

        return outputStr.toString();
    }

    public String getNewFullPath(NamePartView req) {
        StringBuilder outputStr = new StringBuilder();

        Joiner.on(" ▸ ").appendTo(outputStr, req.getFullNamePath().subList(0, req.getFullNamePath().size() - 1));
        if(outputStr.length() > 0) outputStr.append(" ▸ ");

        Change change = req.getPendingChange();
        if(change instanceof NamePartView.ModifyChange)
            outputStr.append(((NamePartView.ModifyChange)change).getNewFullName());
        else
            outputStr.append(req.getFullName());

        return outputStr.toString();
    }

    public boolean isModified(NamePartView req) {
        return req.getPendingChange() instanceof NamePartView.ModifyChange;
    }

    public String getNameClass(NamePartView req) {
        Change change = req.getPendingChange();
        if(change == null) {
            if(req.isDeleted()) return "Delete-Approved";
            return "Insert-Approved";
        }

        StringBuilder ret = new StringBuilder();
        if(change instanceof NamePartView.AddChange) ret.append("Insert-");
        else if(change instanceof NamePartView.ModifyChange) ret.append("Modify-");
        else if(change instanceof NamePartView.DeleteChange) ret.append("Delete-");
        // ERROR!!!! unknown class
        else return "unknown";

        switch(change.getStatus()) {
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
            final NamePartRevision newRequest = namePartService.deleteNamePart(selectedName.getNamePart(), newComment);
            showMessage(FacesMessage.SEVERITY_INFO, "Your request was successfully submitted.", "Request Number: " + newRequest.getId());
        } finally {
            init();
        }
    }

    public void onCancel() {
        try {
            namePartService.cancelChangesForNamePart(selectedName.getNamePart(), newComment);
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
        historyEvents = Lists.newArrayList(Lists.transform(namePartService.revisions(selectedName.getNamePart()), new Function<NamePartRevision, NamePartView>() {
            @Override public NamePartView apply(NamePartRevision revision) {
                return ViewFactory.getView(revision);
            }
        }));
    }

    public List<NamePartView> getValidNames() {
        return validNames;
    }

    public NamePartView getSelectedName() {
        return selectedName;
    }

    public void setSelectedName(NamePartView selectedName) {
        this.selectedName = selectedName;
    }

    public List<NamePartView> getFilteredNames() {
        return filteredNames;
    }

    public void setFilteredNames(List<NamePartView> filteredNames) {
        this.filteredNames = filteredNames;
    }

    public NameCategory getCategory() {
        return category;
    }

    public void setCategory(NameCategory category) {
        this.category = category;
    }

    public String getNewParentID() {
        return newParentID;
    }

    public void setNewParentID(String newParentID) {
        this.newParentID = newParentID;
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

    public boolean isMyRequest() {
        return myRequest;
    }

    public List<NamePartView> getHistoryEvents() {
        return historyEvents;
    }

    public List<NamePartView> getParentCandidates() {
        return parentCandidates;
    }

    public void loadParentCandidates() {
        if (category != null) {
            final @Nullable NameCategory parentCategory = getParentCategory(category);
            if (parentCategory != null) {
                parentCandidates = Lists.transform(namePartService.namesWithCategory(parentCategory), new Function<NamePart, NamePartView>() {
                    @Override public NamePartView apply(NamePart namePart) { return ViewFactory.getView(namePart); }
                });
            } else {
                parentCandidates = ImmutableList.<NamePartView>of();
            }
        }
    }

    public boolean isParentSelectable() {
        return category != null && getParentCategory(category) != null;
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
    }

    private class NamePartRevisionTree {
        private class TreeNode {
            private final NamePartRevisionPair node;
            private final List<TreeNode> children;

            private TreeNode(NamePartRevisionPair pair) {
                node = pair;
                children = new ArrayList<>();
            }
        }

        private final TreeNode root;
        private final HashMap<String, TreeNode> inventory;

        private NamePartRevisionTree() {
            root = new TreeNode(null);
            inventory = new HashMap<>();
        }

        private boolean hasNode(NamePartRevisionPair pair) {
            return inventory.containsKey(pair.uuid);
        }

        private void addChildToParent(@Nullable String parentUuid, NamePartRevisionPair pair) {
            final TreeNode newNode = new TreeNode(pair);
            if(parentUuid != null)
                inventory.get(parentUuid).children.add(newNode);
            else
                root.children.add(newNode);
            inventory.put(pair.uuid, newNode);
        }

        private List<NamePartView> asList() {
            return asList(new ArrayList<NamePartView>(), root);
        }

        private List<NamePartView> asList(List<NamePartView> resultList, TreeNode node) {
            for(TreeNode child : node.children) {
                resultList.add(ViewFactory.getView(child.node.approved, child.node.pending));
                asList(resultList, child);
            }
            return resultList;
        }
    }

    public List<NamePartView> namePartApprovalTree(List<NamePartRevision> approved, List<NamePartRevision> pending) {
        final Map<String, NamePartRevisionPair> completeNamePartList = new HashMap<>();

        for(NamePartRevision approvedNPR : approved)
            completeNamePartList.put(approvedNPR.getNamePart().getUuid(), new NamePartRevisionPair().approved(approvedNPR));

        for(NamePartRevision pendingNPR : pending) {
            final NamePartRevisionPair pair = completeNamePartList.get(pendingNPR.getNamePart().getUuid());
            if(pair != null)
                pair.pending(pendingNPR);
            else
                completeNamePartList.put(pendingNPR.getNamePart().getUuid(), new NamePartRevisionPair().pending(pendingNPR));
        }

        NamePartRevisionTree nprt = new NamePartRevisionTree();
        for(NamePartRevisionPair pair : completeNamePartList.values())
            addNamePartRevisionNode(nprt, pair, completeNamePartList);

        return nprt.asList();
    }

    private void addNamePartRevisionNode(NamePartRevisionTree nprt, NamePartRevisionPair pair, Map<String, NamePartRevisionPair> allPairs ) {
        if(!nprt.hasNode(pair)) {
            String parentId;
            if(pair.approved != null)
                parentId = pair.approved.getParent() != null ? pair.approved.getParent().getUuid() : null;
            else
                parentId = pair.pending.getParent() != null ? pair.pending.getParent().getUuid() : null;
            if(parentId == null) {
                nprt.addChildToParent(null, pair);
            } else {
                // adding existing parent is a NOP
                addNamePartRevisionNode(nprt, allPairs.get(parentId), allPairs);
                nprt.addChildToParent(parentId, pair);
            }
        }
    }

}
