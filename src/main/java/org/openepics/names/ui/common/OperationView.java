package org.openepics.names.ui.common;

/**
 * A wrapper for TreeNode's data with additional information on whether the node is affected by an operation.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public class OperationView<T> {

    private final T data;
    private final boolean isAffected;

    /**
     * @param data the element possibly affected by the operation
     * @param isAffected true if the element is affected
     */
    public OperationView(T data, boolean isAffected) {
        this.data = data;
        this.isAffected = isAffected;
    }

    /**
     * The element possibly affected by the operation.
     */
    public T getData() { return data; }

    /**
     * True if the element is affected.
     */
    public boolean isAffected() { return isAffected; }
}
