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
import com.ibm.xsp.model.TabularDataModel;
import com.ibm.xsp.model.ViewDataSource;
import com.ibm.xsp.util.FacesUtil;

import frostillicus.xsp.model.AbstractModelList;
import frostillicus.xsp.model.ModelManager;
import frostillicus.xsp.model.ModelUtils;

public class ModelListDataSource extends AbstractDataSource implements ViewDataSource {

	private String managerName_;
	private String key_;
	private String search_;
	private String categoryFilter_;
	private String sortColumn_;
	private String sortOrder_;
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

	public void setSearch(final String search) { search_ = search; }
	public String getSearch() {
		if(hasRuntimeProperties()) {
			return getRuntimeProperties().computedSearch;
		}
		if(search_ != null) {
			return search_;
		}
		ValueBinding valueBinding = getValueBinding("search");
		if(valueBinding != null) {
			return (String)valueBinding.getValue(FacesContext.getCurrentInstance());
		}

		return "";
	}

	public void setCategoryFilter(final String categoryFilter) { categoryFilter_ = categoryFilter; }
	public String getCategoryFilter() {
		if(hasRuntimeProperties()) {
			return getRuntimeProperties().computedCategoryFilter;
		}
		if(categoryFilter_ != null) {
			return categoryFilter_;
		}
		ValueBinding valueBinding = getValueBinding("categoryFilter");
		if(valueBinding != null) {
			return (String)valueBinding.getValue(FacesContext.getCurrentInstance());
		}

		return "";
	}

	public void setSortColumn(final String sortColumn) { sortColumn_ = sortColumn; }
	public String getSortColumn() {
		if(hasRuntimeProperties()) {
			return getRuntimeProperties().computedSortColumn;
		}
		if(sortColumn_ != null) {
			return sortColumn_;
		}
		ValueBinding valueBinding = getValueBinding("sortColumn");
		if(valueBinding != null) {
			return (String)valueBinding.getValue(FacesContext.getCurrentInstance());
		}

		return "";
	}

	public void setSortOrder(final String sortOrder) { sortOrder_ = sortOrder; }
	public String getSortOrder() {
		if(hasRuntimeProperties()) {
			return getRuntimeProperties().computedSortOrder;
		}
		if(sortOrder_ != null) {
			return sortOrder_;
		}
		ValueBinding valueBinding = getValueBinding("sortOrder");
		if(valueBinding != null) {
			return (String)valueBinding.getValue(FacesContext.getCurrentInstance());
		}

		return "";
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

		String managerName = getManagerName();
		ModelManager<?> manager = ModelUtils.findManagerInstance(context, managerName);
		Object listObject;
		String key = getKey();
		String categoryFilter = getCategoryFilter();
		if(StringUtil.isEmpty(categoryFilter)) {
			listObject = manager.getValue(key);
		} else {
			// Then treat the key as a collection name
			listObject = manager.getNamedCollection(key, categoryFilter);
		}
		if(listObject == null) {
			throw new IOException("Received null value when retrieving list object from manager using key '" + key + "'");
		}
		if(!(listObject instanceof AbstractModelList)) {
			throw new IOException("Retrieved non-model-list object from manager using key '" + key + "'");
		}

		AbstractModelList<?> list = (AbstractModelList<?>)listObject;

		// Search if applicable
		String search = getSearch();
		if(StringUtil.isNotEmpty(search)) {
			list.search(search);
		}

		// Set a sort column if applicable
		String sortColumn = getSortColumn();
		if(StringUtil.isNotEmpty(sortColumn)) {
			String sortOrder = getSortOrder();
			if(TabularDataModel.SORT_DESCENDING.equalsIgnoreCase(sortOrder)) {
				list.setResortOrder(sortColumn, TabularDataModel.SORT_DESCENDING);
			} else {
				list.setResortOrder(sortColumn, TabularDataModel.SORT_ASCENDING);
			}
		}

		ModelListDataContainer container = new ModelListDataContainer(getBeanId(), getUniqueId(), list);

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
				key_,
				categoryFilter_,
				search_,
				sortColumn_,
				sortOrder_
		};
	}
	@Override
	public void restoreState(final FacesContext context, final Object state) {
		Object[] values = (Object[])state;
		super.restoreState(context, values[0]);
		managerName_ = (String)values[1];
		key_ = (String)values[2];
		categoryFilter_ = (String)values[3];
		search_ = (String)values[4];
		sortColumn_ = (String)values[5];
		sortOrder_ = (String)values[6];
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
		modelProps.computedCategoryFilter = getCategoryFilter();
		modelProps.computedSearch = getSearch();
		modelProps.computedSortColumn = getSortColumn();
		modelProps.computedSortOrder = getSortOrder();
	}

	protected static class ModelListProperties extends RuntimeProperties {
		private static final long serialVersionUID = 1L;

		String computedManagerName;
		String computedKey;
		String computedCategoryFilter;
		String computedSearch;
		String computedSortColumn;
		String computedSortOrder;
	}
}
