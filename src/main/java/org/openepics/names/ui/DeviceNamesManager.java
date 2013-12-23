package org.openepics.names.ui;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.openepics.names.model.DeviceName;
import org.openepics.names.services.NamingConventionEJB;

@ManagedBean
@ViewScoped
public class DeviceNamesManager implements Serializable {

    private static final long serialVersionUID = 1L;
    @EJB
    private NamingConventionEJB ncEJB;
    private static final Logger logger = Logger.getLogger("org.openepics.names.ui.DeviceNamesManager");

    private List<DeviceName> allDeviceNames;
    private List<DeviceName> activeDeviceNames;

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
            allDeviceNames = ncEJB.getAllDeviceNames();
            logger.log(Level.INFO, "Found DeviceNames. Total = " + allDeviceNames.size());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not load any DeviceNames.");
            System.err.println(e);
        }
    }

    public void loadActiveDeviceNames() {
        try {
            activeDeviceNames = ncEJB.getActiveNames();
            logger.log(Level.INFO, "Found active DeviceNames. Total = " + activeDeviceNames.size());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not load any active DeviceNames.");
            System.err.println(e);
        }
    }

    public List<DeviceName> getAllDeviceNames() {
        return allDeviceNames;
    }

    public void setAllDeviceNames(List<DeviceName> allDeviceNames) {
        this.allDeviceNames = allDeviceNames;
    }

    public List<DeviceName> getActiveDeviceNames() {
        return activeDeviceNames;
    }

    public void setActiveDeviceNames(List<DeviceName> activeDeviceNames) {
        this.activeDeviceNames = activeDeviceNames;
    }

}
