package org.openntf.xsp.extlib.el;

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;

import com.ibm.xsp.binding.ValueBindingEx;

public class OpenNTFValueBinding extends ValueBindingEx {
	private String _expression;

	public OpenNTFValueBinding() {
		super();
	}

	public OpenNTFValueBinding(String expression) {
		super();
		_expression = expression;
	}

	@Override
	public Class<?> getType(FacesContext arg0) throws EvaluationException, PropertyNotFoundException {
		// TODO Insert your code that would determine the class to be returned
		// In this sample, we'll just always return a string
		return String.class;
	}

	@Override
	public Object getValue(FacesContext arg0) throws EvaluationException, PropertyNotFoundException {
		// TODO Insert your code that would generate the value to return
		// In this sample, we simply reflect the original expression...
		return _expression;
	}

	@Override
	public boolean isReadOnly(FacesContext arg0) throws EvaluationException, PropertyNotFoundException {
		// TODO Insert your code that determines whether the binding is readonly.
		// In this sample, we are always readonly
		return true;
	}

	@Override
	public void setValue(FacesContext arg0, Object arg1) throws EvaluationException, PropertyNotFoundException {
		// TODO Insert your code that does whatever you want from an active set on the value
		// In this sample, we do nothing

	}

	@Override
	public Object saveState(FacesContext paramFacesContext) {
		Object[] arrayOfObject = new Object[2];
		arrayOfObject[0] = super.saveState(paramFacesContext);
		arrayOfObject[1] = this._expression;
		return arrayOfObject;
	}

	@Override
	public void restoreState(FacesContext paramFacesContext, Object paramObject) {
		Object[] arrayOfObject = (Object[]) paramObject;
		super.restoreState(paramFacesContext, arrayOfObject[0]);
		this._expression = ((String) arrayOfObject[1]);
	}

}
