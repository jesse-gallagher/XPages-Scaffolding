package frostillicus.model.domino;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openntf.domino.View;
import org.openntf.domino.ViewColumn;

public class DominoColumnInfo implements Serializable {
	private static final long serialVersionUID = -8895078572051370217L;



	public static List<DominoColumnInfo> fromView(final View view) {
		List<ViewColumn> columns = view.getColumns();
		List<DominoColumnInfo> result = new ArrayList<DominoColumnInfo>(columns.size());
		for (ViewColumn column : columns) {
			if (column.getColumnValuesIndex() < 65535) {
				result.add(new DominoColumnInfo(column));
			}
		}
		return result;
	}

	public static Map<String, Object> columnValuesToMap(final List<Object> columnValues, final List<DominoColumnInfo> columnInfo) {
		Map<String, Object> result = new HashMap<String, Object>();

		for (int i = 0; i < columnValues.size(); i++) {
			result.put(columnInfo.get(i).getItemName(), columnValues.get(i));
		}

		return result;
	}

	private final String itemName_;
	private final boolean resortAscending_;
	private final boolean resortDescending_;
	private final boolean category_;

	protected DominoColumnInfo(final ViewColumn column) {
		itemName_ = column.getItemName();
		resortAscending_ = column.isResortAscending();
		resortDescending_ = column.isResortDescending();
		category_ = column.isCategory();
	}

	public String getItemName() {
		return itemName_;
	}

	public boolean isResortAscending() {
		return resortAscending_;
	}

	public boolean isResortDescending() {
		return resortDescending_;
	}

	public boolean isCategory() {
		return category_;
	}
}