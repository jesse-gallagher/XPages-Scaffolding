package frostillicus.xsp.model.component;

import java.util.Map;

import javax.faces.context.FacesContext;

import com.ibm.xsp.actions.document.DocumentAdapter;
import com.ibm.xsp.actions.document.DocumentAdapterFactory;

import frostillicus.xsp.model.ModelObject;

/**
 * @since 1.0
 */
public class ModelDocumentAdapterFactory implements DocumentAdapterFactory {
	private final DocumentAdapterFactory delegate_;

	public ModelDocumentAdapterFactory() {
		delegate_ = null;
	}
	public ModelDocumentAdapterFactory(final DocumentAdapterFactory delegate) {
		delegate_ = delegate;
	}

	@Override
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