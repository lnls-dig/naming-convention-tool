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

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import org.openepics.names.model.Role;
import org.openepics.names.model.UserAccount;

import se.esss.ics.rbac.loginmodules.service.RBACSSOSessionService;

/**
 * A session bean holding the UserAccount entity representing the signed in user.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 * @author Karin Rathsman <karin.rathsman@esss.se>
 */
@Alternative
@RequestScoped
public class SessionServiceRBAC extends RBACSSOSessionService implements SessionService {
	
    private static final long serialVersionUID = 3139143565327487407L;
	private static final String EDIT ="Edit";
	private static final String MANAGE="Manage";
	@Inject private UserService userService;
	UserAccount user=null;
	
	/* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#update()
	 */
	@Override
	public void update() {
		 if(isLoggedIn() && isEditor()) userService.createUser(getUsername(), Role.EDITOR);		
	}
	
	/*
	 * (non-Javadoc)
	 * @see se.esss.ics.rbac.loginmodules.service.RBACSSOSessionService#isLoggedIn()
	 */
	@Override
	public boolean isLoggedIn() {
	    return super.isLoggedIn();
	}

	/* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#user()
	 */
	@Override
	public UserAccount user() {
		return isEditor() ? userService.userWithName(getUsername()):null;
	}
 
	/* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#isEditor()
	 */
	@Override
	public boolean isEditor() { 
		return hasPermission(EDIT) || isSuperUser();
	}
	
	/* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#isSuperUser()
	 */
	@Override
	public boolean isSuperUser() { 
		return hasPermission(MANAGE);
	}
	
	/* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#getUsername()
	 */
	@Override
	public String getUsername() {
	    return getLoggedInName();
	}
}