package org.openepics.names.model;

/**
 * UserAccount's role that determines the user's access control permissions. Used by the SessionServiceTst (not RBAC)
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
