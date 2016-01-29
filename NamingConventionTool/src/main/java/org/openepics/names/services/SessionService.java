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
package org.openepics.names.services;

import java.io.Serializable;

import org.openepics.names.model.UserAccount;


import se.esss.ics.rbac.loginmodules.service.Message;


/**
 * An interface defining the session to be used by the application.
 *
 * The used session service is configured through beans.xml using the CDI alternatives mechanism.
 *
 * @author Karin Rathsman 
 */
public interface SessionService extends Serializable{

	/**
	 * 
	 * @return Logged in user. 
	 */
	UserAccount user();
	
	/**
	 * @return True if the user is logged in.
	 */
	boolean isLoggedIn();

	/**
	 * @return True if the user is an editor.
	 */
	boolean isEditor();

	/**
	 * @return True if the user is a superuser.
	 */
	boolean isSuperUser();

	/**
	 * Updates the session information with that of the current user, taken from the JSF context. Should be called on
	 * each login and logout.
	 */
	void update();

	/**
	 * @return User name of logged in user.
	 */
	String getUsername();
	
	/**
	 * Performs authentication of the user identified by the given username and authenticated by the given password.
	 * 
	 * @param username The username
	 * @param password The password
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