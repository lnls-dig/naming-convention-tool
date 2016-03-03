package org.openepics.names.ui.common;

import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.services.views.DeviceRecordView;
import org.openepics.names.services.views.DeviceView;
import org.openepics.names.services.views.NamePartRevisionProvider;
import org.openepics.names.services.views.NamePartView;

import java.util.List;

import javax.annotation.Nullable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

/**
 * A factory bean for creating augmented views of various entities.
 *
 * @author Marko Kolar  
 */
@ManagedBean
@ViewScoped
public class ViewFactory {

    @Inject private RestrictedNamePartService namePartService;

    /**
     * @return A view of the name part at it's most recent approved and pending revisions.
     *
     * @param namePart the name part
     */
    public NamePartView getView(NamePart namePart) {
        return new NamePartView(namePartRevisionProvider(), namePartService.approvedRevision(namePart), namePartService.pendingRevision(namePart), null);
    }

    /**
     * @return A view of the name part at the given revision.
     *
     * @param namePartRevision the name part revision
     */
    public NamePartView getView(NamePartRevision namePartRevision) {
        return new NamePartView(namePartRevisionProvider(), namePartRevision, null, null);
    }

    /**
     * @return A view of the name part based on explicitly given approved and pending revisions. The view of the parent name part can optionally be provided when available, so the data does not need to be queried from the database later.
     *
     * @param approvedRevision the approved revision of the name part, null if there is none
     * @param pendingRevision the pending revision of the name part, null if there is none
     * @param parentView optional view of the parent, so that the data does not need to be fetched from the database. Null if none is provided.
     */
    public NamePartView getView(@Nullable NamePartRevision approvedRevision, @Nullable NamePartRevision pendingRevision, @Nullable NamePartView parentView) {
        return new NamePartView(namePartRevisionProvider(), approvedRevision, pendingRevision, parentView);
    }

    /**
     * @return A view of the device based at the given revision.
     *
     * @param deviceRevision the device revision
     */
    public DeviceView getView(DeviceRevision deviceRevision) {
        return new DeviceView(this, deviceRevision, null, null);
    }

    public DeviceRecordView getRecordView(DeviceRevision deviceRevision){
    	return new DeviceRecordView(this,deviceRevision);
    }
    
    
    /**
     * @return A view of the device based at the given revision. The views of the device's section and device type can optionally be provided when available, so the data does not need to be queried from the database later.
     *
     * @param deviceRevision the device revision
     * @param sectionView optional view of the device's section, so that the data does not need to be fetched from the database. Null if none is provided.
     * @param deviceTypeView optional view of the device's device type, so that the data does not need to be fetched from the database. Null if none is provided.
     */
    public DeviceView getView(DeviceRevision deviceRevision, @Nullable NamePartView sectionView, @Nullable NamePartView deviceTypeView) {
        return new DeviceView(this, deviceRevision, sectionView, deviceTypeView);
    }

    private NamePartRevisionProvider namePartRevisionProvider() {
        return new NamePartRevisionProvider() {
            @Override public @Nullable NamePartRevision approvedRevision(NamePart namePart) { return namePartService.approvedRevision(namePart); }
            @Override public @Nullable NamePartRevision pendingRevision(NamePart namePart) { return namePartService.pendingRevision(namePart);}
            @Override public @Nullable List<NamePartRevision> approvedChildrenRevisions(NamePart namePart) {return namePartService.approvedChildrenRevisions(namePart,true); }
        };
    }
    
    
    
    
}
