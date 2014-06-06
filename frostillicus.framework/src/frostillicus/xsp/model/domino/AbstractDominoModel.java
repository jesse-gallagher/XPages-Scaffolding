package frostillicus.xsp.model.domino;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.*;

import javax.faces.model.DataModel;

import lotus.domino.NotesException;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.model.DataObject;
import com.ibm.xsp.model.FileRowData;
import com.ibm.xsp.model.domino.DominoAttachmentDataModel;
import com.ibm.xsp.model.domino.wrapped.DominoDocument;

import frostillicus.xsp.model.AbstractModelObject;
import frostillicus.xsp.model.ModelUtils;
import frostillicus.xsp.model.Properties;

import org.openntf.domino.*;

import static frostillicus.xsp.model.ModelUtils.stringSet;

public abstract class AbstractDominoModel extends AbstractModelObject {
	private static final long serialVersionUID = 1L;

	private Map<String, Object> columnValues_ = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);

	//	private final DominoDocument dominoDocument_;
	private final DocumentHolder docHolder_;
	private String documentId_;
	private int noteId_;

	private final boolean category_;
	private final int columnIndentLevel_;
	private final String viewRowPosition_;

	/* **********************************************************************
	 * Cover the three ways a model object can be created
	 * ViewEntry and Document are similar, except ViewEntry reads in column
	 * 	values for speedier access
	 ************************************************************************/
	protected AbstractDominoModel(final Database database) {
		DocumentHolder holder;
		try {
			DominoDocument domDoc = DominoDocument.wrap(
			                                            database.getApiPath(),		// database
			                                            database,					// db
			                                            "",							// parentId
			                                            "",							// form
			                                            "",							// computeWithForm
			                                            "",							// concurrencyMode
			                                            false,						// allowDeletedDocs
			                                            "",							// saveLinksAs
			                                            ""							// webQuerySaveAgent
					);
			holder = new DocumentHolder(domDoc);
		} catch(IllegalStateException ise) {
			holder = new DocumentHolder(database.getApiPath(), "");
		}
		docHolder_ = holder;

		documentId_ = "";
		noteId_ = 0;
		category_ = false;
		columnIndentLevel_ = 0;
		viewRowPosition_ = "";
	}

	protected AbstractDominoModel(final ViewEntry entry, final List<DominoColumnInfo> columnInfo) {
		if (entry.isCategory()) {
			category_ = true;
			documentId_ = "";
		} else {
			category_ = false;
			documentId_ = entry.getUniversalID();
		}
		noteId_ = entry.getNoteIDAsInt();

		if(entry.isDocument()) {
			DocumentHolder holder;
			try {
				DominoDocument domDoc = DominoDocument.wrap(
				                                            entry.getAncestorDatabase().getApiPath(),
				                                            entry.getDocument(),
				                                            "",
				                                            "",
				                                            false,
				                                            "",
				                                            ""
						);
				holder = new DocumentHolder(domDoc);
			} catch(IllegalStateException ise) {
				holder = new DocumentHolder(entry.getAncestorDatabase().getApiPath(), entry.getUniversalID());
			}
			docHolder_ = holder;
		} else {
			docHolder_ = null;
		}

		columnIndentLevel_ = entry.getColumnIndentLevel();
		viewRowPosition_ = entry.getPosition('.');

		boolean preferJavaDates = entry.isPreferJavaDates();
		entry.setPreferJavaDates(true);
		List<Object> columnValuesList = entry.getColumnValues();
		Map<String, Object> columnValues = DominoColumnInfo.columnValuesToMap(columnValuesList, columnInfo);

		for (Map.Entry<String, Object> mapEntry : columnValues.entrySet()) {
			if (!"id".equalsIgnoreCase(mapEntry.getKey())) {
				columnValues_.put(mapEntry.getKey(), mapEntry.getValue());
			}
		}

		entry.setPreferJavaDates(preferJavaDates);
	}

	protected AbstractDominoModel(final Document doc) {
		Database database = doc.getParentDatabase();
		documentId_ = doc.getUniversalID();
		noteId_ = new BigInteger(doc.getNoteID(), 16).intValue();
		category_ = false;
		columnIndentLevel_ = 0;
		viewRowPosition_ = "";

		DocumentHolder holder;
		try {
			DominoDocument domDoc = DominoDocument.wrap(
			                                            database.getApiPath(),
			                                            doc,
			                                            "",
			                                            "",
			                                            false,
			                                            "",
			                                            ""
					);
			holder = new DocumentHolder(domDoc);
		} catch(IllegalStateException ise) {
			holder = new DocumentHolder(database.getApiPath(), documentId_);
		}
		docHolder_ = holder;
	}

	/* **********************************************************************
	 * Hooks and utility methods for concrete classes
	 * These are named without "get" to avoid steeping on doc fields' toes
	 ************************************************************************/

	protected abstract Collection<String> nonSummaryFields();

	protected Collection<String> richTextFields() {
		return Arrays.asList(new String[] {});
	}

	protected Collection<String> authorsFields() {
		return Arrays.asList(new String[] {});
	}

	protected Collection<String> readersFields() {
		return Arrays.asList(new String[] {});
	}

	protected Collection<String> namesFields() {
		return Arrays.asList(new String[] {});
	}

	@Override
	public final boolean isNew() {
		return documentId_.isEmpty();
	}

	@Override
	public final boolean isCategory() {
		return category_;
	}

	@Override
	public final int columnIndentLevel() {
		return columnIndentLevel_;
	}

	@Override
	public final String viewRowPosition() {
		return viewRowPosition_;
	}

	@Override
	public final String getId() {
		return documentId_;
	}

	@Override
	public void deleteAttachment(final String fieldName, final String attachmentName) {
		try {
			dominoDocument().removeAttachment(fieldName, attachmentName);
		} catch (NotesException e) {
			ModelUtils.publishException(e);
		}
	}

	@Override
	public Set<String> propertyNames() {
		Set<String> parent = super.propertyNames();
		Set<String> result;
		Properties props = getClass().getAnnotation(Properties.class);
		if(props == null || !props.exhaustive()) {
			result = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
			result.addAll(parent);
			result.addAll(docHolder_.getItemNames());
		} else {
			result = parent;
		}
		return result;
	}

	@Override
	public Set<String> columnPropertyNames() {
		Set<String> parent = super.columnPropertyNames();
		Set<String> result = new TreeSet<String>(parent);
		for(String name : columnValues_.keySet()) {
			result.add(name);
		}
		return result;
	}

	/* **********************************************************************
	 * DataObject methods
	 ************************************************************************/
	@Override
	public Class<?> getType(final Object keyObject) {
		if (!(keyObject instanceof String)) {
			throw new IllegalArgumentException();
		}
		String key = ((String) keyObject).toLowerCase();

		Method getter = findGetter(key);
		return getter == null ? docHolder_.getType(keyObject) : getter.getReturnType();
	}

	@Override
	public Object getValue(final Object keyObject) {
		if (!(keyObject instanceof String)) {
			throw new IllegalArgumentException();
		}
		String key = ((String) keyObject).toLowerCase();

		// First priority: id
		if ("id".equalsIgnoreCase(key)) {
			return documentId_;
		}

		// Second priority: getters
		Method getter = findGetter(key);
		if (getter != null) {
			try {
				return getter.invoke(this);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}

		return getValueImmediate(keyObject);
	}

	@Override
	public boolean isReadOnly(final Object keyObject) {
		if (isCategory()) {
			return true;
		}
		if (!(keyObject instanceof String)) {
			throw new IllegalArgumentException();
		}
		String key = (String) keyObject;

		if ("id".equalsIgnoreCase(key)) {
			return true;
		} else if (findGetter(key) != null && findSetters(key).size() == 0) {
			// Consider a property with a getter but no setters as read-only
			return true;
		}
		return false;
	}

	@Override
	public void setValue(final Object keyObject, final Object value) {
		if (isCategory()) {
			throw new UnsupportedOperationException("Categories cannot be modified");
		}
		if (!(keyObject instanceof String)) {
			throw new IllegalArgumentException();
		}
		String key = ((String) keyObject).toLowerCase();

		// First priority: disallow read-only values
		if (isReadOnly(keyObject)) {
			throw new IllegalArgumentException(key + " is read-only");
		}

		// Second priority: setters
		// Look for appropriately-named setters with a fitting type
		List<Method> setters = findSetters(key);
		for (Method method : setters) {
			try {
				Class<?> param = method.getParameterTypes()[0];
				if (value == null || param.isInstance(value)) {
					method.invoke(this, value);
					return;
				}
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}

		// If we reached here with a matching setter name but no matching type, consider it an illegal argument
		if (setters.size() > 0) {
			throw new IllegalArgumentException("No match found for setter '" + key + "' with type '" + value.getClass().getName() + "'");
		}

		setValueImmediate(keyObject, value);
	}

	// Methods for subclasses to use to bypass the read-only, ID, and getter/setter checks in getValue/setValue
	protected final Object getValueImmediate(final Object keyObject) {
		if (!(keyObject instanceof String)) {
			throw new IllegalArgumentException();
		}
		String key = ((String) keyObject).toLowerCase();

		if(columnValues_.containsKey(key)) {
			return columnValues_.get(key);
		}
		if(docHolder_ != null) {
			Object result = docHolder_.getValue(keyObject);
			if(result instanceof List && ((List<?>)result).isEmpty()) {
				return "";
			}
			return result;
		}
		return "";
	}

	protected final void setValueImmediate(final Object keyObject, final Object value) {
		if (!(keyObject instanceof String)) {
			throw new IllegalArgumentException();
		}
		if (isCategory()) {
			throw new UnsupportedOperationException("Categories cannot be modified");
		}
		docHolder_.setValue(keyObject, value);
	}


	/* **********************************************************************
	 * ViewRowData methods
	 ************************************************************************/
	@Override
	public final String getOpenPageURL(final String pageName, final boolean readOnly) {
		if (isCategory()) {
			return "";
		}
		if(pageName == null) {
			return "";
		}
		return pageName + (pageName.contains("?") ? "&" : "?") + "id=" + documentId_;
	}

	/* **********************************************************************
	 * The dirty work of actually saving or deleting the document
	 ************************************************************************/
	@Override
	public boolean save() {
		if (isCategory()) {
			throw new UnsupportedOperationException("Categories cannot be saved");
		}

		try {
			if (querySave()) {
				Document doc = document(true);

				for(String fieldName : stringSet(namesFields())) {
					Item item = doc.getFirstItem(fieldName);
					if(item == null) {
						item = doc.replaceItemValue(fieldName, "");
					}
					item.setNames(true);
				}
				for(String fieldName : stringSet(authorsFields())) {
					Item item = doc.getFirstItem(fieldName);
					if(item == null) {
						item = doc.replaceItemValue(fieldName, "");
					}
					item.setAuthors(true);
				}
				for(String fieldName : stringSet(readersFields())) {
					Item item = doc.getFirstItem(fieldName);
					if(item == null) {
						item = doc.replaceItemValue(fieldName, "");
					}
					item.setReaders(true);
				}
				for(String fieldName : stringSet(nonSummaryFields())) {
					Item item = doc.getFirstItem(fieldName);
					if(item == null) {
						item = doc.replaceItemValue(fieldName, "");
					}
					item.setSummary(false);
				}

				if(doc.save()) {
					if(documentId_.isEmpty()) {
						documentId_ = doc.getUniversalID();
					}

					postSave();

					// Attempt to update the FT index
					Database database = doc.getParentDatabase();
					Session sessionAsSigner = ModelUtils.getSessionAsSigner();
					if(sessionAsSigner != null) {
						Database signerDB = sessionAsSigner.getDatabase(database.getServer(), database.getFilePath());
						if(signerDB.isFTIndexed()) {
							signerDB.updateFTIndex(false);
						}
					}

					return true;
				}
			}
			return false;
		} catch (Exception ne) {
			ModelUtils.publishException(ne);
			return false;
		}
	}

	@Override
	public boolean delete() {
		if (isCategory()) {
			throw new UnsupportedOperationException("Categories cannot be deleted");
		}

		try {
			if(queryDelete()) {
				//Document doc = document();
				if (docHolder_.isNewNote()) {
					return false;
				}

				if (docHolder_.getDocument(true).remove(true)) {
					postDelete();
					return true;
				}
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
	//@SuppressWarnings("unused")
	public Document document() {
		if(isCategory()) {
			return null;
		}
		return docHolder_.getDocument();
	}

	public Document document(final boolean applyChanges) {
		if(isCategory()) {
			return null;
		}
		return docHolder_.getDocument(applyChanges);
	}

	protected int noteId() { return noteId_; }

	public DominoDocument dominoDocument() { return docHolder_.getDominoDocument(); }

	protected List<Object> evaluate(final String formula) {
		Document doc = document();
		Session session = ModelUtils.getSession();
		return session.evaluate(formula, doc);
	}


	/* **********************************************************************
	 * For file upload/download support
	 ************************************************************************/

	@Override
	public DataModel getAttachmentData(final String key) {
		return new DominoAttachmentDataModel(dominoDocument(), key);
	}

	@Override
	public List<FileRowData> getAttachmentList(final String fieldName) {
		try {
			return dominoDocument().getAttachmentList(fieldName);
		} catch(lotus.domino.NotesException ne) {
			throw new RuntimeException(ne);
		}
	}

	@Override
	public List<FileRowData> getEmbeddedImageList(final String fieldName) {
		try {
			return dominoDocument().getEmbeddedImagesList(fieldName);
		} catch (NotesException e) {
			throw new RuntimeException(e);
		}
	}

	/* **********************************************************************
	 * Switchable backend
	 ************************************************************************/
	private class DocumentHolder implements Serializable, DataObject {
		private static final long serialVersionUID = 1L;

		private final DominoDocument dominoDocument_;
		private final String databasePath_;
		private final String documentId_;
		private final Map<String, Object> changes_;

		public DocumentHolder(final DominoDocument dominoDocument) {
			dominoDocument_ = dominoDocument;
			databasePath_ = null;
			documentId_ = null;
			changes_ = null;
		}
		public DocumentHolder(final String databasePath, final String documentId) {
			dominoDocument_ = null;
			databasePath_ = databasePath;
			documentId_ = documentId;
			changes_ = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
		}

		public boolean isNewNote() {
			try {
				return dominoDocument_.isNewNote();
			} catch(NotesException ne) {
				throw new RuntimeException(ne);
			}
		}
		public Document getDocument(final boolean applyChanges) {
			if(isDominoDocument()) {
				try {
					return (Document)dominoDocument_.getDocument(applyChanges);
				} catch (NotesException ne) {
					throw new RuntimeException(ne);
				}
			} else {
				Database database = ModelUtils.getSession().getDatabase(databasePath_);
				Document doc;
				if(StringUtil.isEmpty(documentId_)) {
					doc = database.createDocument();
				} else {
					doc = database.getDocumentByUNID(documentId_);
				}
				if(applyChanges) {
					for(Map.Entry<String, Object> change : changes_.entrySet()) {
						doc.put(change.getKey(), change.getValue());
					}
				}
				return doc;
			}
		}
		public Document getDocument() { return getDocument(false); }

		public boolean isDominoDocument() {
			return dominoDocument_ != null;
		}

		public DominoDocument getDominoDocument() {
			return dominoDocument_;
		}
		@Override
		public Class<?> getType(final Object key) {
			if(isDominoDocument()) {
				return getDominoDocument().getType(key);
			} else {
				return getValue(key).getClass();
			}
		}
		@Override
		public Object getValue(final Object key) {
			if(isDominoDocument()) {
				return getDominoDocument().getValue(key);
			} else {
				if(changes_.containsKey(key)) {
					return changes_.get(key);
				} else {
					return getDocument().get(key);
				}
			}
		}
		@Override
		public boolean isReadOnly(final Object key) {
			return false;
		}
		@Override
		public void setValue(final Object key, final Object value) {
			if(!(key instanceof String)) {
				throw new IllegalArgumentException("key must be a String");
			}
			if(isDominoDocument()) {
				getDominoDocument().setValue(key, value);
			} else {
				changes_.put((String)key, value);
			}
		}

		public Set<String> getItemNames() {
			Set<String> result = new HashSet<String>();
			for(Item item : getDocument().getItems()) {
				result.add(item.getName());
			}
			return result;
		}
	}
}