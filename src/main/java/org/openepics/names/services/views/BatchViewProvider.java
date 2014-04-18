package org.openepics.names.services.views;

import com.google.common.collect.Maps;
import org.openepics.names.model.Device;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.services.views.DeviceView;
import org.openepics.names.services.views.NamePartView;
import org.openepics.names.util.As;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * A view provider that can provide views for a large number of name parts or devices in an efficient way.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public class BatchViewProvider {

    private final Map<NamePart, NamePartRevision> revisionByNamePart;
    private final Map<Device, DeviceRevision> revisionsByDevice;
    private final Map<NamePart, NamePartView> viewsByNamePart = Maps.newHashMap();
    private final Map<Device, DeviceView> viewsByDevice = Maps.newHashMap();

    /**
     * The provider is initialized with lists of all revisions that it might need when constructing views, so that no
     * additional database queries are necessary. The revisions should be fetched in bulk in single queries to get the
     * best performance.
     *
     * @param sectionRevisions a list of section revisions
     * @param deviceTypeRevisions a list of device type revisions
     * @param deviceRevisions a list of device revisions
     */
    public BatchViewProvider(List<NamePartRevision> sectionRevisions, List<NamePartRevision> deviceTypeRevisions, List<DeviceRevision> deviceRevisions) {
        revisionByNamePart = Maps.newHashMap();
        for (NamePartRevision sectionRevision : sectionRevisions) {
            revisionByNamePart.put(sectionRevision.getNamePart(), sectionRevision);
        }
        for (NamePartRevision deviceTypeRevision : deviceTypeRevisions) {
            revisionByNamePart.put(deviceTypeRevision.getNamePart(), deviceTypeRevision);
        }

        revisionsByDevice = Maps.newHashMap();
        for (DeviceRevision deviceRevision : deviceRevisions) {
            revisionsByDevice.put(deviceRevision.getDevice(), deviceRevision);
        }
    }

    /**
     * The view of the name part
     */
    public NamePartView view(NamePart namePart) {
        return view(As.notNull(revisionByNamePart.get(namePart)));
    }

    /**
     * The view of the name part revision
     */
    public NamePartView view(NamePartRevision revision) {
        final @Nullable NamePartView existingView = viewsByNamePart.get(revision.getNamePart());
        if (existingView != null) {
            return existingView;
        } else {
            final NamePartView newView = new NamePartView(null, revision, null, revision.getParent() != null ? view(revision.getParent()) : null);
            viewsByNamePart.put(revision.getNamePart(), newView);
            return newView;
        }
    }

    /**
     * The view of the device
     */
    public DeviceView view(Device device) {
        return view(As.notNull(revisionsByDevice.get(device)));
    }

    /**
     * The view of the device revision
     */
    public DeviceView view(DeviceRevision revision) {
        final @Nullable DeviceView existingView = viewsByDevice.get(revision.getDevice());
        if (existingView != null) {
            return existingView;
        } else {
            final DeviceView newView = new DeviceView(null, revision, view(revision.getSection()), view(revision.getDeviceType()));
            viewsByDevice.put(revision.getDevice(), newView);
            return newView;
        }
    }
}
