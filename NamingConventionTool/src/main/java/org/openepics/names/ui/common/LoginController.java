package org.openepics.names.ui.common;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.openepics.names.services.SessionService;
import se.esss.ics.rbac.loginmodules.service.Message;

/**
 * A UI controller bean for the login / logout button and form.
 *
 * @author Vasu V  
 * @author K. Rathsman  
 */
@ManagedBean
@ViewScoped
public class LoginController implements Serializable {

    private static final long serialVersionUID = 7124872676453151325L;
    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());
	@Inject private SessionService sessionService;
	private String inputUsername;
	private String inputPassword;

	@PostConstruct
	public synchronized void init() {

	}

	public void prepareLoginPopup() {
		inputUsername = null;
		clearPassword();
	}
	
	public void clearPassword() {
	    inputPassword = null;
	}

	public synchronized void signIn() throws IOException {
		try {
		    Message m = sessionService.login(inputUsername, inputPassword);
			if (m.isSuccessful()) {
				LOGGER.log(Level.INFO, "Login successful for "+ inputUsername);
			} else {
			    showMessage(FacesMessage.SEVERITY_ERROR, "Failed to sign in", m.getMessage()); 
			    LOGGER.log(Level.INFO, "Login failed for "+ inputUsername);
			}
		} finally {
			clearPassword();
			sessionService.update();
		}
	}
	
	public boolean isLoggedIn(){
		return sessionService.isLoggedIn();
	}
	
	public String getUsername(){
		return sessionService.getUsername();
	}
	
	public synchronized void signOut() {
		try {
			Message m = sessionService.logout();
			if (m.isSuccessful()) {
				LOGGER.log(Level.INFO, "Logout successful");
			} else {
			    throw new SecurityException(m.getMessage());
			}
		} finally {
			prepareLoginPopup();
			sessionService.update();
		}
	}
	
    public String getInputUsername() { return inputUsername; }
	public void setInputUsername(String inputUsername) { this.inputUsername = inputUsername; }

	public String getInputPassword() { return inputPassword; }
	public void setInputPassword(String inputPassword) { this.inputPassword = inputPassword; }

	private void showMessage(FacesMessage.Severity severity, String summary, String message) {
		FacesContext context = FacesContext.getCurrentInstance();
		context.addMessage(null, new FacesMessage(severity, summary, message));
	}


}
