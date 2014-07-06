package frostillicus.xsp.controller;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIOutput;
import javax.faces.component.UISelectItem;
import javax.faces.component.UISelectMany;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraints.NotEmpty;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.component.UISelectOneMenu;
import com.ibm.xsp.component.xp.XspDateTimeHelper;
import com.ibm.xsp.component.xp.XspInputText;
import com.ibm.xsp.component.xp.XspSelectManyListbox;
import com.ibm.xsp.component.xp.XspSelectOneMenu;
import com.ibm.xsp.component.xp.XspViewColumn;
import com.ibm.xsp.component.xp.XspViewColumnHeader;
import com.ibm.xsp.convert.DateTimeConverter;
import com.ibm.xsp.extlib.component.data.UIFormLayoutRow;
import com.ibm.xsp.model.DataObject;

import frostillicus.xsp.converter.EnumBindingConverter;

public class ComponentMap implements DataObject, Serializable {
	private static final long serialVersionUID = 1L;

	public static final String COMPONENT_MAP_SERVICE_NAME = "frostillicus.xsp.controller.ComponentMapAdapterFactory";

	private Map<Object, ComponentPropertyMap> cache_ = new HashMap<Object, ComponentPropertyMap>();
	private Set<String> initialized_ = new HashSet<String>();
	private final String controllerPropertyName_;

	public ComponentMap(final String controllerPropertyName) {
		controllerPropertyName_ = controllerPropertyName;
	}

	@Override
	public Class<ComponentPropertyMap> getType(final Object key) {
		return ComponentPropertyMap.class;
	}

	@Override
	public ComponentPropertyMap getValue(final Object key) {
		if(!cache_.containsKey(key)) {
			cache_.put(key, new ComponentPropertyMap(key));
		}
		return cache_.get(key);
	}

	@Override
	public boolean isReadOnly(final Object key) {
		return true;
	}

	@Override
	public void setValue(final Object key, final Object value) { }

	public void initialize() {
		for(ComponentPropertyMap map : cache_.values()) {
			map.initialize();
		}
	}

	private void writeObject(final java.io.ObjectOutputStream stream) throws IOException {
		cache_.clear();
	}


	public class ComponentPropertyMap implements DataObject {
		private Map<String, UIComponent> map_ = new TreeMap<String, UIComponent>(String.CASE_INSENSITIVE_ORDER);
		private Object object_;

		public ComponentPropertyMap(final Object object) {
			object_ = object;
		}

		@Override
		public UIComponent getValue(final Object key) {
			if(!(key instanceof String)) {
				throw new IllegalArgumentException("key must be a String");
			}
			return map_.get(key);
		}


		@Override
		public void setValue(final Object key, final Object value) {
			if(!(key instanceof String)) {
				throw new IllegalArgumentException("key must be a String");
			}
			if(!(value instanceof UIComponent)) {
				throw new IllegalArgumentException("value must be a UIComponent");
			}

			map_.put((String)key, (UIComponent)value);
		}

		@Override
		public boolean isReadOnly(final Object key) { return false; }
		@Override
		public Class<?> getType(final Object key) { return UIComponent.class; }

		public void clear() {
			map_.clear();
		}

