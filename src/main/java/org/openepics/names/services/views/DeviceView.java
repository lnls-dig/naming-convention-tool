package org.openepics.names.services.views;

import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.UserAccount;
import org.openepics.names.ui.common.ViewFactory;

import javax.annotation.Nullable;

/**
 * A view of a Device (its particular revision) that makes it easy to query some of its properties and relations in an
 * object-related fashion.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public class DeviceView {

    private final ViewFactory viewFactory;

    private final DeviceRevision currentRevision;

    private @Nullable NamePartView sectionView;
    private @Nullable NamePartView deviceTypeView;

    /**
     * @param viewFactory a factory for creating views. This is called to construct a view of a related entity when
     * calling the relation property.
     * @param currentRevision the revision the view is based on
     * @param sectionView the view of the device's section. This parameter is optional and can be passed for improving
     * performance (avoiding database queries) when we already have a view of the section. If this is null, the view will
     * be constructed automatically if needed by calling the view factory.
     * @param deviceTypeView the view of the device's device type. This parameter is optional and can be passed for improving
     * performance (avoiding database queries) when we already have a view of the device type. If this is null, the view will
     * be constructed automatically if needed.
     */
    public DeviceView(ViewFactory viewFactory, DeviceRevision currentRevision, @Nullable NamePartView sectionView, @Nullable NamePartView deviceTypeView) {
        this.currentRevision = currentRevision;
        this.viewFactory = viewFactory;
        this.sectionView = sectionView;
        this.deviceTypeView = deviceTypeView;
    }

    /**
     * The device's current revision.
     */
    public DeviceRevision getDevice() { return currentRevision; }

    @Deprecated
    public Long getId() { return currentRevision.getId(); }

    /**
     * The convention name of the device.
     */
    public String getConventionName() {
        return currentRevision.getConventionName();
    }

    /**
     * The view of the section containing the device.
     */
    public NamePartView getSection() {
        if (sectionView == null) {
            sectionView = viewFactory.getView(currentRevision.getSection());
        }
        return sectionView;
    }

    /**
     * The view of the device's device type.
     */
    public NamePartView getDeviceType() {
        if (deviceTypeView == null) {
            deviceTypeView = viewFactory.getView(currentRevision.getDeviceType());
        }
        return deviceTypeView;
    }

    /**
     * An additional identifier that, in combination with other attributes, determine the unique convention name of the
     * device. Null if omitted.
     */
    public @Nullable String getInstanceIndex() { return currentRevision.getInstanceIndex(); }

    /**
     * The user who proposed the current device revision.
     */
    public UserAccount getRequestedBy() { return currentRevision.getRequestedBy(); }
}
