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

package org.openepics.names.ui.devices;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartType;
import org.openepics.names.services.SessionViewService;
import org.openepics.names.services.restricted.RestrictedNamePartService;
import org.openepics.names.services.views.DeviceRecordView;
import org.openepics.names.services.views.DeviceView;
import org.openepics.names.services.views.NamePartView;
import org.openepics.names.ui.common.AlphanumComparator;
import org.openepics.names.ui.common.SelectRecordManager;
import org.openepics.names.ui.common.TreeNodeManager;
import org.openepics.names.ui.common.ViewFactory;
import org.openepics.names.ui.parts.NamePartTreeBuilder;
import org.openepics.names.util.As;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import java.util.*;

/**
 * Utility bean for building JSF TreeNode trees from the section hierarchy and containing devices as leaf nodes.
 */
@ManagedBean
@ViewScoped
public class DevicesTreeBuilder {
	
    @Inject private RestrictedNamePartService namePartService;
    @Inject private NamePartTreeBuilder namePartTreeBuilder;
    @Inject private ViewFactory viewFactory;
    @Inject private TreeNodeManager treeNodeManager;

    private HashMap<NamePart, Set<DeviceRevision>> devicesBySection;
    private HashMap<NamePart, Set<DeviceRevision>> devicesByDeviceType;
    
    private TreeNode areaStructure;
    private TreeNode deviceStructure;
    
    private HashMap<NamePart, NamePartView> viewByDeviceType;
    
	@PostConstruct
	public void init(){
		areaStructure=namePartStructure(NamePartType.SECTION);
		deviceStructure=namePartStructure(NamePartType.DEVICE_TYPE);
	}
	private List<DeviceRecordView> devicesIn(NamePartView subsectionView, NamePartView deviceTypeView, boolean includeDeleted){
	    List<DeviceRecordView> temporary=Lists.newArrayList();
	    for(DeviceRevision revision: devicesBySection.get(subsectionView.getNamePart())){
	    	if(devicesByDeviceType.get(deviceTypeView.getNamePart()).contains(revision)){
	        	DeviceRecordView record=viewFactory.getRecordView(revision,subsectionView, deviceTypeView);
	        	temporary.add(record);
	    	}
	    }	
        Collections.sort(temporary, new Comparator<DeviceRecordView>() {
            @Override public int compare(DeviceRecordView left, DeviceRecordView right) {
                final AlphanumComparator alphanumComparator = new AlphanumComparator();
                return alphanumComparator.compare(left.getConventionName(), right.getConventionName());
            }
        });

	    
		return temporary;
	}
	
	private TreeNode namePartStructure(NamePartType type){
		final List<NamePartRevision> revisions = namePartService.currentApprovedNamePartRevisions(type,true);
        return namePartTreeBuilder.newNamePartTree(revisions);    
	}
	
	/**
	 * Generates a filtered and grouped list of all namePartViews.
	 * @param type namePartType  
	 * @param includeDeleted boolean flag to indicate whether deleted name parts shall be included.
 	 * @return filtered and grouped list for the specified name part type.
	 */
	private List<NamePartView> namePartViewList(TreeNode namePartStructure){
		List<NamePartView> namePartViews=Lists.newArrayList();
        for(TreeNode node: treeNodeManager.filteredNodeList(namePartStructure,true, false)){
        	NamePartView namePartView =node.getData() instanceof NamePartView ? (NamePartView) node.getData() :null;
        	NamePart namePart =namePartView!=null? namePartView.getNamePart():null;
        	if(namePart!=null && (devicesBySection.containsKey(namePart)||devicesByDeviceType.containsKey(namePart))){
        		namePartViews.add(namePartView);
        	}
        }
        return namePartViews;
	}
	
