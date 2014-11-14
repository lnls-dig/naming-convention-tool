package org.openepics.names.services;

import org.openepics.names.model.Role;
import org.openepics.names.model.UserAccount;
import org.openepics.names.services.UserService;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Alternative;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import java.io.Serializable;
import java.security.Principal;

/**
 * A session bean holding the UserAccount entity representing the signed in user.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Alternative
@SessionScoped
public class SessionServiceNotRBAC implements SessionService, Serializable {
    @Inject private UserService userService;
    private UserAccount user = null;

    /**
     * Updates the session information with that of the current user, taken from the JSF context. Should be called on
     * each login and logout.
     */
    @Override
    public void update() {
        final Principal principal = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal();
        user = principal != null ? userService.userWithName(principal.getName()) : null;
    }

	/* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#login(java.lang.String, java.lang.String)
	 */
	@Override
	public void login(String userName, String password) {
	      FacesContext context = FacesContext.getCurrentInstance();
	      HttpServletRequest servletRequest = (HttpServletRequest) context.getExternalContext().getRequest();
			try {
				servletRequest.login(userName, password);
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
	        final FacesContext context = FacesContext.getCurrentInstance();
	        final HttpServletRequest servletRequest = (HttpServletRequest) context.getExternalContext().getRequest();
	        try {
	            servletRequest.logout();
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
	public boolean isEditor() { return user != null && (user.getRole() == Role.EDITOR || user.getRole() == Role.SUPERUSER); }

    /* (non-Javadoc)
	 * @see org.openepics.names.services.SessionService#isSuperUser()
	 */
    @Override
	public boolean isSuperUser() { return user != null && user.getRole() == Role.SUPERUSER; }
}