		@SuppressWarnings("unchecked")
		public void initialize() {
			if(object_ == null) { return; }

			FacesContext facesContext = FacesContext.getCurrentInstance();
			ApplicationEx app = (ApplicationEx)facesContext.getApplication();

			// Now search for an appropriate adapter
			List<ComponentMapAdapterFactory> factories = app.findServices(COMPONENT_MAP_SERVICE_NAME);
			ComponentMapAdapter adapter = null;
			for(ComponentMapAdapterFactory fac : factories) {
				adapter = fac.createAdapter(object_);
				if(adapter != null) {
					break;
				}
			}

			// If we didn't find any, there's no work to do
			if(adapter == null) {
				return;
			}

			ResourceBundle translation = adapter.getTranslationBundle();

			for(Map.Entry<String, UIComponent> entry : map_.entrySet()) {
				UIComponent component = entry.getValue();
				String property = entry.getKey();
				String clientId = component.getClientId(facesContext);
				if(!initialized_.contains(clientId)) {
					ValueBinding binding = component.getValueBinding("binding");

					if(component instanceof UIInput || component instanceof UIOutput) {

						attachValueBinding(component, binding);

						if(component instanceof UIInput) {
							attachConverterAndValidators(component, adapter, property, translation);
						}

					} else if(component instanceof UIFormLayoutRow) {

						UIFormLayoutRow formRow = (UIFormLayoutRow)component;
						if(StringUtil.isEmpty(formRow.getLabel())) {
							formRow.setLabel(adapter.getTranslationForProperty(property));
						}
						UIComponent input;
						if(formRow.getChildCount() == 0) {
							input = createComponent(adapter, property);
							formRow.getChildren().add(input);
							input.setParent(formRow);
						} else {
							// Otherwise, check to see if the first child is an input. If so, use it; otherwise, insert a new one at the start
							if(formRow.getChildren().get(0) instanceof UIInput) {
								input = (UIInput)formRow.getChildren().get(0);
							} else {
								input = createComponent(adapter, property);
								formRow.getChildren().add(0, input);
								input.setParent(formRow);
							}
						}

						attachValueBinding(input, binding);
						attachConverterAndValidators(input, adapter, property, translation);

					} else if(component instanceof XspViewColumn) {
						// TODO make this work

						XspViewColumn column = (XspViewColumn)component;
						column.setColumnName(property);
						if(column.getHeader() == null) {
							XspViewColumnHeader header = new XspViewColumnHeader();
							header.setValue(adapter.getTranslationForProperty(property));
							column.getChildren().add(header);
							header.setParent(column);
							column.setHeader(header);
						}
					}


					initialized_.add(clientId);
				}

			}
		}

		@SuppressWarnings("unchecked")
		private UIComponent createComponent(final ComponentMapAdapter adapter, final String property) {
			UIInput input;
			Type valueType = adapter.getGenericType(property);

			if(valueType instanceof Class && ((Class<?>)valueType).isEnum()) {
				// Single-value enum
				input = new XspSelectOneMenu();
			} else if(valueType instanceof ParameterizedType) {
				ParameterizedType ptype = (ParameterizedType)valueType;
				if(Collection.class.isAssignableFrom((Class<?>)ptype.getRawType())) {
					// Then it's a collection of one form or another with one and only one type argument
					Type genericType = ptype.getActualTypeArguments()[0];
					if(String.class.equals(genericType)) {
						input = new XspInputText();
						((XspInputText)input).setMultipleSeparator(";");
						((XspInputText)input).setMultipleTrim(true);
					} else if(genericType instanceof Class && ((Class<?>)genericType).isEnum()) {
						input = new XspSelectManyListbox();
						input.getAttributes().put("multiple", Boolean.TRUE);
					} else {
						input = new XspInputText();
					}
				} else {
					// Punt back to single-value text
					input = new XspInputText();
				}
			} else {
				// Assume single-value text
				input = new XspInputText();
			}
			return input;
		}

