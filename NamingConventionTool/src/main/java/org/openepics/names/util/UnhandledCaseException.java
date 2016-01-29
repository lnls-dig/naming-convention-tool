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
package org.openepics.names.util;

/**
 * An exception signaling that an if/else-if/else or case branch that should be exhaustive encountered an unexpected
 * possibility.
 *
 * @author Marko Kolar  
 */
public class UnhandledCaseException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8307112021289371195L;
}
