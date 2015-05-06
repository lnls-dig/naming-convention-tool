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

import javax.annotation.Nullable;
import javax.persistence.*;
import java.io.Serializable;

/**
 * A superclass implementing the properties required by JPA. It that should be extended by all classes that need to be
 * persisted to the database.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@MappedSuperclass
public class Persistable implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected @Nullable Long id;

    @Version
    private @Nullable Integer version;

    /**
     * The JPA entity ID.
     */
    public @Nullable Long getId() { return id; }

    /**
     * The JPA entity version.
     */
    public @Nullable Integer getVersion() { return version; }
}
