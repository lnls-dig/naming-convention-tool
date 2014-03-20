package org.openepics.names.ui.common;

import com.google.common.collect.Lists;
import org.openepics.names.ui.parts.NamePartView;
import org.openepics.names.util.As;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.annotation.Nullable;
import java.util.List;

/**
* @author Marko Kolar <marko.kolar@cosylab.com>
*/
public abstract class TreeViewFilter<T> {

    protected abstract boolean addToTreeView(@Nullable T nodeView);

    public TreeNode apply(TreeNode node) {
        final List<TreeNode> childNodes = Lists.newArrayList();
        for (TreeNode child : node.getChildren()) {
            final TreeNode childView = apply(child);
            if (childView != null) {
                childNodes.add(childView);
            }
        }

        final @Nullable T nodeView = (T) node.getData();
        if (!childNodes.isEmpty() || (nodeView != null && addToTreeView(As.notNull(nodeView)))) {
            final TreeNode result = new DefaultTreeNode(nodeView, null);
            result.setExpanded(true);
            for (TreeNode childView : childNodes) {
                result.getChildren().add(childView);
                childView.setParent(result);
            }
            return result;
        } else {
            return null;
        }
    }
}
