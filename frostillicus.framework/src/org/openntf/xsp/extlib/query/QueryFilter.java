package org.openntf.xsp.extlib.query;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

abstract class QueryFilter {
	private String propertyName;
	private Object content;
	protected static Logger _logger;
	static {
		_logger = Logger.getLogger(QueryFilter.class.getName());
	}

	protected QueryFilter(String propertyName, Object content) {
		setPropertyName(propertyName);
		setContent(content);
	}

	protected QueryFilter() {

	}

	private void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getPropertyName() {
		return this.propertyName;
	}

	private void setContent(Object content) {
		this.content = content;
	}

	public Object getContent() {
		return this.content;
	}

	private Method getExpectedBeanGetter(Class<?> clazz) {
		Method result = null;
		char[] charArray = getPropertyName().toCharArray();
		charArray[0] = Character.toUpperCase(charArray[0]);
		String propName = new String(charArray);
		String guessedName = "get" + propName;
		try {
			result = clazz.getMethod(guessedName, (Class[]) null);
		} catch (NoSuchMethodException e1) {
			try {
				guessedName = "is" + propName;
				result = clazz.getMethod(guessedName, (Class[]) null);
			} catch (NoSuchMethodException e) {
				// nothing necessary. Result is still null
			}
		}
		return result;
	}

	private Method getMethod(UIComponent component) {
		Method result = null;
		try {
			if (component != null) {
				result = getExpectedBeanGetter(component.getClass());
			} else {
				_logger.warning("Attempting to access a method of a null component");
			}
		} catch (Exception e) {
			_logger.log(Level.WARNING, "Unhandled Exception", e);
		}
		return result;
	}

	protected Object getPropertyValue(UIComponent component) {
		Object result = null;
		Method crystal = getMethod(component);
		if (crystal != null) {
			try {
				result = invokeMethod(component, crystal);
			} catch (Exception e) {
				_logger.log(Level.WARNING, "Unhandled Exception", e);
			}
		}
		return result;
	}

	private Object invokeMethod(Object obj, Method crystal) throws Exception {
		Object result = null;
		try {
			result = crystal.invoke(obj, (Object[]) null);
		} catch (IllegalArgumentException e) {
			result = e.getMessage();
		} catch (IllegalAccessException e) {
			result = e.getMessage();
		} catch (InvocationTargetException e) {
			result = e.getMessage();
		}
		return result;
	}

	protected boolean isCompatibleWith(UIComponent component) {
		boolean result = false;
		Method crystal = getMethod(component);
		if (crystal != null) {
			Object content = getContent();
			if (content != null) {
				result = crystal.getReturnType().isAssignableFrom(content.getClass());
			} else {
				_logger.warning("Cannot verify compatibility with component, content is null");
			}
		}
		return result;
	}

	public abstract boolean matches(UIComponent component, FacesContext context);

}
