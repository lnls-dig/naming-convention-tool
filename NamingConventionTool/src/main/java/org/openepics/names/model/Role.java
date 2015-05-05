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
 * UserAccount's role that determines the user's access control permissions. Used by the SessionServiceTest (not RBAC)
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public enum Role {
	/**
     * Editor role giving permissions to propose changes to the Logical Area and Device Category structures and to add,
     * modify or delete Devices.
     */
    EDITOR,

    /**
     * Super user role giving permission to approve or reject proposed changes to Area and Device
     * structures, in addition to normal Editor permissions.
     */
    SUPERUSER
}
