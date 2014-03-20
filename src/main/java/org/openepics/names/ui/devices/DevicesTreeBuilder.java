package org.openepics.names.ui.devices;

import java.util.List;

import javax.annotation.Nullable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

import org.openepics.names.model.Device;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartType;
import org.openepics.names.services.DeviceService;
import org.openepics.names.services.restricted.RestrictedDeviceService;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.ui.common.OperationView;
import org.openepics.names.ui.common.ViewFactory;
import org.openepics.names.ui.parts.NamePartTreeBuilder;
import org.openepics.names.ui.parts.NamePartView;
import org.openepics.names.util.As;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.HashMap;

@ManagedBean
@ViewScoped
public class DevicesTreeBuilder {
	
    @Inject private RestrictedNamePartService namePartService;
    @Inject private RestrictedDeviceService deviceService;
    @Inject private NamePartTreeBuilder namePartTreeBuilder;
    @Inject private ViewFactory viewFactory;
    private HashMap<Integer,List<Device>> allDevicesForSection;
	
	public TreeNode devicesTree(boolean withDeleted) {
		final List<NamePartRevision> approvedRevisions = namePartService.currentApprovedRevisions(NamePartType.SECTION, withDeleted);
        final List<NamePartRevision> pendingRevisions = Lists.newArrayList();
        
        final TreeNode root = namePartTreeBuilder.namePartApprovalTree(approvedRevisions, pendingRevisions, true);
        
        final List<Device> devices = Lists.newArrayList();
        devices.addAll(deviceService.devices(withDeleted));
        
        allDevicesForSection = new HashMap<>();
        for (Device device : devices) {
        	List<Device> devicesForCurrentSection = Lists.newArrayList();
        	if (allDevicesForSection.containsKey(deviceService.currentRevision(device).getSection().getId())) {
        		devicesForCurrentSection = allDevicesForSection.get(deviceService.currentRevision(device).getSection().getId());
        	}        	
        	devicesForCurrentSection.add(device);
    		allDevicesForSection.put(deviceService.currentRevision(device).getSection().getId(), devicesForCurrentSection);
        }
        
        return namePartTreeWithDevices(root);
	}
	
	private TreeNode namePartTreeWithDevices(TreeNode node) {
	    for (TreeNode child : node.getChildren()) {
			namePartTreeWithDevices(child);
		}    	
    	
    	final @Nullable NamePartView view = (NamePartView) node.getData();    	
    	if (view != null && allDevicesForSection.containsKey(view.getId())) {
    		List<Device> devicesForSection = allDevicesForSection.get(view.getId());
    		for (Device device : devicesForSection) {
		    	final TreeNode child = new DefaultTreeNode(viewFactory.getView(device), null);
                node.getChildren().add(child);
    			child.setParent(node);
    		}
    	}
    	return node;    	
	}
	
}
