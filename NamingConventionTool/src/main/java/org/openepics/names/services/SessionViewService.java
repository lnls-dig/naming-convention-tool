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
import java.util.Map;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

@SessionScoped
public class SessionViewService implements Serializable{
	
    private static final long serialVersionUID = 827187290632697101L;
    /**
	 * Bean to preserve status of tree node (expanded or collapsed) during a session. 
	 * @author Karin Rathsman <karin.rathsman@esss.se>
	 */
	private Map<Object,Boolean> nodeMap = new HashMap<Object,Boolean>(); 

	@Inject
	public SessionViewService(){
		init();
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
			return object!=null ? nodeMap.get(object): true;			
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
}
