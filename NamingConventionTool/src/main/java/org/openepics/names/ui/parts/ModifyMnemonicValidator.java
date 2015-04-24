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
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@RequestScoped
@FacesValidator("custom.modifyMnemonicValidator")
public class ModifyMnemonicValidator implements Validator {

    @Override public void validate(FacesContext facesContext, UIComponent uiComponent, Object o) throws ValidatorException {
        final NamePartsController controller = (NamePartsController) facesContext.getApplication().getExpressionFactory().createValueExpression(facesContext.getELContext(), "#{namePartsController}", Object.class).getValue(facesContext.getELContext());

        if (!controller.isModifyMnemonicValid((String) o)) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "The mnemonic does not conform to the Naming Convention rules."));
        } else if ( controller.isModifyMnemonicRequired() && !controller.isModifyMnemonicUnique((String) o)) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "The mnemonic is not unique or is too similar to an existing one."));
        } else {
            Marker.doNothing();
        }
    }
}
