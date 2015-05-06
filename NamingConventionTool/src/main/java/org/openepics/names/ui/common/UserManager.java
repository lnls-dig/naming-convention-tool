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

import org.openepics.names.services.SessionService;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

/**
 * A bean exposing the information about the logged in user to the UI.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@ManagedBean
@ViewScoped
public class UserManager {

    @Inject private SessionService sessionService;

    public boolean isLoggedIn() { return sessionService.isLoggedIn(); }
    public boolean isEditor() { return sessionService.isEditor(); }
    public boolean isSuperUser() { return sessionService.isSuperUser();} 
    public String getUsername() {return sessionService.getUsername();}
}
