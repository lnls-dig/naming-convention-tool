/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openepics.names.ui;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import org.openepics.names.model.NamePartRevisionStatus;

/**
 *
 * @author Miha Vitorovic <miha.vitorovic@cosylab.com>
 */
@FacesConverter(forClass = NamePartRevisionStatus.class)
public class NamePartRevisionStatusConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        try {
            if(value == null) return null;
            return NamePartRevisionStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new ConverterException("Expecting NamePartRevisionStatus name at FaceContext: "
                    + context.getApplication().toString() + " UI:" + component.getId());
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if(!(value instanceof NamePartRevisionStatus))
            throw new ConverterException("Expecting NamePartRevisionStatus at FaceContext: "
                    + context.getApplication().toString() + " UI:" + component.getId());
        return ((NamePartRevisionStatus)value).name();
    }
}
