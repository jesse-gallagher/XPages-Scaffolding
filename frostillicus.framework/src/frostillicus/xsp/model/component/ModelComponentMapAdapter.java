package frostillicus.xsp.model.component;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ResourceBundle;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.validation.metadata.ConstraintDescriptor;

import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.designer.context.XSPContext;

import frostillicus.xsp.controller.ArbitraryValidator;
import frostillicus.xsp.controller.ComponentMapAdapter;
import frostillicus.xsp.model.ModelObject;

public class ModelComponentMapAdapter implements ComponentMapAdapter {
	private final ModelObject model_;

	public ModelComponentMapAdapter(final ModelObject model) {
		model_ = model;
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
	public Set<ConstraintDescriptor<?>> getConstraintDescriptors(final Object property) {
		return model_.getConstraintDescriptors(property);
	}

	@Override
	public Validator createValidator(final Object property) {
		return new ArbitraryValidator(model_.getClass(), model_.getField(property).getName());
	}

	@Override
	public Type getGenericType(final Object property) {
		return model_.getGenericType(property);
	}
}
