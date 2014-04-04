package org.openepics.names.ui.common;

import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.services.NamingConvention;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.services.views.DeviceView;
import org.openepics.names.services.views.NamePartRevisionProvider;
import org.openepics.names.services.views.NamePartView;

import javax.annotation.Nullable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

/**
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@ManagedBean
@ViewScoped
public class ViewFactory {

    @Inject private RestrictedNamePartService namePartService;

    public NamePartView getView(NamePart namePart) {
        return new NamePartView(namePartRevisionProvider(), namePartService.approvedRevision(namePart), namePartService.pendingRevision(namePart), null);
    }

    public NamePartView getView(NamePartRevision namePartRevision) {
        return new NamePartView(namePartRevisionProvider(), namePartRevision, null, null);
    }

    public NamePartView getView(NamePartRevision namePartRevision, @Nullable NamePartView parentView) {
        return new NamePartView(namePartRevisionProvider(), namePartRevision, null, parentView);
    }

    public NamePartView getView(@Nullable NamePartRevision approvedRevision, @Nullable NamePartRevision pendingRevision) {
        return new NamePartView(namePartRevisionProvider(), approvedRevision, pendingRevision, null);
    }

    public NamePartView getView(@Nullable NamePartRevision approvedRevision, @Nullable NamePartRevision pendingRevision, @Nullable NamePartView parentView) {
        return new NamePartView(namePartRevisionProvider(), approvedRevision, pendingRevision, parentView);
    }

    public DeviceView getView(DeviceRevision deviceRevision) {
        return new DeviceView(this, deviceRevision, null, null);
    }

    public DeviceView getView(DeviceRevision deviceRevision, @Nullable NamePartView sectionView, @Nullable NamePartView deviceTypeView) {
        return new DeviceView(this, deviceRevision, sectionView, deviceTypeView);
    }

    private NamePartRevisionProvider namePartRevisionProvider() {
        return new NamePartRevisionProvider() {
            @Override public @Nullable NamePartRevision approvedRevision(NamePart namePart) { return namePartService.approvedRevision(namePart); }
            @Override public @Nullable NamePartRevision pendingRevision(NamePart namePart) { return namePartService.pendingRevision(namePart); }
        };
    }
}
