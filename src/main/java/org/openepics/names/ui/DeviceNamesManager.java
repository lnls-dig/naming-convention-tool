package org.openepics.names.ui;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.io.Serializable;
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
import org.openepics.names.services.NamingConvention;
import org.openepics.names.services.NamingConventionEJB;

@ManagedBean
@ViewScoped
public class DeviceNamesManager implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private NamingConventionEJB ncEJB;
    @Inject private NamingConvention namingConvention;
    private static final Logger logger = Logger.getLogger("org.openepics.names.ui.DeviceNamesManager");

    private List<DeviceRevision> allDeviceNames;
    private List<DeviceRevision> activeDeviceNames;
    private List<DeviceRevision> historyDeviceNames;

    public DeviceNamesManager() {
        // EMPTY
    }

    @PostConstruct
    public void init() {
        loadAllDeviceNames();
        loadActiveDeviceNames();
    }

    // TODO check usage
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

    // TODO check usage
    public List<DeviceView> getAllDeviceNames() {
        return Lists.transform(allDeviceNames, new Function<DeviceRevision, DeviceView>() {
            @Override
            public DeviceView apply(DeviceRevision deviceName) {
                return new DeviceView(deviceName, namingConvention.getNamingConventionName(deviceName));
            }
        });
    }

    // TODO check usage
    public void setAllDeviceNames(List<DeviceRevision> allDeviceNames) {
        this.allDeviceNames = allDeviceNames;
    }

    public List<DeviceView> getActiveDeviceNames() {
        return Lists.transform(activeDeviceNames, new Function<DeviceRevision, DeviceView>() {
            @Override
            public DeviceView apply(DeviceRevision deviceName) {
                return new DeviceView(deviceName, namingConvention.getNamingConventionName(deviceName));
            }
        });
    }

    public void setActiveDeviceNames(List<DeviceRevision> activeDeviceNames) {
        this.activeDeviceNames = activeDeviceNames;
    }

    public void findHistory(String nameId) {
        try {
            historyDeviceNames = ncEJB.getDeviceNameHistory(nameId);
        } catch (Exception e) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Encountered an error",
                    e.getMessage());
            System.err.println(e);
        }
    }

    public List<DeviceView> getHistoryEvents() {
        return historyDeviceNames == null ? null : Lists.transform(historyDeviceNames, new Function<DeviceRevision, DeviceView>() {
            @Override
            public DeviceView apply(DeviceRevision deviceName) {
                return new DeviceView(deviceName, namingConvention.getNamingConventionName(deviceName));
            }
        });
    }

    private void showMessage(FacesMessage.Severity severity, String summary, String message) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(severity, summary, message));
    }

}
