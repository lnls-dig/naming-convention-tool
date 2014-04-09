package org.openepics.names.ui.common;

import com.google.common.collect.Lists;
import org.openepics.names.util.As;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A filter that transforms a TreeNode tree into a new filtered tree based on acceptance criteria for node's data.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
*/
public abstract class TreeFilter<T> {

    /**
     * True if the filter should accept the node based on its data.
     *
     * @param nodeData the node's data
     */
    protected abstract boolean accepts(@Nullable T nodeData);

    /**
     * Takes the input tree and produces a new filtered tree.
     *
     * @param node the root node of the tree to be filtered
     * @return the root node of the new filtered tree. Subtrees with no accepted elements are culled. If no elements are
     * accepted, null will be returned.
     */
    public @Nullable TreeNode apply(TreeNode node) {
        final List<TreeNode> childImages = Lists.newArrayList();
        for (TreeNode child : node.getChildren()) {
            final @Nullable TreeNode childImage = apply(child);
            if (childImage != null) {
                childImages.add(childImage);
            }
        }

        final @Nullable T nodeData = (T) node.getData();
        if (!childImages.isEmpty() || (nodeData != null && accepts(As.notNull(nodeData)))) {
            final TreeNode result = new DefaultTreeNode(nodeData, null);
            result.setExpanded(true);
            for (TreeNode childImage : childImages) {
                result.getChildren().add(childImage);
                childImage.setParent(result);
            }
            return result;
        } else {
            return null;
        }
    }
}
