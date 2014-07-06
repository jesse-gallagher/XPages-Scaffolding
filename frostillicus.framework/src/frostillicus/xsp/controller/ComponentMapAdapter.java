package frostillicus.xsp.controller;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.Set;

import javax.faces.validator.Validator;
import javax.validation.metadata.ConstraintDescriptor;

public interface ComponentMapAdapter {
	public ResourceBundle getTranslationBundle();

	public String getTranslationForProperty(Object property);

	public Set<ConstraintDescriptor<?>> getConstraintDescriptors(Object property);

	public Validator createValidator(Object property);

	public Type getGenericType(Object property);

	public Collection<String> getPropertyNames();
}
