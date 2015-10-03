package org.openntf.xsp.extlib.query;

import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

@SuppressWarnings("rawtypes")
public class InstanceFilter extends QueryFilter {
	private Class klass;
	protected static Logger _logger;
	static {
		_logger = Logger.getLogger(InstanceFilter.class.getName());
	}

	public InstanceFilter(final Class klass) {
		setKlass(klass);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean matches(final UIComponent component, final FacesContext context) {
		return getKlass().isAssignableFrom(component.getClass());
	}

	public void setKlass(final Class klass) {
		this.klass = klass;
	}

	public Class getKlass() {
		return klass;
	}

}
