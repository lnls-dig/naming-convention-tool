package org.openepics.names.services;

import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;
import org.openepics.names.model.UserAccount;

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
	 * ServletRequest
	 * @return
	 */
	HttpServletRequest getRequest();
}