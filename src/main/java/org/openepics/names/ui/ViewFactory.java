package org.openepics.names.ui;

import org.openepics.names.model.Device;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.ui.names.NamePartView;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public class ViewFactory {

    public static NamePartView getView(NamePart namePart) {
        throw new IllegalStateException();
    }

    public static NamePartView getView(NamePartRevision namePartRevision) {
        throw new IllegalStateException();
    }

    public static DeviceView getView(Device device) {
        throw new IllegalStateException();
    }

    public static DeviceView getView(DeviceRevision deviceRevision) {
        throw new IllegalStateException();
    }
}
