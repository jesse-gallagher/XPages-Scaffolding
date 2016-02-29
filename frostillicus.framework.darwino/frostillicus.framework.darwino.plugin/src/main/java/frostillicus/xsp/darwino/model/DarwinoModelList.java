package frostillicus.xsp.darwino.model;

import java.sql.SQLException;
import java.util.RandomAccess;

import com.darwino.commons.json.JsonException;
import com.darwino.commons.log.Logger;
import com.darwino.commons.util.StringUtil;
import com.darwino.jsonstore.Cursor;
import com.darwino.jsonstore.Database;
import com.darwino.jsonstore.Document;
import com.darwino.jsonstore.Store;

import frostillicus.xsp.darwino.Activator;
import frostillicus.xsp.darwino.SqlContextApplicationListener;
import frostillicus.xsp.model.AbstractModelList;

public class DarwinoModelList<E extends AbstractDarwinoModel> extends AbstractModelList<E>implements RandomAccess {
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = Activator.log;

	private final String connectionBeanName_;
	private final String databaseId_;
	private final String instanceId_;
	private final String storeId_;
	private String searchQuery_;
	
	public DarwinoModelList(String connectionBeanName, Store store, Class<E> clazz) throws JsonException {
		super(clazz);
		
		connectionBeanName_ = connectionBeanName;
		storeId_ = store.getId();
		databaseId_ = store.getDatabase().getId();
		instanceId_ = store.getDatabase().getInstance().getId();

		if(log.isDebugEnabled()) {
			log.debug("{0}: initializing with connectionBeanName={1}, storeId={2}, databaseId={3}, instanceId={4}", getClass().getSimpleName(), connectionBeanName_, storeId_, databaseId_, instanceId_); //$NON-NLS-1$
		}
	}

	@Override
	public E get(int index) {
		try {
			if(log.isDebugEnabled()) {
				log.debug("{0}#get: for index {1}", getClass().getSimpleName(), index); //$NON-NLS-1$
			}
			
			Cursor cursor = getCursor();
			cursor.range(index, 0);
			// TODO switch to using the entry data
			Document doc = cursor.findOne().loadDocument();
			if(log.isDebugEnabled()) {
				log.debug("{0}#get: got doc {1}", getClass().getSimpleName(), doc); //$NON-NLS-1$
			}
			return createFromDocument(doc);
		} catch(JsonException e) {
			throw new RuntimeException(e);
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected E createFromDocument(Document doc) throws JsonException {
		try {
			E result = getClazz().newInstance();
			result.initFromDocument(connectionBeanName_, doc);
			return result;
		} catch (IllegalAccessException iae) {
			throw new RuntimeException(iae);
		} catch (InstantiationException ie) {
			throw new RuntimeException(ie);
		}
	}

	@Override
	public int size() {
		try {
			int count = getCursor().count();
			if(log.isDebugEnabled()) {
				log.debug("{0}#size: got count {1}", getClass().getSimpleName(), count); //$NON-NLS-1$
			}
			return count;
		} catch (JsonException e) {
			throw new RuntimeException(e);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public E getByKey(Object key) {
		try {
			Cursor cursor = getCursor();
			cursor.key(key);
			if(cursor.count() > 0) {
				Document doc = cursor.findOneDocument(0);
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
	public void search(String searchQuery) {
		this.searchQuery_ = searchQuery;
	}

	@Override
	public void stampAll(String propertyName, Object value) {
		for(E obj : this) {
			obj.setValue(propertyName, value);
			obj.save();
		}
	}

	/* ******************************************************************************
	 * Internal utility methods
	 ********************************************************************************/
	private Store getStore() throws JsonException, SQLException {
		Database database = SqlContextApplicationListener.getDatabase(connectionBeanName_, instanceId_, databaseId_);
		return database.getStore(storeId_);
	}
	private Cursor getCursor() throws JsonException, SQLException {
		Cursor cursor = getStore().openCursor();
		if(StringUtil.isNotEmpty(searchQuery_)) {
			if(log.isTraceEnabled()) {
				log.trace("{0}: searching with query {1}", getClass().getSimpleName(), searchQuery_); //$NON-NLS-1$
			}
			cursor.query(searchQuery_);
		}
		return cursor;
	}
}
