package frostillicus.xsp.controller;

import java.lang.reflect.Type;
import java.util.ResourceBundle;
import java.util.Set;

import javax.faces.validator.Validator;
import javax.validation.metadata.ConstraintDescriptor;

public interface ComponentMapAdapter {
	public ResourceBundle getTranslationBundle();

	public Set<ConstraintDescriptor<?>> getConstraintDescriptors(Object property);

	public Validator createValidator(Object property);

	public Type getGenericType(Object property);
}
