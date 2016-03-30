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

import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Alternative;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.openepics.names.model.Role;
import org.openepics.names.model.UserAccount;

import se.esss.ics.rbac.loginmodules.service.Message;

/**
 * A session bean holding the UserAccount entity representing the signed in user.
 *
 * @author Marko Kolar  
 * @author Karin Rathsman  
 */
@Alternative
@SessionScoped
public class SessionServiceTest implements SessionService {
    private static final long serialVersionUID = -8467983622257857750L;
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
    	return isLoggedIn() ? userService.emAttached(user) : null; 
    }
    
    /* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#isLoggedIn()
	 */
    @Override
	public boolean isLoggedIn() { 
    	return  user != null; 
    }

    /* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#isEditor()
	 */
    @Override
	public boolean isEditor() { return isLoggedIn() && (user.getRole() == Role.EDITOR || user.getRole() == Role.SUPERUSER); }

    /* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#isSuperUser()
	 */
    @Override
	public boolean isSuperUser() { return isLoggedIn() && user.getRole() == Role.SUPERUSER; }

    /* (non-Javadoc)
     * @see org.openepics.names.services.SessionService#getUsername()
     */
	@Override
	public String getUsername() {
		return isLoggedIn() ? user.getUsername(): null;
	}	

	
	private HttpServletRequest getRequest() {
		return (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
	}

	@PreDestroy
	private void cleanup(){
		user=null;
	}

    @Override
    public Message login(String username, String password) {
        try {
        	getRequest().login(username, password);
        	return new Message("Sign In successful.",true);
        } catch (ServletException e) {
            return new Message(e.getMessage(),false);
        }
    }

    @Override
    public Message logout() {
        try {
            getRequest().logout();
            return new Message("Sign Out successful.",true);
        } catch (ServletException e) {
            return new Message(e.getMessage(),false);
        } finally {
        	getRequest().getSession().invalidate();
        }
    }
}
