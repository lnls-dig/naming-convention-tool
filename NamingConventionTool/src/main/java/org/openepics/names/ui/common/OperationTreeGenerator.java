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
package org.openepics.names.ui.common;

import com.google.common.collect.Lists;
import org.openepics.names.util.As;
import org.openepics.names.util.UnhandledCaseException;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A generator that takes a TreeNode tree as input and, based on the selected nodes, generates a new tree with
 * additional information on what nodes will be affected by an operation. This information is contained in
 * OperationView objects that wrap the original tree nodes' data.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public abstract class OperationTreeGenerator<T> {
    /**
     * True if the selected element can be affected by the operation.
     *
     * @param element the selected element
     */
    protected abstract boolean canAffect(T element);

    /**
     * True if for the selected element the tree of its children are also all treated as selected.
     *
     * @param element the selected element
     */
    protected abstract boolean autoSelectChildren(T element);

    /**
     * True if for the given element its selected children are ignored (treated as if they were not selected)
     *
     * @param element the element
     * @param isSelected is the element selected
     */
    protected abstract boolean ignoreSelectedChildren(T element, boolean isSelected);

    /**
     * Takes the tree the operation is acting on and produces a new tree of OperationView objects that describe which
     * elements in the input tree are affected by the operation.
     *
     * @param node the root node of the tree the operation is acting on
     * @return the root node of a new tree containing OperationViews that describe if an element is affected by the
     * operation. Subtrees with no affected elements are culled. If no elements are affected by the operation, null will
     * be returned.
     */
    public @Nullable TreeNode apply(@Nullable TreeNode node) {
        return node != null ? view(node, SelectionMode.MANUAL) : null;
    }

    private @Nullable TreeNode view(TreeNode node, SelectionMode selectionMode) {
        final @Nullable T element = (T) node.getData();

        final SelectionMode childrenSelectionMode;
        if (selectionMode == SelectionMode.AUTO) {
            childrenSelectionMode = SelectionMode.AUTO;
        } else if (selectionMode == SelectionMode.MANUAL) {
            if (node.isSelected() && element != null && autoSelectChildren(As.notNull(element))) {
                childrenSelectionMode = SelectionMode.AUTO;
            } else if (element != null && ignoreSelectedChildren(As.notNull(element), node.isSelected())) {
                childrenSelectionMode = SelectionMode.DISABLED;
            } else {
                childrenSelectionMode = SelectionMode.MANUAL;
            }
        } else if (selectionMode == SelectionMode.DISABLED) {
            childrenSelectionMode = SelectionMode.DISABLED;
        } else {
            throw new UnhandledCaseException();
        }

        final List<TreeNode> childViews = Lists.newArrayList();
        for (TreeNode child : node.getChildren()) {
            final TreeNode childView = view(child, childrenSelectionMode);
            if (childView != null) {
                childViews.add(childView);
            }
        }

        final boolean affectNode = (selectionMode == SelectionMode.AUTO || (selectionMode == SelectionMode.MANUAL && node.isSelected())) && canAffect(element);
        if (affectNode || !childViews.isEmpty() ) {
            final TreeNode result = new DefaultTreeNode(new OperationView<T>(element, affectNode), null);
            result.setExpanded(true);
            for (TreeNode childView : childViews) {
                result.getChildren().add(childView);
                childView.setParent(result);
            }
            return result;
        } else {
            return null;
        }
    }

    private enum SelectionMode { MANUAL, AUTO, DISABLED }
}
