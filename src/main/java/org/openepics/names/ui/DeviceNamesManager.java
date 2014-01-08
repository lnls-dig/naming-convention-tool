package org.openepics.names.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.services.NamingConventionEJB;

@ManagedBean
@ViewScoped
public class DeviceNamesManager implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private NamingConventionEJB ncEJB;
    private static final Logger logger = Logger.getLogger("org.openepics.names.ui.DeviceNamesManager");

    private List<DeviceView> allDeviceNames;
    private List<DeviceView> activeDeviceNames;
    private List<DeviceView> historyDeviceNames;

    public DeviceNamesManager() {
        // EMPTY
    }

    @PostConstruct
    public void init() {
        loadAllDeviceNames();
        loadActiveDeviceNames();
    }

    public void loadAllDeviceNames() {
        try {
            List<DeviceRevision> allDeviceNames = ncEJB.getAllDeviceNames();
            this.allDeviceNames = allDeviceNames.isEmpty() ? null : new ArrayList<DeviceView>();
            for(DeviceRevision dev : allDeviceNames)
                this.allDeviceNames.add(ViewFactory.getView(dev.getDevice()));
            logger.log(Level.FINER, "Found DeviceNames: " + allDeviceNames.size());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not load any DeviceNames: " + e.getMessage(), e);
        }
    }

    public void loadActiveDeviceNames() {
        try {
            List<DeviceRevision> activeDeviceNames = ncEJB.getActiveNames();
            this.activeDeviceNames = activeDeviceNames.isEmpty() ? null : new ArrayList<DeviceView>();
            for(DeviceRevision dev : activeDeviceNames)
                this.activeDeviceNames.add(ViewFactory.getView(dev.getDevice()));
            logger.log(Level.FINER, "Found active DeviceNames: " + activeDeviceNames.size());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not load any active DeviceNames: " + e.getMessage(), e);
        }
    }

    public List<DeviceView> getAllDeviceNames() {
        return allDeviceNames;
    }

    public List<DeviceView> getActiveDeviceNames() {
        return activeDeviceNames;
    }

    public void findHistory(String nameId) {
        try {
            List<DeviceRevision> historyDeviceNames = ncEJB.getDeviceNameHistory(nameId);
            this.historyDeviceNames = historyDeviceNames.isEmpty() ? null : new ArrayList<DeviceView>();
            for(DeviceRevision dev : historyDeviceNames)
                this.historyDeviceNames.add(ViewFactory.getView(dev.getDevice()));
        } catch (Exception e) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    public List<DeviceView> getHistoryEvents() {
        return historyDeviceNames;
    }

    private void showMessage(FacesMessage.Severity severity, String summary, String message) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(severity, summary, message));
    }

}
