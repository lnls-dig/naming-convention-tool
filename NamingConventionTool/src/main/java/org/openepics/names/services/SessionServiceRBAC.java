package org.openepics.names.services;

import org.openepics.names.model.Role;
import org.openepics.names.model.UserAccount;

import se.lu.esss.ics.rbac.loginmodules.RBACPrincipal;

import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Alternative;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import java.util.Set;
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
public class SessionServiceRBAC implements SessionService {
	private static final Logger LOGGER = Logger.getLogger(SessionServiceRBAC.class.getName());
	private static final String EDIT ="Edit";
	private static final String MANAGE="Manage";
	@Inject private UserService userService;
	@Inject protected HttpServletRequest servletRequest;
	private Set<String> rbacPermissions=null;
	private String username=null;
	private boolean loggedIn=false;
	private UserAccount user=null;

	/* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#update()
	 */
	@Override
	public void update() {
		final RBACPrincipal rbacPrincipal= (RBACPrincipal) getRequest().getUserPrincipal();
		if(rbacPrincipal!=null){
			loggedIn=true;
			rbacPermissions=rbacPrincipal.getPermissions();
			username=rbacPrincipal.getName();
			if(isEditor()) user=userService.getExisitngOrCreatedUser(username, Role.EDITOR);
			//  TODO: The class role is not used with rbac but still needs to be set for the user class.   
		} else {
			loggedIn=false;
			rbacPermissions=null;
			username=null;
			user=null;
		}
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


	/* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#user()
	 */
	@Override
	public UserAccount user() { 
		return user;
	}

	/* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#isLoggedIn()
	 */
	@Override
	public boolean isLoggedIn() { 
		return loggedIn; 
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

	/**
	 * @param permission
	 * @return true if the logged in user has the specified permission, false otherwise.  
	 */
	private boolean hasPermission(String permission) {
		return isLoggedIn() ? rbacPermissions.contains(permission): false;
	}
	
	/* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#getRequest()
	 */
    public HttpServletRequest getRequest() {
        return (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
    }

	/* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#getUsername()
	 */
	@Override
	public String getUsername() {
		return isLoggedIn() ? username:null;
	}
    
}