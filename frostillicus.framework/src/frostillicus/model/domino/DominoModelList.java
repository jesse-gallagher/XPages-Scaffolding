package frostillicus.model.domino;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.*;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.TabularDataModel;

import frostillicus.model.AbstractModelList;
import frostillicus.model.ModelUtils;

import org.openntf.domino.*;

@SuppressWarnings("serial")
public class DominoModelList<E extends AbstractDominoModel> extends AbstractModelList<E> {

	@SuppressWarnings("unused")
	private transient Map<Integer, E> cache_ = new HashMap<Integer, E>();

	private final String server_;
	private final String filePath_;
	private final String viewName_;
	private final String category_;
	private final List<DominoColumnInfo> columnInfo_;
	private String searchQuery_;

	private final boolean invalid_;

	private Set<Integer> collapsedIds_ = new TreeSet<Integer>();
	private Set<Integer> expandedIds_ = new TreeSet<Integer>();

	private transient boolean reset_ = false;

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
			Constructor<E> con = getClazz().getConstructor(ViewEntry.class, List.class);
			return con.newInstance(entry, columnInfo);
		} catch (NoSuchMethodException nsme) {
			throw new RuntimeException(nsme);
		} catch (InvocationTargetException ite) {
			throw new RuntimeException(ite);
		} catch (IllegalAccessException iae) {
			throw new RuntimeException(iae);
		} catch (InstantiationException ie) {
			throw new RuntimeException(ie);
		}
	}

	/* **********************************************************************
	 * List methods
	 ************************************************************************/
	@SuppressWarnings("deprecation")
	public E get(final int index) {
		if(invalid_) { return null; }

		if (!getCache().containsKey(index)) {
			try {
				if (searchQuery_ == null || searchQuery_.isEmpty()) {
					ViewNavigator nav = getNavigator();

					// getNth is top-level only, so let's skip to what we need
					int lastFetchedIndex = 0;
					Map<String, Object> requestScope = ExtLibUtil.getRequestScope();
					String key = "lastFetchedIndex-" + toString();
					if (requestScope.containsKey(key)) {
						lastFetchedIndex = (Integer) requestScope.get(key);
					}
					nav.skip(index - lastFetchedIndex);

					requestScope.put(key, index);

					// If we're in a collapsed category, we have to skip further
					// TODO make this work with multi-level categories
					try {
						getCache().put(index, createFromViewEntry(nav.getCurrent(), columnInfo_));
					} catch(NullPointerException npe) {
						// Then we've probably hit an "Object has been removed or recycled" on the nav

						if(!reset_) {
							reset_ = true;
							clearCache();
							return get(index);
						}

						System.out.println("=============================== NPE in DominoModelList");
						System.out.println("=============================== Current class: " + getClass().getName());
						System.out.println("=============================== Desired index: " + index);
						System.out.println("=============================== Current cache: " + getCache());
						System.out.println("=============================== Current reported size: " + size());
						throw npe;
					}
				} else {
					ViewEntryCollection vec = getEntries();
					getCache().put(index, createFromViewEntry(vec.getNthEntry(index + 1), columnInfo_));
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return getCache().get(index);
	}

	public int size() {
		if(invalid_) { return 0; }
		try {
			if (searchQuery_ == null || searchQuery_.isEmpty()) {
				ViewNavigator nav = getNavigator();
				return nav == null ? 0 : getNavigator().getCount();
			} else {
				ViewEntryCollection vec = getEntries();
				return vec == null ? 0 : vec.getCount();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
		if (category_ != null) {
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
		if(invalid_) return;

		if (category_ != null) {
			return;
		}

		clearCache();

		// Find the proper-cased name, since Domino cares
		String properColumnName = findColumnByName(columnName).getItemName();

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
		int noteId = new BigInteger(current.getNoteID(), 16).intValue();
		collapsedIds_.remove(noteId);
		expandedIds_.add(noteId);
		clearCache();
	}

	private final DominoColumnInfo findColumnByName(final String columnName) {
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
		if(invalid_) return;

		if (category_ != null) {
			throw new UnsupportedOperationException("Cannot search a category-filtered view");
		}

		clearCache();

		searchQuery_ = searchQuery;
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
		final Map<String, Object> requestScope = ExtLibUtil.getRequestScope();
		final String key = "viewnav-" + this.toString();
		if (!requestScope.containsKey(key)) {
			requestScope.put(key, getNewNavigator());
		}
		return (ViewNavigator) requestScope.get(key);
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
		final Map<String, Object> requestScope = ExtLibUtil.getRequestScope();
		final String key = "viewentries-" + this.toString();
		if (!requestScope.containsKey(key)) {
			View view = getView();
			if (getResortColumn() != null) {
				view.FTSearchSorted(searchQuery_, 0, getResortColumn(), isAscending(), false, false, false);
			} else {
				view.FTSearch(searchQuery_);
			}

			requestScope.put(key, view.getAllEntries());
		}
		return (ViewEntryCollection) requestScope.get(key);
	}

	protected View getView() {
		Database database = ModelUtils.getDatabase(server_, filePath_);
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

		final Map<String, Object> requestScope = ExtLibUtil.getRequestScope();
		String thisToString = this.toString();
		requestScope.remove("viewnav-" + thisToString);
		requestScope.remove("viewentries-" + thisToString);
		requestScope.remove("lastFetchedIndex-" + thisToString);
	}

	@SuppressWarnings("unchecked")
	private Map<Integer, E> getCache() {
		Map<String, Object> cacheScope = ExtLibUtil.getRequestScope();
		String key = toString() + "_entrycache";
		if (!cacheScope.containsKey(key)) {
			cacheScope.put(key, new HashMap<Integer, E>());
		}
		return (Map<Integer, E>) cacheScope.get(key);
	}
}