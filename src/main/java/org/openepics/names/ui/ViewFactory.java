package org.openepics.names.ui;

import javax.annotation.Nullable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import org.openepics.names.model.Device;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.ui.names.NamePartView;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@ManagedBean
@ViewScoped
public class ViewFactory {

    @Inject private RestrictedNamePartService namePartService;

    public NamePartView getView(NamePart namePart) {
        return new NamePartView(namePartService, namePartService.approvedRevision(namePart), namePartService.pendingRevision(namePart));
    }

    public NamePartView getView(NamePartRevision namePartRevision) {
        throw new IllegalStateException();
    }

    public NamePartView getView(@Nullable NamePartRevision approvedRevision, @Nullable NamePartRevision pendingRevision) {
        return new NamePartView(namePartService, approvedRevision, pendingRevision);
    }

    public static DeviceView getView(Device device) {
        throw new IllegalStateException();
    }

    public static DeviceView getView(DeviceRevision deviceRevision) {
        throw new IllegalStateException();
    }
}
