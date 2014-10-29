package converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.openntf.domino.Name;
import org.openntf.domino.utils.DominoUtils;
import org.openntf.domino.utils.Factory;

public class IterableCommonNameConverter implements Converter {

	/* (non-Javadoc)
	 * @see javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext, javax.faces.component.UIComponent, java.lang.String)
	 */
	public Object getAsObject(final FacesContext context, final UIComponent component, final String value) {
		return value;
	}

	/* (non-Javadoc)
	 * @see javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext, javax.faces.component.UIComponent, java.lang.Object)
	 */
	public String getAsString(final FacesContext context, final UIComponent component, final Object value) {
		if (value instanceof Iterable) {
			StringBuilder result = new StringBuilder();
			boolean appended = false;
			for (Object node : (Iterable<?>) value) {
				if (appended)
					result.append(", ");
				else
					appended = true;
				String personName = String.valueOf(node);
				result.append(DominoUtils.toCommonName(personName));
			}
			return result.toString();
		}
		String personName = String.valueOf(value);
		if (!DominoUtils.isHierarchicalName(personName)) {
			Name uName = Factory.getSession().createName(personName);
			personName = uName.getCanonical();
		}
		return DominoUtils.toCommonName(personName);
	}

}