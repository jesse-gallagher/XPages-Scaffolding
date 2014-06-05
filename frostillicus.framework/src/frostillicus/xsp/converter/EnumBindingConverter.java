package frostillicus.xsp.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.el.ValueBinding;

public class EnumBindingConverter implements Converter {

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAsObject(final FacesContext facesContext, final UIComponent component, final String value) {
		ValueBinding binding = component.getValueBinding("value");
		Class<? extends Enum> enumType = binding.getType(facesContext);

		return Enum.valueOf(enumType, value);
	}

	@Override
	public String getAsString(final FacesContext facesContext, final UIComponent component, final Object value) {
		return String.valueOf(value);
	}

}