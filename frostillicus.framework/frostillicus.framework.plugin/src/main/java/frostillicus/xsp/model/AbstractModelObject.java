package frostillicus.xsp.model;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.persistence.Column;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;

import com.ibm.xsp.designer.context.XSPContext;
import com.ibm.xsp.model.ViewRowData;

import frostillicus.xsp.util.FrameworkUtils;

public abstract class AbstractModelObject extends DataModel implements ModelObject {
	private static final long serialVersionUID = 1L;

	private transient Map<String, Method> getterCache_;
	private transient Map<String, List<Method>> setterCache_;
	private boolean frozen_;

	/* **********************************************************************
	 * Hooks and utility methods for concrete classes
	 * These are named without "get" to avoid steeping on doc fields' toes
	 ************************************************************************/
	protected boolean querySave() {
		return true;
	}

	protected void postSave() {
	}

	protected boolean queryDelete() {
		return true;
	}

	protected void postDelete() {
	}

	@Override
	public Set<String> propertyNames(final boolean includeSystem, final boolean includeAll) {
		Set<String> result = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		for(Field field : getClass().getDeclaredFields()) {
			if(!Modifier.isStatic(field.getModifiers()) && !field.getName().endsWith("_")) {
				result.add(field.getName());
			}
		}
		return result;
	}

