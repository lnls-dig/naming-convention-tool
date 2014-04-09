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
 * The validator for the Instance Index field in the Modify form.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@RequestScoped
@FacesValidator("custom.modifyInstanceIndexValidator")
public class ModifyInstanceIndexValidator implements Validator {

    @Override public void validate(FacesContext facesContext, UIComponent uiComponent, Object o) throws ValidatorException {
        final DevicesController controller = (DevicesController) facesContext.getApplication().getExpressionFactory().createValueExpression(facesContext.getELContext(), "#{devicesController}", Object.class).getValue(facesContext.getELContext());
        final @Nullable String instanceIndex = normalize((String) o);

        if (!controller.isModifyInstanceIndexValid(instanceIndex)) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "The instance index does not conform to the Naming Convention rules."));
        } else if (!controller.isModifyInstanceIndexUnique(instanceIndex)) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "The instance index results in a non-unique device name."));
        } else {
            Marker.doNothing();
        }
    }

    private String normalize(String input) {
        return !input.isEmpty() ? input : null;
    }
}
