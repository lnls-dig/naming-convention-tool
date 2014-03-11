package org.openepics.names.ui.devices;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

import org.openepics.names.model.Device;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartType;
import org.openepics.names.services.DeviceService;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.ui.common.ViewFactory;
import org.openepics.names.ui.parts.NamePartTreeBuilder;
import org.openepics.names.ui.parts.NamePartView;
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
    @Inject private DeviceService deviceService;
    @Inject private NamePartTreeBuilder namePartTreeBuilder;
    @Inject private ViewFactory viewFactory;
    private HashMap<Integer,List<Device>> allDevices;
	
	public TreeNode devicesTree(boolean withDeleted) {
		final List<NamePartRevision> approvedRevisions = ImmutableList.copyOf(Collections2.filter(namePartService.currentApprovedRevisions(true), new Predicate<NamePartRevision>() {
            @Override public boolean apply(NamePartRevision revision) { return revision.getNamePart().getNamePartType() == NamePartType.SECTION; }
        }));
        final List<NamePartRevision> pendingRevisions = Lists.newArrayList();
        
        TreeNode root = namePartTreeBuilder.namePartApprovalTree(approvedRevisions, pendingRevisions, true);
        
        List<Device> devices = deviceService.devices(withDeleted);
        allDevices = new HashMap<>();
        for (Device device : devices) {
        	List<Device> temp;
        	if (allDevices.containsKey(deviceService.currentRevision(device).getSection().getId())) {
        		temp = allDevices.get(deviceService.currentRevision(device).getSection().getId());
        	} else {
        		temp = Lists.newArrayList();
        	}
        	temp.add(device);
    		allDevices.put(deviceService.currentRevision(device).getSection().getId(), temp);
        }
        
        return namePartTreeWithDevices(root);
	}
	
	private TreeNode namePartTreeWithDevices(TreeNode node) {
		final List<TreeNode> childNodes = Lists.newArrayList();
    	
    	if (node.getChildCount() > 0) {
    		for (TreeNode child : node.getChildren()) {
    			TreeNode temp = namePartTreeWithDevices(child);
    			if (temp != null) {
    				childNodes.add(temp);
    			} 
    		}
    	}
    	NamePartView view = (NamePartView) node.getData();
    	if (view != null && allDevices.containsKey(view.getId())) {
    		List<Device> devicesForSection = allDevices.get(view.getId());
    		for (Device device : devicesForSection) {
    			final TreeNode child = new DefaultTreeNode(viewFactory.getView(device), null);
    			child.setParent(node);
    		}
    	}
    	return node;    	
	}
	
}
