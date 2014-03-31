package org.openepics.names.ui.common;

import com.google.common.collect.Lists;
import org.openepics.names.util.As;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.annotation.Nullable;
import java.util.List;

/**
* @author Marko Kolar <marko.kolar@cosylab.com>
*/
public abstract class TreeViewFilter<T> {

    protected abstract boolean accepts(@Nullable T nodeView);

    public TreeNode apply(TreeNode node) {
        final List<TreeNode> childViews = Lists.newArrayList();
        for (TreeNode child : node.getChildren()) {
            final TreeNode childView = apply(child);
            if (childView != null) {
                childViews.add(childView);
            }
        }

        final @Nullable T nodeView = (T) node.getData();
        if (!childViews.isEmpty() || (nodeView != null && accepts(As.notNull(nodeView)))) {
            final TreeNode result = new DefaultTreeNode(nodeView, null);
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
}
