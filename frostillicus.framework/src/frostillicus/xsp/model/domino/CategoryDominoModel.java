package frostillicus.xsp.model.domino;

import java.util.List;

import org.openntf.domino.ViewEntry;

public class CategoryDominoModel extends AbstractDominoModel {
	private static final long serialVersionUID = 1L;

	protected CategoryDominoModel(final ViewEntry entry, final List<DominoColumnInfo> columnInfo) {
		super(entry, columnInfo);
	}
}