package org.openntf.xsp.extlib.query;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

public class ContainsFilter extends QueryFilter {
	protected static Logger _logger;
	static {
		_logger = Logger.getLogger(ContainsFilter.class.getName());
	}
	
	public ContainsFilter(String propertyName, Object content) {
		super(propertyName, content);
	}

	@Override
	public boolean matches(UIComponent component, FacesContext context) {
		boolean result = false;
		if (isCompatibleWith(component)) {
			Object currentValue = getPropertyValue(component);
			Object content = getContent();
			if (currentValue instanceof String
					&& content instanceof CharSequence) {
				result = ((String) currentValue)
						.contains((CharSequence) content);
			}
		}
		return result;
	}
}