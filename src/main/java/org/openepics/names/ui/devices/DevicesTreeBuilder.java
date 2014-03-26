package org.openepics.names.ui.devices;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.openepics.names.model.*;
import org.openepics.names.services.restricted.RestrictedDeviceService;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.ui.common.ViewFactory;
import org.openepics.names.ui.parts.NamePartTreeBuilder;
import org.openepics.names.ui.parts.NamePartView;
import org.openepics.names.util.As;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.annotation.Nullable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@ManagedBean
@ViewScoped
public class DevicesTreeBuilder {
	
    @Inject private RestrictedNamePartService namePartService;
    @Inject private RestrictedDeviceService deviceService;
    @Inject private NamePartTreeBuilder namePartTreeBuilder;
    @Inject private ViewFactory viewFactory;

    private HashMap<NamePart, Set<DeviceRevision>> devicesBySection;
    private HashMap<NamePart, NamePartView> viewByDeviceType;
	
	public TreeNode devicesTree(boolean withDeleted) {
		final List<NamePartRevision> sectionRevisions = namePartService.currentApprovedRevisions(NamePartType.SECTION, withDeleted);
        final TreeNode sectionTree = namePartTreeBuilder.namePartApprovalTree(sectionRevisions, Lists.<NamePartRevision>newArrayList(), false);

        final List<NamePartRevision> deviceTypeRevisions = namePartService.currentApprovedRevisions(NamePartType.DEVICE_TYPE, withDeleted);
        final TreeNode deviceTypeTree = namePartTreeBuilder.namePartApprovalTree(deviceTypeRevisions, Lists.<NamePartRevision>newArrayList(), false);
        viewByDeviceType = Maps.newHashMap();
        populateDeviceTypeViews(deviceTypeTree);
        
        devicesBySection = Maps.newHashMap();
        for (DeviceRevision device : deviceService.currentRevisions(withDeleted)) {
        	final Set<DeviceRevision> devicesForSection = devicesForSection(device.getSection());
        	devicesForSection.add(device);
        }
        
        populateDeviceNodes(sectionTree);

        return sectionTree;
	}

    private void populateDeviceTypeViews(TreeNode node) {
        for (TreeNode child : node.getChildren()) {
            populateDeviceTypeViews(child);
        }

        final @Nullable NamePartView deviceTypeView = (NamePartView) node.getData();
        if (deviceTypeView != null) {
            viewByDeviceType.put(deviceTypeView.getNamePart(), deviceTypeView);
        }
    }

    private Set<DeviceRevision> devicesForSection(NamePart section) {
        final Set<DeviceRevision> currentSet = devicesBySection.get(section);
        if (currentSet == null) {
            final Set<DeviceRevision> newSet = Sets.newHashSet();
            devicesBySection.put(section, newSet);
            return newSet;
        } else {
            return currentSet;
        }
    }

    private NamePartView deviceTypeView(NamePart deviceType) {
        return As.notNull(viewByDeviceType.get(deviceType));
    }
	
	private void populateDeviceNodes(TreeNode node) {
	    for (TreeNode child : node.getChildren()) {
			populateDeviceNodes(child);
		}    	
    	
    	final @Nullable NamePartView sectionView = (NamePartView) node.getData();
        if (sectionView != null) {
            for (DeviceRevision device : devicesForSection(sectionView.getNamePart())) {
                final TreeNode child = new DefaultTreeNode(viewFactory.getView(device, sectionView, deviceTypeView(device.getDeviceType())), null);
                node.getChildren().add(child);
                child.setParent(node);
            }
        }
	}
	
}
