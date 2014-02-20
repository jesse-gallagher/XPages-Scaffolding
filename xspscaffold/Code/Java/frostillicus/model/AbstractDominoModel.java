package frostillicus.model;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import javax.faces.context.FacesContext;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.ViewRowData;
import com.ibm.xsp.model.FileRowData;
import com.ibm.xsp.model.domino.wrapped.DominoDocument;
import com.ibm.xsp.component.UIFileuploadEx.UploadedFile;
import com.ibm.xsp.http.IUploadedFile;

import org.openntf.domino.*;

public abstract class AbstractDominoModel implements ModelObject {
	private static final long serialVersionUID = 1L;

	private Map<String, Object> values_ = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
	private transient Map<String, Method> getterCache_ = new HashMap<String, Method>();
	private transient Map<String, List<Method>> setterCache_ = new HashMap<String, List<Method>>();

	private final String server_;
	private final String filePath_;
	private String documentId_;

	private final boolean category_;
	private final int columnIndentLevel_;
	private final String viewRowPosition_;

	/* **********************************************************************
	 * Cover the three ways a model object can be created
	 * ViewEntry and Document are similar, except ViewEntry reads in column
	 * 	values for speedier access
	 ************************************************************************/
	protected AbstractDominoModel(final Database database) {
		server_ = database.getServer();
		filePath_ = database.getFilePath();
		documentId_ = "";
		category_ = false;
		columnIndentLevel_ = 0;
		viewRowPosition_ = "";
	}

	@SuppressWarnings("unchecked")
	protected AbstractDominoModel(final ViewEntry entry, final List<DominoColumnInfo> columnInfo) {
		if (entry.isCategory()) {
			category_ = true;
			documentId_ = "";
			server_ = "";
			filePath_ = "";
		} else {
			Base parent = entry.getParent();
			View parentView = null;
			if (parent instanceof ViewNavigator) {
				parentView = ((ViewNavigator) parent).getParentView();
			} else if (parent instanceof ViewEntryCollection) {
				parentView = ((ViewEntryCollection) parent).getParent();
			} else {
				parentView = (View) parent;
			}
			Database database = parentView.getParent();
			server_ = database.getServer();
			filePath_ = database.getFilePath();

			category_ = false;
			documentId_ = entry.getUniversalID();
		}

		columnIndentLevel_ = entry.getColumnIndentLevel();
		viewRowPosition_ = entry.getPosition('.');

		boolean preferJavaDates = entry.isPreferJavaDates();
		entry.setPreferJavaDates(true);
		List<Object> columnValuesList = entry.getColumnValues();
		Map<String, Object> columnValues = ModelUtils.columnValuesToMap(columnValuesList, columnInfo);

		for (Map.Entry<String, Object> mapEntry : columnValues.entrySet()) {
			if (!"id".equalsIgnoreCase(mapEntry.getKey())) {
				setValueImmediate(mapEntry.getKey(), mapEntry.getValue());
			}
		}

		entry.setPreferJavaDates(preferJavaDates);
	}

	protected AbstractDominoModel(final Document doc) {
		Database database = doc.getParentDatabase();
		server_ = database.getServer();
		filePath_ = database.getFilePath();
		documentId_ = doc.getUniversalID();
		category_ = false;
		columnIndentLevel_ = 0;
		viewRowPosition_ = "";
	}

	/* **********************************************************************
	 * Hooks and utility methods for concrete classes
	 * These are named without "get" to avoid steeping on doc fields' toes
	 ************************************************************************/
	protected boolean querySave() {
		return true;
	}

	protected void postSave() {
	}

	protected boolean queryDelete() {
		return true;
	}

	protected void postDelete() {
	}

	protected abstract Collection<String> nonSummaryFields();

	protected Collection<String> authorsFields() {
		return Arrays.asList(new String[] {});
	}

	protected Collection<String> readersFields() {
		return Arrays.asList(new String[] {});
	}

	protected Collection<String> namesFields() {
		return Arrays.asList(new String[] {});
	}

