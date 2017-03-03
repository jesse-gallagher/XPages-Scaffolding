package frostillicus.xsp.model.component;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.ibm.xsp.model.AbstractDataContainer;
import com.ibm.xsp.model.ViewDataContainer;

import frostillicus.xsp.model.AbstractModelList;

/**
 * @since 1.0
 */
public class ModelListDataContainer extends AbstractDataContainer implements ViewDataContainer {
	private AbstractModelList<?> modelList_;

	public ModelListDataContainer() { }
	public ModelListDataContainer(final String beanId, final String id, final AbstractModelList<?> modelList) {
		super(beanId, id);
		modelList_ = modelList;
	}

	@Override
	public AbstractModelList<?> getView() {
		return modelList_;
	}

	@Override
	public void deserialize(final ObjectInput in) throws IOException {
		try {
			modelList_ = (AbstractModelList<?>)in.readObject();
		} catch(ClassNotFoundException cnfe) {
			IOException ioe = new IOException("Error while deserializing object");
			ioe.initCause(cnfe);
			throw ioe;
		}
	}

	@Override
	public void serialize(final ObjectOutput out) throws IOException {
		out.writeObject(modelList_);
	}

}