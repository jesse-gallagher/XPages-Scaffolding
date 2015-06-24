package frostillicus.xsp.model.component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import javax.faces.context.FacesContext;

import org.apache.commons.io.input.ClassLoaderObjectInputStream;

import com.ibm.xsp.context.FacesContextEx;
import com.ibm.xsp.event.FacesContextListener;
import com.ibm.xsp.model.AbstractDataContainer;
import com.ibm.xsp.model.DocumentDataContainer;

import frostillicus.xsp.model.ModelObject;

/* ******************************************************************************************
 * Container class to encapsulate the actual object. Since models are Serializable,
 * this is pretty simple
 ********************************************************************************************/
public class ModelDataContainer extends AbstractDataContainer implements DocumentDataContainer {
	private ModelObject modelObject_;
	private byte[] serializedModel_;
	private transient FacesContextListener contextListener_;

	public ModelDataContainer() {
	}

	public ModelDataContainer(final String beanId, final String uniqueId, final ModelObject modelObject) throws IOException {
		super(beanId, uniqueId);

		modelObject_ = modelObject;
		
		installFacesListener();
	}

	private void installFacesListener() {
		if (this.contextListener_ == null) {
			FacesContextEx facesContext = FacesContextEx.getCurrentInstance();
			this.contextListener_ = new FacesContextListener() {
				public void beforeContextReleased(FacesContext facesContext) {
					try {
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ObjectOutputStream oos = new ObjectOutputStream(bos);
						oos.writeObject(modelObject_);
						modelObject_ = null;
						oos.flush();
						bos.flush();
						serializedModel_ = bos.toByteArray();
						contextListener_ = null;
					} catch(Exception e) {
						e.printStackTrace();
					}
				}

				public void beforeRenderingPhase(FacesContext facesContext) {
					
				}
			};
			facesContext.addRequestListener(this.contextListener_);
		}
	}

	public ModelObject getModelObject() {
		restoreWrappedObject();
		return modelObject_;
	}

	@Override
	public Object getDocument() {
		restoreWrappedObject();
		return modelObject_;
	}
	
	private void restoreWrappedObject() {
		try {
			if(modelObject_ == null) {
				// Then this must be a new request
				installFacesListener();
				
				ByteArrayInputStream bis = new ByteArrayInputStream(serializedModel_);
				FacesContext facesContext = FacesContext.getCurrentInstance();
				ClassLoaderObjectInputStream ois = new ClassLoaderObjectInputStream(facesContext.getContextClassLoader(), bis);
				modelObject_ = (ModelObject)ois.readObject();
				ois.close();
				bis.close();
				serializedModel_ = null;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deserialize(final ObjectInput in) throws IOException {
		try {
			modelObject_ = (ModelObject) in.readObject();
		} catch (ClassNotFoundException cnfe) {
			IOException ioe = new IOException("Error while deserializing object");
			ioe.initCause(cnfe);
			throw ioe;
		}
	}

	@Override
	public void serialize(final ObjectOutput out) throws IOException {
		restoreWrappedObject();
		out.writeObject(modelObject_);
	}
}