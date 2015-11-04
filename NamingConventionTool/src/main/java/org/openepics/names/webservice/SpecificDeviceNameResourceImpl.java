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
package org.openepics.names.webservice;

import java.util.UUID;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.openepics.names.jaxb.DeviceNameElement;
import org.openepics.names.jaxb.SpecificDeviceNameResource;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.services.NamePartService;
import org.openepics.names.services.views.DeviceView;
import org.openepics.names.ui.common.ViewFactory;
import org.openepics.names.util.As;

/**
 * This is implementation of {@link SpecificDeviceNameResource} interface.
 * 
 * @author Andraz Pozar <andraz.pozar@cosylab.com>
 */
@Stateless
public class SpecificDeviceNameResourceImpl implements SpecificDeviceNameResource {
	@Inject private NamePartService namePartService;
	@Inject private ViewFactory viewFactory;
	@Override
	public DeviceNameElement getDeviceName(String reqUuid) {
		final @Nullable DeviceRevision deviceRevision=getDeviceRevsion(reqUuid);
		if (deviceRevision != null) {			
			if(!deviceRevision.isDeleted()){
				final DeviceNameElement deviceData = new DeviceNameElement(deviceRevision.getDevice().getUuid(),deviceRevision.getConventionName(),"ACTIVE");
				final @Nullable DeviceView deviceView = deviceRevision !=null? viewFactory.getView(deviceRevision):null;
				deviceData.setSection(As.notNull(deviceView.getSection().getParent()).getMnemonic());
				deviceData.setSubSection(deviceView.getSection().getMnemonic());
				deviceData.setDiscipline(As.notNull(As.notNull(deviceView.getDeviceType().getParent()).getParent()).getMnemonic());
				deviceData.setDeviceType(deviceView.getDeviceType().getMnemonic());
				deviceData.setInstanceIndex(deviceView.getInstanceIndex());
				deviceData.setName(deviceView.getConventionName());
				return deviceData;
			} else {
				return new DeviceNameElement(deviceRevision.getDevice().getUuid(),deviceRevision.getConventionName(),"DELETED");
			}
		} else {		
			final @Nullable DeviceRevision obsoleteRevision=namePartService.latestObsoleteDeviceRevisionNamed(reqUuid);
			if(obsoleteRevision==null){
				return null;
			}else if(obsoleteRevision.isDeleted()){
				return new DeviceNameElement(obsoleteRevision.getDevice().getUuid(),obsoleteRevision.getConventionName(),"DELETED");
			} else {
				return new DeviceNameElement(obsoleteRevision.getDevice().getUuid(),obsoleteRevision.getConventionName(),"OBSOLETE");
			}
		}
	}

	private @Nullable DeviceRevision getDeviceRevsion(String string) {
		UUID uuid;
		try {
			uuid=UUID.fromString(string);
		} catch (Exception e) {
			uuid=null;
		}

		if (uuid!=null){
			return namePartService.currentDeviceRevision(uuid);			
		} else {
			DeviceRevision deviceRevision=  namePartService.currentDeviceRevision(string);
			return deviceRevision;
		}

	}
}
