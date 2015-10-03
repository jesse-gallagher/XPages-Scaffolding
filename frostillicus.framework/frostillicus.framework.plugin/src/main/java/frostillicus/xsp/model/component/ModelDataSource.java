/*
 * Much of this code is "inspired by" com.ibm.xsp.extlib.model.ObjectDataSource in the XPages Extension Library
 */

package frostillicus.xsp.model.component;

import java.io.IOException;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.model.DataModel;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.FacesExceptionEx;
import com.ibm.xsp.model.AbstractDataSource;
import com.ibm.xsp.model.DataContainer;

import frostillicus.xsp.model.AbstractModelObject;
import frostillicus.xsp.model.ModelManager;
import frostillicus.xsp.model.ModelObject;
import frostillicus.xsp.model.ModelUtils;

public class ModelDataSource extends AbstractDataSource implements com.ibm.xsp.model.ModelDataSource {

	private String managerName_;
	private String key_;
	private Boolean readonly_;

	public ModelDataSource() {
	}

	/* ******************************************************************************
	 * Property getter/setters
	 ********************************************************************************/
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
	public boolean isReadonly() {
		if(readonly_ != null) {
			return readonly_;
		}

		ValueBinding vb = getValueBinding("readonly");
		if(vb != null) {
			Boolean val = (Boolean)vb.getValue(FacesContext.getCurrentInstance());
			if(val != null) {
				return val;
			}
		}

		return false;
	}

	public void setReadonly(final boolean readonly) {
		readonly_ = readonly;
	}


	/* ******************************************************************************
	 * Data Source methods
	 ********************************************************************************/

	@Override
	protected String composeUniqueId() {
		return StringUtil.concatStrings(new String[] { getClass().getName(), getManagerName(), getKey(), String.valueOf(readonly_) }, '|', false);
	}

	@Override
	public ModelObject getDataObject() {
		ModelDataContainer ac = (ModelDataContainer)getDataContainer();
		if(ac != null) {
			ModelObject obj = ac.getModelObject();
			if(isReadonly()) {
				obj.freeze();
			}
			return obj;
		}
		return null;
	}
	@Override
	public DataModel getDataModel() {
		return (DataModel)getDataObject();
	}

	@Override
	public DataContainer load(final FacesContext context) throws IOException {
		return new ModelDataContainer(getBeanId(), getUniqueId(), fetchModelObject());
	}
	@Override
	public void refresh() {
		FacesContext context = getFacesContext();
		if (context == null) {
			return;
		}

		// clear the current value
		putDataContainer(context, null);
	}

	@Override
	public void readRequestParams(final FacesContext context, final Map<String, Object> requestMap) {
		// NOP
	}

	@Override
	public boolean save(final FacesContext context, final DataContainer data) throws FacesExceptionEx {
		ModelObject modelObject = ((ModelDataContainer)data).getModelObject();
		return modelObject.save();
	}

	/*	@Override
	public Container getDataContainer(final FacesContext context) {
		Container c = (Container)super.getDataContainer(context);
		if(c!=null) {
			c.setDataSource(this);
		}
		return c;
	}*/

	private ModelObject fetchModelObject() throws IOException {
		String managerName = getManagerName();
		String key = getKey();
		// Now actually init the container
		ModelManager<?> manager = ModelUtils.findManagerInstance(FacesContext.getCurrentInstance(), managerName);
		if(manager == null) {
			throw new IOException("Unable to locate frostillic.us manager for name '" + managerName + "'");
		}
		//		String key = StringUtil.isNotEmpty(id_) ? id_ : StringUtil.isNotEmpty(key_) ? key_ : "new";
		Object modelObject = manager.getValue(key);
		if(modelObject == null) {
			throw new IOException("Received null value when retrieving object from manager using key '" + key + "'");
		}
		if(!(modelObject instanceof AbstractModelObject)) {
			throw new IOException("Retrieved non-model object from manager using key '" + key + "'");
		}

		return (ModelObject)modelObject;
	}

	/* ******************************************************************************
	 * StateHolder methods
	 ********************************************************************************/
	@Override
	public Object saveState(final FacesContext context) {
		if(isTransient()) {
			return null;
		}
		return new Object[] {
				super.saveState(context),
				managerName_,
				key_,
				readonly_
		};
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
	}

	protected static class ModelProperties extends RuntimeProperties {
		private static final long serialVersionUID = 1L;

		String computedManagerName;
		String computedKey;
	}
}
