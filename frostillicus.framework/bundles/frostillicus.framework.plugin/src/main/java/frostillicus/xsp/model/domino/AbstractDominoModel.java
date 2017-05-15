package frostillicus.xsp.model.domino;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.*;

import javax.faces.model.DataModel;
import javax.persistence.Table;

import lotus.domino.NotesException;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.http.MimeMultipart;
import com.ibm.xsp.model.DataObject;
import com.ibm.xsp.model.FileRowData;
import com.ibm.xsp.model.domino.DominoAttachmentDataModel;
import com.ibm.xsp.model.domino.DominoUtils;
import com.ibm.xsp.model.domino.wrapped.DominoDocument;

import frostillicus.xsp.model.AbstractModelObject;
import frostillicus.xsp.model.ModelUtils;
import frostillicus.xsp.util.FrameworkUtils;

import org.openntf.domino.*;
import org.openntf.domino.utils.Factory;

import static frostillicus.xsp.model.ModelUtils.stringSet;

/**
 * @since 1.0
 */
public abstract class AbstractDominoModel extends AbstractModelObject {
	private static final long serialVersionUID = 1L;

	private Map<String, Object> columnValues_ = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);

	//	private final DominoDocument dominoDocument_;
	private DocumentHolder docHolder_;
	private String documentId_;
	private int noteId_;

	private boolean category_;
	private int columnIndentLevel_;
	private String viewRowPosition_;

	/* **********************************************************************
	 * Cover the three ways a model object can be created
	 * ViewEntry and Document are similar, except ViewEntry reads in column
	 * 	values for speedier access
	 ************************************************************************/
	public void initFromDatabase(final Database database) {
		docHolder_ = new DocumentHolder(database.getApiPath(), "");

		documentId_ = "";
		noteId_ = 0;
		category_ = false;
		columnIndentLevel_ = 0;
		viewRowPosition_ = "";

		// Look for an @Table annotation to set the form
		Table tableAnnotation = getClass().getAnnotation(Table.class);
		if(tableAnnotation != null) {
			setValueImmediate("Form", tableAnnotation.name());
		}
	}

	public void initFromViewEntry(final ViewEntry entry, final List<DominoColumnInfo> columnInfo) {
		if (entry.isCategory()) {
			category_ = true;
			documentId_ = "";
		} else {
			category_ = false;
			documentId_ = entry.getUniversalID();
		}
		noteId_ = entry.getNoteIDAsInt();

		if(entry.isDocument()) {
			docHolder_ = new DocumentHolder(entry.getAncestorDatabase().getApiPath(), entry.getUniversalID());
		} else {
			docHolder_ = null;
		}

		columnIndentLevel_ = entry.getColumnIndentLevel();
		viewRowPosition_ = entry.getPosition();

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

	public void initFromDocument(final Document doc) {
		Database database = doc.getParentDatabase();
		documentId_ = doc.getUniversalID();
		noteId_ = new BigInteger(doc.getNoteID(), 16).intValue();
		category_ = false;
		columnIndentLevel_ = 0;
		viewRowPosition_ = "";
		docHolder_ = new DocumentHolder(database.getApiPath(), documentId_);;
	}

	/* **********************************************************************
	 * Hooks and utility methods for concrete classes
	 * These are named without "get" to avoid steeping on doc fields' toes
	 ************************************************************************/

	protected Collection<String> nonSummaryFields() {
		return Arrays.asList(new String[] { });
	}

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
	public final boolean category() {
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
	public void deleteAttachment(final Object keyObj, final String attachmentName) {
		String fieldName = String.valueOf(keyObj);
		try {
			if(FrameworkUtils.isFaces()) {
				dominoDocument().removeAttachment(fieldName, attachmentName);
			} else {
				// TODO implement this
				throw new UnsupportedOperationException("Attachment deletion not yet supported for non-Faces contexts.");
			}
		} catch (NotesException e) {
			ModelUtils.publishException(e);
		}
	}

	@Override
	public Set<String> propertyNames(final boolean includeSystem, final boolean includeAll) {
		Set<String> parent = super.propertyNames(includeSystem, includeAll);
		Set<String> result;
		// If there are no declared columns, read all doc fields
		if(includeAll || (parent.isEmpty() && !category())) {
			result = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
			result.addAll(parent);
			result.addAll(docHolder_.getItemNames(includeSystem));
		} else {
			result = parent;
		}
		return result;
	}

	@Override
	public Set<String> columnPropertyNames() {
		return new TreeSet<String>(columnValues_.keySet());
	}

	@Override
	public Date lastModified() {
		Document doc = document();
		if(doc != null) {
			return doc.getLastModifiedDate();
		}
		return null;
	}

	@Override
	public Date created() {
		Document doc = document();
		if(doc != null) {
			return doc.getCreatedDate();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> modifiedBy() {
		List<String> result = new ArrayList<String>();
		Document doc = document();
		if(doc != null) {
			result.addAll((List<String>)(List<?>)doc.getItemValue("$UpdatedBy"));
		}
		return result;
	}

	/* **********************************************************************
	 * DataObject methods
	 ************************************************************************/
	@Override
	public Class<?> getType(final Object keyObject) {
		Class<?> result = super.getType(keyObject);
		if(result == null || result == Object.class) {
			if(docHolder_ == null) {
				return null;
			} else {
				return docHolder_.getType(keyObject);
			}
		} else {
			return result;
		}
	}

	// Methods for subclasses to use to bypass the read-only, ID, and getter/setter checks in getValue/setValue
	@Override
	protected final Object getValueImmediate(final Object keyObject) {
		if (!(keyObject instanceof String)) {
			throw new IllegalArgumentException();
		}
		String key = ((String) keyObject).toLowerCase();

		if(columnValues_.containsKey(key)) {
			return coaxValue(key, columnValues_.get(key));
		}
		if(docHolder_ != null) {
			Object result = docHolder_.getValue(keyObject);
			if(result instanceof List && ((List<?>)result).isEmpty()) {
				return "";
			}
			return coaxValue(key, result);
		}
		return "";
	}

	@Override
	protected final void setValueImmediate(final Object keyObject, final Object value) {
		if (!(keyObject instanceof String)) {
			throw new IllegalArgumentException();
		}
		if (category()) {
			throw new UnsupportedOperationException("Categories cannot be modified");
		}

		if(value instanceof Enum) {
			docHolder_.setValue(keyObject, ((Enum<?>)value).name());
		} else {
			// Look to translate known value types
			Type fieldType = getGenericType(keyObject);
			if(fieldType.equals(java.sql.Time.class) && value instanceof Date) {
				DateTime val = document().getAncestorSession().createDateTime((Date)value);
				val.setAnyDate();
				docHolder_.setValue(keyObject, val);
			} else if(fieldType.equals(java.sql.Date.class) && value instanceof Date) {
				DateTime val = document().getAncestorSession().createDateTime((Date)value);
				val.setAnyTime();
				docHolder_.setValue(keyObject, val);
			} else if(fieldType.equals(Boolean.class) || fieldType.equals(Boolean.TYPE)) {
				if(Boolean.TRUE.equals(value) || "Y".equals(value) || ((Integer)1).equals(value) || "true".equals(value)) {
					docHolder_.setValue(keyObject, "Y");
				} else if(value == null || "".equals(value)) {
					docHolder_.setValue(keyObject, null);
				} else {
					docHolder_.setValue(keyObject, "N");
				}
			} else {
				docHolder_.setValue(keyObject, value);
			}

		}
	}

	/* **********************************************************************
	 * The dirty work of actually saving or deleting the document
	 ************************************************************************/

	protected boolean querySaveDocument(final Document doc) {
		return true;
	}

	@Override
	public boolean save() {
		if (category()) {
			throw new UnsupportedOperationException("Categories cannot be saved");
		}

		try {
			if (querySave()) {
				if(!super.save()) {
					return false;
				}

				if(isNew()) {
					setValueImmediate("$$ModelCreatedAt", new Date());
					setValueImmediate("$$ModelCreatedBy", FrameworkUtils.getUserName());
				}
				setValueImmediate("$$ModelModifiedAt", new Date());
				setValueImmediate("$$ModelModifiedBy", FrameworkUtils.getUserName());

				Document doc = document(true);

				Session session = doc.getAncestorSession();
				session.evaluate(" @SetField('$$ModelUNID'; @DocumentUniqueID) ", doc);

				if(!querySaveDocument(doc)) {
					return false;
				}

				// Clean up any date/time-only fields
				for(Field field : getClass().getDeclaredFields()) {
					if(field.getType().equals(java.sql.Date.class)) {
						session.evaluate(MessageFormat.format(" @If(@IsTime({0}); @SetField(\"{0}\"; @Date({0})); \"\") ", field.getName()), doc);
					} else if(field.getType().equals(java.sql.Time.class)) {
						session.evaluate(MessageFormat.format(" @If(@IsTime({0}); @SetField(\"{0}\"; @Time({0})); \"\") ", field.getName()), doc);
					}
				}

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
					Session sessionAsSigner = FrameworkUtils.getSessionAsSigner();
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
		if (category()) {
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
	public Document document() {
		if(category()) {
			return null;
		}
		return docHolder_.getDocument();
	}

	public Document document(final boolean applyChanges) {
		if(category()) {
			return null;
		}
		return docHolder_.getDocument(applyChanges);
	}

	protected int noteId() { return noteId_; }

	public DominoDocument dominoDocument() { return docHolder_.getDominoDocument(); }

	protected List<Object> evaluate(final String formula) {
		Document doc = document();
		Session session = FrameworkUtils.getSession();
		return session.evaluate(formula, doc);
	}


	/* **********************************************************************
	 * For file upload/download support
	 ************************************************************************/

	@Override
	public DataModel getAttachmentData(final Object keyObj) {
		String key = String.valueOf(keyObj);
		if(FrameworkUtils.isFaces()) {
			return new DominoAttachmentDataModel(dominoDocument(), key);
		} else {
			// TODO implement this
			throw new UnsupportedOperationException("Getting attachment data not yet supported for non-Faces contexts");
		}
	}

	@Override
	public List<FileRowData> getAttachmentList(final Object keyObj) {
		String fieldName = String.valueOf(keyObj);
		try {
			if(FrameworkUtils.isFaces()) {
				return dominoDocument().getAttachmentList(fieldName);
			} else {
				// TODO implement this
				throw new UnsupportedOperationException("Getting attachment list not yet supported for non-Faces contexts");
			}
		} catch(lotus.domino.NotesException ne) {
			throw new RuntimeException(ne);
		}
	}

	@Override
	public List<FileRowData> getEmbeddedImageList(final Object keyObj) {
		String fieldName = String.valueOf(keyObj);
		try {
			if(FrameworkUtils.isFaces()) {
				return dominoDocument().getEmbeddedImagesList(fieldName);
			} else {
				// TODO implement this
				throw new UnsupportedOperationException("Getting embedded images list not yet supported for non-Faces contexts");
			}
		} catch (NotesException e) {
			throw new RuntimeException(e);
		}
	}

	/* **********************************************************************
	 * Switchable backend
	 ************************************************************************/
	private class DocumentHolder implements Serializable, DataObject {
		private static final long serialVersionUID = 1L;

		private final String databasePath_;
		private final String documentId_;
		private final boolean isDominoDocument_;
		private DominoDocument dominoDocument_;
		private final Map<String, Object> changes_;

		private transient Document storedDoc_;
		private transient boolean storedChanges_ = false;

		public DocumentHolder(final String databasePath, final String documentId) {
			databasePath_ = databasePath;
			documentId_ = documentId;
			isDominoDocument_ = FrameworkUtils.isFaces();
			if(isDominoDocument_) {
				changes_ = null;
			} else {
				changes_ = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
			}
		}

		public boolean isNewNote() {
			try {
				if(StringUtil.isEmpty(documentId_)) {
					return true;
				} else {
					if(isDominoDocument()) {
						return getDominoDocument().isNewNote();
					} else {
						Document doc = getDocument(false);
						return doc.isNewNote();
					}
				}
			} catch(NotesException ne) {
				throw new RuntimeException(ne);
			}
		}
		public Document getDocument(final boolean applyChanges) {
			if(isDominoDocument()) {
				try {
					return (Document)getDominoDocument().getDocument(applyChanges);
				} catch (NotesException ne) {
					throw new RuntimeException(ne);
				}
			} else {
				if(storedDoc_ == null) {
					Database database = FrameworkUtils.getSession().getDatabase(databasePath_);
					if(StringUtil.isEmpty(documentId_)) {
						storedDoc_ = database.createDocument();
					} else {
						storedDoc_ = database.getDocumentByUNID(documentId_);
					}
				}
				if(applyChanges && !storedChanges_) {
					for(Map.Entry<String, Object> change : changes_.entrySet()) {
						storedDoc_.put(change.getKey(), change.getValue());
					}
					storedChanges_ = true;
				}
				return storedDoc_;
			}
		}
		public Document getDocument() { return getDocument(false); }

		public boolean isDominoDocument() {
			return isDominoDocument_;
		}

		public DominoDocument getDominoDocument() {
			if(isDominoDocument_ && dominoDocument_ == null) {
				Database database = FrameworkUtils.getSession().getDatabase(databasePath_);
				if(StringUtil.isEmpty(documentId_)) {
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
				} else {
					Document doc = database.getDocumentByUNID(documentId_);
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
			}
			return dominoDocument_;
		}
		@Override
		public Class<?> getType(final Object key) {
			if(isDominoDocument()) {
				return getDominoDocument().getType(key);
			} else {
				Object val = getValue(key);
				if(val != null) {
					return val.getClass();
				} else {
					return Object.class;
				}
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
					String itemName = String.valueOf(key);
					Document doc = getDocument();
					if(doc.hasItem(itemName)) {
						Item item = doc.getFirstItem(itemName);
						switch(item.getTypeEx()) {
						case RICHTEXT:
							try {
								DominoUtils.HtmlConverterWrapper converter = new DominoUtils.HtmlConverterWrapper();
								converter.convertItem(Factory.toLotus(doc), itemName);
								return converter;
							} catch(NotesException ne) {
								throw new RuntimeException(ne);
							}
						case MIME_PART:
							// TODO this would be better converted elsewhere, but eh...
							MIMEEntity entity = item.getMIMEEntity();
							if(entity.getNthHeader("X-Java-Class") == null) {
								return item;
							} else {
								return doc.get(key);
							}
						default:
							return doc.get(key);
						}
					} else {
						return doc.get(key);
					}
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

			// convert java.sql types to Date
			Object storedValue = null;
			if(value instanceof java.sql.Date) {
				storedValue = new Date(((java.sql.Date)value).getTime());
			} else if(value instanceof java.sql.Time) {
				storedValue = new Date(((java.sql.Time)value).getTime());
			} else {
				storedValue = value;
			}

			if(isDominoDocument()) {
				// for Rich Text items, do a manual conversion to a MimeMultipart
				if(stringSet(richTextFields()).contains(key) && value instanceof String) {
					getDominoDocument().setValue(key, MimeMultipart.fromHTML(storedValue));
				} else {
					getDominoDocument().setValue(key, storedValue);
				}
			} else {
				changes_.put((String)key, storedValue);
				storedChanges_ = false;
			}
		}

		public Set<String> getItemNames(final boolean includeSystem) {
			Set<String> result = new HashSet<String>();
			for(Item item : getDocument().getItems()) {
				String itemName = item.getName();
				if(includeSystem || !itemName.startsWith("$")) {
					result.add(itemName);
				}
			}
			return result;
		}

		private void writeObject(final java.io.ObjectOutputStream out) throws IOException {
			out.defaultWriteObject();
			
			if(isDominoDocument()) {
				dominoDocument_.beforeSerializing();
			}
		}
		private void readObject(final java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
			in.defaultReadObject();
			if(isDominoDocument()) {
				dominoDocument_.afterDeserializing(null);
				dominoDocument_.restoreWrappedDocument();
			}
		}
	}
}