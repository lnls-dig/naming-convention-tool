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

package org.openepics.names.ui.devices;

import org.openepics.names.util.Marker;

import javax.annotation.Nullable;
import javax.faces.application.FacesMessage;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 * The validator for the Instance index field in the Add form.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@RequestScoped
@FacesValidator("custom.addInstanceIndexValidator")
public class AddInstanceIndexValidator implements Validator {

    @Override public void validate(FacesContext facesContext, UIComponent uiComponent, Object o) throws ValidatorException {
        final DevicesController controller = (DevicesController) facesContext.getApplication().getExpressionFactory().createValueExpression(facesContext.getELContext(), "#{devicesController}", Object.class).getValue(facesContext.getELContext());
        final @Nullable String instanceIndex = normalize((String) o);

        if (!controller.isAddInstanceIndexValid(instanceIndex)) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "The instance index does not conform to the Naming Convention rules."));
        } else if (!controller.isAddInstanceIndexUnique(instanceIndex)) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "The instance index results in a non-unique device name."));
        } else {
            Marker.doNothing();
        }
    }

    private String normalize(String input) {
        return !input.isEmpty() ? input : null;
    }
}
