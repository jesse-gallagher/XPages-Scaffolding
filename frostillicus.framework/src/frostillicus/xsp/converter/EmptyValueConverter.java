package frostillicus.xsp.converter;

import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import com.ibm.commons.util.StringUtil;

public class EmptyValueConverter implements Converter, StateHolder {
	private boolean transient_;
	private String emptyString_;

	@Override
	public Object getAsObject(final FacesContext context, final UIComponent component, final String value) {
		return value;
	}

	@Override
	public String getAsString(final FacesContext context, final UIComponent component, final Object value) {
		String emptyString = getEmptyString();
		if(StringUtil.isEmpty(emptyString)) {
			emptyString = "(empty)";
		}

		if(value == null) {
			return emptyString;
		}
		String stringValue = value.toString();
		if(StringUtil.isEmpty(stringValue)) {
			return emptyString;
		}

		return stringValue;
	}


	public void setEmptyString(final String emptyString) {
		emptyString_ = emptyString;
	}
	public String getEmptyString() { return emptyString_; }

	@Override
	public void setTransient(final boolean newTransient) {
		transient_ = newTransient;
	}
	@Override
	public boolean isTransient() { return transient_; }

	@Override
	public void restoreState(final FacesContext facesContext, final Object state) {
		Object[] stateArray = (Object[])state;
		emptyString_ = (String)stateArray[0];
	}
	@Override
	public Object saveState(final FacesContext facesContext) {
		return new Object[] { emptyString_ };
	}
}
