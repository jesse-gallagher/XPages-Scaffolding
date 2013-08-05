package converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

public class IterableCommaConverter implements Converter {

	public Object getAsObject(final FacesContext context, final UIComponent component, final String value) {
		return value;
	}

	public String getAsString(final FacesContext context, final UIComponent component, final Object value) {
		if (value instanceof Iterable) {
			StringBuilder result = new StringBuilder();
			boolean appended = false;
			for (Object node : (Iterable<?>) value) {
				if (appended)
					result.append(", ");
				else
					appended = true;
				result.append(String.valueOf(node));
			}
			return result.toString();
		}
		return String.valueOf(value);
	}

}