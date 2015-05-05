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

import org.openepics.names.model.NamePartType;
import org.openepics.names.util.NotImplementedException;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.enterprise.inject.Alternative;

import java.util.List;

/**
 * An empty stub for the naming convention used by FRIB.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 * @author Karin Rathsman <karin.rathsman@esss.se>
 */
@Alternative
@Stateless
public class FribNamingConvention implements NamingConvention {


    @Override public boolean isInstanceIndexValid(List<String> sectionPath, List<String> deviceTypePath, @Nullable String instanceIndex) {
        throw new NotImplementedException();
    }

    @Override public String equivalenceClassRepresentative(String name) {
        throw new NotImplementedException();
    }

    @Override public String conventionName(List<String> sectionPath, List<String> deviceTypePath, @Nullable String instanceIndex) {
        throw new NotImplementedException();
    }

    @Override public boolean canMnemonicsCoexist(List<String> newMnemonicPath, NamePartType newMnemonicType, List<String> comparableMnemonicPath, NamePartType comparableMnemonicType) {
        throw new NotImplementedException();
    }

	@Override
	public String deviceDefinition(List<String> deviceTypePath) {
		throw new NotImplementedException();
	}

	@Override
	public String areaName(List<String> sectionPath) {
		throw new NotImplementedException();
	}

	@Override
	public boolean isMnemonicRequired(List<String> mnemonicPath, NamePartType mnemonicType) {
		throw new NotImplementedException();
	}

	@Override
	public String getNamePartTypeName(List<String> sectionPath, NamePartType namePartType) {
		throw new NotImplementedException();
	}

	@Override
	public boolean isMnemonicValid(List<String> mnemonicPath, NamePartType mnemonicType) {
		throw new NotImplementedException();
	}

	@Override
	public String getNamePartTypeMnemonic(List<String> sectionPath, NamePartType namePartType) {
		throw new NotImplementedException();
	}
}
