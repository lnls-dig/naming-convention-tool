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

	private static final String EDITOR="NamingUser";
	private static final String SUPERUSER="NamingAdministrator";
//	private static final String APPROVER="NamingApprover";

	@Inject private UserService userService;
	@Inject protected HttpServletRequest servletRequest;

	private Set<String> rbacRoles;
//	private Set<String> rbacPermissions;
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
			rbacRoles = rbacPrincipal.getRoles();
			user = userService.updatedUserWithName(rbacPrincipal.getName(), getRole());
		} else {
			rbacRoles = null;
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
		} catch (ServletException e) {
			throw new SecurityException("Logout Failed", e);
		}
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
	public boolean isEditor() { 
		return hasRbacRole(EDITOR) || isSuperUser();
	}
	/* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#isSuperUser()
	 */
	@Override
	public boolean isSuperUser() { 
		return hasRbacRole(SUPERUSER);
	}

//	private boolean hasPermission(String permission) {
//		if (!isLoggedIn()) {
//			return false;
//		}
//		return rbacPermissions.contains(permission);
//	}

	/**
	 * @param rbacRole
	 * @return true if the logged in user has the specified rbacRole, false otherwise.  
	 */
	private boolean hasRbacRole(String rbacRole){
		return  isLoggedIn() && rbacRoles!=null && !rbacRoles.isEmpty() && rbacRoles.contains(rbacRole);
	}
	/**
	 * @return roles.  
	 */
	private Role getRole(){
		if(isSuperUser()){
			return Role.SUPERUSER;
		} else if (isEditor()){
			return Role.EDITOR;
		} else {
			return Role.LOGIN;
		}
	}
	
    private HttpServletRequest getRequest() {
        return (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
    }


}