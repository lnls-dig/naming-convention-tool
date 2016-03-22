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


import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import org.openepics.names.model.DeviceRevision;
import org.openepics.names.services.views.DeviceView;
import org.openepics.names.ui.export.ExcelExport;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;

import javax.annotation.Nullable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * A UI controller bean for the Device Names screen.
 */
@ManagedBean
@ViewScoped
public class DevicesController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Inject private ExcelExport excelExport;


//	private TreeNode viewDevice;
	private TreeNode deleteView;
//
//	private String deviceNameFilter, appliedDeviceNameFilter = "";
//	private String deviceTypeFilter, appliedDeviceTypeFilter = "";
	


//	public String sectionPath(DeviceView deviceView) {
//		return Joiner.on(" ▸ ").join(deviceView.getSection().getNamePath());
//	}


//	public @Nullable String getDeviceNameFilter() { return deviceNameFilter; }
//	public void setDeviceNameFilter(@Nullable String deviceNameFilter) { this.deviceNameFilter = deviceNameFilter; }
//
//	public @Nullable String getDeviceTypeFilter() { return deviceTypeFilter; }
//	public void setDeviceTypeFilter(@Nullable String deviceTypeFilter) { this.deviceTypeFilter = deviceTypeFilter; }


//	public TreeNode getViewDevice() { return viewDevice; }
//	public void setViewDevice(TreeNode viewDevice) { this.viewDevice = viewDevice; }

//	public void clearDeviceNameFilter() {
//		deviceNameFilter = null;
//		checkForFilterChanges();
//	}
//
//	public void clearDeviceTypeFilter() {
//		deviceTypeFilter = null;
//		checkForFilterChanges();
//	}
//
////	public void checkForFilterChanges() {
//		final boolean filterHasChanged = !Objects.equals(deviceNameFilter, appliedDeviceNameFilter) || !Objects.equals(deviceTypeFilter, appliedDeviceTypeFilter);
//		if (filterHasChanged) {
//			appliedDeviceNameFilter = deviceNameFilter;
//			appliedDeviceTypeFilter = deviceTypeFilter;
////			viewDevice = filteredView(viewRoot);
//			RequestContext.getCurrentInstance().update("ManageNameForm:devicesTree");
//		}        
//	}


//	public TreeNode getNode(DeviceRevision deviceRevision){
//		for(TreeNode node: getNodeList(viewDevice)){
//				if(node.getData() instanceof DeviceView){
//					DeviceRevision device=((DeviceView) node.getData()).getDevice();
//					if(device.getConventionName().equals(deviceRevision.getConventionName())){
//						return node;
//					};
//				}
//		}
//		return null;
//	}

	
	
	
//	private List<TreeNode> getNodeList(TreeNode node) {
//		final List<TreeNode> nodeList= Lists.newArrayList();
//		nodeList.add(node);
//		for(TreeNode child:node.getChildren()){
//			nodeList.addAll(getNodeList(child));
//		}
//		return nodeList;
//	}
	public TreeNode getDeleteView() { return deleteView; }

//	public boolean canDelete() { return deleteView != null; }
//	public boolean canAdd() { return true;}



	public StreamedContent getAllDataExport() {  
		return new DefaultStreamedContent(excelExport.exportFile(), "xlsx", "NamingConventionExport.xlsx");
	}

	public String historyRevisionStyleClass(DeviceView req) {
		return req != null && req.getDevice().isDeleted() ? "Deleted" : "Approved";
	}


//	public String deviceTypePath(DeviceView deviceView) {
//		return Joiner.on(" ▸ ").join(deviceView.getDeviceType().getNamePath());
//	}



	}
