package config;

import java.io.IOException;
import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.faces.context.FacesContext;
import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.model.DataObject;

public class AppConfig implements Serializable, DataObject {
	private static final long serialVersionUID = -5125445506949601097L;

	private transient ResourceBundle config;

	public Class<?> getType(final Object keyObject) {
		return String.class;
	}

	public Object getValue(final Object keyObject) {
		if (!(keyObject instanceof String)) {
			throw new IllegalArgumentException();
		}
		ResourceBundle config = getConfig();
		if(config.containsKey((String)keyObject)) {
			return config.getString((String) keyObject);
		}
		return "";
	}

	public boolean isReadOnly(final Object keyObject) {
		return true;
	}

	public void setValue(final Object keyObject, final Object value) {
	}

	private ResourceBundle getConfig() {
		if (config == null) {
			try {
				ApplicationEx app = (ApplicationEx) FacesContext.getCurrentInstance().getApplication();
				config = app.getResourceBundle("config", Locale.getDefault());
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		return config;
	}
}