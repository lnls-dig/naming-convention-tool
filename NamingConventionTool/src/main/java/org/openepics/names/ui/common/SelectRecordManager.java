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

package org.openepics.names.ui.common;

import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

import org.openepics.names.services.SessionViewService;
import org.openepics.names.services.views.DeviceRecordView;

/**
 * @author Karin Rathsman  
 *
 */

@ManagedBean
@ViewScoped
public class SelectRecordManager{

	@Inject private SessionViewService sessionViewService;
	/**
	 * 
	 */
	@PostConstruct
	public void init(){
//		sessionViewService.updateSelectedRecords();
	}
		
	public void setSelectedRecords(@Nullable List<DeviceRecordView> selectedRecords) {
		sessionViewService.setSelectedRecords(selectedRecords);
	}

	public @Nullable List<DeviceRecordView> getSelectedRecords() {
		return sessionViewService.getSelectedRecords();
	}
	
	public @Nullable DeviceRecordView getSelectedRecord(){
		return sessionViewService.getSelectedRecord();
	}
	
	public boolean isIncludeDeleted(){
		return sessionViewService.isIncludeDeleted();
	}

	public void setIncludeDeleted(boolean includeDeleted){
		sessionViewService.setIncludeDeleted(includeDeleted);
	}

//	public boolean isSelected(DeviceRecordView record){
//		DeviceRecordView selected=sessionViewService.getSelectedRecord();
//		return selected!=null? selected.equals(record):false;
//	}

}
