package frostillicus.xsp;

import java.util.*;
import java.io.Serializable;
import lotus.domino.*;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.*;

@SuppressWarnings("unchecked")
public class SortableMapView extends TabularDataModel implements Serializable, TabularDataSource {
	private static final long serialVersionUID = 1L;
	
	private List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> originalData;
	
	private Set<String> selectedIds = new LinkedHashSet<String>();

	private String sortColumn = "";
	private String sortOrder = "";

	public SortableMapView(String viewName) throws NotesException {
		Database database = ExtLibUtil.getCurrentDatabase();
		View view = database.getView(viewName);
		view.setAutoUpdate(false);

		ViewNavigator nav = view.createViewNav();
		nav.setBufferMaxEntries(400);

		populateData(nav);
	}

	public SortableMapView(ViewNavigator nav) throws NotesException {
		populateData(nav);
	}
	
	public SortableMapView() {
		
	}

	private void populateData(ViewNavigator nav) throws NotesException {
		View view = nav.getParentView();

		// Find the column names, since they're preferable to indexes
		Map<Integer, String> columnNameMap = new HashMap<Integer, String>();
		for(ViewColumn col : (List<ViewColumn>)view.getColumns()) {
			if(col.getColumnValuesIndex() < 65535) {
				columnNameMap.put(col.getColumnValuesIndex(), col.getItemName());
				
				if(col.isSorted() && !col.isCategory() && StringUtil.isEmpty(sortColumn)) {
					boolean ascending = !col.isSortDescending();
					sortColumn = col.getItemName();
					sortOrder = ascending ? TabularDataModel.SORT_ASCENDING : TabularDataModel.SORT_DESCENDING;
				}
			}
		}

		ViewEntry entry = nav.getFirst();
		while(entry != null) {
			entry.setPreferJavaDates(true);
			List<Object> columnValues = (List<Object>)entry.getColumnValues();

			Map<String, Object> entryMap = new FakeEntryData();
			for(Integer index : columnNameMap.keySet()) {
				if(!(columnValues.get(index) instanceof Comparable)) {
					entryMap.put(columnNameMap.get(index), columnValues.get(index).toString());
				} else {
					entryMap.put(columnNameMap.get(index), (Comparable<?>)columnValues.get(index));
				}
			}
			entryMap.put("documentId", entry.getUniversalID());
			entryMap.put("noteId", entry.getNoteID());
			this.data.add(entryMap);

			ViewEntry tempEntry = entry;
			entry = nav.getNext(entry);
			tempEntry.recycle();
		}

		this.originalData = new ArrayList<Map<String, Object>>(this.data);
	}
	
	public void addEntry(ViewRowData entry) {
		this.data.add((Map<String, Object>)entry);
		this.originalData = this.data;
	}

	@Override
	public int getRowCount() { return this.data.size(); }

	@Override
	public ViewRowData getRowData() { return (ViewRowData)this.data.get(this.getRowIndex()); }

	@Override
	public boolean isColumnSortable(String columnName) { return true; }

	@Override
	public int getResortType(String columnName) { return TabularDataModel.RESORT_BOTH; }

	@Override
	public void setResortOrder(String columnName, String sortOrder) {
		if(!columnName.equals(this.sortColumn)) {
			// Switching columns means switch back to ascending by default
			this.sortOrder = sortOrder.equals(TabularDataModel.SORT_DESCENDING) ? TabularDataModel.SORT_DESCENDING : TabularDataModel.SORT_ASCENDING;
			Collections.sort(this.data, new MapComparator(columnName, !sortOrder.equals(TabularDataModel.SORT_DESCENDING)));
			this.sortColumn = columnName;
		} else {
			this.sortColumn = columnName;
			if(sortOrder.equals(TabularDataModel.SORT_ASCENDING) || (sortOrder.equals("toggle") && StringUtil.isEmpty(this.sortOrder))) {
				this.sortOrder = TabularDataModel.SORT_ASCENDING;
				Collections.sort(this.data, new MapComparator(columnName, true));
			} else if(sortOrder.equals(TabularDataModel.SORT_DESCENDING) || (sortOrder.equals("toggle") && this.sortOrder.equals(TabularDataModel.SORT_ASCENDING))) {
				this.sortOrder = TabularDataModel.SORT_DESCENDING;
				Collections.sort(this.data, new MapComparator(columnName, false));
			} else {
				this.sortOrder = "";
				this.data = new ArrayList<Map<String, Object>>(this.originalData);
			}
		}
	}

