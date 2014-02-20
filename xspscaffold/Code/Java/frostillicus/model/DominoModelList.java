package frostillicus.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.TabularDataModel;
import org.openntf.domino.*;

@SuppressWarnings("serial")
public class DominoModelList<E extends AbstractDominoModel> extends AbstractModelList<E> {

	@SuppressWarnings("unused")
	private transient Map<Integer, E> cache_ = new HashMap<Integer, E>();

	private final String server_;
	private final String filePath_;
	private final String viewName_;
	private final String category_;
	private String sortColumn_;
	private boolean ascending_;
	private final List<DominoColumnInfo> columnInfo_;
	private String searchQuery_;
	private Class<E> clazz_;

	private final boolean invalid_;

	private Map<String, Integer> collapsedPositions_ = new HashMap<String, Integer>();

	public DominoModelList(final Database database, final String viewName, final String category, final Class<E> clazz) {
		server_ = database.getServer();
		filePath_ = database.getFilePath();
		viewName_ = viewName;
		category_ = category;
		clazz_ = clazz;

		// Gather the view info now
		View view = database.getView(viewName_);
		invalid_ = view == null;

		columnInfo_ = invalid_ ? null : ModelUtils.getColumnInfo(view);
	}

	protected E createFromViewEntry(final ViewEntry entry, final List<DominoColumnInfo> columnInfo) {
		try {
			Constructor<E> con = clazz_.getConstructor(ViewEntry.class, List.class);
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
						// Test to see if the nav itself is the problem in the NPE
						//						@SuppressWarnings("unused")
						//int count = nav.getCount();

						ViewEntry current = nav.getCurrent();
						String currentPosition = current.getPosition('.');
						if (currentPosition.contains(".")) {
							int topLevel = Integer.valueOf(ModelUtils.strLeft(currentPosition, "."));
							for (String position : collapsedPositions_.keySet()) {
								int collapseIndex = Integer.valueOf(position.contains(".") ? ModelUtils.strLeft(position, ".") : position);
								if (currentPosition.startsWith(position + ".")) {
									//int skipCount = current.getSiblingCount() - Integer.valueOf(ModelUtils.strRightBack(currentPosition, ".")) + 1;
									//nav.skip(skipCount);
									//break;
								}
								if (collapseIndex <= topLevel) {
									nav.skip(collapsedPositions_.get(position));
								}
							}
						}

						//getCache().put(index, createFromViewEntry(nav.getNth(index + 1), columnInfo_));
						getCache().put(index, createFromViewEntry(nav.getCurrent(), columnInfo_));
						//					System.out.println("fetched index " + index + ", which is pos " + nav.getCurrent().getPosition('.'));
						//					nav.skip(1);
					} catch(NullPointerException npe) {
						// Then we've probably hit an "Object has been removed or recycled" on the nav
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
				int hiddenEntries = 0;
				for (int hiddenCount : collapsedPositions_.values()) {
					hiddenEntries += hiddenCount;
				}
				return nav == null ? 0 : getNavigator().getCount() - hiddenEntries;
			} else {
				ViewEntryCollection vec = getEntries();
				return vec == null ? 0 : vec.getCount();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean add(final E e) {
		throw new UnsupportedOperationException();
	}

	public void add(final int index, final E element) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(final Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(final int index, final Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}

	public boolean contains(final Object o) {
		throw new UnsupportedOperationException();
	}

	public boolean containsAll(final Collection<?> c) {
		for (Object o : c) {
			if (!contains(o))
				return false;
		}
		return true;
	}

	public Iterator<E> iterator() {
		return new DominoListIterator<E>();
	}

	public int indexOf(final Object o) {
		for (int i = 0; i < size(); i++) {
			if (get(i).equals(o)) {
				return i;
			}
		}
		return -1;
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public int lastIndexOf(final Object o) {
		for (int i = size() - 1; i >= 0; i--) {
			if (get(i).equals(o)) {
				return i;
			}
		}
		return -1;
	}

	public ListIterator<E> listIterator() {
		return new DominoListIterator<E>();
	}

	public ListIterator<E> listIterator(final int index) {
		throw new UnsupportedOperationException();
	}

	public E remove(final int index) {
		throw new UnsupportedOperationException();
	}

	public boolean remove(final Object o) {
		throw new UnsupportedOperationException();
	}

	public boolean removeAll(final Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	public boolean retainAll(final Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	public E set(final int index, final E element) {
		throw new UnsupportedOperationException();
	}

	public List<E> subList(final int fromIndex, final int toIndex) {
		throw new UnsupportedOperationException();
	}

	public Object[] toArray() {
		Object[] result = new Object[size()];
		for (int i = 0; i < size(); i++) {
			result[i] = get(i);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public <T extends Object> T[] toArray(final T[] a) {
		return (T[]) toArray();
	};

	/* **********************************************************************
	 * TabularDataModel methods
	 ************************************************************************/
	@Override
	public int getRowCount() {
		return size();
	}

	@Override
	public Object getRowData() {
		return get(getRowIndex());
	}

	@Override
	public String getResortColumn() {
		return sortColumn_;
	}

	@Override
	public int getResortState(final String columnName) {
		if (category_ != null) {
			return TabularDataModel.RESORT_NONE;
		}

		if (sortColumn_ != null && sortColumn_.equalsIgnoreCase(columnName)) {
			return ascending_ ? TabularDataModel.RESORT_ASCENDING : TabularDataModel.RESORT_DESCENDING;
		}
		return TabularDataModel.RESORT_NONE;
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
	public boolean isColumnSortable(final String columnName) {
		if (category_ != null) {
			return false;
		}

		return getResortType(columnName) != TabularDataModel.RESORT_NONE;
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
			if (ascending_) {
				ascending_ = false;
				sortColumn_ = properColumnName;
			} else if (sortColumn_ == null) {
				ascending_ = true;
				sortColumn_ = properColumnName;
			} else {
				sortColumn_ = null;
			}
		} else {
			sortColumn_ = properColumnName;
			ascending_ = sortOrder.equals(TabularDataModel.SORT_ASCENDING);
		}
	}

	@Override
	public boolean isColumnCategorized(final String columnName) {
		DominoColumnInfo colInfo = findColumnByName(columnName);
		return colInfo != null && colInfo.isCategory();
	}

	@Override
	public int getRowType() {
		if (get(getRowIndex()).isCategory()) {
			return TabularDataModel.TYPE_CATEGORY;
		}
		return TabularDataModel.TYPE_ENTRY;
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
		return collapsedPositions_.containsKey(getRowPosition());
	}

	@Override
	public void collapseRow(final String rowPosition) {
		if (!collapsedPositions_.containsKey(rowPosition)) {
			try {
				// TODO make this work with multi-level categories
				ViewNavigator nav = getNewNavigator();
				nav.gotoPos(rowPosition, '.');
				collapsedPositions_.put(rowPosition, nav.getCurrent().getDescendantCount());
			} catch (Exception ne) {
				throw new RuntimeException(ne);
			}
			clearCache();
		}
	}

	@Override
	public void expandRow(final String rowPosition) {
		if (collapsedPositions_.containsKey(rowPosition)) {
			collapsedPositions_.remove(rowPosition);
			clearCache();
		}
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
	public void search(final String searchQuery) {
		if(invalid_) return;

		if (category_ != null) {
			throw new UnsupportedOperationException("Cannot search a category-filtered view");
		}

		clearCache();

		searchQuery_ = searchQuery;
	}

	@SuppressWarnings("deprecation")
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
		return "[" + getClass().getName() + ": " + server_ + "!!" + filePath_ + "/" + viewName_ + "-" + category_ + sortColumn_ + ascending_ + searchQuery_ + "]";
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
		return nav;
	}

	protected ViewEntryCollection getEntries() {
		final Map<String, Object> requestScope = ExtLibUtil.getRequestScope();
		final String key = "viewentries-" + this.toString();
		if (!requestScope.containsKey(key)) {
			View view = getView();
			if (sortColumn_ != null) {
				view.FTSearchSorted(searchQuery_, 0, sortColumn_, ascending_, false, false, false);
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
		if (category_ == null) {
			if (sortColumn_ != null) {
				view.resortView(sortColumn_, ascending_);
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

	private class DominoListIterator<K> implements ListIterator<K> {
		private int index_ = 0;

		public void add(final K value) {
			throw new UnsupportedOperationException();
		}

		public boolean hasNext() {
			return index_ < size();
		}

		public boolean hasPrevious() {
			return index_ > 0;
		}

		@SuppressWarnings("unchecked")
		public K next() {
			if (index_ == size()) {
				throw new NoSuchElementException();
			}
			return (K) get(index_++);
		}

		public int nextIndex() {
			return index_ == size() ? index_ : index_ + 1;
		}

		@SuppressWarnings("unchecked")
		public K previous() {
			if (index_ == 0) {
				throw new NoSuchElementException();
			}
			return (K) get(index_--);
		}

		public int previousIndex() {
			return index_ == 0 ? index_ : index_ - 1;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		public void set(final K value) {
			throw new UnsupportedOperationException();
		}
	}
}