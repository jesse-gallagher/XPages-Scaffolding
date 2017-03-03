package frostillicus.xsp.converter;

import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 * @since 1.0
 */
public class IterableConverter implements Converter, StateHolder {
	private boolean transient_;
	private String delimiter_;

	@Override
	public Object getAsObject(final FacesContext facesContext, final UIComponent component, final String value) {
		return value;
	}

	@Override
	public String getAsString(final FacesContext facesContext, final UIComponent component, final Object value) {
		if (value instanceof Iterable) {
			StringBuilder result = new StringBuilder();
			boolean appended = false;
			for (Object node : (Iterable<?>) value) {
				if (appended)
					result.append(delimiter_);
				else
					appended = true;
				result.append(String.valueOf(node));
			}
			return result.toString();
		}
		return String.valueOf(value);
	}

	public void setDelimiter(String delimiter) {
		delimiter_ = delimiter;
	}
	public String getDelimiter() { return delimiter_; }

	@Override
	public void setTransient(boolean newTransient) {
		transient_ = newTransient;
	}
	@Override
	public boolean isTransient() { return transient_; }

	@Override
	public void restoreState(FacesContext facesContext, Object state) {
		Object[] stateArray = (Object[])state;
		delimiter_ = (String)stateArray[0];
	}
	@Override
	public Object saveState(FacesContext facesContext) {
		return new Object[] { delimiter_ };
	}
}