package org.openepics.names.ui.common;

import org.openepics.names.services.SessionService;
import org.primefaces.context.RequestContext;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;

/**
 * A UI controller bean for the login / logout button and form.
 *
 * @author Vasu V <vuppala@frib.msu.org>
 */
@ManagedBean
@ViewScoped
public class LoginController implements Serializable {

    @Inject private SessionService sessionService;

    private String inputUsername;
    private String inputPassword;
    private String originalURL;

    @PostConstruct
    public void init() {
        FacesContext context = FacesContext.getCurrentInstance();
        originalURL = (String) context.getExternalContext().getRequestMap().get(RequestDispatcher.FORWARD_REQUEST_URI);
    }

    public void prepareLoginPopup() {
        inputUsername = null;
        inputPassword = null;
        RequestContext.getCurrentInstance().reset("loginForm:grid");
    }

    public void onLogin() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();

        try {
            request.login(inputUsername, inputPassword);
            RequestContext.getCurrentInstance().addCallbackParam("loginSuccess", true);
            showMessage(FacesMessage.SEVERITY_INFO, "You are logged in. Welcome to Proteus.", inputUsername);
            if (originalURL != null) {
                context.getExternalContext().redirect(originalURL);
            }
        } catch (ServletException e) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Login Failed! Please try again. ", "Status: ");
            RequestContext.getCurrentInstance().addCallbackParam("loginSuccess", false);
        } finally {
            inputPassword = null;
            sessionService.update();
        }
        updatePageElements();
    }

    public void onLogout() {
        final FacesContext context = FacesContext.getCurrentInstance();
        final HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        try {
            request.logout();
            showMessage(FacesMessage.SEVERITY_INFO, "You have been logged out.", "Thank you!");
        } catch (ServletException e) {
            throw new RuntimeException(e);
        } finally {
            sessionService.update();
        }
        updatePageElements();
    }
    
    private void updatePageElements() {
        RequestContext.getCurrentInstance().update("ReqSubForm:filterMenu");
        RequestContext.getCurrentInstance().update("ReqSubForm:reqMenu");
        RequestContext.getCurrentInstance().update("ManageNameForm:ncReqMenu");
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
