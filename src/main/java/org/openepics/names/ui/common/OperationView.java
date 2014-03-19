package org.openepics.names.ui.common;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import java.util.List;

/**
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public class OperationView<T> {

    private final T data;
    private final boolean isAffected;

    public OperationView(T data, boolean isAffected) {
        this.data = data;
        this.isAffected = isAffected;
    }

    public T getNamePartView() { return data; }
    public boolean isAffected() { return isAffected; }
}
