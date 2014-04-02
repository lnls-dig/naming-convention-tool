package org.openepics.names.ui.common;

/**
 * A wrapper for TreeNode's data with additional information on whether the node is affected by an operation.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public class OperationView<T> {

    private final T data;
    private final boolean isAffected;

    public OperationView(T data, boolean isAffected) {
        this.data = data;
        this.isAffected = isAffected;
    }

    public T getData() { return data; }
    public boolean isAffected() { return isAffected; }
}
