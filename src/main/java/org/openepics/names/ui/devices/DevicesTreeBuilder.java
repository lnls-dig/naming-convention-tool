package org.openepics.names.ui.devices;

import com.google.common.collect.Lists;
import org.openepics.names.model.Device;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartType;
import org.openepics.names.services.restricted.RestrictedDeviceService;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.ui.common.ViewFactory;
import org.openepics.names.ui.parts.NamePartTreeBuilder;
import org.openepics.names.ui.parts.NamePartView;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.annotation.Nullable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;

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
        
        final TreeNode root = namePartTreeBuilder.namePartApprovalTree(approvedRevisions, pendingRevisions, false);
        
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
