package frostillicus.model;

import java.io.Serializable;
import org.openntf.domino.ViewColumn;

public class DominoColumnInfo implements Serializable {
	private static final long serialVersionUID = -8895078572051370217L;

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