package config;

import java.io.Serializable;
import java.util.*;

import org.openntf.domino.*;

import com.ibm.xsp.model.DataObject;

import frostillicus.xsp.bean.ApplicationScoped;
import frostillicus.xsp.bean.ManagedBean;
import frostillicus.xsp.util.FrameworkUtils;

@ManagedBean(name="appConfig")
@ApplicationScoped
public class AppConfig implements Serializable, DataObject {
	private static final long serialVersionUID = 1L;

	private static final String[] EXPECTED_FIELDS = {

	};

	private Boolean complete_ = null;

	public static AppConfig get() {
		AppConfig instance = (AppConfig)FrameworkUtils.resolveVariable(AppConfig.class.getAnnotation(ManagedBean.class).name());
		return instance == null ? new AppConfig() : instance;
	}

	public Class<?> getType(final Object keyObject) {
		return String.class;
	}

	public Object getValue(final Object keyObject) {
		if (!(keyObject instanceof String)) {
			throw new IllegalArgumentException();
		}
		if("keySet".equals(keyObject)) { return keySet(); }
		Document config = getConfig();
		if(config.containsKey(keyObject)) {
			List<Object> val = config.getItemValue((String)keyObject);
			if(val.size() == 1) {
				if(val.get(0) instanceof Number || val.get(0) instanceof Date || val.get(0) instanceof DateTime || val.get(0) instanceof String) {
					return val.get(0);
				} else {
					return val;
				}
			} else if(val.isEmpty()) {
				return "";
			} else {
				return val;
			}

		}
		return "";
	}

	public boolean isReadOnly(final Object keyObject) {
		return false;
	}

	public void setValue(final Object keyObject, final Object value) {
		if (!(keyObject instanceof String)) {
			throw new IllegalArgumentException();
		}
		getConfig().replaceItemValue((String)keyObject, value);
	}

	public boolean save() {
		return getConfig().save();
	}

	public Set<String> keySet() {
		return new TreeSet<String>(getConfig().keySet());
	}

	public boolean isComplete() {
		if(complete_ == null) {
			Document doc = getConfig();
			for(String fieldName : EXPECTED_FIELDS) {
				if(doc.getItemValueString(fieldName).isEmpty()) {
					complete_ = Boolean.FALSE;
					break;
				}
			}
			if(complete_ == null) { complete_ = Boolean.TRUE; }
		}

		return complete_;
	}

	private Document getConfig() {
		Map<String, Object> requestScope = FrameworkUtils.getRequestScope();
		String key = serialVersionUID + "document";
		if(!requestScope.containsKey(key)) {
			requestScope.put(key, FrameworkUtils.getDatabase().getDocumentWithKey("AppConfig", true));
		}
		return (Document)requestScope.get(key);
	}
}