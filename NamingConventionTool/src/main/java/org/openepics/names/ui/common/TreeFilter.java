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
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import javax.annotation.Nullable;
import java.util.List;


/**
 * A filter that transforms a TreeNode tree into a new filtered tree based on acceptance criteria for node's data.
 *
 * @author Marko Kolar  
*/
public abstract class TreeFilter<T> {

    /**
     * @return True if the filter should accept the node based on its data.
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
