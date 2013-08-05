package converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import com.ibm.xsp.extlib.util.ExtLibUtil;

import lotus.domino.*;

public class CommonNameConverter implements Converter {

	public Object getAsObject(final FacesContext context, final UIComponent component, final String value) {
		return value;
	}

	public String getAsString(final FacesContext context, final UIComponent component, final Object value) {
		if (value instanceof String) {
			return toCommon((String) value);
		} else if (value instanceof Iterable) {
			StringBuilder result = new StringBuilder();
			boolean appended = false;
			for (Object node : (Iterable<?>) value) {
				if (node != null) {
					if (appended) {
						result.append(", ");
					} else {
						appended = true;
					}
					result.append(node.toString());
				}
			}
			return result.toString();
		}
		return String.valueOf(value);
	}

	private String toCommon(final String value) {
		try {
			Session session = ExtLibUtil.getCurrentSession();
			Name name = session.createName(value);
			String result = name.getCommon();
			name.recycle();
			return result;
		} catch (NotesException ne) {
			return ne.toString();
		}
	}

}