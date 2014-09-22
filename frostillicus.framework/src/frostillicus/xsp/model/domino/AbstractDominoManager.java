package frostillicus.xsp.model.domino;

import java.util.*;

import org.openntf.domino.*;

import frostillicus.xsp.model.ModelManager;
import frostillicus.xsp.model.ModelUtils;
import frostillicus.xsp.util.FrameworkUtils;

public abstract class AbstractDominoManager<E extends AbstractDominoModel> implements ModelManager<E> {
	private static final long serialVersionUID = 1L;

	abstract protected String getViewPrefix();

	@SuppressWarnings("unchecked")
	protected Class<E> getModelClass() {
		Class<?> enclosingClass = getClass().getEnclosingClass();
		if(AbstractDominoModel.class.isAssignableFrom(enclosingClass)) {
			return (Class<E>)enclosingClass;
		}
		throw new RuntimeException("No model class found.");
	}

	@Override
	public DominoModelList<E> getNamedCollection(final String name, final String category) {
		return new DominoModelList<E>(getDatabase(), getViewPrefix() + name, category, getModelClass());
	}

	protected E createFromDocument(final Document doc) {
		try {
			E model = getModelClass().newInstance();
			model.initFromDocument(doc);
			return model;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public E create() {
		try {
			E model = getModelClass().newInstance();
			model.initFromDatabase(getDatabase());
			return model;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Database getDatabase() {
		return FrameworkUtils.getDatabase();
	}

	@Override
	public Class<E> getType(final Object key) {
		return getModelClass();
	}

	@Override
	public Object getValue(final Object keyObject) {
		if (!(keyObject instanceof String)) {
			throw new IllegalArgumentException();
		}
		String key = (String) keyObject;

		try {
			Object result = null;
			if ("new".equals(key)) {
				result = create();
			} else if (ModelUtils.isUnid(key)) {
				Database database = getDatabase();
				Document doc = database.getDocumentWithKey(key);
				if(doc == null) {
					result = null;
				} else {
					result = createFromDocument(doc);
				}
			} else {
				Map<String, Object> cacheScope = ModelUtils.getCacheScope();
				String cacheKey = getClass().getName() + key;
				if (!cacheScope.containsKey(cacheKey)) {
					if (key.contains("^^")) {
						String[] bits = key.split("\\^\\^");
						cacheScope.put(cacheKey, getNamedCollection(bits[0], bits.length == 1 ? "" : bits[1]));
					} else {
						cacheScope.put(cacheKey, getNamedCollection(key, null));
					}
				}
				result = cacheScope.get(cacheKey);
			}
			return result;
		} catch (Exception ne) {
			// I'll want to know about this
			throw ne instanceof RuntimeException ? (RuntimeException)ne : new RuntimeException(ne);
		}
	}

	@Override
	public E getById(final String id) {
		Database database = getDatabase();
		Document doc = database.getDocumentWithKey(id);
		if(doc == null) {
			return null;
		} else {
			return createFromDocument(doc);
		}
	}

	@Override
	public boolean isReadOnly(final Object key) {
		return true;
	}

	@Override
	public void setValue(final Object key, final Object value) {
		throw new UnsupportedOperationException();
	}
}