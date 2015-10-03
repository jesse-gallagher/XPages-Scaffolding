package org.openntf.xsp.extlib.query;

import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

public class StartsWithFilter extends QueryFilter {
	protected static Logger _logger;
	static {
		_logger = Logger.getLogger(StartsWithFilter.class.getName());
	}

	public StartsWithFilter(final String propertyName, final String content) {
		super(propertyName, content);
	}

	@Override
	public boolean matches(final UIComponent component, final FacesContext context) {
		boolean result = false;
		if (isCompatibleWith(component)) {
			Object propertyValue = getPropertyValue(component);
			if (propertyValue instanceof String
					&& getContent() instanceof String) {
				result = ((String) propertyValue).toLowerCase().startsWith(
				                                                           ((String) getContent()).toLowerCase());
			}
		}
		return result;
	}

}
