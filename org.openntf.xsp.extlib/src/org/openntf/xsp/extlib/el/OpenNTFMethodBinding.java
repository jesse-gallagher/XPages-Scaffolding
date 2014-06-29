package org.openntf.xsp.extlib.el;

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodNotFoundException;

import com.ibm.xsp.binding.MethodBindingEx;

public class OpenNTFMethodBinding extends MethodBindingEx {
	private String _expression;

	public OpenNTFMethodBinding() {
		super();
	}

	public OpenNTFMethodBinding(String expression) {
		super();
		_expression = expression;
	}

	@Override
	public Class<?> getType(FacesContext arg0) throws MethodNotFoundException {
		// TODO Determine what your resulting type for this method binding is
		return null;
	}

	@Override
	public Object invoke(FacesContext arg0, Object[] arg1) throws EvaluationException, MethodNotFoundException {
		// TODO Whatever execution behavior you want with a return of whatever you want
		return null;
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
