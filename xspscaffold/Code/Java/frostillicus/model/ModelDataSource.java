/*
 * Much of this code is "inspired by" com.ibm.xsp.extlib.model.ObjectDataSource in the XPages Extension Library
 */

package frostillicus.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.FacesExceptionEx;
import com.ibm.xsp.actions.document.DocumentAdapter;
import com.ibm.xsp.actions.document.DocumentAdapterFactory;
import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.binding.ActionOutcomeUrl;
import com.ibm.xsp.factory.FactoryLookup;
import com.ibm.xsp.model.AbstractDataContainer;
import com.ibm.xsp.model.AbstractDataSource;
import com.ibm.xsp.model.DataContainer;
import com.ibm.xsp.model.DataModelFactory;
import com.ibm.xsp.model.DocumentDataContainer;
import com.ibm.xsp.model.FileDownloadValue;
import com.ibm.xsp.model.FileRowData;

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

	public DataModel getDataModel() {
		return getDataObject();
	}

	@Override
	public boolean isReadonly() {
		return false;
	}

	@Override
	public DataContainer load(final FacesContext context) throws IOException {
		initFactories(context);

		// Now actually init the container
		ModelManager<?> manager = ModelUtils.findModelManager(context, managerName_);
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

	@SuppressWarnings("deprecation")
	private void initFactories(final FacesContext context) throws IOException {
		// DataModelFactory is used by file-download controls as part of retrieving
		// attachment lists. Unfortunately, the default one will intercept calls
		// for our object, so we have to find and wrap it with a delegating variant
		ApplicationEx app = (ApplicationEx)FacesContext.getCurrentInstance().getApplication();
		FactoryLookup lookup = app.getFactoryLookup();

		String dataModelFactoryKey = "com.ibm.xsp.DOMINO_DATAMODEL_FACTORY";
		DataModelFactory existingDMF = (DataModelFactory)lookup.getFactory(dataModelFactoryKey);
		if(existingDMF != null) {
			if(!(existingDMF instanceof ModelObjectFactory)) {
				lookup.setFactory(dataModelFactoryKey, new ModelObjectFactory(existingDMF));
			}
		} else {
			try {
				lookup.setFactory("frostillicus.model.MODEL_OBJECT_FACTORY", ModelObjectFactory.class);
			} catch (InstantiationException e) {
				throw new IOException(e);
			} catch (IllegalAccessException e) {
				throw new IOException(e);
			}
		}

		// The same goes for the DocumentAdapterFactory, which is used as part of
		// sending "delete attachment" messages
		String documentAdapterFactoryKey = "com.ibm.xsp.DESIGNER_DOMINO_ADAPTER_FACTORY";
		DocumentAdapterFactory existingDAF = (DocumentAdapterFactory)lookup.getFactory(documentAdapterFactoryKey);
		if(existingDAF != null) {
			if(!(existingDAF instanceof ModelDocumentAdapterFactory)) {
				lookup.setFactory(documentAdapterFactoryKey, new ModelDocumentAdapterFactory(existingDAF));
			}
		} else {
			try {
				lookup.setFactory("frostillicus.model.MODEL_DOCUMENT_ADAPTER_FACTORY", ModelDocumentAdapterFactory.class);
			} catch (InstantiationException e) {
				throw new IOException(e);
			} catch (IllegalAccessException e) {
				throw new IOException(e);
			}
		}
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


	/* ******************************************************************************************
	 * Container class to encapsulate the actual object. Since models are Serializable,
	 * this is pretty simple
	 ********************************************************************************************/
	protected static class Container extends AbstractDataContainer implements DocumentDataContainer {
		private AbstractModelObject modelObject_;

		public Container() { }
		public Container(final String beanId, final String id, final AbstractModelObject modelObject) {
			super(beanId, id);
			modelObject_ = modelObject;
		}

		public AbstractModelObject getModelObject() {
			return modelObject_;
		}
		public Object getDocument() {
			return getModelObject();
		}

		public void deserialize(final ObjectInput in) throws IOException {
			try {
				modelObject_ = (AbstractModelObject)in.readObject();
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

	/* ******************************************************************************************
	 * Delegating factories to work around the horrors of the xp:fileDownload control
	 ********************************************************************************************/
	protected static class ModelObjectFactory implements DataModelFactory {
		private final DataModelFactory delegate_;

		public ModelObjectFactory() {
			delegate_ = null;
		}
		public ModelObjectFactory(final DataModelFactory delegate) {
			delegate_ = delegate;
		}

		@SuppressWarnings("unchecked")
		public DataModel createDataModel(final Object obj) {
			if(obj instanceof FileDownloadValue) {
				// Then it will be a HashMap with one entry, representing a model object -> field name pair
				FileDownloadValue download = (FileDownloadValue)obj;
				for(Map.Entry<Object, String> downloadEntry : ((Map<Object, String>)download.getValue()).entrySet()) {
					if(downloadEntry.getKey() instanceof ModelObject) {
						return ((ModelObject)downloadEntry.getKey()).getAttachmentData(downloadEntry.getValue());
					}
				}

				return delegate_.createDataModel(obj);
			} else if(delegate_ != null) {
				return delegate_.createDataModel(obj);
			}
			return null;
		}

	}

	protected static class ModelDocumentAdapterFactory implements DocumentAdapterFactory {
		private final DocumentAdapterFactory delegate_;

		public ModelDocumentAdapterFactory() {
			delegate_ = null;
		}
		public ModelDocumentAdapterFactory(final DocumentAdapterFactory delegate) {
			delegate_ = delegate;
		}

		public DocumentAdapter createDocumentAdapter(final FacesContext context, final Object obj) {
			// A valid document adapter will come along as a Map "tuple" of model object -> field name
			if(obj instanceof Map) {
				for(Map.Entry<?, ?> entry : ((Map<?, ?>)obj).entrySet()) {
					if(entry.getKey() instanceof ModelObject) {
						return new ModelDocumentAdapter((ModelObject)entry.getKey());
					}
				}
			}
			return delegate_.createDocumentAdapter(context, obj);
		}

	}

	protected static class ModelDocumentAdapter implements DocumentAdapter {
		private final String id_;

		public ModelDocumentAdapter(final ModelObject modelObject) {
			id_ = modelObject.getId();
		}

		public void addAttachment(final FacesContext context, final Object document, final String name, final InputStream istream, final int length, final String description, final String type) {
			System.out.println("called addAttachment");
			// NOP
		}

		public void addOpenPageParameters(final FacesContext context, final String var, final ActionOutcomeUrl outcomeUrl) {
			// NOP
		}

		public void delete(final FacesContext context, final Object document) {
			((ModelObject)document).delete();
		}

		@SuppressWarnings("unchecked")
		public void deleteAttachments(final FacesContext context, final Object document, final String name, final boolean deleteAll) {
			// In this case, "document" is a HashMap tuple
			Map.Entry<ModelObject, String> tuple = ((Map<ModelObject, String>)document).entrySet().iterator().next();
			tuple.getKey().deleteAttachment(tuple.getValue(), name);
		}

		public String getDocumentId(final FacesContext context, final String var) {
			return id_;
		}

		public String getDocumentPage(final FacesContext context, final String documentId) {
			// TODO Do this, maybe?
			return null;
		}

		public List<FileRowData> getEmbeddedImageList(final Object document, final String fieldName) {
			System.out.println("requesting embedded image list for a " + document.getClass().getName());
			return ((ModelObject)document).getAttachmentList(fieldName);
		}

		public String getParentId(final FacesContext context, final Object document) {
			return "";
		}

		public boolean isEditable(final FacesContext context, final Object document) {
			return true;
		}

		public void modifyField(final FacesContext context, final Object document, final String name, final Object value) {
			((ModelObject)document).setValue(name, value);
		}

		public void save(final FacesContext context, final Object document) {
			((ModelObject)document).save();
		}

		public void setDocument(final FacesContext context, final Object document, final Object value) {
			// NOP
		}

		public void setUserReadOnly(final FacesContext context, final Object document, final boolean readOnly) {
			// TODO Implement when read-only support exists at the model level
			// NOP
		}

	}
}
