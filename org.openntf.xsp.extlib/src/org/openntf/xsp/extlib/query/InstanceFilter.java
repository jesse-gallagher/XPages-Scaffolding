package org.openntf.xsp.extlib.query;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

public class InstanceFilter extends QueryFilter {
	private Class klass;
	protected static Logger _logger;
	static {
		_logger = Logger.getLogger(InstanceFilter.class.getName());
	}

	public InstanceFilter(Class klass) {
		setKlass(klass);
	}

	@Override
	public boolean matches(UIComponent component, FacesContext context) {
		return getKlass().isAssignableFrom(component.getClass());
	}

	public void setKlass(Class klass) {
		this.klass = klass;
	}

	public Class getKlass() {
		return klass;
	}

}
