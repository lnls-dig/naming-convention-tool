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

    public void init() {
        final Principal principal = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal();
        user = principal != null ? userService.userWithName(principal.getName()) : null;
    }

    public UserAccount user() { return user != null ? userService.emAttached(user) : null; }
    public boolean isLoggedIn() { return user != null; }
    public boolean isEditor() { return user != null && (user.getRole() == Role.EDITOR || user.getRole() == Role.SUPERUSER); }
    public boolean isSuperUser() { return user != null && user.getRole() == Role.SUPERUSER; }
}
