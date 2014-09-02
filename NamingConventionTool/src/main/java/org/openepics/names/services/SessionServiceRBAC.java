package org.openepics.names.services;
import org.openepics.names.model.Role;
import org.openepics.names.model.UserAccount;

import se.lu.esss.ics.rbac.loginmodules.RBACPrincipal;

import javax.enterprise.inject.Alternative;
import javax.faces.bean.SessionScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import java.io.Serializable;

/**
 * A session bean holding the UserAccount entity representing the signed in user.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Alternative
@SessionScoped
public class SessionServiceRBAC implements Serializable, SessionService {

	@Inject private UserService userService;
	@Inject protected HttpServletRequest servletRequest;

	final RBACPrincipal rbacPrincipal = (RBACPrincipal) servletRequest.getUserPrincipal();
    private UserAccount user = null;

    /**
     * Updates the session information with that of the current user, taken from the JSF context. Should be called on
     * each login and logout.
     */
    public void update() {
    	updateUserAccount();
        user = rbacPrincipal != null ? userService.userWithName(rbacPrincipal.getName()) : null;
    }
	
	/* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#login(java.lang.String, java.lang.String)
	 */
	@Override
	public void login(String userName, String password) { 
		try {
			if (!isLoggedIn()) { 
				servletRequest.login(userName, password); 
			}
		} catch (ServletException e) {
			throw new SecurityException("Login Failed !", e); 
		} finally {
			update();
		}
		
	}

	/* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#logout()
	 */
	@Override
	public void logout() { 
		try {      
			servletRequest.logout();
			servletRequest.getSession().invalidate(); 
		} catch (ServletException e) {
			throw new SecurityException("Error while logging out!", e); 
		} finally {
			update();
		}
	}

	/* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#isLoggedIn()
	 */
	@Override
	public boolean isLoggedIn() { 
		return servletRequest.getUserPrincipal() != null; 
	}

	/* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#isEditor()
	 */
	@Override
	public boolean isEditor() { 
//		final RBACPrincipal rbacPrincipal = (RBACPrincipal) servletRequest.getUserPrincipal();
		return rbacPrincipal.getPermissions().contains("Edit")||isSuperUser(); 
	}

	/* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#isSuperUser()
	 */
	@Override
	public boolean isSuperUser() { 
//		final RBACPrincipal rbacPrincipal = (RBACPrincipal) servletRequest.getUserPrincipal();
		return rbacPrincipal.getPermissions().contains("Manage"); 		
	}

	@Override
	public UserAccount user(){
		return user != null ? userService.emAttached(user) : null; 
	}

	private void updateUserAccount(){
		if (isLoggedIn()){
			Role role=Role.LOGIN;
			if (isSuperUser()){
				role=Role.SUPERUSER;
			} else if (isEditor() ){
				role=Role.EDITOR;
			}
			userService.update( new UserAccount(rbacPrincipal.getName(),role));
		}
	}
}