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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.servlet.ServletException;

import org.openepics.names.model.Role;
import org.openepics.names.model.UserAccount;

import se.esss.ics.rbac.loginmodules.RBACPrincipal;
import se.esss.ics.rbac.loginmodules.service.RBACSSOSessionService;

/**
 * A session bean holding the UserAccount entity representing the signed in user.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 * @author Karin Rathsman <karin.rathsman@esss.se>
 */
@Alternative
@SessionScoped
public class SessionServiceRBAC extends RBACSSOSessionService implements SessionService {
	
    private static final long serialVersionUID = 3139143565327487407L;
    private static final Logger LOGGER = Logger.getLogger(SessionServiceRBAC.class.getName());
	private static final String EDIT ="Edit";
	private static final String MANAGE="Manage";
	@Inject private UserService userService;
	private UserAccount user=null;

	/* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#update()
	 */
	@Override
	public void update() {
		final RBACPrincipal rbacPrincipal= (RBACPrincipal) getRequest().getUserPrincipal();
		if(rbacPrincipal!=null){
			if(isEditor()) user=userService.getExisitngOrCreatedUser(getUsername(), Role.EDITOR);
			//  TODO: The class role is not used with rbac but still needs to be set for the user class.   
		} else {
			user=null;
		}
	}
	
	@PreDestroy
	private void cleanup(){
		if(isLoggedIn()) {
			try {
				getRequest().logout();
				LOGGER.log(Level.INFO, "Logout successful");	        	
			} catch (ServletException e) {
				throw new SecurityException("Logout Failed", e);
			} finally {
				update();
			}			
		}
	}

	/*
	 * (non-Javadoc)
	 * @see se.esss.ics.rbac.loginmodules.service.RBACSSOSessionService#isLoggedIn()
	 */
	@Override
	public boolean isLoggedIn() {
	    clearLoggedIn();
	    checkCookie();
	    return super.isLoggedIn();
	}

	/* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#user()
	 */
	@Override
	public UserAccount user() { 
		return user;
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