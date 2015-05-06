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
package org.openepics.names.model;

/**
 * Status of the name part in the request / approve workflow.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public enum NamePartRevisionStatus {
    /** The proposed revision has been approved by the administrator. */
    APPROVED,

    /** The proposed revision has been cancelled by the user that requested it. */
    CANCELLED,

    /** The proposed revision is pending approval by the administrator. */
    PENDING,

    /** The proposed revision has been rejected by the administrator. */
    REJECTED
}
