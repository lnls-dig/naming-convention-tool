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
package org.openepics.names.services.views;

import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;

import javax.annotation.Nullable;

/**
 * An interface for retrieving revisions of NameParts.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public interface NamePartRevisionProvider {

    /**
     * The relevant (usually current) approved revision of the namePart.
     *
     * @param namePart the name part to retrieve the revision for
     */
    @Nullable NamePartRevision approvedRevision(NamePart namePart);

    /**
     * The relevant (usually current) pending revision of the namePart.
     *
     * @param namePart the name part to retrieve the revision for
     */
    @Nullable NamePartRevision pendingRevision(NamePart namePart);
}
