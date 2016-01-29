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

package org.openepics.names.ui.parts;

import org.openepics.names.util.Marker;

import javax.faces.application.FacesMessage;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 * The validator for the Mnemonic field in the Modify form.
 *
 * @author Marko Kolar  
 * @author Karin Rathsman  
 */
@RequestScoped
@FacesValidator("custom.modifyMnemonicValidator")
public class ModifyMnemonicValidator implements Validator {

    @Override public void validate(FacesContext facesContext, UIComponent uiComponent, Object o) throws ValidatorException {
        final NamePartsController controller = (NamePartsController) facesContext.getApplication().getExpressionFactory().createValueExpression(facesContext.getELContext(), "#{namePartsController}", Object.class).getValue(facesContext.getELContext());

        if (!controller.isModifyMnemonicValid((String) o)) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "The mnemonic does not conform to the Naming Convention rules."));
        } else if ( controller.isMnemonicRequired() && !controller.isModifyMnemonicUnique((String) o)) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "The mnemonic is not unique or is too similar to an existing one."));
        } else {
            Marker.doNothing();
        }
    }
}
