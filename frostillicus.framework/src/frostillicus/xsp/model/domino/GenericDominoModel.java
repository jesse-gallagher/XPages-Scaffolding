package frostillicus.xsp.model.domino;

import java.util.List;

import org.openntf.domino.Database;
import org.openntf.domino.Document;
import org.openntf.domino.ViewEntry;

public class GenericDominoModel extends AbstractDominoModel {
	private static final long serialVersionUID = 1L;

	protected GenericDominoModel(final ViewEntry entry, final List<DominoColumnInfo> columnInfo) {
		super(entry, columnInfo);
	}

	public GenericDominoModel(final Database database) {
		super(database);
	}

	public GenericDominoModel(final Document doc) {
		super(doc);
	}
}