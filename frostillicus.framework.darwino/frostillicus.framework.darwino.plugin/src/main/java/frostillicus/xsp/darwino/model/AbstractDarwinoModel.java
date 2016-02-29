package frostillicus.xsp.darwino.model;

import static frostillicus.xsp.model.ModelUtils.stringSet;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.faces.model.DataModel;
import javax.persistence.Table;

import org.openntf.domino.Item;
import org.openntf.domino.Session;

import com.darwino.commons.json.JsonException;
import com.darwino.commons.json.JsonObject;
import com.darwino.commons.util.StringUtil;
import com.darwino.jsonstore.Database;
import com.darwino.jsonstore.Document;
import com.darwino.jsonstore.Store;
import com.ibm.xsp.model.FileRowData;

import frostillicus.xsp.darwino.SqlContextApplicationListener;
import frostillicus.xsp.model.AbstractModelObject;
import frostillicus.xsp.model.ModelUtils;
import frostillicus.xsp.util.FrameworkUtils;

/**
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public abstract class AbstractDarwinoModel extends AbstractModelObject {
	private static final long serialVersionUID = 1L;
	
	private String connectionBeanName_;
	private String databaseId_;
	private String instanceId_;
	private String storeId_;
	private String documentId_;
	private boolean category_;
	private JsonObject docJson_;
	
	/* ******************************************************************************
	 * Model initialization
	 ********************************************************************************/
	
	public void initFromStore(String connectionBeanName, final Store store) throws JsonException {
		connectionBeanName_ = connectionBeanName;
		storeId_ = store.getId();
		databaseId_ = store.getDatabase().getId();
		instanceId_ = store.getDatabase().getInstance().getId();
		documentId_ = ""; //$NON-NLS-1$
		category_ = false;

		// Look for an @Table annotation to set the form
		Table tableAnnotation = getClass().getAnnotation(Table.class);
		if(tableAnnotation != null) {
			setValueImmediate("form", tableAnnotation.name()); //$NON-NLS-1$
		}
		
		docJson_ = new JsonObject();
	}

	public void initFromDocument(String connectionBeanName, final Document doc) throws JsonException {
		connectionBeanName_ = connectionBeanName;
		storeId_ = doc.getStore().getId();
		databaseId_ = doc.getStore().getDatabase().getId();
		instanceId_ = doc.getStore().getDatabase().getInstance().getId();
		documentId_ = doc.getUnid();
		category_ = false;
		category_ = false;
		docJson_ = (JsonObject)doc.getJson();
	}

	@Override
	public boolean delete() {
		if (category()) {
			throw new UnsupportedOperationException("Categories cannot be deleted");
		}
		
		try {
			if(queryDelete()) {
				if (isNew()) {
					return false;
				}

				getDatabase().getStore(storeId_).deleteDocument(documentId_);
			}
			return false;
		} catch (Exception ne) {
			ModelUtils.publishException(ne);
			return false;
		}
	}

	@Override
	public void deleteAttachment(Object key, String attachmentName) {
	}

	@Override
	public List<FileRowData> getAttachmentList(Object key) {
		return null;
	}

	@Override
	public String getId() {
		return documentId_;
	}

	@Override
	public boolean category() {
		return category_;
	}

	@Override
	public int columnIndentLevel() {
		return 0;
	}

	@Override
	public String viewRowPosition() {
		return null;
	}

	@Override
	public DataModel getAttachmentData(Object key) {
		return null;
	}

	@Override
	public List<FileRowData> getEmbeddedImageList(Object key) {
		return null;
	}

	@Override
	public Set<String> columnPropertyNames() {
		return Collections.emptySet();
	}

	@Override
	public Date lastModified() {
		if(isNew()) {
			return null;
		}
		
		try {
			return document().getLastModificationDate();
		} catch(JsonException e) {
			throw new RuntimeException(e);
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Date created() {
		if(isNew()) {
			return null;
		}
		
		try {
			return document().getCreationDate();
		} catch(JsonException e) {
			throw new RuntimeException(e);
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<String> modifiedBy() {
		if(isNew()) {
			return Collections.emptyList();
		}
		
		try {
			return Arrays.asList(document().getLastModificationUser());
		} catch(JsonException e) {
			throw new RuntimeException(e);
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected Object getValueImmediate(Object keyObject) {
		return docJson_.get(keyObject);
	}

	@Override
	protected void setValueImmediate(Object keyObject, Object value) {
		docJson_.put(StringUtil.toString(keyObject), value);
	}
	
	@Override
	public boolean save() {
		if (category()) {
			throw new UnsupportedOperationException("Categories cannot be saved"); //$NON-NLS-1$
		}

		try {
			if (querySave()) {
				if(!super.save()) {
					return false;
				}

				if(isNew()) {
					setValueImmediate("$$ModelCreatedAt", new Date()); //$NON-NLS-1$
					setValueImmediate("$$ModelCreatedBy", FrameworkUtils.getUserName()); //$NON-NLS-1$
				}
				setValueImmediate("$$ModelModifiedAt", new Date()); //$NON-NLS-1$
				setValueImmediate("$$ModelModifiedBy", FrameworkUtils.getUserName()); //$NON-NLS-1$

				Document doc = document(true);

				doc.save();
				if(documentId_.isEmpty()) {
					documentId_ = doc.getUnid();
				}

				postSave();

				return true;
			}
			return false;
		} catch (Exception ne) {
			ModelUtils.publishException(ne);
			return false;
		}
	}
	
	/* **********************************************************************
	 * Misc. leftovers
	 ************************************************************************/

	public Document document() throws JsonException, SQLException {
		return document(false);
	}
	public Document document(boolean applyChanges) throws JsonException, SQLException {
		Database database = getDatabase();
		Document doc = null;
		if(isNew()) {
			doc = database.getStore(storeId_).newDocument();
		} else {
			doc = database.getStore(storeId_).loadDocument(documentId_);
		}
		if(applyChanges) {
			doc.setJson(docJson_.makeCopy());
		}
		return doc;
	}
	
	/* ******************************************************************************
	 * Internal utility methods
	 ********************************************************************************/
	protected Database getDatabase() throws SQLException, JsonException {
		return SqlContextApplicationListener.getDatabase(connectionBeanName_, instanceId_, databaseId_);
	}
	
}
