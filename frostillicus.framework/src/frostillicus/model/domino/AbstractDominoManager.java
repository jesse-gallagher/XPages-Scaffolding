package frostillicus.model.domino;

import java.util.*;

import javax.faces.context.FacesContext;

import org.openntf.domino.*;
import com.ibm.xsp.extlib.util.ExtLibUtil;

import frostillicus.model.ModelManager;
import frostillicus.model.ModelUtils;

public abstract class AbstractDominoManager<E extends AbstractDominoModel> implements ModelManager<E> {
	private static final long serialVersionUID = 1L;

	abstract protected Class<E> getModelClass();
	abstract protected String getViewPrefix();

	public DominoModelList<E> getNamedCollection(final String name, final String category) {
		return new DominoModelList<E>(getDatabase(), getViewPrefix() + name, category, getModelClass());
	}

	protected E createFromDocument(final Document doc) {
		try {
			return getModelClass().getConstructor(Document.class).newInstance(doc);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected E create() {
		try {
			return getModelClass().getConstructor(Database.class).newInstance(getDatabase());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Database getDatabase() {
		return (Database)ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "database");
	}

	public Class<?> getType(final Object key) {
		return Object.class;
	}

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
				Document doc = database.getDocumentByUNID(key);
				result = createFromDocument(doc);
			} else {
				Map<String, Object> cacheScope = ExtLibUtil.getViewScope();
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
			throw new RuntimeException(ne);
		}
	}

	public boolean isReadOnly(final Object key) {
		return true;
	}

	public void setValue(final Object key, final Object value) {
		throw new UnsupportedOperationException();
	}
}