package org.openepics.names.model;

/**
 * UserAccount's role that determines the user's access control permissions.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public enum Role {
	/**
     * Login role giving permissions to login,
     * 
     */
	LOGIN,

	/**
     * Editor role giving permissions to propose changes to the Logical Area and Device Category structures and to add,
     * modify or delete Devices
     */
    EDITOR,

    /**
     * Super user role giving permission to approve or reject proposed changes to Area and Device
     * structures, in addition to normal Editor permissions.
     */
    SUPERUSER
}
