package org.openepics.names.ui.common;

import org.openepics.names.services.SessionService;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;

/**
 * A UI controller bean for the login / logout button and form.
 *
 * @author Vasu V <vuppala@frib.msu.org>
 * @author K. Rathsman <karin.rathsman@esss.se>
 */
@ManagedBean
@ViewScoped
public class LoginController implements Serializable {

    @Inject private SessionService sessionService;

    private String inputUsername;
    private String inputPassword;
    private boolean loginRequested;

    @PostConstruct
    public void init() {
    	loginRequested = !sessionService.isLoggedIn();
    }

    public void prepareLoginPopup() {
        inputUsername = null;
        inputPassword = null;
        loginRequested= true;
    }

    public void signIn() throws IOException {
        try {
          sessionService.login(inputUsername, inputPassword);
          loginRequested=false;
          showMessage(FacesMessage.SEVERITY_INFO, "Signed In ",inputUsername);
        } catch (SecurityException e) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Failed! Please try again. ", "Status: ");
        } finally {
            inputPassword = null;
            sessionService.update();
        }
    }

    public void signOut() {
        try {
            sessionService.logout();
            loginRequested=false;
           showMessage(FacesMessage.SEVERITY_INFO, "You have been signed out.", "Thank you!");
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } finally {
           sessionService.update();
        }
    }
    public void cancel(){
    	prepareLoginPopup();
    	loginRequested=false;
    }

    public String getInputUsername() { return inputUsername; }
    public void setInputUsername(String inputUsername) { this.inputUsername = inputUsername; }

    public String getInputPassword() { return inputPassword; }
    public void setInputPassword(String inputPassword) { this.inputPassword = inputPassword; }

    private void showMessage(FacesMessage.Severity severity, String summary, String message) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(severity, summary, message));
    }
    
    /**
     * @return true if login is currently requested, else false.
     */
    public boolean isLoginRequested(){
    	return loginRequested;
    }
}
