package org.openepics.names.ui.common;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.openepics.names.services.SessionService;
import org.openepics.names.ui.devices.DeviceTableController;
import org.openepics.names.ui.parts.NamePartsController;

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

	public String getActiveIndex(){
		String viewId=FacesContext.getCurrentInstance().getViewRoot().getViewId();
		if(viewId.equals("/index.xhtml")) {
			return "0";
		}else if(viewId.equals("/about.xhtml")) {
			return "1";
		}else if(viewId.equals("/devices.xhtml")) {
			return "2";
		}else if(viewId.equals("/parts.xhtml")){
			String type = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("type");
			if(type==null){
				FacesContext facesContext=FacesContext.getCurrentInstance();
				NamePartsController namePartsController=(NamePartsController) facesContext.getApplication().getExpressionFactory().createValueExpression(facesContext.getELContext(), "#{namePartsController}", Object.class).getValue(facesContext.getELContext()); 
				type=namePartsController.getType();
			}
			if(type!=null && type.equals("section")) {
				return "3";
			}else if (type!=null && type.equals("deviceType")) {
				return "4";	
			} else {
				return "0";
			}
		}else if(viewId.equals("/help.xhtml")) {
			return "5";
		} else {
			return "0";
		}
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
//			    showMessage(FacesMessage.SEVERITY_INFO, "Signed in sucessfull", m.getMessage()); 
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
	
	public synchronized String signOut() {
		try {
			Message m = sessionService.logout();
			if (m.isSuccessful()) {
				LOGGER.log(Level.INFO, "Logout successful");
//			    showMessage(FacesMessage.SEVERITY_INFO, "Sign out sucessfull", m.getMessage()); 

			} else {
			    showMessage(FacesMessage.SEVERITY_ERROR, "Failed to sign out", m.getMessage()); 
			    throw new SecurityException(m.getMessage());
			}
		} finally {
			prepareLoginPopup();
			sessionService.update();
		}
			 return FacesContext.getCurrentInstance().getViewRoot().getViewId();

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
