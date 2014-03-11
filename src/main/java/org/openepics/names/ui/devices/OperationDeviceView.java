/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openepics.names.ui.devices;

/**
 *
 * @author Miha Vitorovič <miha.vitorovic@cosylab.com>
 */
public class OperationDeviceView {
    final private DeviceView deviceView;
    final private boolean isAffected;

    public OperationDeviceView(DeviceView deviceView, boolean isAffected) {
        this.deviceView = deviceView;
        this.isAffected = isAffected;
    }

    public DeviceView getDeviceView() { return deviceView; }

    public boolean isAffected() { return isAffected; }
}
