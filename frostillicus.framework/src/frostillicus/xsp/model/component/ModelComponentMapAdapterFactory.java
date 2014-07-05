package frostillicus.xsp.model.component;

import frostillicus.xsp.controller.ComponentMapAdapter;
import frostillicus.xsp.controller.ComponentMapAdapterFactory;
import frostillicus.xsp.model.ModelObject;

public class ModelComponentMapAdapterFactory implements ComponentMapAdapterFactory {

	@Override
	public ComponentMapAdapter createAdapter(final Object obj) {
		if(obj instanceof ModelObject) {
			return new ModelComponentMapAdapter((ModelObject)obj);
		}
		return null;
	}
}
