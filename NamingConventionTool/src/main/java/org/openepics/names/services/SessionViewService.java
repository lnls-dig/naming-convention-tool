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

import org.openepics.names.services.views.DeviceRecordView;
import com.google.common.collect.Lists;

/**
 * Bean to preserve status of tree node (expanded or collapsed) during a session. 
 * @author Karin Rathsman 
 */
@SessionScoped
public class SessionViewService implements Serializable{

	private static final long serialVersionUID = 827187290632697101L;
	private Map<Object,Boolean> nodeMap = new HashMap<Object,Boolean>();
	private List<DeviceRecordView> selectedRecords;

	@Inject
	public SessionViewService(){
		init();
		selectedRecords=null;
	}

	public void init(){
		if(nodeMap==null){
			nodeMap = new HashMap<Object,Boolean>();
		}
	}	

	public boolean isExpanded(Object object){
		try {
			return object!=null ? nodeMap.get(object): true;			
		} catch (Exception e) {
			collapse(object);
			return false;			
		}
	}

	public void expand(Object object) {
		nodeMap.put(object, true);		
	}

	public void collapse(Object object) {
		nodeMap.put(object, false);
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
				if(!deviceRecordView.getDeviceView().getDevice().isDeleted()){
					temporary.add(deviceRecordView);
				}
			}
			selectedRecords=temporary;
		}
	}

	public @Nullable DeviceRecordView getSelectedRecord() {
		return getSelectedRecords()!=null && getSelectedRecords().size()==1? getSelectedRecords().get(0):null;		
	}
}
