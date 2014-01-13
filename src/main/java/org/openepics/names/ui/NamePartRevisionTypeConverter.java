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
import org.openepics.names.model.NamePartRevisionType;

/**
 *
 * @author Miha Vitorovic <miha.vitorovic@cosylab.com>
 */
@FacesConverter(forClass = NamePartRevisionType.class)
public class NamePartRevisionTypeConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        try {
            if (value == null) return null;
            return NamePartRevisionType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new ConverterException("Expecting NamePartRevisionType name at FaceContext: "
                    + context.getApplication().toString() + " UI:" + component.getId());
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (!(value instanceof NamePartRevisionType))
            throw new ConverterException("Expecting NamePartRevisionType at FaceContext: "
                    + context.getApplication().toString() + " UI:" + component.getId());
        return ((NamePartRevisionType) value).name();
    }
}
