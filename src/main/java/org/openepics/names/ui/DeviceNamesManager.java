package org.openepics.names.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import org.openepics.names.model.Device;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.services.restricted.RestrictedDeviceService;

@ManagedBean
@ViewScoped
public class DeviceNamesManager implements Serializable {

    @Inject private RestrictedDeviceService deviceService;

    private List<DeviceView> allDeviceNames;
    private List<DeviceView> activeDeviceNames;
    private List<DeviceView> historyDeviceNames;

    @PostConstruct
    public void init() {
        loadAllDeviceNames();
        loadActiveDeviceNames();
    }

    public void loadAllDeviceNames() {
        final List<Device> allDeviceNames = deviceService.devices();
        this.allDeviceNames = allDeviceNames.isEmpty() ? null : new ArrayList<DeviceView>();
        for (Device dev : allDeviceNames) {
            this.allDeviceNames.add(ViewFactory.getView(dev));
        }
    }

    public void loadActiveDeviceNames() {
        final List<Device> activeDeviceNames = deviceService.devices(); // TODO
        this.activeDeviceNames = activeDeviceNames.isEmpty() ? null : new ArrayList<DeviceView>();
        for (Device dev : activeDeviceNames) {
            this.activeDeviceNames.add(ViewFactory.getView(dev));
        }
    }

    public List<DeviceView> getAllDeviceNames() {
        return allDeviceNames;
    }

    public List<DeviceView> getActiveDeviceNames() {
        return activeDeviceNames;
    }

    public void findHistory(String nameId) {
        final List<DeviceRevision> historyDeviceNames = deviceService.revisions(null); // TODO
        this.historyDeviceNames = historyDeviceNames.isEmpty() ? null : new ArrayList<DeviceView>();
        for (DeviceRevision dev : historyDeviceNames) {
            this.historyDeviceNames.add(ViewFactory.getView(dev.getDevice()));
        }
    }

    public List<DeviceView> getHistoryEvents() {
        return historyDeviceNames;
    }
}
