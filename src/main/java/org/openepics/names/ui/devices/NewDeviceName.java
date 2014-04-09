package org.openepics.names.ui.devices;

import org.openepics.names.model.NamePart;

import javax.annotation.Nullable;

class NewDeviceName {
    private final NamePart sectionPart;
    private final NamePart deviceTypePart;
    private final @Nullable String index;

    public NewDeviceName(NamePart sectionPart, NamePart deviceTypePart, @Nullable String index) {
        this.sectionPart = sectionPart;
        this.deviceTypePart = deviceTypePart;
        this.index = index;
    }

    public NamePart getSectionPart() { return sectionPart; }
    public NamePart getDeviceTypePart() { return deviceTypePart; }
    public @Nullable String getIndex() { return index; }
}