	@Override
	public boolean save() {
		if(readonly()) { return true; }

		// Time for validation!

		// We'll be getting values up to twice, so do a bit of cache
		Map<String, Object> valCache = new HashMap<String, Object>();

		// First, check that the data types of all @Columns match
		boolean invalidSetters = false;
		for(Field field : getClass().getDeclaredFields()) {
			if(field.getAnnotation(Column.class) != null) {
				Object val;
				if(!valCache.containsKey(field.getName())) {
					valCache.put(field.getName(), getValue(field.getName()));
				}
				val = valCache.get(field.getName());

				boolean valid = checkSetter(field, val);
				if(!valid) {
					FrameworkUtils.addMessage(FacesMessage.SEVERITY_ERROR, "Field '" + field.getName() + "' is of invalid type " + val.getClass().getName(), null);
					invalidSetters = true;
					continue;
				}
			}
		}
		if(invalidSetters) { return false; }


		// Now, build a validator for the class
		Validator validator = Validation.byDefaultProvider().configure()
				.messageInterpolator(new XSPLocaleResourceBundleMessageInterpolator())
				.buildValidatorFactory().getValidator();

		// Run through the constrained fields to populate their values from the model object
		BeanDescriptor desc = validator.getConstraintsForClass(this.getClass());
		for(PropertyDescriptor prop : desc.getConstrainedProperties()) {
			try {
				Field field = getClass().getDeclaredField(prop.getPropertyName());
				Object val;
				if(!valCache.containsKey(field.getName())) {
					valCache.put(field.getName(), getValue(field.getName()));
				}
				val = valCache.get(field.getName());

				field.setAccessible(true);
				field.set(this, val);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			} catch (NoSuchFieldException e) {
				throw new RuntimeException(e);
			}
		}


		// Now run the constraint tests and publish any failures
		Set<ConstraintViolation<AbstractModelObject>> constraintViolations = validator.validate(this);
		if(!constraintViolations.isEmpty()) {
			if(FrameworkUtils.isFaces()) {
				// In a Faces environment, report the problems to the UI
				// TODO decide if this is a good idea
				for(ConstraintViolation<AbstractModelObject> violation : constraintViolations) {
					FrameworkUtils.addMessage(FacesMessage.SEVERITY_ERROR, violation.getPropertyPath() + ": " + violation.getMessage(), null);
				}
				return false;
			} else {
				// Otherwise, throw an outright exception
				throw new ConstraintViolationException(constraintViolations);
			}
		}

		return true;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean checkSetter(final Field field, final Object val) {
		Class<?> fieldClass = field.getType();

		if(val != null) {
			// See if it's an invalid value
			// It may be an enum
			if(fieldClass.isEnum()) {
				if(val instanceof String) {
					try {
						Enum.valueOf((Class<? extends Enum>)fieldClass, (String)val);
					} catch(IllegalArgumentException e) {
						return false;
					}
				} else {
					return false;
				}
			} else if(!fieldClass.isAssignableFrom(val.getClass())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean readonly() {
		return category() || frozen_;
	}

	@Override
	public void freeze() {
		frozen_ = true;
	}
	@Override
	public void unfreeze() {
		frozen_ = false;
	}

	@Override
	public boolean frozen() {
		return frozen_;
	}

	@Override
	public final boolean isNew() {
		return getId().isEmpty();
	}

	@Override
	public Type getGenericType(final Object keyObject) {
		if (!(keyObject instanceof String)) {
			throw new IllegalArgumentException();
		}
		String key = ((String) keyObject).toLowerCase();

		Method getter = findGetter(key);
		if(getter != null) {
			return getter.getGenericReturnType();
		} else {
			// Look for the property in the classes declared fields, case-insensitive
			for(Field field : getClass().getDeclaredFields()) {
				if(field.getName().equalsIgnoreCase(key)) {
					return field.getGenericType();
				}
			}
			// If we're here, there's no definition
			return Object.class;
		}
	}

	@Override
	public Set<ConstraintDescriptor<?>> getConstraintDescriptors(final Object keyObj) {
		String key = String.valueOf(keyObj);
		final Validator validator = Validation.byDefaultProvider().configure().buildValidatorFactory().getValidator();

		BeanDescriptor beanDesc = validator.getConstraintsForClass(getClass());
		for(PropertyDescriptor prop : beanDesc.getConstrainedProperties()) {
			if(prop.getPropertyName().equalsIgnoreCase(key)) {
				return prop.getConstraintDescriptors();
			}
		}
		return new HashSet<ConstraintDescriptor<?>>();
	}

	@Override
	public Field getField(final Object keyObj) {
		String key = String.valueOf(keyObj);
		for(Field field : getClass().getDeclaredFields()) {
			if(field.getName().equalsIgnoreCase(key)) {
				return field;
			}
		}
		return null;
	}

	/* **********************************************************************
	 * DataModel methods
	 ************************************************************************/
	@Override
	public int getRowCount() {
		return 1;
	}

	@Override
	public Object getRowData() {
		return this;
	}

	@Override
	public int getRowIndex() {
		return 0;
	}

	@Override
	public Object getWrappedData() {
		return this;
	}

	@Override
	public boolean isRowAvailable() {
		return true;
	}

	@Override
	public void setRowIndex(final int paramInt) {
		// NOP
	}

	@Override
	public void setWrappedData(final Object paramObject) {
		// NOP
	}

	@Override
	public Class<?> getType(final Object keyObject) {
		if (!(keyObject instanceof String)) {
			throw new IllegalArgumentException();
		}
		String key = ((String) keyObject).toLowerCase();

		Method getter = findGetter(key);
		if(getter != null) {
			return getter.getReturnType();
		} else {
			// Look for the property in the classes declared fields, case-insensitive
			for(Field field : getClass().getDeclaredFields()) {
				if(field.getName().equalsIgnoreCase(key)) {
					return field.getType();
				}
			}
			// If we're here, there's no definition
			return Object.class;
		}
	}

	@Override
	public boolean isReadOnly(final Object keyObject) {
		if(readonly()) {
			return true;
		}

		if (category()) {
			return true;
		}
		if (!(keyObject instanceof String)) {
			throw new IllegalArgumentException();
		}
		String key = (String) keyObject;

		if ("id".equalsIgnoreCase(key)) {
			return true;
		} else if (findGetter(key) != null && findSetters(key).size() == 0) {
			// Consider a property with a getter but no setters as read-only
			return true;
		}
		return false;
	}

	@Override
	public Object getValue(final Object keyObject) {
		if (!(keyObject instanceof String)) {
			throw new IllegalArgumentException();
		}
		String key = ((String) keyObject).toLowerCase();

		// First priority: id
		if ("id".equalsIgnoreCase(key)) {
			return getId();
		}

		// Second priority: getters
		Method getter = findGetter(key);
		if (getter != null) {
			try {
				return getter.invoke(this);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException("InvocationTargetException when asking for '" + keyObject + "' on an object of class " + getClass().getName(), e.getCause());
			}
		}

		return getValueImmediate(keyObject);
	}


	@Override
	public void setValue(final Object keyObject, final Object value) {
		if (category()) {
			throw new UnsupportedOperationException("Categories cannot be modified");
		}
		if (!(keyObject instanceof String)) {
			throw new IllegalArgumentException();
		}
		String key = ((String) keyObject).toLowerCase();

		// First priority: disallow read-only values
		if (isReadOnly(keyObject)) {
			throw new IllegalArgumentException(key + " is read-only");
		}

		// Second priority: setters
		// Look for appropriately-named setters with a fitting type
		List<Method> setters = findSetters(key);
		for (Method method : setters) {
			try {
				Class<?> param = method.getParameterTypes()[0];
				if (value == null || param.isInstance(value)) {
					method.invoke(this, value);
					return;
				}
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}

		// If we reached here with a matching setter name but no matching type, consider it an illegal argument
		if (setters.size() > 0) {
			throw new IllegalArgumentException("No match found for setter '" + key + "' with type '" + value.getClass().getName() + "'");
		}

		setValueImmediate(keyObject, value);
	}

	protected abstract Object getValueImmediate(Object keyObject);
	protected abstract void setValueImmediate(Object keyObject, Object value);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Object coaxValue(final String keyObject, final Object value) {
		Class<?> type = getType(keyObject);
		if(type != null) {
			if(type.isEnum() && value != null && !"".equals(value)) {
				return Enum.valueOf((Class<? extends Enum>)type, String.valueOf(value));
			} else if(Boolean.class.equals(type) || Boolean.TYPE.equals(type)) {
				if(value != null) {
					if(value.equals(1) || value.equals("Y") || value.equals("true")) {
						return true;
					} else if(value.equals(0) || value.equals("N") || value.equals("false")) {
						return false;
					}
				}
				// For un-set values, boolean gets false, while java.lang.Boolean gets null
				if(Boolean.TYPE.equals(type)) {
					return false;
				} else {
					return null;
				}
			} else if(List.class.isAssignableFrom(type)) {
				if(value == null) {
					return new ArrayList<Object>();
				} else if(!List.class.isAssignableFrom(value.getClass())) {
					return new ArrayList<Object>(Arrays.asList(value));
				}
			} else if(value instanceof Date && java.sql.Date.class.equals(type)) {
				// Then the value should be a java.util.Date
				return new java.sql.Date(((Date)value).getTime());
			} else if(value instanceof Date && java.sql.Time.class.equals(type)) {
				// Then the value should be a java.util.Date
				return new java.sql.Time(((Date)value).getTime());
			}
		}
		return value;
	}

	/* **********************************************************************
	 * ViewRowData methods
	 ************************************************************************/
	@Override
	public final Object getColumnValue(final String key) {
		return getValue(key);
	}

	@Override
	public final void setColumnValue(final String key, final Object value) {
		setValue(key, value);
	}

	@Override
	public final ViewRowData.ColumnInfo getColumnInfo(final String key) {
		return null;
	}

	@Override
	public final boolean isReadOnly(final String key) {
		return isReadOnly((Object) key);
	}

	@Override
	public final Object getValue(final String key) {
		return getValue((Object) key);
	}

	@Override
	public final String getOpenPageURL(final String pageName, final boolean readOnly) {
		if (category()) {
			return "";
		}
		if(pageName == null) {
			return "";
		}
		return pageName + (pageName.contains("?") ? "&" : "?") + "id=" + getId();
	}

	/* **********************************************************************
	 * Reflection seeker methods
	 ************************************************************************/
	protected final Method findGetter(final String key) {
		String lkey = key.toLowerCase();
		if (!getterCache().containsKey(lkey)) {
			Method result = null;
			for (Method method : getClass().getMethods()) {
				String methodName = method.getName().toLowerCase();
				if (method.getParameterTypes().length == 0 && (methodName.equals("get" + lkey) || methodName.equals("is" + lkey))) {
					try {
						result = method;
						break;
					} catch (IllegalArgumentException e) {
						throw new RuntimeException(e);
					}
				}
			}
			getterCache().put(lkey, result);
		}
		return getterCache().get(lkey);
	}

	protected final List<Method> findSetters(final String key) {
		String lkey = key.toLowerCase();
		if (!setterCache().containsKey(lkey)) {
			List<Method> result = new ArrayList<Method>();
			for (Method method : getClass().getMethods()) {
				Class<?>[] parameters = method.getParameterTypes();
				String methodName = method.getName().toLowerCase();
				if (parameters.length == 1 && methodName.equals("set" + lkey)) {
					result.add(method);
				}
			}
			setterCache().put(lkey, result);
		}
		return setterCache().get(lkey);
	}

	private synchronized Map<String, Method> getterCache() {
		if(getterCache_ == null) {
			getterCache_ = Collections.synchronizedMap(new HashMap<String, Method>());
		}
		return getterCache_;
	}
	private synchronized Map<String, List<Method>> setterCache() {
		if(setterCache_ == null) {
			setterCache_ = Collections.synchronizedMap(new HashMap<String, List<Method>>());
		}
		return setterCache_;
	}

	/* **********************************************************************
	 * Validation support
	 ************************************************************************/
	private static class XSPLocaleResourceBundleMessageInterpolator extends ResourceBundleMessageInterpolator {
		@Override
		public String interpolate(final String message, final MessageInterpolator.Context context) {
			Locale locale;
			if(FrameworkUtils.isFaces()) {
				locale = XSPContext.getXSPContext(FacesContext.getCurrentInstance()).getLocale();
			} else {
				locale = Locale.getDefault();
			}
			return interpolate(message, context, locale);
		}
	}
}
