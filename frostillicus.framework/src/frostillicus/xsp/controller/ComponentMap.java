package frostillicus.xsp.controller;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectItem;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraints.NotEmpty;

import com.ibm.xsp.application.ApplicationEx;
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

		@SuppressWarnings({ "rawtypes", "unchecked" })
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
				System.out.println("found no adapter for object of type " + object_.getClass().getName());
				return;
			}

			ResourceBundle translation = adapter.getTranslationBundle();

			for(Map.Entry<String, UIComponent> entry : map_.entrySet()) {
				UIComponent component = entry.getValue();
				String property = entry.getKey();
				String clientId = component.getClientId(facesContext);
				if(!initialized_.contains(clientId)) {
					ValueBinding binding = component.getValueBinding("binding");

					/* ******************************************************************************
					 * Add a value binding
					 ********************************************************************************/
					if(component.getValueBinding("value") == null) {
						Pattern bindingPattern = Pattern.compile("^\\#\\{" + ControllingViewHandler.BEAN_NAME + "\\." + controllerPropertyName_ + "\\[(.*)\\]((\\.|\\[).*)\\}$");
						Matcher matcher = bindingPattern.matcher(binding.getExpressionString());
						if(matcher.matches()) {
							String modelName = matcher.group(1);
							String elProp = matcher.group(2);
							String valueString = "#{" + modelName + elProp + "}";
							component.setValueBinding("value", facesContext.getApplication().createValueBinding(valueString));
						}
					}

					if(component instanceof UIInput) {
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

						// Add selectItems for single-value enums
						if(valueType instanceof Class && ((Class<?>)valueType).isEnum()) {
							if(input.getConverter() == null) {
								input.setConverter(new EnumBindingConverter((Class<? extends Enum>)valueType));
							}

							if(input instanceof UISelectOne) {
								Enum<?>[] constants = (Enum<?>[]) ((Class<?>)valueType).getEnumConstants();
								UISelectOne select = (UISelectOne)input;

								UISelectItem empty = new UISelectItem();
								empty.setItemLabel("- Choose One -");
								empty.setItemValue("");
								select.getChildren().add(empty);
								empty.setParent(select);

								for(Enum<?> constant : constants) {
									UISelectItem item = new UISelectItem();

									// Look for a localized label
									String label = constant.name();
									if(translation != null) {
										String transKey = ((Class<?>)valueType).getName() + "." + constant.name();
										try {
											label = translation.getString(transKey);
										} catch(Exception e) {
											// Ignore
										}
									}

									item.setItemLabel(label);

									item.setItemValue(constant);
									select.getChildren().add(item);
									item.setParent(select);
								}
							}
						}
					}


					initialized_.add(clientId);
				}

			}
		}
	}
}
