package frostillicus.model;

import java.io.Serializable;
import java.util.*;
import lotus.domino.*;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.DataObject;

public abstract class AbstractDominoManager<E extends AbstractDominoModel> implements Serializable, DataObject {
	private static final long serialVersionUID = 1L;

	abstract protected DominoModelList<E> getNamedCollection(final String name, final String category) throws NotesException;

	abstract protected E createFromDocument(final Document doc) throws NotesException;

	abstract protected E create() throws NotesException;

	protected Database getDatabase() throws NotesException {
		return ExtLibUtil.getCurrentDatabase();
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
				try {
					Database database = getDatabase();
					Document doc = database.getDocumentByUNID(key);
					result = createFromDocument(doc);
					doc.recycle();
				} catch (NotesException ne) {
					// Then the doc wasn't in the DB, most likely - ignore
				}
			} else {
				Map<String, Object> cacheScope = ExtLibUtil.getViewScope();
				String cacheKey = getClass().getName() + key;
				if (!cacheScope.containsKey(cacheKey)) {
					if (key.contains("^^")) {
						String[] bits = key.split("\\^\\^");
						cacheScope.put(cacheKey, getNamedCollection(bits[0], bits[1]));
					} else {
						cacheScope.put(cacheKey, getNamedCollection(key, null));
					}

				}
				result = cacheScope.get(cacheKey);
			}
			return result;
		} catch (NotesException ne) {
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