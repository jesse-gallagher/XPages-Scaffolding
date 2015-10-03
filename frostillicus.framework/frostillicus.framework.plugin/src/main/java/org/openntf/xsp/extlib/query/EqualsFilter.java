package org.openntf.xsp.extlib.query;

import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

public class EqualsFilter extends QueryFilter {
	protected static Logger _logger;
	static {
		_logger = Logger.getLogger(EqualsFilter.class.getName());
	}

	public EqualsFilter(final String propertyName, final Object content) {
		super(propertyName, content);
	}

	@Override
	public boolean matches(final UIComponent component, final FacesContext context) {
		boolean result = false;
		if (isCompatibleWith(component)) {
			Object propertyValue = getPropertyValue(component);
			if (propertyValue instanceof String
					&& getContent() instanceof String) {
				result = ((String) propertyValue)
						.equalsIgnoreCase((String) getContent());
			} else {
				if (propertyValue != null) {
					result = propertyValue.equals(getContent());
				}
			}
		}
		return result;
	}

}
