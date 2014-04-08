package org.openepics.names.services;

import org.openepics.names.model.Role;
import org.openepics.names.model.UserAccount;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.security.Principal;

/**
 * A session bean holding the UserAccount entity representing the signed in user.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@SessionScoped
public class SessionService implements Serializable {

    @Inject private UserService userService;

    private UserAccount user = null;

    /**
     * Updates the session information with that of the current user, taken from the JSF context. Should be called on
     * each login and logout.
     */
    public void update() {
        final Principal principal = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal();
        user = principal != null ? userService.userWithName(principal.getName()) : null;
    }

    /**
     * The UserAccount of the currently logged in user.
     */
    public UserAccount user() { return user != null ? userService.emAttached(user) : null; }

    /**
     * True if the user is logged in.
     */
    public boolean isLoggedIn() { return user != null; }

    /**
     * True if the user is an editor.
     */
    public boolean isEditor() { return user != null && (user.getRole() == Role.EDITOR || user.getRole() == Role.SUPERUSER); }

    /**
     * True if the user is a superuser.
     */
    public boolean isSuperUser() { return user != null && user.getRole() == Role.SUPERUSER; }
}
