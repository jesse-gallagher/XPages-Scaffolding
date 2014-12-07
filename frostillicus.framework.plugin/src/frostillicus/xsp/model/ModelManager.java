package frostillicus.xsp.model;

import java.io.Serializable;

import com.ibm.xsp.model.DataObject;

public interface ModelManager<E extends ModelObject> extends Serializable, DataObject {
	public AbstractModelList<E> getNamedCollection(final String name, final String category);

	public E create();

	public E getById(String id);
}