	@Override
	public int getResortState(String columnName) {
		if(!StringUtil.equals(this.sortColumn, columnName)) {
			return TabularDataModel.RESORT_NONE;
		}
		return this.sortOrder.equals(TabularDataModel.SORT_ASCENDING) ? TabularDataModel.RESORT_ASCENDING :
			this.sortOrder.equals(TabularDataModel.SORT_DESCENDING) ? TabularDataModel.RESORT_DESCENDING :
				TabularDataModel.RESORT_NONE;
	}

	@Override
	public String getResortColumn() {
		return this.sortColumn;
	}
	
	@Override
	public String getRowId() {
		return (String)getRowData().getColumnValue("noteId");
	}
	
	@Override
	public void addSelectedId(String noteId) {
		this.selectedIds.add(noteId);
	}
	@Override
	public void removeSelectedId(String noteId) {
		this.selectedIds.remove(noteId);
	}
	@Override
	public Iterator<String> getSelectedIds() {
		return this.selectedIds.iterator();
	}
	@Override
	public void clearSelectedIds() {
		this.selectedIds.clear();
	}

	// View Panels know how to deal with ViewRowData better than Maps, apparently, so just pass through
	//  the ViewRowData methods to their Map equivalents
	private static class FakeEntryData extends HashMap<String, Object> implements ViewRowData {
		private static final long serialVersionUID = 1L;

		public Object getColumnValue(String arg0) {
			return this.get(arg0);
		}
		public Object getValue(String arg0) { return this.get(arg0); }
		public ColumnInfo getColumnInfo(String arg0) { return null; }
		public String getOpenPageURL(String arg0, boolean arg1) { return null; }
		public boolean isReadOnly(String arg0) { return false; }

		public void setColumnValue(String arg0, Object arg1) {
				this.put(arg0, arg1);
		}
	}

	// A basic class to compare two Maps by a given comparable key common in each,
	//  allowing for descending order
	private static class MapComparator implements Comparator<Map<String, Object>>, Serializable {
		private static final long serialVersionUID = 1L;

		private final String key;
		private final boolean ascending;

		public MapComparator(String key, boolean ascending) {
			this.key = key;
			this.ascending = ascending;
		}

		public int compare(Map<String, Object> o1, Map<String, Object> o2) {
			Object valA = o1.get(key);
			if(valA == null) {
				valA = "";
			}
			Object valB = o2.get(key);
			if(valB == null) { 
				valB = "";
			}

			return (ascending ? 1 : -1) * ColumnValueComparator.INSTANCE.compare(valA, valB);
		}
	}

	private static enum ColumnValueComparator implements Comparator<Object> {
		INSTANCE;

		public int compare(Object o1, Object o2) {
			// First, order by type
			ColumnValueType typeA = ColumnValueType.valueOf(o1.getClass());
			ColumnValueType typeB = ColumnValueType.valueOf(o2.getClass());
			int typeCompare = typeA.compareTo(typeB);
			if(typeCompare != 0) {
				return typeCompare;
			}

			// Then they must be the same class
			if(typeA == ColumnValueType.DATETIME) {
				try {
					Date dateA = ((DateTime)o1).toJavaDate();
					Date dateB = ((DateTime)o2).toJavaDate();
					return dateA.compareTo(dateB);
				} catch(NotesException ne) {
					throw new RuntimeException(ne);
				}
			} else {
				return ((Comparable<Object>)o1).compareTo((Comparable<Object>)o2);
			}
		}

	}

	private static enum ColumnValueType {
		STRING(String.class), DOUBLE(Double.class), DATE(Date.class), DATETIME(DateTime.class);

		private final Class<?> clazz_;

		private ColumnValueType(Class<?> clazz) {
			clazz_ = clazz;
		}

		public static ColumnValueType valueOf(Class<?> clazz) {
			for(ColumnValueType val : values()) {
				if(val.clazz_.equals(clazz)) {
					return val;
				}
			}
			throw new IllegalArgumentException("No type found for class " + clazz);
		}
	}
}