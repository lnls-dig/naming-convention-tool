package org.openepics.names.ui;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import org.openepics.names.model.UserAccount;
import org.openepics.names.services.SessionService;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@ManagedBean
@ViewScoped
public class UserManager {

    @Inject private SessionService sessionService;

    public UserAccount getUser() { return sessionService.user(); }
    public boolean isLoggedIn() { return sessionService.isLoggedIn(); }
    public boolean isEditor() { return sessionService.isEditor(); }
    public boolean isSuperUser() { return sessionService.isSuperUser(); }
}
