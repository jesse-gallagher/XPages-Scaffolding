package frostillicus.xsp.converter;

import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.el.ValueBinding;

import com.ibm.commons.util.StringUtil;

/**
 * @since 1.0
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class EnumBindingConverter implements Converter, StateHolder {
	private Class<? extends Enum> enumType_ = null;
	private boolean transient_ = false;

	public EnumBindingConverter() { }
	public EnumBindingConverter(final Class<? extends Enum> enumType) {
		enumType_ = enumType;
	}

	@Override
	public Object getAsObject(final FacesContext facesContext, final UIComponent component, final String value) {
		if(StringUtil.isEmpty(value)) {
			return value;
		}

		if(enumType_ == null) {
			ValueBinding binding = component.getValueBinding("value");
			Class<? extends Enum> enumType = binding.getType(facesContext);
			return Enum.valueOf(enumType, value);
		} else {
			return Enum.valueOf(enumType_, value);
		}
	}

	@Override
	public String getAsString(final FacesContext facesContext, final UIComponent component, final Object value) {
		return String.valueOf(value);
	}

	@Override
	public Object saveState(final FacesContext facesContext) {
		return new Object[] {
				enumType_,
				transient_
		};
	}
	@Override
	public void restoreState(final FacesContext facesContext, final Object stateObj) {
		Object[] state = (Object[])stateObj;
		enumType_ = (Class<? extends Enum>)state[0];
		transient_ = (Boolean)state[1];
	}
	@Override
	public boolean isTransient() {
		return transient_;
	}
	@Override
	public void setTransient(final boolean isTransient) {
		transient_ = isTransient;
	}
}