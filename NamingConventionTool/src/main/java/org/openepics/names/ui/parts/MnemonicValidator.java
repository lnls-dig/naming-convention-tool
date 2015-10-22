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
 * The validator for the Mnemonic field in the Add form.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@RequestScoped
@FacesValidator("custom.mnemonicValidator")
public class MnemonicValidator implements Validator {

    @Override public void validate(FacesContext facesContext, UIComponent uiComponent, Object o) throws ValidatorException {
        final NamePartsController controller = (NamePartsController) facesContext.getApplication().getExpressionFactory().createValueExpression(facesContext.getELContext(), "#{namePartsController}", Object.class).getValue(facesContext.getELContext());
        
        if (!controller.isMnemonicValid((String) o)) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Mnemonic: Validation Error:","Value is not valid accoding to the naming convention rules"));
        } else if ( !controller.isMnemonicUnique((String) o) && controller.isMnemonicRequired())  {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Mnemonic: Validation Error:"," Value is not unique according to the naming conveniton rules"));
//        } else if((controller.getFormMnemonic().length()>4 || controller.getFormMnemonic().length()<2)&& controller.isMnemonicRequired()){
//        	throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_WARN,"Mnemonic: Warning:"," Value should range between 2 and 4 characters although 1-6 characters are allowed."));
        	
        } else {
            Marker.doNothing();
        }
    }
}
