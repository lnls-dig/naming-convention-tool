package org.openepics.names.services;

import java.io.Serializable;

import org.openepics.names.model.UserAccount;

public interface SessionService extends Serializable{

	public abstract void login(String userName, String password);

	public abstract void logout();

	/**
	 * Name of logged in user. 
	 * @return username
	 */
	public abstract UserAccount user();
	
	/**
	 * True if the user is logged in.
	 */
	public abstract boolean isLoggedIn();

	/**
	 * True if the user is an editor.
	 */
	public abstract boolean isEditor();

	/**
	 * True if the user is a superuser.
	 */
	public abstract boolean isSuperUser();

	public abstract void update();

}