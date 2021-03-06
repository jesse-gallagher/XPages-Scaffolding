package frostillicus.xsp.model.domino;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.openntf.domino.Database;
import org.openntf.domino.View;
import org.openntf.domino.ViewEntry;
import org.openntf.domino.ViewEntryCollection;
import org.openntf.domino.ViewNavigator;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.model.TabularDataModel;

import frostillicus.xsp.model.AbstractModelList;
import frostillicus.xsp.util.FrameworkUtils;

/**
 * @since 1.0
 */
@SuppressWarnings("serial")
public class DominoModelList<E extends AbstractDominoModel> extends AbstractModelList<E> implements RandomAccess {
	/**
	 * Domino-specific full-text search options
	 * 
	 * @since 1.2.0
	 */
	public static enum DominoSearchOption implements SearchOption {
		EXACT, WORD_VARIANTS, FUZZY 
	}
	
	private transient Map<String, Object> internalCacheScope_ = new HashMap<String, Object>();;

	private final String server_;
	private final String filePath_;
	private final String viewName_;
	private final String category_;
	private final List<DominoColumnInfo> columnInfo_;
	private String searchQuery_;
	private Set<SearchOption> searchOptions_ = new HashSet<>();

	private final boolean invalid_;
	private int size_ = -1;

	private Set<Integer> collapsedIds_ = new TreeSet<Integer>();
	private Set<Integer> expandedIds_ = new TreeSet<Integer>();

	private transient int reset_ = 0;

	// This is intended to store an extra reference to the Navigator and Entry, in case they're being recycled unnecessarily
	// It's not meant for actual use
	private transient ViewNavigator nav_ = null;
	@SuppressWarnings("unused")
	private transient ViewEntry current_ = null;

	public DominoModelList(final Database database, final String viewName, final String category, final Class<E> clazz) {
		super(clazz);

		server_ = database.getServer();
		filePath_ = database.getFilePath();
		viewName_ = viewName;
		category_ = category;

		// Gather the view info now
		View view = database.getView(viewName_);
		invalid_ = view == null;

		columnInfo_ = invalid_ ? null : DominoColumnInfo.fromView(view);
	}

	protected E createFromViewEntry(final ViewEntry entry, final List<DominoColumnInfo> columnInfo) {
		try {
			E result = getClazz().newInstance();
			result.initFromViewEntry(entry, columnInfo);
			return result;
		} catch (IllegalAccessException iae) {
			throw new RuntimeException(iae);
		} catch (InstantiationException ie) {
			throw new RuntimeException(ie);
		}
	}

	@Override
	public void stampAll(final String propertyName, final Object value) {
		ViewEntryCollection vec = getEntries();
		this.search("foo", DominoSearchOption.FUZZY, DominoSearchOption.WORD_VARIANTS);
		vec.stampAll(propertyName, value);
	}

	/* **********************************************************************
	 * List methods
	 ************************************************************************/
	@Override
	@SuppressWarnings("deprecation")
	public E get(final int index) {
		if(invalid_) { return null; }

		Map<Integer, E> cache = getCache();
		if (!cache.containsKey(index)) {
			try {
				if (searchQuery_ == null || searchQuery_.isEmpty()) {
					ViewNavigator nav = getNavigator();

					// getNth is top-level only, so let's skip to what we need
					int lastFetchedIndex = 0;
					Map<String, Object> requestScope = getRequestScope();
					String key = "lastFetchedIndex-" + toString();
					if (requestScope.containsKey(key)) {
						lastFetchedIndex = (Integer) requestScope.get(key);
					}
					nav.skip(index - lastFetchedIndex);

					requestScope.put(key, index);

					try {
						// Try to get the current one, which may be null due to an as-yet-unsolved
						// "object has been removed or recycled" error. Check for that and switch to
						// our exception handler to retry
						ViewEntry entry = nav.getCurrent();
						current_ = entry;
						if(entry == null) { throw new NullPointerException(); }
						cache.put(index, createFromViewEntry(entry, columnInfo_));
					} catch(NullPointerException npe) {
						// Then we've probably hit an "Object has been removed or recycled" on the nav

						if(reset_ < 10) {
							reset_++;
							clearCache();
							return get(index);
						}

						System.out.println("=============================== NPE in DominoModelList");
						System.out.println("=============================== Current class: " + getClass().getName());
						System.out.println("=============================== Type: " + getClazz().getName());
						System.out.println("=============================== Desired index: " + index);
						System.out.println("=============================== Current cache: " + cache);
						System.out.println("=============================== Current reported size: " + size());
						throw npe;
					} catch(RuntimeException re) {
						if(String.valueOf(re.getCause()).contains("Argument has been removed or recycled")) {
							if(reset_ < 10) {
								reset_++;
								clearCache();
								return get(index);
							}
						}
						// Otherwise, throw it up
						throw re;
					}
				} else {
					ViewEntryCollection vec = getEntries();
					cache.put(index, createFromViewEntry(vec.getNthEntry(index + 1), columnInfo_));
				}
			} catch (Exception e) {
				throw e instanceof RuntimeException ? (RuntimeException)e : new RuntimeException(e);
			}
		}
		return cache.get(index);
	}

