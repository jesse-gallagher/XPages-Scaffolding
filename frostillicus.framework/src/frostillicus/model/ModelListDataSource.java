package frostillicus.model;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.model.DataModel;

import com.ibm.xsp.FacesExceptionEx;
import com.ibm.xsp.model.AbstractDataContainer;
import com.ibm.xsp.model.AbstractDataSource;
import com.ibm.xsp.model.DataContainer;
import com.ibm.xsp.model.ViewDataContainer;
import com.ibm.xsp.model.ViewDataSource;
import com.ibm.xsp.util.FacesUtil;

public class ModelListDataSource extends AbstractDataSource implements ViewDataSource {

	private String managerName_;
	private String key_;
	private MethodBinding queryOpenView_;
	private MethodBinding postOpenView_;

	public ModelListDataSource() { }

	public void setManagerName(final String managerName) { managerName_ = managerName; }
	public String getManagerName() { return managerName_; }

	public void setKey(final String key) { key_ = key; }
	public String getKey() { return key_; }

	@Override
	protected String composeUniqueId() {
		return getClass().getName();
	}

	@Override
	public AbstractModelList<?> getDataObject() {
		Container ac = (Container)getDataContainer();
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

	public void setQueryOpenView(final MethodBinding binding) { queryOpenView_ = binding; }
	public MethodBinding getQueryOpenView() { return queryOpenView_; }

	public void setPostOpenView(final MethodBinding binding) { postOpenView_ = binding; }
	public MethodBinding getPostOpenView() { return postOpenView_; }


	public boolean isView(final Object view) {
		return view == getDataObject();
	}

	public Container openView(final FacesContext context) throws IOException {
		MethodBinding queryOpenView = getQueryOpenView();
		if (queryOpenView != null && FacesUtil.isCancelled(queryOpenView.invoke(context, null))) {
			return null;
		}

		ModelManager<?> manager = ModelUtils.findModelManager(context, managerName_);
		Object listObject = manager.getValue(key_);
		if(listObject == null) {
			throw new IOException("Received null value when retrieving list object from manager using key '" + key_ + "'");
		}
		if(!(listObject instanceof AbstractModelList)) {
			throw new IOException("Retrieved non-model-list object from manager using key '" + key_ + "'");
		}

		Container container = new Container(getBeanId(), getUniqueId(), (AbstractModelList<?>)listObject);

		MethodBinding postOpenView = getPostOpenView();
		if(postOpenView != null) {
			postOpenView.invoke(context, null);
		}

		return container;
	}

	public DataModel getDataModel() {
		return getDataObject();
	}

	public static class Container extends AbstractDataContainer implements ViewDataContainer {
		private AbstractModelList<?> modelList_;

		public Container() { }
		public Container(final String beanId, final String id, final AbstractModelList<?> modelList) {
			super(beanId, id);
			modelList_ = modelList;
		}

		public AbstractModelList<?> getView() {
			return modelList_;
		}

		public void deserialize(final ObjectInput in) throws IOException {
			try {
				modelList_ = (AbstractModelList<?>)in.readObject();
			} catch(ClassNotFoundException cnfe) {
				IOException ioe = new IOException("Error while deserializing object");
				ioe.initCause(cnfe);
				throw ioe;
			}
		}

		public void serialize(final ObjectOutput out) throws IOException {
			out.writeObject(modelList_);
		}

	}
}
