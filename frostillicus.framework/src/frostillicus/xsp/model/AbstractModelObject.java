package frostillicus.xsp.model;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.designer.context.XSPContext;
import com.ibm.xsp.model.ViewRowData;

import frostillicus.xsp.util.FrameworkUtils;

public abstract class AbstractModelObject extends DataModel implements ModelObject {
	private static final long serialVersionUID = 1L;

	private transient Map<String, Method> getterCache_ = new HashMap<String, Method>();
	private transient Map<String, List<Method>> setterCache_ = new HashMap<String, List<Method>>();
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
	public Set<String> propertyNames(final boolean includeSystem) {
		Set<String> result = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		Properties props = getClass().getAnnotation(Properties.class);
		if(props != null) {
			result.addAll(Arrays.asList(props.value()));
		}
		return result;
	}
	@Override
	public Set<String> columnPropertyNames() {
		Set<String> result = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		Properties props = getClass().getAnnotation(Properties.class);
		if(props != null && props.includeWithView()) {
			result.addAll(Arrays.asList(props.value()));
		}
		return result;
	}

	@Override
	public boolean save() {
		if(frozen_) { return false; }

		// Time for validation!

		// First, build a validator for the class
		Validator validator = Validation.byDefaultProvider().configure()
				.messageInterpolator(new XSPLocaleResourceBundleMessageInterpolator())
				.buildValidatorFactory().getValidator();

		// Run through the constrained fields to populate their values from the model object
		BeanDescriptor desc = validator.getConstraintsForClass(this.getClass());
		for(PropertyDescriptor prop : desc.getConstrainedProperties()) {
			try {
				Field field = getClass().getDeclaredField(prop.getPropertyName());

				Class<?> fieldClass = field.getType();
				Object val = getValue(field.getName());
				if(val != null && !fieldClass.isAssignableFrom(val.getClass())) {
					FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Field '" + field.getName() + "' is of invalid type " + val.getClass().getName(), null);
					FacesContext.getCurrentInstance().addMessage(null, message);
				}

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
					FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, violation.getPropertyPath() + ": " + violation.getMessage(), null);
					FacesContext.getCurrentInstance().addMessage(null, message);
				}
				return false;
			} else {
				// Otherwise, throw an outright exception
				throw new ConstraintViolationException(constraintViolations);
			}
		}


		RequiredFields reqAnnotation = getClass().getAnnotation(RequiredFields.class);
		if(reqAnnotation != null) {
			for(String field : reqAnnotation.value()) {
				Object val = getValue(field);

				boolean empty = false;
				if(val == null) {
					empty = true;
				} else if(val instanceof String && StringUtil.isEmpty((String)val)) {
					empty = true;
				} else {
					try {
						Method isEmpty = val.getClass().getMethod("isEmpty");
						if(isEmpty.getReturnType().equals(Boolean.TYPE) || isEmpty.getReturnType().equals(Boolean.class)) {
							empty = (Boolean)isEmpty.invoke(val);
						}
					} catch(NoSuchMethodException e) {
						System.out.println(e);
						// Ignore
					} catch(InvocationTargetException e) {
						System.out.println(e);
						// Ignore
					} catch(IllegalAccessException e) {
						System.out.println(e);
						// Ignore
					}
				}

				if(empty) {
					if(FrameworkUtils.isFaces()) {
						FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Field '" + field + "' is required", null);
						FacesContext.getCurrentInstance().addMessage(null, message);
					}
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public boolean readonly() {
		return frozen_ == true;
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
	public final boolean isNew() {
		return getId().isEmpty();
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
		return getter == null ? null : getter.getReturnType();
	}

	@Override
	public boolean isReadOnly(final Object keyObject) {
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
				throw new RuntimeException(e);
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
		if (!getterCache_.containsKey(lkey)) {
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
			getterCache_.put(lkey, result);
		}
		return getterCache_.get(lkey);
	}

	protected final List<Method> findSetters(final String key) {
		String lkey = key.toLowerCase();
		if (!setterCache_.containsKey(lkey)) {
			List<Method> result = new ArrayList<Method>();
			for (Method method : getClass().getMethods()) {
				Class<?>[] parameters = method.getParameterTypes();
				String methodName = method.getName().toLowerCase();
				if (parameters.length == 1 && methodName.equals("set" + lkey)) {
					result.add(method);
				}
			}
			setterCache_.put(lkey, result);
		}
		return setterCache_.get(lkey);
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
