package org.openepics.names.ui.common;

import com.google.common.collect.Lists;
import org.openepics.names.util.As;
import org.openepics.names.util.UnhandledCaseException;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.annotation.Nullable;
import java.util.List;

/**
* @author Marko Kolar <marko.kolar@cosylab.com>
*/
public abstract class OperationsTreePreview<T> {
    protected abstract boolean isAffected(T nodeView);
    protected abstract boolean autoSelectChildren(T nodeView);
    protected abstract boolean ignoreSelectedChildren(T nodeView, boolean isSelected);

    public @Nullable TreeNode apply(@Nullable TreeNode node) {
        return node != null ? view(node, SelectionMode.MANUAL) : null;
    }

    private @Nullable TreeNode view(TreeNode node, SelectionMode selectionMode) {
        final @Nullable T data = (T) node.getData();

        final SelectionMode childrenSelectionMode;
        if (selectionMode == SelectionMode.AUTO) {
            childrenSelectionMode = SelectionMode.AUTO;
        } else if (selectionMode == SelectionMode.MANUAL) {
            if (node.isSelected() && data != null && autoSelectChildren(As.notNull(data))) {
                childrenSelectionMode = SelectionMode.AUTO;
            } else if (data != null && ignoreSelectedChildren(As.notNull(data), node.isSelected())) {
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

        final boolean affectNode = (selectionMode == SelectionMode.AUTO || (selectionMode == SelectionMode.MANUAL && node.isSelected())) && isAffected(data);
        if (affectNode || !childViews.isEmpty() ) {
            final TreeNode result = new DefaultTreeNode(new OperationView<T>(data, affectNode), null);
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
