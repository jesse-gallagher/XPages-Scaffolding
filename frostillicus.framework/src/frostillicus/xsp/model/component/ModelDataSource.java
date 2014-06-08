/*
 * Much of this code is "inspired by" com.ibm.xsp.extlib.model.ObjectDataSource in the XPages Extension Library
 */

package frostillicus.xsp.model.component;

import java.io.IOException;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.FacesExceptionEx;
import com.ibm.xsp.factory.FactoryLookup;
import com.ibm.xsp.model.AbstractDataSource;
import com.ibm.xsp.model.DataContainer;

import frostillicus.xsp.model.AbstractModelObject;
import frostillicus.xsp.model.ModelManager;
import frostillicus.xsp.model.ModelObject;
import frostillicus.xsp.model.ModelUtils;

public class ModelDataSource extends AbstractDataSource implements com.ibm.xsp.model.ModelDataSource {

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
	public AbstractModelObject getDataObject() {
		Container ac = (Container)getDataContainer();
		if(ac != null) {
			return ac.getModelObject();
		}
		return null;
	}

	@Override
	public DataModel getDataModel() {
		return getDataObject();
	}

	@Override
	public boolean isReadonly() {
		return false;
	}

	@Override
	public DataContainer load(final FacesContext context) throws IOException {
		// Now actually init the container
		ModelManager<?> manager = ModelUtils.findModelManager(context, managerName_);
		if(manager == null) {
			throw new IOException("Unable to locate frostillic.us manager for name '" + managerName_ + "'");
		}
		String key = StringUtil.isEmpty(key_) ? "new" : key_;
		Object modelObject = manager.getValue(key);
		if(modelObject == null) {
			throw new IOException("Received null value when retrieving object from manager using key '" + key + "'");
		}
		if(!(modelObject instanceof AbstractModelObject)) {
			throw new IOException("Retrieved non-model object from manager using key '" + key + "'");
		}

		return new Container(getBeanId(), getUniqueId(), (AbstractModelObject)modelObject);
	}

	@SuppressWarnings("unused")
	private static void dumpExistingFactories(final FactoryLookup lookup) {
		try {
			Class<?> facLookupClass = FactoryLookup.class;
			java.lang.reflect.Field factoriesField = facLookupClass.getDeclaredField("_factories");
			factoriesField.setAccessible(true);
			Map<?, ?> factories = (Map<?, ?>)factoriesField.get(lookup);
			for(Map.Entry<?, ?> entry : factories.entrySet()) {
				System.out.println(">> " + entry.getKey() + " => " + entry.getValue());
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
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
}