	public List<DeviceRecordView> deviceRecords(){
        devicesByDeviceType = Maps.newHashMap();
        devicesBySection = Maps.newHashMap();
        boolean includeDeleted=true;
        for (DeviceRevision device : namePartService.currentDeviceRevisions(true)) {
        	final Set<DeviceRevision> devicesForSection = devicesForSection(device.getSection());
        	devicesForSection.add(device);
        	final Set<DeviceRevision> devicesForDeviceType=devicesForDeviceType(device.getDeviceType());
        	devicesForDeviceType.add(device);
        }       

        List<DeviceRecordView> temporary=Lists.newArrayList();        
        final List<NamePartView> subsectionViews= namePartViewList(areaStructure);
        final List<NamePartView> deviceTypeViews= namePartViewList(deviceStructure);
        for(NamePartView subsectionView: subsectionViews){
       	for(NamePartView deviceTypeView: deviceTypeViews){
           	temporary.addAll(devicesIn(subsectionView,deviceTypeView,includeDeleted));
        	}
       }
		return temporary;
	}
	
    /**
     * Produces a tree of sections with contained devices as leaf nodes from the approved revisions in the database.
     *
     * @param withDeleted true if the tree should include deleted devices
     * @return the root node of the tree
     */
	@Deprecated
	public TreeNode devicesTree(boolean withDeleted) {
		final List<NamePartRevision> sectionRevisions = namePartService.currentApprovedNamePartRevisions(NamePartType.SECTION, withDeleted);
        final TreeNode sectionTree = namePartTreeBuilder.newNamePartTree(sectionRevisions);
        
        final List<NamePartRevision> deviceTypeRevisions = namePartService.currentApprovedNamePartRevisions(NamePartType.DEVICE_TYPE, withDeleted);
        final TreeNode deviceTypeTree = namePartTreeBuilder.newNamePartTree(deviceTypeRevisions);
        viewByDeviceType = Maps.newHashMap();
        populateDeviceTypeViews(deviceTypeTree);
        devicesBySection = Maps.newHashMap();
        for (DeviceRevision device : namePartService.currentDeviceRevisions(withDeleted)) {
        	final Set<DeviceRevision> devicesForSection = devicesForSection(device.getSection());
        	devicesForSection.add(device);
        }       
        populateDeviceNodes(sectionTree);

        return sectionTree;
	}
	
	@Deprecated
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
    
    private Set<DeviceRevision> devicesForDeviceType(NamePart deviceType) {
        final Set<DeviceRevision> currentSet = devicesByDeviceType.get(deviceType);
        if (currentSet == null) {
            final Set<DeviceRevision> newSet = Sets.newHashSet();
            devicesByDeviceType.put(deviceType, newSet);
            return newSet;
        } else {
            return currentSet;
        }
    }

	@Deprecated
    private NamePartView deviceTypeView(NamePart deviceType) {
        return As.notNull(viewByDeviceType.get(deviceType));
    }
	
	@Deprecated
	private void populateDeviceNodes(TreeNode node) {
	    for (TreeNode child : node.getChildren()) {
			populateDeviceNodes(child);
		}    	
    	
    	final @Nullable NamePartView sectionView = (NamePartView) node.getData();
        if (sectionView != null) {
            final List<TreeNode> children = Lists.newArrayList();
            for (DeviceRevision device : devicesForSection(sectionView.getNamePart())) {
                final TreeNode child = new DefaultTreeNode(viewFactory.getView(device, sectionView, deviceTypeView(device.getDeviceType())), null);
                children.add(child);
            }
            Collections.sort(children, new Comparator<TreeNode>() {
                @Override public int compare(TreeNode left, TreeNode right) {
                    final DeviceView leftView = (DeviceView) left.getData();
                    final DeviceView rightView = (DeviceView) right.getData();
                    final AlphanumComparator alphanumComparator = new AlphanumComparator();
                    return alphanumComparator.compare(leftView.getConventionName(), rightView.getConventionName());
                }
            });
            for (TreeNode child : children) {
                node.getChildren().add(child);
                child.setParent(node);
            }
        }
	}
	/**
	 * @return the areaStructure
	 */
	public TreeNode getAreaStructure() {
		if(areaStructure==null){
			areaStructure=namePartStructure(NamePartType.SECTION);
		}
		return areaStructure;
	}

	/**
	 * @return the deviceStructure
	 */
	public TreeNode getDeviceStructure() {
		return deviceStructure;
	}

	
}
