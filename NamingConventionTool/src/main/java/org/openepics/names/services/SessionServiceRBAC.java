package org.openepics.names.services;

import org.openepics.names.model.Role;
import org.openepics.names.model.UserAccount;

import se.lu.esss.ics.rbac.loginmodules.RBACPrincipal;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Alternative;
import javax.faces.bean.ManagedBean;
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
@ManagedBean
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

	/**
	 * Updates the session information with that of the current user, taken from the JSF context. Should be called on
	 * each login and logout.
	 * 
	 */
	@Override
	public void update() {
		final RBACPrincipal rbacPrincipal= (RBACPrincipal) getRequest().getUserPrincipal();
		if(rbacPrincipal!=null){
			loggedIn=true;
			rbacPermissions=rbacPrincipal.getPermissions();
			username=rbacPrincipal.getName();
			if(isEditor()) user=userService.getExisitngOrCreatedUser(username, Role.EDITOR);
			// TODO Implement useraccountrevision in the model to allow revisions of UserAccounts. (can only be changed in the database). On the other hand, the class role is not used, since permissions are given directly from rbac.   
		} else {
			loggedIn=false;
			rbacPermissions=null;
			username=null;
			user=null;
		}
	}

	/* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#login(java.lang.String, java.lang.String)
	 */
	@Override
	public void login(String userName, String password) {
		try {
			getRequest().login(userName, password);	
			LOGGER.log(Level.INFO, "Login successful for "+ userName);
		} catch (ServletException e){
			throw new SecurityException("Login Failed !", e); 
		} finally {
			password = null;
			update();
		}
	}

	/* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#logout()
	 */
	@Override
	public void logout() {
		try {
			getRequest().logout();
			getRequest().getSession().invalidate();
			LOGGER.log(Level.INFO, "Logout successful");

		} catch (ServletException e) {
			throw new SecurityException("Logout Failed", e);
		} finally {
			update();
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
	
    private HttpServletRequest getRequest() {
        return (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
    }

	@Override
	public String getUsername() {
		return isLoggedIn() ? username:null;
	}
    
}