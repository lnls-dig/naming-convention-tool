package org.openepics.names.services;

import java.io.Serializable;

import org.openepics.names.model.UserAccount;

import se.esss.ics.rbac.loginmodules.service.Message;

public interface SessionService extends Serializable{

	/**
	 * Logged in user. 
	 * @return
	 */
	UserAccount user();
	
	/**
	 * True if the user is logged in.
	 */
	boolean isLoggedIn();

	/**
	 * True if the user is an editor.
	 */
	boolean isEditor();

	/**
	 * True if the user is a superuser.
	 */
	boolean isSuperUser();

	/**
	 * Updates the session information with that of the current user, taken from the JSF context. Should be called on
	 * each login and logout.
	 */
	void update();

	/**
	 * Username of logged in user.
	 * @return
	 */
	String getUsername();
	
	/**
	 * Performs authentication of the user identified by the given username and authenticated by the given password.
	 * 
	 * @param username the username
	 * @param password the password
	 * @return a message describing the result
	 */
	Message login(String username, String password);
	
	/**
	 * Log out the currently logged in user.
	 * 
	 * @return a message describing the result
	 */
	Message logout();
}