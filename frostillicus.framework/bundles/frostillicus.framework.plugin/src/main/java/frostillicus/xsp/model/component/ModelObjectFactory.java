package frostillicus.xsp.model.component;

import java.util.Map;

import javax.faces.model.DataModel;

import com.ibm.xsp.model.DataModelFactory;
import com.ibm.xsp.model.FileDownloadValue;

import frostillicus.xsp.model.ModelObject;

/**
 * Delegating factories to work around the horrors of the xp:fileDownload control
 * 
 * @since 1.0
 */
public class ModelObjectFactory implements DataModelFactory {
	private final DataModelFactory delegate_;

	public ModelObjectFactory() {
		delegate_ = null;
	}
	public ModelObjectFactory(final DataModelFactory delegate) {
		delegate_ = delegate;
	}

	@Override
	@SuppressWarnings("unchecked")
	public DataModel createDataModel(final Object obj) {
		if(obj instanceof FileDownloadValue) {
			// Then it will be a HashMap with one entry, representing a model object -> field name pair
			FileDownloadValue download = (FileDownloadValue)obj;
			Object downloadValue = download.getValue();
			if(downloadValue instanceof Map) {
				for(Map.Entry<Object, String> downloadEntry : ((Map<Object, String>)downloadValue).entrySet()) {
					if(downloadEntry.getKey() instanceof ModelObject) {
						return ((ModelObject)downloadEntry.getKey()).getAttachmentData(downloadEntry.getValue());
					}
				}
			}

			return delegate_.createDataModel(obj);
		} else if(delegate_ != null) {
			return delegate_.createDataModel(obj);
		}
		return null;
	}

}