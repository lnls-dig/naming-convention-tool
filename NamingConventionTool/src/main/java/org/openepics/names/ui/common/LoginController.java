package org.openepics.names.ui.common;

import org.openepics.names.services.SessionService;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A UI controller bean for the login / logout button and form.
 *
 * @author Vasu V <vuppala@frib.msu.org>
 * @author K. Rathsman <karin.rathsman@esss.se>
 */
@ManagedBean
@ViewScoped
public class LoginController implements Serializable {

	private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());
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
			sessionService.getRequest().login(inputUsername, inputPassword);
			loginRequested=false;
			LOGGER.log(Level.INFO, "Login successful for "+ inputUsername);
		} catch (ServletException e){
			showMessage(FacesMessage.SEVERITY_ERROR, "Failed to sign in", "Status: "); 
			LOGGER.log(Level.INFO, "Login failed for "+ inputUsername);
		} finally {
			sessionService.update();
			inputPassword = null;
		}
	}
	
	public void signOut() {
		try {
			sessionService.getRequest().logout();
			LOGGER.log(Level.INFO, "Logout successful");	        	
		} catch (ServletException e) {
			throw new SecurityException("Failed to sign out", e);
		} finally {
			sessionService.update();
			sessionService.getRequest().getSession().invalidate();
			prepareLoginPopup();
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