		private void attachValueBinding(final UIComponent component, final ValueBinding binding) {
			if(component.getValueBinding("value") == null) {
				Pattern bindingPattern = Pattern.compile("^\\#\\{" + ControllingViewHandler.BEAN_NAME + "\\." + controllerPropertyName_ + "\\[(.*)\\]((\\.|\\[).*)\\}$");
				Matcher matcher = bindingPattern.matcher(binding.getExpressionString());
				if(matcher.matches()) {
					String modelName = matcher.group(1);
					String elProp = matcher.group(2);
					String valueString = "#{" + modelName + elProp + "}";
					component.setValueBinding("value", FacesContext.getCurrentInstance().getApplication().createValueBinding(valueString));
				}
			}
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		private void attachConverterAndValidators(final UIComponent component, final ComponentMapAdapter adapter, final Object property, final ResourceBundle translation) {
			UIInput input = (UIInput)component;

			/* ******************************************************************************
			 * Add support based on constraints
			 ********************************************************************************/
			Set<ConstraintDescriptor<?>> constraints = adapter.getConstraintDescriptors(property);

			if(!constraints.isEmpty()) {
				boolean required = false;
				for(ConstraintDescriptor<?> desc : constraints) {
					// First, add basic required support
					Object annotation = desc.getAnnotation();
					if(annotation instanceof NotNull || annotation instanceof NotEmpty) {
						required = true;
						break;
					} else if(annotation instanceof Size && ((Size)annotation).min() > 0) {
						required = true;
					}
				}
				if(required) {
					input.setRequired(true);
				}

				// Now, add arbitrary validators
				input.addValidator(adapter.createValidator(property));
			}


			/* ******************************************************************************
			 * Add support based on the property type
			 ********************************************************************************/
			Type valueType = adapter.getGenericType(property);

			// Determine if we're dealing with a Collection or not
			Type baseType;
			if(valueType instanceof ParameterizedType) {
				ParameterizedType ptype = (ParameterizedType)valueType;
				if(Collection.class.isAssignableFrom((Class<?>)ptype.getRawType())) {
					baseType = ptype.getActualTypeArguments()[0];
				} else {
					baseType = valueType;
				}
			} else {
				baseType = valueType;
			}


			// Add selectItems for single-value enums
			if(baseType instanceof Class && ((Class<?>)baseType).isEnum()) {
				Class<? extends Enum> enumType = (Class<? extends Enum>)baseType;

				if(input.getConverter() == null) {
					input.setConverter(new EnumBindingConverter(enumType));
				}

				if(input.getChildren().isEmpty()) {
					if(input instanceof UISelectOne || input instanceof UISelectMany) {
						if(input instanceof UISelectOneMenu) {
							addEmptySelectItem(component, enumType, translation);
						}

						populateEnumSelectItems(input, enumType, translation);
					}
				}
			}

			// Add a converter and helper for date/time fields
			if(baseType.equals(Date.class)) {
				if(input.getConverter() == null) {
					DateTimeConverter converter = new DateTimeConverter();
					converter.setType(DateTimeConverter.TYPE_BOTH);
					input.setConverter(converter);
				}
				XspDateTimeHelper helper = new XspDateTimeHelper();
				component.getChildren().add(helper);
				helper.setParent(component);
			} else if(baseType.equals(java.sql.Date.class)) {
				if(input.getConverter() == null) {
					DateTimeConverter converter = new DateTimeConverter();
					converter.setType(DateTimeConverter.TYPE_DATE);
					input.setConverter(converter);
				}
				XspDateTimeHelper helper = new XspDateTimeHelper();
				component.getChildren().add(helper);
				helper.setParent(component);
			} else if(baseType.equals(java.sql.Time.class)) {
				if(input.getConverter() == null) {
					DateTimeConverter converter = new DateTimeConverter();
					converter.setType(DateTimeConverter.TYPE_TIME);
					input.setConverter(converter);
				}
				XspDateTimeHelper helper = new XspDateTimeHelper();
				component.getChildren().add(helper);
				helper.setParent(component);
			}
		}


		@SuppressWarnings("unchecked")
		private void addEmptySelectItem(final UIComponent component, final Class<?> enumType, final ResourceBundle translation) {
			UISelectItem empty = new UISelectItem();
			try {
				String transKey = enumType.getName() + ".(SELECT_ONE)";
				empty.setItemLabel(translation.getString(transKey));
			} catch(Exception e) {
				try {
					empty.setItemLabel(translation.getString("(SELECT_ONE)"));
				} catch(Exception e2) {
					empty.setItemLabel(" - Select One -");
				}
			}
			empty.setItemValue("");
			component.getChildren().add(empty);
			empty.setParent(component);
		}

		@SuppressWarnings("unchecked")
		private void populateEnumSelectItems(final UIComponent component, final Class<?> enumType, final ResourceBundle translation) {

			Enum<?>[] constants = (Enum<?>[])enumType.getEnumConstants();
			for(Enum<?> constant : constants) {
				UISelectItem item = new UISelectItem();

				// Look for a localized label
				String label = constant.name();
				if(translation != null) {
					String transKey = enumType.getName() + "." + constant.name();
					try {
						label = translation.getString(transKey);
					} catch(Exception e) {
						// Ignore
					}
				}

				item.setItemLabel(label);

				item.setItemValue(constant);
				component.getChildren().add(item);
				item.setParent(component);
			}
		}
	}
}
