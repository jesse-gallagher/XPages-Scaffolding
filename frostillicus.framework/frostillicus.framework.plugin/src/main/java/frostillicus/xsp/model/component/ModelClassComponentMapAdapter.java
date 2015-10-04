package frostillicus.xsp.model.component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.validation.metadata.ConstraintDescriptor;

import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.designer.context.XSPContext;

import frostillicus.xsp.controller.ComponentMapAdapter;
import frostillicus.xsp.model.ModelObject;

/**
 * @since 1.0
 */
public class ModelClassComponentMapAdapter implements ComponentMapAdapter {
	private final Class<? extends ModelObject> clazz_;

	public ModelClassComponentMapAdapter(final Class<? extends ModelObject> clazz) {
		clazz_ = clazz;
	}

	@Override
	public Object getObject() {
		return clazz_;
	}

	@Override
	public ResourceBundle getTranslationBundle() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ApplicationEx app = (ApplicationEx)facesContext.getApplication();
		try {
			return app.getResourceBundle("model_translation", XSPContext.getXSPContext(facesContext).getLocale());
		} catch(IOException ioe) {
			return null;
		}
	}

	@Override
	public String getTranslationForProperty(final Object property) {
		ResourceBundle translation = getTranslationBundle();
		try {
			return translation.getString(clazz_.getName() + "." + property);
		} catch(Exception e) {
			return String.valueOf(property);
		}
	}
	@Override
	public Set<ConstraintDescriptor<?>> getConstraintDescriptors(final Object property) {
		return Collections.emptySet();
	}

	@Override
	public Validator createValidator(final Object property) {
		return null;
	}

	@Override
	public Type getGenericType(final Object property) {
		if(property == null) { return null; }
		String propertyName = String.valueOf(property);
		for(Field field : clazz_.getDeclaredFields()) {
			if(field.getName().equalsIgnoreCase(propertyName)) {
				return field.getGenericType();
			}
		}
		return Object.class;
	}

	@Override
	public Collection<String> getPropertyNames() {
		List<String> result = new ArrayList<String>();
		for(Field field : clazz_.getDeclaredFields()) {
			if(!field.getName().endsWith("_")) {
				result.add(field.getName());
			}
		}
		return result;
	}
}
