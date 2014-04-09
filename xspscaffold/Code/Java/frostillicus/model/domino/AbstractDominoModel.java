package frostillicus.model.domino;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.*;

import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;

import lotus.domino.NotesException;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.FileRowData;
import com.ibm.xsp.model.domino.DominoAttachmentDataModel;
import com.ibm.xsp.model.domino.wrapped.DominoDocument;
import frostillicus.model.AbstractModelObject;
import frostillicus.model.ModelUtils;

import org.openntf.domino.*;
import static frostillicus.model.ModelUtils.stringSet;

public abstract class AbstractDominoModel extends AbstractModelObject {
	private static final long serialVersionUID = 1L;

	private Map<String, Object> columnValues_ = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);

	private final DominoDocument dominoDocument_;
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
		dominoDocument_ = DominoDocument.wrap(
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
			dominoDocument_ = DominoDocument.wrap(
					entry.getAncestorDatabase().getApiPath(),
					entry.getDocument(),
					"",
					"",
					false,
					"",
					""
			);
		} else {
			dominoDocument_ = null;
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

		dominoDocument_ = DominoDocument.wrap(
				database.getApiPath(),
				doc,
				"",
				"",
				false,
				"",
				""
		);
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

	public final boolean isNew() {
		return documentId_.isEmpty();
	}

	public final boolean isCategory() {
		return category_;
	}

	public final int columnIndentLevel() {
		return columnIndentLevel_;
	}

	public final String viewRowPosition() {
		return viewRowPosition_;
	}

	public final String getId() {
		return documentId_;
	}

	public void deleteAttachment(final String fieldName, final String attachmentName) {
		try {
			dominoDocument().removeAttachment(fieldName, attachmentName);
		} catch (NotesException e) {
			ModelUtils.publishException(e);
		}
	}

	/* **********************************************************************
	 * DataObject methods
	 ************************************************************************/
	public Class<?> getType(final Object keyObject) {
		if (!(keyObject instanceof String)) {
			throw new IllegalArgumentException();
		}
		String key = ((String) keyObject).toLowerCase();

		Method getter = findGetter(key);
		return getter == null ? dominoDocument().getType(keyObject) : getter.getReturnType();
	}

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
		DominoDocument domDoc = dominoDocument();
		if(domDoc != null) {
			Object result = dominoDocument().getValue(keyObject);
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
		dominoDocument().setValue(keyObject, value);
	}


	/* **********************************************************************
	 * ViewRowData methods
	 ************************************************************************/
	public final String getOpenPageURL(final String pageName, final boolean readOnly) {
		if (isCategory()) {
			return "";
		}
		return pageName + (pageName.contains("?") ? "&" : "?") + "id=" + documentId_;
	}

	/* **********************************************************************
	 * The dirty work of actually saving or deleting the document
	 ************************************************************************/
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
					lotus.domino.Session sessionAsSigner = (lotus.domino.Session)ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "sessionAsSigner");
					lotus.domino.Database signerDB = sessionAsSigner.getDatabase(database.getServer(), database.getFilePath());
					if(signerDB.isFTIndexed()) {
						signerDB.updateFTIndex(false);
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

	public boolean delete() {
		if (isCategory()) {
			throw new UnsupportedOperationException("Categories cannot be deleted");
		}

		try {
			if(queryDelete()) {
				//Document doc = document();
				if (dominoDocument_.isNewNote())
					return false;

				if (dominoDocument_.getDocument(true).remove(true)) {
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
		if(isCategory()) return null;
		return (Document)dominoDocument_.getDocument();
	}

	public Document document(final boolean applyChanges) {
		if(isCategory()) return null;
		try {
			return (Document)dominoDocument_.getDocument(applyChanges);
		} catch (NotesException e) {
			ModelUtils.publishException(e);
			return null;
		}
	}

	protected int noteId() { return noteId_; }

	public DominoDocument dominoDocument() { return dominoDocument_; }

	protected List<Object> evaluate(final String formula) {
		Document doc = document();
		Session session = (Session) ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "session");
		return session.evaluate(formula, doc);
	}


	/* **********************************************************************
	 * For file upload/download support
	 ************************************************************************/

	public DataModel getAttachmentData(final String key) {
		return new DominoAttachmentDataModel(dominoDocument(), key);
	}

	public List<FileRowData> getAttachmentList(final String fieldName) {
		try {
			return dominoDocument().getAttachmentList(fieldName);
		} catch(lotus.domino.NotesException ne) {
			throw new RuntimeException(ne);
		}
	}

	public List<FileRowData> getEmbeddedImageList(final String fieldName) {
		try {
			return dominoDocument().getEmbeddedImagesList(fieldName);
		} catch (NotesException e) {
			throw new RuntimeException(e);
		}
	}
}