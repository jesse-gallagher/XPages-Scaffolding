package config;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.designer.context.XSPContext;
import com.ibm.xsp.model.DataObject;

import frostillicus.xsp.bean.SessionScoped;
import frostillicus.xsp.bean.ManagedBean;
import frostillicus.xsp.util.FrameworkUtils;

@ManagedBean(name="translation")
@SessionScoped
public class Translation implements Serializable, DataObject {
	private static final long serialVersionUID = 1L;

	public static Translation get() {
		Translation existing = (Translation)FrameworkUtils.resolveVariable(Translation.class.getAnnotation(ManagedBean.class).name());
		return existing == null ? new Translation() : existing;
	}

	private transient ResourceBundle bundle_;
	private Map<Object, String> cache_ = new HashMap<Object, String>();

	public Class<String> getType(final Object key) {
		return String.class;
	}

	public String getValue(final Object key) {
		if(!cache_.containsKey(key)) {
			try {
				ResourceBundle bundle = getTranslationBundle();
				cache_.put(key, bundle.getString(String.valueOf(key)));
			} catch(IOException ioe) {
				throw new RuntimeException(ioe);
			} catch(MissingResourceException mre) {
				cache_.put(key, "[Untranslated " + key + "]");
			}
		}
		return cache_.get(key);
	}

	public boolean isReadOnly(final Object key) {
		return true;
	}

	public void setValue(final Object key, final Object value) {
		throw new UnsupportedOperationException();
	}


	private ResourceBundle getTranslationBundle() throws IOException {
		if(bundle_ == null) {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			ApplicationEx app = (ApplicationEx)facesContext.getApplication();
			bundle_ = app.getResourceBundle("translation", XSPContext.getXSPContext(facesContext).getLocale());
		}
		return bundle_;
	}
}
