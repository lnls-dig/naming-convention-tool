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

import org.openepics.names.model.Role;
import org.openepics.names.model.UserAccount;
import org.openepics.names.services.UserService;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Alternative;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A session bean holding the UserAccount entity representing the signed in user.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 * @author Karin Rathsman <karin.rathsman@esss.se>
 */
@Alternative
@SessionScoped
public class SessionServiceTest implements SessionService {
    @Inject private UserService userService;
	@Inject protected HttpServletRequest servletRequest;
	private static final Logger LOGGER = Logger.getLogger(SessionServiceTest.class.getName());
    private UserAccount user = null;

    /*
     * (non-Javadoc)
     * @see org.openepics.names.services.SessionService#update()
     */
    @Override
    public void update() {
        final Principal principal = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal();
        user = principal != null ? userService.userWithName(principal.getName()) : null;
    }

	/* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#user()
	 */
    @Override
	public UserAccount user() { 
    	return user != null ? userService.emAttached(user) : null; 
    }

    /* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#isLoggedIn()
	 */
    @Override
	public boolean isLoggedIn() { 
    	return user != null; 
    }

    /* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#isEditor()
	 */
    @Override
	public boolean isEditor() { return user != null && (user.getRole() == Role.EDITOR || user.getRole() == Role.SUPERUSER); }

    /* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#isSuperUser()
	 */
    @Override
	public boolean isSuperUser() { return user != null && user.getRole() == Role.SUPERUSER; }

    /* (non-Javadoc)
     * @see org.openepics.names.services.SessionService#getUsername()
     */
	@Override
	public String getUsername() {
		return user != null ? user.getUsername(): null;
	}	
		
	/* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#getRequest()
	 */
	@Override
	public HttpServletRequest getRequest() {
		return (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
	}

	@PreDestroy
	public void cleanup(){
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
}
