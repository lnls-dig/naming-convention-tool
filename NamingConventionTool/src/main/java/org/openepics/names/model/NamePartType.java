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

/**
 * Type of a NamePart specifying whether it belongs to the Area Structure or the Device Structure.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public enum NamePartType {
    /** A (sub)section of the Logical Area Structure. */
    SECTION,

    /** A device (sub)type of the Device Category Structure. */
    DEVICE_TYPE
}
