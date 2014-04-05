/*
 * Much of this code is "inspired by" com.ibm.xsp.extlib.model.ObjectDataSource in the XPages Extension Library
 */

package frostillicus.model;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

import javax.faces.context.FacesContext;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.FacesExceptionEx;
import com.ibm.xsp.model.AbstractDataContainer;
import com.ibm.xsp.model.AbstractDataSource;
import com.ibm.xsp.model.DataContainer;
import com.ibm.xsp.model.DocumentDataContainer;

public class ModelDataSource extends AbstractDataSource {

	private String managerName_;
	private String key_;

	public ModelDataSource() { }

	public void setManagerName(final String managerName) { managerName_ = managerName; }
	public String getManagerName() { return managerName_; }

	public void setKey(final String key) { key_ = key; }
	public String getKey() { return key_; }

	@Override
	protected String composeUniqueId() {
		return getClass().getName();
	}

	@Override
	public ModelObject getDataObject() {
		Container ac = (Container)getDataContainer();
		if(ac != null) {
			return ac.getModelObject();
		}
		return null;
	}

	@Override
	public boolean isReadonly() {
		return false;
	}

	@Override
	public DataContainer load(final FacesContext context) throws IOException {
		ModelManager<?> manager = ModelUtils.findModelManager(context, managerName_);
		String key = StringUtil.isEmpty(key_) ? "new" : key_;
		Object modelObject = manager.getValue(key);
		if(modelObject == null) {
			throw new IOException("Received null value when retrieving object from manager using key '" + key + "'");
		}
		if(!(modelObject instanceof ModelObject)) {
			throw new IOException("Retrieved non-model object from manager using key '" + key + "'");
		}

		return new Container(getBeanId(), getUniqueId(), (ModelObject)modelObject);
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


	protected static class Container extends AbstractDataContainer implements DocumentDataContainer {
		private ModelObject modelObject_;

		public Container() { }
		public Container(final String beanId, final String id, final ModelObject modelObject) {
			super(beanId, id);
			modelObject_ = modelObject;
		}

		public ModelObject getModelObject() {
			return modelObject_;
		}
		public Object getDocument() {
			return getModelObject();
		}

		public void deserialize(final ObjectInput in) throws IOException {
			try {
				modelObject_ = (ModelObject)in.readObject();
			} catch(ClassNotFoundException cnfe) {
				IOException ioe = new IOException("Error while deserializing object");
				ioe.initCause(cnfe);
				throw ioe;
			}
		}

		public void serialize(final ObjectOutput out) throws IOException {
			out.writeObject(modelObject_);
		}

	}
}
