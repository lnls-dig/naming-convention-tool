package org.openepics.names.ui.common;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;

/**
 * A factory for the CustomExceptionHandler. Used to define a global JSF exception handler through faces-config.xml.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public class CustomExceptionHandlerFactory extends ExceptionHandlerFactory {

    private final ExceptionHandlerFactory parent;

    public CustomExceptionHandlerFactory(ExceptionHandlerFactory parent) {
        this.parent = parent;
    }
    
    @Override public ExceptionHandler getExceptionHandler() { return new CustomExceptionHandler(parent.getExceptionHandler()); }
}
