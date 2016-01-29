/*-
* Copyright (c) 2014 European Spallation Source
* Copyright (c) 2014 Cosylab d.d.
*
* This file is part of Naming Service.
* Naming Service is free software: you can redistribute it and/or modify it under
* the terms of the GNU General Public License as published by the Free
* Software Foundation, either version 2 of the License, or any newer version.
*
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
* FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
* more details.
*
* You should have received a copy of the GNU General Public License along with
* this program. If not, see https://www.gnu.org/licenses/gpl-2.0.txt
*/
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
 * @author Marko Kolar  
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
     * @param namePart a name part
     * @return The view of the name part
     */
    public NamePartView view(NamePart namePart) {
        return view(As.notNull(revisionByNamePart.get(namePart)));
    }

    /**
     * @param revision a revision of a name part
     * @return The view of the name part revision
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
     * @param device a device
     * @return The view of the device
     */
    public DeviceView view(Device device) {
        return view(As.notNull(revisionsByDevice.get(device)));
    }

    /**
     * @param revision a revision of a device
     * @return The view of the device revision
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