	@Override
	public int size() {
		if(size_ != -1) { return size_; }
		if(invalid_) { return 0; }

		try {
			if (searchQuery_ == null || searchQuery_.isEmpty()) {
				ViewNavigator nav = getNavigator();
				size_ = nav == null ? 0 : nav.getCount();
			} else {
				ViewEntryCollection vec = getEntries();
				size_ = vec == null ? 0 : vec.getCount();
			}
		} catch (Exception e) {
			throw e instanceof RuntimeException ? (RuntimeException)e : new RuntimeException(e);
		}
		return size_;
	}

	/* **********************************************************************
	 * TabularDataModel methods
	 ************************************************************************/
	@Override
	public boolean isColumnSortable(final String columnName) {
		if (category_ != null) {
			return false;
		}

		return super.isColumnSortable(columnName);
	}

	@Override
	public int getResortType(final String columnName) {
		if (category_ != null || invalid_) {
			return TabularDataModel.RESORT_NONE;
		}

		for (DominoColumnInfo col : columnInfo_) {
			if (col.getItemName().equalsIgnoreCase(columnName)) {
				if (col.isResortAscending() && col.isResortDescending()) {
					return TabularDataModel.RESORT_BOTH;
				} else if (col.isResortAscending()) {
					return TabularDataModel.RESORT_ASCENDING;
				} else if (col.isResortDescending()) {
					return TabularDataModel.RESORT_DESCENDING;
				}
				return TabularDataModel.RESORT_NONE;
			}
		}
		return TabularDataModel.RESORT_NONE;
	}

	@Override
	public void setResortOrder(final String columnName, final String sortOrder) {
		if(invalid_) {
			return;
		}

		if (category_ != null) {
			return;
		}

		clearCache();

		// Find the proper-cased name, since Domino cares
		DominoColumnInfo col = findColumnByName(columnName);
		if(col == null) {
			throw new IllegalArgumentException(MessageFormat.format("View \"{0}\" does not contain a column named \"{1}\"", this.getView().getName(), columnName));
		}
		String properColumnName = col.getItemName();

		if (TabularDataModel.SORT_TOGGLE.equals(sortOrder)) {
			// Cycle between ascending, descending, and off
			if (isAscending()) {
				setAscending(false);
				setResortColumn(properColumnName);
			} else if (getResortColumn() == null) {
				setAscending(true);
				setResortColumn(properColumnName);
			} else {
				setResortColumn(null);
			}
		} else {
			setResortColumn(properColumnName);
			setAscending(sortOrder.equals(TabularDataModel.SORT_ASCENDING));
		}
	}

	@Override
	public boolean isColumnCategorized(final String columnName) {
		DominoColumnInfo colInfo = findColumnByName(columnName);
		return colInfo != null && colInfo.isCategory();
	}

	@Override
	public int getColumnIndentLevel() {
		return get(getRowIndex()).columnIndentLevel();
	}

	@Override
	public String getRowPosition() {
		return get(getRowIndex()).viewRowPosition();
	}

	@Override
	public boolean isRowCollapsed() {
		return collapsedIds_.contains(get(getRowIndex()).noteId());
	}

	@Override
	public void collapseRow(final String rowPosition) {
		ViewNavigator nav = getNewNavigator();
		nav.gotoPos(rowPosition, '.');
		ViewEntry current = nav.getCurrent();
		current_ = current;
		int noteId = new BigInteger(current.getNoteID(), 16).intValue();
		collapsedIds_.add(noteId);
		expandedIds_.remove(noteId);
		clearCache();
	}

	@Override
	public void expandRow(final String rowPosition) {
		ViewNavigator nav = getNewNavigator();
		nav.gotoPos(rowPosition, '.');
		ViewEntry current = nav.getCurrent();
		current_ = current;
		int noteId = new BigInteger(current.getNoteID(), 16).intValue();
		collapsedIds_.remove(noteId);
		expandedIds_.add(noteId);
		clearCache();
	}

	private final DominoColumnInfo findColumnByName(final String columnName) {
		if(invalid_) { return null; }
		for (DominoColumnInfo col : columnInfo_) {
			if (col.getItemName().equalsIgnoreCase(columnName)) {
				return col;
			}
		}
		return null;
	}

	/* **********************************************************************
	 * Misc. leftovers
	 ************************************************************************/

