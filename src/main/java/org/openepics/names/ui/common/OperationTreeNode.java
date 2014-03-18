package org.openepics.names.ui.common;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import java.util.List;

/**
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public class OperationTreeNode extends DefaultTreeNode {

    private final boolean isAffected;

    public OperationTreeNode(Object data, boolean isAffected) {
        super(data, null);
        this.isAffected = isAffected;
    }

    public boolean isAffected() { return isAffected; }
}
