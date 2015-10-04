package frostillicus.xsp.controller;

import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import com.ibm.xsp.complex.ValueBindingObject;
import com.ibm.xsp.component.UIOutputLabel;
import com.ibm.xsp.util.FacesUtilExtsn;
import com.ibm.xsp.validator.AbstractValidator;

/**
 * @since 1.0
 */
public class ArbitraryValidator extends AbstractValidator implements ValueBindingObject {
	private Class<?> clazz_;
	private String property_;

	public ArbitraryValidator() { }
	public ArbitraryValidator(final Class<?> clazz, final String property) {
		clazz_ = clazz;
		property_ = property;
	}

	@Override
	public void validate(final FacesContext context, final UIComponent c, final Object value) throws ValidatorException {
		UIOutputLabel label = (UIOutputLabel)FacesUtilExtsn.getLabelFor(c);
		Validator validator = Validation.byDefaultProvider().configure().buildValidatorFactory().getValidator();
		Set<?> violations = validator.validateValue(clazz_, property_, value);
		if(!violations.isEmpty()) {
			ConstraintViolation<?> violation = (ConstraintViolation<?>)violations.iterator().next();
			String propDisplay = label == null ? property_ : (String)label.getValue();
			throw new ValidatorException(new FacesMessage(propDisplay + ": ", violation.getMessage()));
		}
	}

	@Override
	public Object saveState(final FacesContext context) {
		Object[] state = new Object[3];
		state[0] = super.saveState(context);
		state[1] = clazz_;
		state[2] = property_;
		return state;
	}
	@Override
	public void restoreState(final FacesContext context, final Object stateObj) {
		Object[] state = (Object[])stateObj;
		super.restoreState(context, state[0]);
		clazz_ = (Class<?>)state[1];
		property_ = (String)state[2];
	}
}