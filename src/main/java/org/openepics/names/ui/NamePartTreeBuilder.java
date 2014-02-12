/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openepics.names.ui;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.ui.names.NamePartView;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 *
 * @author mvitorovic
 */
@ManagedBean
@ViewScoped
public class NamePartTreeBuilder {

    @Inject private ViewFactory viewFactory;

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
        private final boolean expandedTree;
        private final int selectableLevel;
        private final NamePart selected;

        private NamePartRevisionTree(boolean expandedTree, int selectableLevel, NamePart selected) {
            root = new NamePartRevisionTreeNode(null);
            inventory = new HashMap<>();
            this.expandedTree = expandedTree;
            this.selectableLevel = selectableLevel;
            this.selected = selected;
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
            return asViewTree(new DefaultTreeNode(null, null), root, 0);
        }

        private TreeNode asViewTree(TreeNode parentNode, NamePartRevisionTreeNode nprNode, int level) {
            final List<TreeNode> children = Lists.newArrayList();
            for (NamePartRevisionTreeNode child : nprNode.children) {
                final TreeNode node = new DefaultTreeNode(viewFactory.getView(child.node.approved, child.node.pending), null);
                node.setExpanded(expandedTree);
                node.setSelectable(level >= selectableLevel);
                if (isSelected(node)) selectNode(node);
                asViewTree(node, child, level+1);
                children.add(node);
            }
            Collections.sort(children, new Comparator<TreeNode>() {
                @Override public int compare(TreeNode left, TreeNode right) {
                    final NamePartView leftView = (NamePartView) left.getData();
                    final NamePartView rightView = (NamePartView) right.getData();
                    return leftView.getFullName().compareTo(rightView.getFullName());
                }
            });
            for (TreeNode child : children) {
                child.setParent(parentNode);
            }
            return parentNode;
        }

        private boolean isSelected(TreeNode node) {
            return (selected != null) && (selected.equals(((NamePartView)(node.getData())).getNamePart()));
        }

        private void selectNode(TreeNode node) {
            node.setSelected(true);
            TreeNode treeNode = node;
            while(treeNode.getParent() != null) {
                treeNode.setExpanded(true);
                treeNode = treeNode.getParent();
            }
        }
    }

    public TreeNode namePartApprovalTree(List<NamePartRevision> approved, List<NamePartRevision> pending, boolean expandedTree) {
        return namePartApprovalTree(approved, pending, expandedTree, 0, null);
    }

    public TreeNode namePartApprovalTree(List<NamePartRevision> approved, List<NamePartRevision> pending, boolean expandedTree, int selectableLevel) {
        return namePartApprovalTree(approved, pending, expandedTree, selectableLevel, null);
    }

    public TreeNode namePartApprovalTree(List<NamePartRevision> approved, List<NamePartRevision> pending, boolean expandedTree, int selectableLevel, NamePart selected) {
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

        NamePartRevisionTree nprt = new NamePartRevisionTree(expandedTree, selectableLevel, selected);
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
