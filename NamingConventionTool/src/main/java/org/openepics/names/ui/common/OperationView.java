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