	protected Collection<String> attachmentFields() {
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

	/* **********************************************************************
	 * DataObject methods
	 ************************************************************************/
	public Class<?> getType(final Object keyObject) {
		if (!(keyObject instanceof String)) {
			throw new IllegalArgumentException();
		}
		String key = ((String) keyObject).toLowerCase();

		Method getter = findGetter(key);
		return getter == null ? Object.class : getter.getReturnType();
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

	@SuppressWarnings("unchecked")
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

		// Finally, store in the local cache for later storage
		// File attachments are, shockingly, special
		if (attachmentFields().contains(key)) {
			if (!(value instanceof UploadedFile)) {
				throw new IllegalArgumentException(key + " is an attachment field");
			}

			List<UploadedFile> result = (List<UploadedFile>) values_.get(key);
			if (result == null) {
				result = new ArrayList<UploadedFile>();
				values_.put(key, result);
			}
			System.out.println("result is " + result);
			UploadedFile file = (UploadedFile) value;
			result.add(file);
			IUploadedFile iUpload = file.getUploadedFile();
			System.out.println("added " + iUpload.getClientFileName() + " to " + key);
			System.out.println("does " + iUpload.getServerFileName() + " exist? " + iUpload.getServerFile().exists());
		} else {
			values_.put(key, value);
		}
	}

	// Methods for subclasses to use to bypass the read-only, ID, and getter/setter checks in getValue/setValue
	protected final Object getValueImmediate(final Object keyObject) {
		if (!(keyObject instanceof String)) {
			throw new IllegalArgumentException();
		}
		String key = ((String) keyObject).toLowerCase();

		// check the cache for an existing value; if not found, fetch from the document
		if (!values_.containsKey(key) && !isCategory()) {
			try {
				Document doc = document();

				Item item = doc.getFirstItem(key);

				if (item == null) {
					values_.put(key, null);
				} else if (item instanceof RichTextItem) {
					Database database = doc.getParentDatabase();
					String databaseName = database.getServer() + "!!" + database.getFilePath();
					DominoDocument wrappedDoc = DominoDocument.wrap(databaseName, doc, null, null, false, null, null);
					values_.put(key, wrappedDoc.getValue(key));
				} else {
					List<Object> itemValue = item.getValues();
					if (itemValue == null) {
						values_.put(key, null);
					} else {
						for (int i = 0; i < itemValue.size(); i++) {
							Object val = itemValue.get(i);
							if (val instanceof DateTime) {
								DateTime dt = (DateTime) val;
								itemValue.set(i, dt.toJavaDate());
							}
						}
						switch (itemValue.size()) {
						case 0:
							values_.put(key, null);
							break;
						case 1:
							values_.put(key, itemValue.get(0));
							break;
						default:
							values_.put(key, itemValue);
						break;
						}
					}
				}
			} catch (Exception ne) {
				ModelUtils.publishException(ne);
				return null;
			}
		}
		return values_.get(key);
	}

	protected final void setValueImmediate(final Object keyObject, final Object value) {
		if (!(keyObject instanceof String)) {
			throw new IllegalArgumentException();
		}
		String key = ((String) keyObject).toLowerCase();
		values_.put(key, value);
	}

	/* **********************************************************************
	 * Reflection seeker methods
	 ************************************************************************/
	private final Method findGetter(final String key) {
		String lkey = key.toLowerCase();
		if (!getterCache_.containsKey(lkey)) {
			Method result = null;
			for (Method method : getClass().getMethods()) {
				String methodName = method.getName().toLowerCase();
				if (method.getParameterTypes().length == 0 && (methodName.equals("get" + lkey) || methodName.equals("is" + lkey))) {
					try {
						result = method;
						break;
					} catch (IllegalArgumentException e) {
						throw new RuntimeException(e);
					}
				}
			}
			getterCache_.put(lkey, result);
		}
		return getterCache_.get(lkey);
	}

	private final List<Method> findSetters(final String key) {
		String lkey = key.toLowerCase();
		if (!setterCache_.containsKey(lkey)) {
			List<Method> result = new ArrayList<Method>();
			for (Method method : getClass().getMethods()) {
				Class<?>[] parameters = method.getParameterTypes();
				String methodName = method.getName().toLowerCase();
				if (parameters.length == 1 && methodName.equals("set" + lkey)) {
					result.add(method);
				}
			}
			setterCache_.put(lkey, result);
		}
		return setterCache_.get(lkey);
	}

	/* **********************************************************************
	 * ViewRowData methods
	 ************************************************************************/
	public final Object getColumnValue(final String key) {
		return getValue(key);
	}

	public final void setColumnValue(final String key, final Object value) {
		setValue(key, value);
	}

	public final ViewRowData.ColumnInfo getColumnInfo(final String key) {
		return null;
	}

	public final String getOpenPageURL(final String pageName, final boolean readOnly) {
		if (isCategory()) {
			return "";
		}
		return pageName + (pageName.contains("?") ? "&" : "?") + "id=" + documentId_;
	}

	public final boolean isReadOnly(final String key) {
		return isReadOnly((Object) key);
	}

	public final Object getValue(final String key) {
		return getValue((Object) key);
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
				Document doc = document();
				if (doc.isNewNote())
					return false;

				if (doc.remove(true)) {
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
	@SuppressWarnings("unused")
	protected Document document() {
		Map<String, Object> requestScope = ExtLibUtil.getRequestScope();

		Database database = ModelUtils.getDatabase(server_, filePath_);
		Document result = null;
		if (documentId_.isEmpty()) {
			result = database.createDocument();

			String key = server_ + "!!" + filePath_ + "/new";
			//			requestScope.put(key, result);
		} else {
			String key = server_ + "!!" + filePath_ + "/" + documentId_;
			result = database.getDocumentByUNID(documentId_);
			//			if(!requestScope.containsKey(key)) {
			//				requestScope.put(key, database.getDocumentByUNID(documentId_));
			//			}
			//			result = (Document)requestScope.get(key);
		}
		return result;
	}

	protected Document document(final boolean applyChanges) {
		Document doc = document();
		if (applyChanges)
			applyChanges(doc);
		return doc;
	}

	@SuppressWarnings("unchecked")
	private void applyChanges(final Document doc) {
		Session session = (Session)ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "session");

		Set<String> nonSummaryFields = stringSet(nonSummaryFields());
		Collection<String> authorsFields = stringSet(authorsFields());
		Collection<String> readersFields = stringSet(readersFields());
		Collection<String> namesFields = stringSet(namesFields());
		Collection<String> attachmentFields = stringSet(attachmentFields());

		for (Map.Entry<String, Object> entry : values_.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();

			if (!key.startsWith("$")) {
				if (attachmentFields.contains(key.toLowerCase())) {
					RichTextItem rtitem = null;
					if (doc.hasItem(key)) {
						Item item = doc.getFirstItem(key);
						if (item instanceof RichTextItem) {
							rtitem = (RichTextItem) item;
						} else {
							doc.removeItem(key);
							rtitem = doc.createRichTextItem(key);
						}
					} else {
						rtitem = doc.createRichTextItem(key);
					}

					List<UploadedFile> files = (List<UploadedFile>) value;
					for (UploadedFile file : files) {
						// h/t http://stackoverflow.com/questions/17073250/how-to-upload-and-save-an-attachment-via-xpages-java-bean

						IUploadedFile iUploadedFile = file.getUploadedFile();
						// get the server file (with a cryptic filename)
						File serverFile = iUploadedFile.getServerFile();

						// get the original filename
						String fileName = iUploadedFile.getClientFileName();

						File correctedFile = new File(serverFile.getParentFile().getAbsolutePath() + File.separator + fileName);

						// rename the file to its original name
						System.out.println("renaming " + serverFile.getAbsolutePath() + " to " + correctedFile.getAbsolutePath());
						boolean success = serverFile.renameTo(correctedFile);

						// facesContext.getApplication().getApplicationProperty("xsp.persistence.dir.xspupload", "")
						if (success) {
							rtitem.embedObject(EmbeddedObject.EMBED_ATTACHMENT, "", correctedFile.getAbsolutePath(), null);
							System.out.println("attached " + correctedFile.getAbsolutePath());

							// if we're done: rename it back to the original filename, so it gets cleaned up by the server
							correctedFile.renameTo(iUploadedFile.getServerFile());
						} else {
							System.out.println("failed rename");
						}
					}

				} else {
					Item item = null;
					if (value instanceof Date) {
						DateTime dt = session.createDateTime((Date) value);
						item = doc.replaceItemValue(entry.getKey(), dt);
					} else if (value instanceof List) {
						Vector<Object> listVal = new Vector<Object>((List<?>) value);
						for (int i = 0; i < listVal.size(); i++) {
							if (listVal.get(i) instanceof Date) {
								listVal.set(i, session.createDateTime((Date) listVal.get(i)));
							}
						}
						item = doc.replaceItemValue(key, listVal);
					} else if(value instanceof com.ibm.xsp.http.MimeMultipart) {
						item = doc.replaceItemValue(key, value.toString());
					} else {
						value = value == null ? "" : value;
						item = doc.replaceItemValue(key, value);
					}
					String lkey = key.toLowerCase();
					item.setSummary("form".equals(lkey) || !nonSummaryFields.contains(lkey));
					item.setAuthors(authorsFields.contains(lkey));
					item.setReaders(readersFields.contains(lkey));
					item.setNames(namesFields.contains(lkey));
				}
			}
		}

		// Set $Created for new docs
		if (doc.isNewNote()) {
			DateTime created = session.createDateTime(new Date());
			doc.replaceItemValue("$Created", created);
		}
	}

	protected List<Object> evaluate(final String formula) {
		Document doc = document();
		Session session = (Session) ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "session");
		return session.evaluate(formula, doc);
	}


	private SortedSet<String> stringSet(final Collection<String> input) {
		SortedSet<String> result = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		result.addAll(input);
		return result;
	}

	/* **********************************************************************
	 * For file-upload support
	 ************************************************************************/
	@SuppressWarnings("unused")
	private class ModelFileRowData implements FileRowData, Serializable {
		private static final long serialVersionUID = 8426819039178643175L;

		private long created_;
		private String href_;
		private long lastModified_;
		private long length_;
		private String name_;
		private String type_;

		public long getCreated() {
			return created_;
		}

		public void setCreated(final long created) {
			created_ = created;
		}

		public String getHref() {
			return href_;
		}

		public void setHref(final String href) {
			href_ = href;
		}

		public long getLastModified() {
			return lastModified_;
		}

		public void setLastModified(final long lastModified) {
			lastModified_ = lastModified;
		}

		public long getLength() {
			return length_;
		}

		public void setLength(final long length) {
			length_ = length;
		}

		public String getName() {
			return name_;
		}

		public void setName(final String name) {
			name_ = name;
		}

		public String getType() {
			return type_;
		}

		public void setType(final String type) {
			type_ = type;
		}
	}
}