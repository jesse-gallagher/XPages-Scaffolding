package frostillicus.xsp.model.component;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.faces.context.FacesContext;

import com.ibm.xsp.model.AbstractDataContainer;

import frostillicus.xsp.model.AbstractModelObject;
import frostillicus.xsp.model.ModelManager;
import frostillicus.xsp.model.ModelObject;
import frostillicus.xsp.model.ModelUtils;

/* ******************************************************************************************
 * Container class to encapsulate the actual object. Since models are Serializable,
 * this is pretty simple
 ********************************************************************************************/
public class Container extends AbstractDataContainer  {
	private String managerName_;
	private String key_;
	private boolean readonly_;
	private AbstractModelObject modelObject_;

	public Container() { }

	public Container(final String beanId, final String uniqueId, final String managerName, final String key, final boolean readonly) throws IOException {
		super(beanId, uniqueId);

		managerName_ = managerName;
		key_ = key;
		readonly_ = readonly;

		// Now actually init the container
		ModelManager<?> manager = ModelUtils.findManagerInstance(FacesContext.getCurrentInstance(), managerName);
		if(manager == null) {
			throw new IOException("Unable to locate frostillic.us manager for name '" + managerName + "'");
		}
		//		String key = StringUtil.isNotEmpty(id_) ? id_ : StringUtil.isNotEmpty(key_) ? key_ : "new";
		System.out.println("fetching model object with key '" + key + "'");
		Object modelObject = manager.getValue(key);
		if(modelObject == null) {
			throw new IOException("Received null value when retrieving object from manager using key '" + key + "'");
		}
		if(!(modelObject instanceof AbstractModelObject)) {
			throw new IOException("Retrieved non-model object from manager using key '" + key + "'");
		}

		if(readonly) {
			((ModelObject)modelObject).freeze();
		}
		modelObject_ = (AbstractModelObject)modelObject;
	}

	public AbstractModelObject getModelObject() {
		return modelObject_;
	}

	@Override
	public void deserialize(final ObjectInput in) throws IOException {
		System.out.println("deserializing " + getClass().getName());
		try {
			managerName_ = readUTF(in);
			key_ = readUTF(in);
			readonly_ = in.readBoolean();
			modelObject_ = (AbstractModelObject)in.readObject();
		} catch(ClassNotFoundException cnfe) {
			IOException ioe = new IOException("Error while deserializing object");
			ioe.initCause(cnfe);
			throw ioe;
		}
	}

	@Override
	public void serialize(final ObjectOutput out) throws IOException {
		System.out.println("serializing " + getClass().getName());
		writeUTF(out, managerName_);
		writeUTF(out, key_);
		out.writeBoolean(readonly_);
		out.writeObject(modelObject_);
	}

}