package frostillicus.xsp.model.component;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.ibm.xsp.model.AbstractDataContainer;
import com.ibm.xsp.model.DocumentDataContainer;

import frostillicus.xsp.model.AbstractModelObject;

/* ******************************************************************************************
 * Container class to encapsulate the actual object. Since models are Serializable,
 * this is pretty simple
 ********************************************************************************************/
public class Container extends AbstractDataContainer implements DocumentDataContainer {
	private AbstractModelObject modelObject_;

	public Container() { }
	public Container(final String beanId, final String id, final AbstractModelObject modelObject) {
		super(beanId, id);
		modelObject_ = modelObject;
	}

	public AbstractModelObject getModelObject() {
		return modelObject_;
	}
	@Override
	public Object getDocument() {
		return getModelObject();
	}

	@Override
	public void deserialize(final ObjectInput in) throws IOException {
		try {
			modelObject_ = (AbstractModelObject)in.readObject();
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