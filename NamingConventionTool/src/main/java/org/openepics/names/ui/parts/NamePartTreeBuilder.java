/*-
* Copyright (c) 2014 European Spallation Source
* Copyright (c) 2014 Cosylab d.d.
*
* This file is part of Naming Service.
* Naming Service is free software: you can redistribute it and/or modify it under
* the terms of the GNU General Public License as published by the Free
* Software Foundation, either version 2 of the License, or any newer version.
*
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
* FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
* more details.
*
* You should have received a copy of the GNU General Public License along with
* this program. If not, see https://www.gnu.org/licenses/gpl-2.0.txt
*/
package org.openepics.names.ui.parts;

import com.google.common.collect.Lists;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.services.views.NamePartView;
import org.openepics.names.ui.common.AlphanumComparator;
import org.openepics.names.ui.common.ViewFactory;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.annotation.Nullable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import java.util.*;

/**
 * A utility bean for building JSF TreeNode trees from the NamePart hierarchy.
 *
 * @author mvitorovic
 */
@ManagedBean
@ViewScoped
public class NamePartTreeBuilder {

    @Inject private ViewFactory viewFactory;

    /**
     * Builds a tree of NamePartViews from the provided lists of revisions.
     *
     * @param approved the list of approved NamePart revisions
     * @param pending the list of pending NamePart revisions
     * @param expandedTree true if the constructed tree should be fully expanded
     * @return the root node of the tree
     */
    public TreeNode newNamePartTree(List<NamePartRevision> approved, List<NamePartRevision> pending, boolean expandedTree) {
        return newNamePartTree(approved, pending, expandedTree, 0, null);
    }

    /**
     * Builds a tree of NamePartViews from the provided lists of revisions.
     *
     * @param approved the list of approved NamePart revisions
     * @param pending the list of pending NamePart revisions
     * @param expandedTree true if the constructed tree should be fully expanded
     * @param selectableLevel the depth level starting from 0 below which (inclusively) node selection is made possible
     * @return the root node of the tree
     */
    public TreeNode newNamePartTree(List<NamePartRevision> approved, List<NamePartRevision> pending, boolean expandedTree, int selectableLevel) {
        return newNamePartTree(approved, pending, expandedTree, selectableLevel, null);
    }

    /**
     * Builds a tree of NamePartViews from the provided lists of revisions.
     *
     * @param approved the list of approved NamePart revisions
     * @param pending the list of pending NamePart revisions
     * @param expandedTree true if the constructed tree should be fully expanded
     * @param selectableLevel the depth level starting from 0 below which (inclusively) node selection is made possible
     * @param selected the NamePart in the resulting tree that should be preselected and the part of the tree containing
     * expanded
     * @return the root node of the tree
     */
    public TreeNode newNamePartTree(List<NamePartRevision> approved, List<NamePartRevision> pending, boolean expandedTree, int selectableLevel, NamePart selected) {
        final Map<UUID, NamePartRevisionPair> completeNamePartList = new HashMap<>();

        for (NamePartRevision approvedNPR : approved) {
            completeNamePartList.put(approvedNPR.getNamePart().getUuid(), new NamePartRevisionPair().approved(approvedNPR));
        }

        for (NamePartRevision pendingNPR : pending) {
            final NamePartRevisionPair pair = completeNamePartList.get(pendingNPR.getNamePart().getUuid());
            if (pair != null) {
                pair.pending(pendingNPR);
            } else {
                completeNamePartList.put(pendingNPR.getNamePart().getUuid(), new NamePartRevisionPair().pending(pendingNPR));
            }
        }

        final NamePartRevisionTree nprt = new NamePartRevisionTree(expandedTree, selectableLevel, selected);
        for (NamePartRevisionPair pair : completeNamePartList.values()) {
            addNamePartRevisionNode(nprt, pair, completeNamePartList);
        }

        return nprt.asViewTree();
    }

    private void addNamePartRevisionNode(NamePartRevisionTree nprt, NamePartRevisionPair pair, Map<UUID, NamePartRevisionPair> allPairs) {
        if (!nprt.hasNode(pair)) {
            final UUID parentId = pair.getParentUuid();
            if (parentId == null) {
                nprt.addChildToParent(null, pair);
            } else {
                addNamePartRevisionNode(nprt, allPairs.get(parentId), allPairs);
                nprt.addChildToParent(parentId, pair);
            }
        }
    }

    private class NamePartRevisionPair {
        private UUID uuid;
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

        private @Nullable UUID getParentUuid() {
            if (approved != null) {
                return approved.getParent() != null ? approved.getParent().getUuid() : null;
            } else {
                return pending.getParent() != null ? pending.getParent().getUuid() : null;
            }
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
        private final HashMap<UUID, NamePartRevisionTreeNode> inventory;
        private final boolean expandedTree;
        private final int selectableLevel;
        private final NamePart selectedNamePart;
        private TreeNode selectedTreeNode;

        private NamePartRevisionTree(boolean expandedTree, int selectableLevel, NamePart selected) {
            root = new NamePartRevisionTreeNode(null);
            inventory = new HashMap<>();
            this.expandedTree = expandedTree;
            this.selectableLevel = selectableLevel;
            this.selectedNamePart = selected;
        }

        private boolean hasNode(NamePartRevisionPair pair) {
            return inventory.containsKey(pair.uuid);
        }

        private void addChildToParent(@Nullable UUID parentUuid, NamePartRevisionPair pair) {
            final NamePartRevisionTreeNode newNode = new NamePartRevisionTreeNode(pair);
            if (parentUuid != null) {
                inventory.get(parentUuid).children.add(newNode);
            } else {
                root.children.add(newNode);
            }
            inventory.put(pair.uuid, newNode);
        }

        private TreeNode asViewTree() {
            TreeNode treeRoot = asViewTree(new DefaultTreeNode(null, null), root, 0);
            TreeNode treeNode = selectedTreeNode;
            if (treeNode != null && selectedNamePart != null) {
	            while(treeNode.getParent() != null) {
	                treeNode.setExpanded(true);
	                treeNode = treeNode.getParent();
	            }
            } else if ((treeNode != null && selectedNamePart == null) || (treeNode == null && selectedNamePart != null)) {
            	throw new IllegalStateException();
            }

            return treeRoot;
        }

        private TreeNode asViewTree(TreeNode parentNode, NamePartRevisionTreeNode nprNode, int level) {
            final List<TreeNode> children = Lists.newArrayList();
            for (NamePartRevisionTreeNode child : nprNode.children) {
                final TreeNode node = new DefaultTreeNode(viewFactory.getView(child.node.approved, child.node.pending, (NamePartView) parentNode.getData()), null);
                node.setExpanded(expandedTree);
                node.setSelectable(level >= selectableLevel);
                if (isSelected(node)) {
                    node.setSelected(true);
                    selectedTreeNode = node;
                }
                asViewTree(node, child, level+1);
                children.add(node);
            }
            Collections.sort(children, new Comparator<TreeNode>() {
                @Override public int compare(TreeNode left, TreeNode right) {
                    final NamePartView leftView = (NamePartView) left.getData();
                    final NamePartView rightView = (NamePartView) right.getData();
                    final AlphanumComparator alphanumComparator = new AlphanumComparator();
                    return alphanumComparator.compare(leftView.getName(), rightView.getName());
                }
            });
            for (TreeNode child : children) {
                parentNode.getChildren().add(child);
                child.setParent(parentNode);
            }
            return parentNode;
        }

        private boolean isSelected(TreeNode node) {
            return (selectedNamePart != null) && (selectedNamePart.equals(((NamePartView)(node.getData())).getNamePart()));
        }
    }
}
