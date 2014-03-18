package org.openepics.names.ui.common;

import com.google.common.collect.Lists;
import org.primefaces.model.TreeNode;

import javax.annotation.Nullable;
import java.util.List;

/**
* @author Marko Kolar <marko.kolar@cosylab.com>
*/
public abstract class OperationsTreePreview<T> {
    protected abstract boolean nodeIsAffected(@Nullable T nodeView);
    protected abstract boolean selectionModeAuto(@Nullable T nodeView);
    protected abstract boolean selectionModeDisabled(@Nullable T nodeView, boolean isSelected);

    public OperationTreeNode apply(TreeNode node) {
        return view(node, SelectionMode.MANUAL);
    }

    private OperationTreeNode view(TreeNode node, SelectionMode selectionMode) {
        final @Nullable T data = (T) node.getData();

        final SelectionMode childrenSelectionMode;
        if (selectionMode == SelectionMode.AUTO) {
            childrenSelectionMode = SelectionMode.AUTO;
        } else if (selectionMode == SelectionMode.MANUAL) {
            if (node.isSelected() && selectionModeAuto(data)) {
                childrenSelectionMode = SelectionMode.AUTO;
            } else if (selectionModeDisabled(data, node.isSelected())) {
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
            final TreeNode childView = view(child, childrenSelectionMode);
            if (childView != null) {
                childViews.add(childView);
            }
        }

        final boolean affectNode = (selectionMode == SelectionMode.AUTO || (selectionMode == SelectionMode.MANUAL && node.isSelected())) && nodeIsAffected(data);
        if (affectNode || !childViews.isEmpty() ) {
            final OperationTreeNode result = new OperationTreeNode(data, affectNode);
            result.setExpanded(true);
            for (TreeNode childView : childViews) {
                childView.setParent(result);
            }
            return result;
        } else {
            return null;
        }
    }

    private enum SelectionMode { MANUAL, AUTO, DISABLED }
}
