package frostillicus.xsp.model.component;

import java.io.IOException;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.model.DataModel;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.FacesExceptionEx;
import com.ibm.xsp.model.AbstractDataSource;
import com.ibm.xsp.model.DataContainer;
import com.ibm.xsp.model.ViewDataSource;
import com.ibm.xsp.util.FacesUtil;

import frostillicus.xsp.model.AbstractModelList;
import frostillicus.xsp.model.ModelManager;
import frostillicus.xsp.model.ModelUtils;

public class ModelListDataSource extends AbstractDataSource implements ViewDataSource {

	private String managerName_;
	private String key_;
	private MethodBinding queryOpenView_;
	private MethodBinding postOpenView_;

	public ModelListDataSource() { }

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


	/* ******************************************************************************
	 * Data Source methods
	 ********************************************************************************/

	@Override
	protected String composeUniqueId() {
		return StringUtil.concatStrings(new String[] { getClass().getName(), getManagerName(), getKey() }, '|', false);
	}

	@Override
	public AbstractModelList<?> getDataObject() {
		ModelListDataContainer ac = (ModelListDataContainer)getDataContainer();
		if(ac != null) {
			return ac.getView();
		}
		return null;
	}

	@Override
	public boolean isReadonly() { return true; }

	@Override
	public DataContainer load(final FacesContext context) throws IOException {
		return openView(context);
	}

	@Override
	public void readRequestParams(final FacesContext context, final Map<String, Object> requestMap) {
		// NOP
	}

	@Override
	public boolean save(final FacesContext context, final DataContainer data) throws FacesExceptionEx {
		return false;
	}

	@Override
	public void setQueryOpenView(final MethodBinding binding) { queryOpenView_ = binding; }
	@Override
	public MethodBinding getQueryOpenView() { return queryOpenView_; }

	@Override
	public void setPostOpenView(final MethodBinding binding) { postOpenView_ = binding; }
	@Override
	public MethodBinding getPostOpenView() { return postOpenView_; }


	@Override
	public boolean isView(final Object view) {
		return view == getDataObject();
	}

	@Override
	public ModelListDataContainer openView(final FacesContext context) throws IOException {
		MethodBinding queryOpenView = getQueryOpenView();
		if (queryOpenView != null && FacesUtil.isCancelled(queryOpenView.invoke(context, null))) {
			return null;
		}

		ModelManager<?> manager = ModelUtils.findManagerInstance(context, managerName_);
		Object listObject = manager.getValue(key_);
		if(listObject == null) {
			throw new IOException("Received null value when retrieving list object from manager using key '" + key_ + "'");
		}
		if(!(listObject instanceof AbstractModelList)) {
			throw new IOException("Retrieved non-model-list object from manager using key '" + key_ + "'");
		}

		ModelListDataContainer container = new ModelListDataContainer(getBeanId(), getUniqueId(), (AbstractModelList<?>)listObject);

		MethodBinding postOpenView = getPostOpenView();
		if(postOpenView != null) {
			postOpenView.invoke(context, null);
		}

		return container;
	}

	@Override
	public DataModel getDataModel() {
		return getDataObject();
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
				key_
		};
	}
	@Override
	public void restoreState(final FacesContext context, final Object state) {
		Object[] values = (Object[])state;
		super.restoreState(context, values[0]);
		managerName_ = (String)values[1];
		key_ = (String)values[2];
	}


	/* ******************************************************************************
	 * Class-specific RuntimeProperties implementation
	 ********************************************************************************/
	@Override
	protected ModelListProperties getRuntimeProperties() {
		return (ModelListProperties)super.getRuntimeProperties();
	}
	@Override
	protected ModelListProperties createRuntimeProperties() {
		return new ModelListProperties();
	}
	@Override
	protected void initializeRuntimeProperties(final RuntimeProperties properties) {
		super.initializeRuntimeProperties(properties);
		ModelListProperties modelProps = (ModelListProperties)properties;
		modelProps.computedManagerName = getManagerName();
		modelProps.computedKey = getKey();
	}

	protected static class ModelListProperties extends RuntimeProperties {
		private static final long serialVersionUID = 1L;

		String computedManagerName;
		String computedKey;
	}
}
