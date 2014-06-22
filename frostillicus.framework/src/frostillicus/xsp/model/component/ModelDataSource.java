/*
 * Much of this code is "inspired by" com.ibm.xsp.extlib.model.ObjectDataSource in the XPages Extension Library
 */

package frostillicus.xsp.model.component;

import java.io.IOException;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.FacesExceptionEx;
import com.ibm.xsp.model.AbstractDataSource;
import com.ibm.xsp.model.DataContainer;

import frostillicus.xsp.model.AbstractModelObject;
import frostillicus.xsp.model.ModelObject;

public class ModelDataSource extends AbstractDataSource implements com.ibm.xsp.model.DataSource {

	private String managerName_;
	private String key_;
	//	private String id_;
	private Boolean readonly_;

	public ModelDataSource() { }

	public void setManagerName(final String managerName) { managerName_ = managerName; }
	public String getManagerName() {
		if(hasRuntimeProperties()) {
			return getRuntimeProperties().computedManagerName;
		}
		if(managerName_ != null) {
			return managerName_;
		}
		ValueBinding valueBinding = getValueBinding("managerName");
		if(valueBinding != null) {
			return (String)valueBinding.getValue(FacesContext.getCurrentInstance());
		}

		return null;
	}

	public void setKey(final String key) { key_ = key; }
	public String getKey() {
		if(hasRuntimeProperties()) {
			return getRuntimeProperties().computedKey;
		}
		if(key_ != null) {
			return key_;
		}
		ValueBinding valueBinding = getValueBinding("key");
		if(valueBinding != null) {
			return (String)valueBinding.getValue(FacesContext.getCurrentInstance());
		}

		return "new";
	}

	@Override
	protected String composeUniqueId() {
		return StringUtil.concatStrings(new String[] { getClass().getName(), getManagerName(), getKey() }, '|', false);
	}

	@Override
	public AbstractModelObject getDataObject() {
		Container ac = (Container)getDataContainer();
		if(ac != null) {
			AbstractModelObject obj = ac.getModelObject();
			Boolean internalReadonly = isReadonlyInternal();
			if(internalReadonly != null && internalReadonly) {
				obj.freeze();
			}
			return obj;
		}
		return null;
	}

	public void setReadonly(final boolean readonly) {
		readonly_ = readonly;
	}

	@Override
	public boolean isReadonly() {
		Boolean internal = isReadonlyInternal();
		if(internal != null) {
			return internal;
		}
		return getDataObject().readonly();
	}

	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
	                                                    value="NP_BOOLEAN_RETURN_NULL",
	                                                    justification="This is intentional, to signify an unset value")
	protected Boolean isReadonlyInternal() {
		if(hasRuntimeProperties()) {
			return getRuntimeProperties().computedReadonly;
		}
		if(readonly_ != null) {
			return readonly_;
		}
		ValueBinding valueBinding = getValueBinding("readonly");
		if(valueBinding != null) {
			Object result = valueBinding.getValue(FacesContext.getCurrentInstance());
			if(result == null) {
				return false;
			} else {
				return (Boolean)result;
			}
		}
		return null;
	}

	@Override
	public DataContainer load(final FacesContext context) throws IOException {
		Boolean readonly = isReadonlyInternal();
		return new Container(getBeanId(), getUniqueId(), getManagerName(), getKey(), readonly == null ? false : readonly);
	}

	@Override
	public void readRequestParams(final FacesContext context, final Map<String, Object> requestMap) {
		// NOP
	}

	@Override
	public boolean save(final FacesContext context, final DataContainer data) throws FacesExceptionEx {
		ModelObject modelObject = ((Container)data).getModelObject();
		return modelObject.save();
	}

	/* ******************************************************************************
	 * StateHolder methods
	 ********************************************************************************/
	@Override
	public Object saveState(final FacesContext context) {
		if(isTransient()) {
			return null;
		}
		Object[] state = new Object[4];
		state[0] = super.saveState(context);
		state[1] = managerName_;
		state[2] = key_;
		state[3] = readonly_;
		return state;
	}
	@Override
	public void restoreState(final FacesContext context, final Object state) {
		Object[] values = (Object[])state;
		super.restoreState(context, values[0]);
		managerName_ = (String)values[1];
		key_ = (String)values[2];
		readonly_ = (Boolean)values[3];
	}


	/* ******************************************************************************
	 * Class-specific RuntimeProperties implementation
	 ********************************************************************************/
	@Override
	protected ModelProperties getRuntimeProperties() {
		return (ModelProperties)super.getRuntimeProperties();
	}
	@Override
	protected ModelProperties createRuntimeProperties() {
		return new ModelProperties();
	}
	@Override
	protected void initializeRuntimeProperties(final RuntimeProperties properties) {
		super.initializeRuntimeProperties(properties);
		ModelProperties modelProps = (ModelProperties)properties;
		modelProps.computedManagerName = getManagerName();
		modelProps.computedKey = getKey();
		modelProps.computedReadonly = isReadonly();
	}

	protected static class ModelProperties extends RuntimeProperties {
		private static final long serialVersionUID = 1L;

		String computedManagerName;
		String computedKey;
		Boolean computedReadonly;
	}
}
