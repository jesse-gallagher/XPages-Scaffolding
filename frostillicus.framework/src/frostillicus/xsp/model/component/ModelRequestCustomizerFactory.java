package frostillicus.xsp.model.component;

import javax.faces.context.FacesContext;

import com.ibm.xsp.actions.document.DocumentAdapterFactory;
import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.context.RequestCustomizerFactory;
import com.ibm.xsp.context.RequestParameters;
import com.ibm.xsp.factory.FactoryLookup;
import com.ibm.xsp.model.DataModelFactory;

/**
 * I don't actually care about customizing requests. Instead, I want to use this as a hook to reliably
 * attach the data model factories.
 * I have to override the factories this way rather than using the standard extension points because
 * IBM's factories don't behave nicely with non-DominoDocument DocumentDataSources. Instead of returning
 * null, they try to cast all DocumentDataSources to DominoDocument, causing a ClassCastException.
 * 
 * @author jgallagher
 *
 */
public class ModelRequestCustomizerFactory extends RequestCustomizerFactory {

	@SuppressWarnings("deprecation")
	@Override
	public void initializeParameters(final FacesContext facesContext, final RequestParameters requestParameters) {
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
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
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
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
