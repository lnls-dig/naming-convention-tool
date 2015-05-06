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

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import java.util.Iterator;

/**
 * A global JSF exception handler that displays caught exceptions in the UI as popup messages.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public class CustomExceptionHandler extends ExceptionHandlerWrapper {
    
    private final ExceptionHandler wrapped;

    public CustomExceptionHandler(ExceptionHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override public ExceptionHandler getWrapped() { return this.wrapped; }

    @Override public void handle() throws FacesException {
        final Iterator iterator = getUnhandledExceptionQueuedEvents().iterator();
        while (iterator.hasNext()) {
            final ExceptionQueuedEvent event = (ExceptionQueuedEvent) iterator.next();
            final ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
            final Throwable throwable = context.getException();
            try {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Unexpected error", throwable.getMessage()));
            } finally {
                iterator.remove();
            }
        }
        wrapped.handle();
    }
}
