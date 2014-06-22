package frostillicus.xsp.model.component;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.ibm.xsp.model.AbstractDataContainer;
import com.ibm.xsp.model.DocumentDataContainer;

import frostillicus.xsp.model.ModelObject;

/* ******************************************************************************************
 * Container class to encapsulate the actual object. Since models are Serializable,
 * this is pretty simple
 ********************************************************************************************/
public class ModelDataContainer extends AbstractDataContainer implements DocumentDataContainer {
	private ModelObject modelObject_;

	public ModelDataContainer() { }

	public ModelDataContainer(final String beanId, final String uniqueId, final ModelObject modelObject) throws IOException {
		super(beanId, uniqueId);

		modelObject_ = modelObject;
	}

	public ModelObject getModelObject() {
		return modelObject_;
	}

	@Override
	public Object getDocument() {
		return modelObject_;
	}

	@Override
	public void deserialize(final ObjectInput in) throws IOException {
		try {
			modelObject_ = (ModelObject)in.readObject();
		} catch(ClassNotFoundException cnfe) {
			IOException ioe = new IOException("Error while deserializing object");
			ioe.initCause(cnfe);
			throw ioe;
		}
	}

	@Override
	public void serialize(final ObjectOutput out) throws IOException {
		out.writeObject(modelObject_);
	}

}