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
package org.openepics.names.services;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

import org.openepics.names.model.NamePart;
import org.openepics.names.services.views.DeviceRecordView;
import com.google.common.collect.Lists;

/**
 * Bean to preserve status of tree node (expanded or collapsed) during a session. 
 * @author Karin Rathsman 
 */
@SessionScoped
public class SessionViewService implements Serializable{

	private static final long serialVersionUID = 827187290632697101L;
	private Map<Object,NodeStatus> nodeMap;
	private List<DeviceRecordView> selectedRecords;
	private boolean includeDeleted;
	private boolean includeOffsite;

	@Inject
	public SessionViewService(){
		nodeMap= new HashMap<Object,NodeStatus>();
		selectedRecords=null;
		setIncludeDeleted(false);
	}

	private NodeStatus nodeStatus(Object object){
		if(nodeMap==null){
			nodeMap= new HashMap<Object,NodeStatus>();
		} 
		if (!nodeMap.containsKey(object)){
			nodeMap.put(object, new NodeStatus());
		}
		return nodeMap.get(object);
	}
	
	/**
	 * 
	 * @param object node object
	 * @return true if object is expanded or object is null.
	 */
	public boolean isExpanded(@Nullable Object object){
		return object!=null ? nodeStatus(object).isExpanded():true; 
	}
	
	/**
	 * 
	 * @param object node object
	 * @return true if object is selected. False if object is null.
	 */
	public boolean isSelected(@Nullable Object object){
		return object!=null ? nodeStatus(object).isSelected() :false;
	}

	/**
	 * 
	 * @param object node object
	 * @return true if object is filtered. False if object is null
	 */
	public boolean isFiltered(@Nullable Object object) {
		return object!=null ? nodeStatus(object).isFiltered() :false;
	}

	
	/**
	 * expands object
	 * @param object data with node status.
	 */
	public void expand(@Nullable Object object) {
		if(object!=null){
			nodeStatus(object).setExpanded(true);		
		}
	}

	/**
	 * collapses object
	 * @param object data with node status.
	 */
	public void collapse(Object object) {
		if(object!=null){
			nodeStatus(object).setExpanded(false);		
		}
	}

	/**
	 * selects object
	 * @param object data with node status.
	 */
	public void select(Object object) {
		if(object!=null){
			nodeStatus(object).setSelected(true);		
		}
	}

	/**
	 * unselects object
	 * @param object data with node status.
	 */
	public void unselect(Object object) {
		if(object!=null){
			nodeStatus(object).setSelected(false);		
		}
	}
	
	public void unfilter(Object object) {
		if(object!=null){
			nodeStatus(object).setFiltered(false);		
		}
		
	}

	public void filter(Object object) {
		if(object!=null){
			nodeStatus(object).setFiltered(true);		
		}
	}

	@PreDestroy
	public void cleanup(){
		nodeMap.clear();
	}
		
	public void setSelectedRecords(@Nullable List<DeviceRecordView> selectedRecords) {
		this.selectedRecords=selectedRecords;
	}

	public @Nullable List<DeviceRecordView> getSelectedRecords() {
		return selectedRecords;
	}

	public void updateSelectedRecords(){
		if(selectedRecords!=null){
			List<DeviceRecordView> temporary=Lists.newArrayList();
			for (DeviceRecordView deviceRecordView : selectedRecords) {
//				if(!deviceRecordView.getDeviceView().getDevice().isDeleted()){
				if(!deviceRecordView.isDeleted()){
					temporary.add(deviceRecordView);
				}
			}
			selectedRecords=temporary;
		}
	}

	public @Nullable DeviceRecordView getSelectedRecord() {
		return getSelectedRecords()!=null && getSelectedRecords().size()==1? getSelectedRecords().get(0):null;
	}
	
	
	/**
	 * @return the includeDeleted
	 */
	public boolean isIncludeDeleted() {
		return includeDeleted;
	}

	/**
	 * @param includeDeleted the includeDeleted to set
	 */
	public void setIncludeDeleted(boolean includeDeleted) {
		this.includeDeleted = includeDeleted;
	}


	/**
	 * @return the includeOffsite
	 */
	public boolean isIncludeOffsite() {
		return includeOffsite;
	}

	/**
	 * @param includeOffsite the includeOffsite to set
	 */
	public void setIncludeOffsite(boolean includeOffsite) {
		this.includeOffsite = includeOffsite;
	}


	public class NodeStatus {
		private boolean expanded;
		private boolean selected;
		private boolean filtered;
		
		private NodeStatus(){
			this.expanded=false;
			this.selected=false;
			this.filtered=true;
		}
		/**		
		 * @param filtered the filtered to set
		 */
		public void setFiltered(boolean filtered) {
			this.filtered=filtered;
		}
		
		/**
		 * 
		 * @return true if filtered
		 */
		public boolean isFiltered(){
			return filtered;
		}
		
		/**
		 * @return true if expanded
		 */
		public boolean isExpanded() {
			return expanded;
		}
		/**
		 * @param expanded the expanded to set
		 */
		public void setExpanded(boolean expanded) {
			this.expanded = expanded;
		}
		/**
		 * @return true if selected
		 */
		public boolean isSelected() {
			return selected;
		}
		/**
		 * @param selected the selected to set
		 */
		public void setSelected(boolean selected) {
			this.selected = selected;
		}	
	}


		 
}
