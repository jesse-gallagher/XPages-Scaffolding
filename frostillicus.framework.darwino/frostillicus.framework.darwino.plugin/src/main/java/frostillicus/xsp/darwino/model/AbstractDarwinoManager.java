package frostillicus.xsp.darwino.model;

import java.sql.SQLException;
import java.util.Map;

import com.darwino.commons.json.JsonException;
import com.darwino.commons.log.Logger;
import com.darwino.jsonstore.Database;
import com.darwino.jsonstore.Document;
import com.darwino.jsonstore.Store;

import frostillicus.xsp.darwino.Activator;
import frostillicus.xsp.darwino.SqlContextApplicationListener;
import frostillicus.xsp.model.ModelManager;
import frostillicus.xsp.model.ModelUtils;

/**
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public abstract class AbstractDarwinoManager<E extends AbstractDarwinoModel> implements ModelManager<E> {
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = Activator.log;
	
	public AbstractDarwinoManager() {
		if(log.isTraceEnabled()) {
			log.trace("{0}: Init", getClass().getName());
		}
	}
	
	@SuppressWarnings("unchecked")
	protected Class<E> getModelClass() {
		Class<?> enclosingClass = getClass().getEnclosingClass();
		if(AbstractDarwinoModel.class.isAssignableFrom(enclosingClass)) {
			return (Class<E>)enclosingClass;
		}
		throw new RuntimeException("No model class found.");
	}
	
	protected abstract String getConnectionBeanName();
	
	protected abstract String getDbName();
	
	protected String getInstanceId() {
		return ""; //$NON-NLS-1$
	}
	
	protected String getStoreId() {
		return Database.STORE_DEFAULT;
	}
	
	protected Database getDatabase() throws SQLException, JsonException {
		return SqlContextApplicationListener.getDatabase(getConnectionBeanName(), getInstanceId(), getDbName());
	}
	protected Store getStore() throws JsonException, SQLException {
		return getDatabase().getStore(getStoreId());
	}

	@Override
	public Class<?> getType(Object key) {
		return getModelClass();
	}

	@Override
	public Object getValue(Object keyObject) {
		if (!(keyObject instanceof String)) {
			throw new IllegalArgumentException();
		}
		String key = (String) keyObject;

		try {
			Object result = null;
			if ("new".equals(key)) { //$NON-NLS-1$
				result = create();
			} else if (ModelUtils.isUnid(key)) {
				result = getById(key);
			} else {
				if(log.isDebugEnabled()) {
					log.debug("{0}#getValue: getting named collection for {1}", getClass().getSimpleName(), keyObject); //$NON-NLS-1$
				}
				
				Map<String, Object> cacheScope = ModelUtils.getCacheScope();
				String cacheKey = getClass().getName() + key;
				if (!cacheScope.containsKey(cacheKey)) {
					if (key.contains("^^")) { //$NON-NLS-1$
						String[] bits = key.split("\\^\\^"); //$NON-NLS-1$
						cacheScope.put(cacheKey, getNamedCollection(bits[0], bits.length == 1 ? "" : bits[1])); //$NON-NLS-1$
					} else {
						cacheScope.put(cacheKey, getNamedCollection(key, null));
					}
				}
				result = cacheScope.get(cacheKey);
			}
			if(log.isDebugEnabled()) {
				log.debug("{0}#getValue: returning result {1}", getClass().getSimpleName(), result);
			}
			return result;
		} catch (Exception ne) {
			// I'll want to know about this
			throw ne instanceof RuntimeException ? (RuntimeException)ne : new RuntimeException(ne);
		}
	}

	@Override
	public DarwinoModelList<E> getNamedCollection(String name, String category) {
		try {
			return new DarwinoModelList<E>(getConnectionBeanName(), getStore(), getModelClass());
		} catch(JsonException e) {
			throw new RuntimeException(e);
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected E createFromDocument(final Document doc) {
		try {
			E model = getModelClass().newInstance();
			model.initFromDocument(getConnectionBeanName(), doc);
			return model;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public E create() {
		try {
			E model = getModelClass().newInstance();
			model.initFromStore(getConnectionBeanName(), getStore());
			return model;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public E getById(String id) {
		try {
			Store store = getStore();
			Document doc = store.loadDocument(id);
			if(doc != null) {
				return createFromDocument(doc);
			} else {
				return null;
			}
		} catch(JsonException e) {
			throw new RuntimeException(e);
		} catch(SQLException e) {
			throw new RuntimeException(e);
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
