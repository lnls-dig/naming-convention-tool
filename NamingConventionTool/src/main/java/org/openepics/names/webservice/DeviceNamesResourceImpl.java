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

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.openepics.names.jaxb.DeviceNameElement;
import org.openepics.names.jaxb.SpecificDeviceNameResource;
import org.openepics.names.jaxb.DeviceNamesResource;
import org.openepics.names.model.DeviceRevision;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartType;
import org.openepics.names.services.NamePartService;
import org.openepics.names.services.views.BatchViewProvider;
import org.openepics.names.services.views.DeviceView;
import org.openepics.names.util.As;

import com.google.common.collect.Lists;

/**
 * This is implementation of {@link DeviceNamesResource} interface.
 * 
 * @author Andraz Pozar <andraz.pozar@cosylab.com>
 */
@Stateless
public class DeviceNamesResourceImpl implements DeviceNamesResource {
	@Inject private NamePartService namePartService;
	@Inject private SpecificDeviceNameResource deviceNameResource;

	@Override
	public List<DeviceNameElement> getAllDeviceNames() {
		final List<NamePartRevision> sectionRevisions = namePartService.currentApprovedNamePartRevisions(NamePartType.SECTION, false);
		final List<NamePartRevision> deviceTypeRevisions = namePartService.currentApprovedNamePartRevisions(NamePartType.DEVICE_TYPE, false);
		final List<DeviceRevision> deviceRevisions = namePartService.currentDeviceRevisions(false);
		final BatchViewProvider viewProvider = new BatchViewProvider(sectionRevisions, deviceTypeRevisions, deviceRevisions);

		final List<DeviceNameElement> deviceNames = Lists.newArrayList();

		for (DeviceRevision deviceRevision : namePartService.currentDeviceRevisions(true)) {
			if(!deviceRevision.isDeleted()){
				final DeviceNameElement deviceData = new DeviceNameElement();
				deviceData.setStatus("ACTIVE");
				deviceData.setUuid(deviceRevision.getDevice().getUuid());
				deviceData.setSection(As.notNull(viewProvider.view(deviceRevision.getSection()).getParent()).getMnemonic());
				deviceData.setSubSection(viewProvider.view(deviceRevision.getSection()).getMnemonic());
				deviceData.setDiscipline(As.notNull(As.notNull(viewProvider.view(deviceRevision.getDeviceType()).getParent()).getParent()).getMnemonic());
				deviceData.setDeviceType(viewProvider.view(deviceRevision.getDeviceType()).getMnemonic());
				deviceData.setInstanceIndex(viewProvider.view(deviceRevision).getInstanceIndex());
				deviceData.setName(viewProvider.view(deviceRevision).getConventionName());
				deviceNames.add(deviceData);
			} else {
				final DeviceNameElement deviceData = new DeviceNameElement();
				deviceData.setStatus("DELETED");
				deviceData.setName(deviceRevision.getConventionName());
				deviceData.setUuid(deviceRevision.getDevice().getUuid());
				deviceNames.add(deviceData);
			}
		}
		for(DeviceRevision deviceRevision : namePartService.obsoleteDeviceRevisions()) {
			final DeviceNameElement deviceData = new DeviceNameElement();
			deviceData.setStatus("OBSOLETE");
			deviceData.setName(deviceRevision.getConventionName());
			deviceData.setUuid(deviceRevision.getDevice().getUuid());
			deviceNames.add(deviceData);
		}

		return deviceNames;
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
			DeviceRevision deviceRevision=namePartService.currentDeviceRevision(string);
			return deviceRevision;
		}
	}

	@Override
	public SpecificDeviceNameResource getSpecificDeviceNameSubresource() {
		return deviceNameResource;
	}
}
