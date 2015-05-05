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
package org.openepics.names.model;

import com.google.common.base.Preconditions;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Objects;

/**
 * An entity representing a user account used to sing in to the application.
 *
 * @author Vasu V <vuppala@frib.msu.org>
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Entity
public class UserAccount extends Persistable {

    private String username;

    @Enumerated(EnumType.STRING) private Role role;

    protected UserAccount() {}

    /**
     * @param username the name identifying the user
     * @param role the role that determines the user's access control permissions
     */
    public UserAccount(String username, Role role) {
        Preconditions.checkArgument(username != null && !username.isEmpty());
        Preconditions.checkNotNull(role);
        this.username = username;
        this.role = role;
    }

    /**
     * The name identifying the user
     */
    public String getUsername() { return username; }

    /**
     * The role that determines the user's access control permissions
     */
    public Role getRole() { return role; }

    @Override public int hashCode() {
        return Objects.hashCode(username);
    }

    @Override public boolean equals(Object other) {
        return other instanceof UserAccount && ((UserAccount) other).getUsername().equals(getUsername());
    }
}