	@Override
	public void search(final String searchQuery) {
		search(searchQuery, new SearchOption[0]);
	}
	
	@Override
	public void search(String searchQuery, SearchOption... options) {
		if(invalid_) {
			return;
		}

		if (category_ != null) {
			throw new UnsupportedOperationException("Cannot search a category-filtered view");
		}

		clearCache();

		searchQuery_ = searchQuery;
		if(options == null) {
			searchOptions_ = Collections.emptySet();
		} else {
			searchOptions_ = new HashSet<>(Arrays.asList(options));
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public E getByKey(final Object key) {
		View view = getView();
		ViewEntry entry;
		if (key instanceof List) {
			Vector<Object> keyVec = new Vector<Object>((List<?>) key);
			entry = view.getEntryByKey(keyVec, true);
		} else {
			entry = view.getEntryByKey(key, true);
		}
		if (entry != null) {
			return createFromViewEntry(entry, columnInfo_);
		}
		return null;
	}

	@Override
	public String toString() {
		return "[" + getClass().getName() + ": " + server_ + "!!" + filePath_ + "/" + viewName_ + "-" + category_ + getResortColumn() + isAscending() + searchQuery_ + "]";
	}

	protected ViewNavigator getNavigator() {
		final Map<String, Object> requestScope = getRequestScope();
		final String key = "viewnav-" + this.toString();
		if (!requestScope.containsKey(key)) {
			requestScope.put(key, getNewNavigator());
		}
		nav_ = (ViewNavigator)requestScope.get(key);
		return nav_;
	}

	protected ViewNavigator getNewNavigator() {
		View view = getView();
		ViewNavigator nav = null;
		if (category_ == null) {
			nav = view.createViewNav();
		} else {
			nav = view.createViewNavFromCategory(category_);
		}
		nav.setBufferMaxEntries(50); // The most common use will likely be a paged view

		int[] expandedIds = new int[expandedIds_.size()];
		int i = 0;
		for(Integer id : expandedIds_) {
			expandedIds[i++] = id;
		}
		int[] collapsedIds = new int[collapsedIds_.size()];
		i = 0;
		for(Integer id : collapsedIds_) {
			collapsedIds[i++] = id;
		}
		nav.setAutoExpandGuidance(50, collapsedIds, expandedIds);

		return nav;
	}

	protected ViewEntryCollection getEntries() {
		final Map<String, Object> requestScope = getRequestScope();
		final String key = "viewentries-" + this.toString();
		if (!requestScope.containsKey(key)) {
			View view = getView();
			if(StringUtil.isNotEmpty(searchQuery_)) {
				boolean exact = searchOptions_.contains(DominoSearchOption.EXACT);
				boolean variants = searchOptions_.contains(DominoSearchOption.WORD_VARIANTS);
				boolean fuzzy = searchOptions_.contains(DominoSearchOption.FUZZY);
				view.FTSearchSorted(searchQuery_, 0, StringUtil.toString(getResortColumn()), isAscending(), exact, variants, fuzzy);
			}

			if(category_ != null) {
				requestScope.put(key, view.getAllEntriesByKey(category_));
			} else {
				requestScope.put(key, view.getAllEntries());
			}
		}
		return (ViewEntryCollection) requestScope.get(key);
	}

	public View getView() {
		Database database = FrameworkUtils.getDatabase(server_, filePath_);
		View view = database.getView(viewName_);
		view.setAutoUpdate(false);
		view.setEnableNoteIDsForCategories(true);
		if (category_ == null) {
			if (getResortColumn() != null) {
				view.resortView(getResortColumn(), isAscending());
			} else {
				view.resortView();
			}
		}
		return view;
	}

	public final void clearCache() {
		getCache().clear();
		getView().refresh();
		size_ = -1;

		final Map<String, Object> requestScope = getRequestScope();
		String thisToString = this.toString();
		requestScope.remove("viewnav-" + thisToString);
		requestScope.remove("viewentries-" + thisToString);
		requestScope.remove("lastFetchedIndex-" + thisToString);
	}

	private Map<String, Object> getRequestScope() {
		if(FrameworkUtils.getViewScope() == null) {
			// This is the case in Xots
			return new HashMap<String, Object>();
		}
		return FrameworkUtils.isFaces() ? FrameworkUtils.getRequestScope() : internalCacheScope_;
	}

	@SuppressWarnings("unchecked")
	private Map<Integer, E> getCache() {
		Map<String, Object> cacheScope = FrameworkUtils.isFaces() ? FrameworkUtils.getRequestScope() : internalCacheScope_;
		String key = toString() + "_entrycache";
		if (!cacheScope.containsKey(key)) {
			cacheScope.put(key, new HashMap<Integer, E>());
		}
		return (Map<Integer, E>) cacheScope.get(key);
	}
}