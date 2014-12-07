package frostillicus.xsp;

import com.ibm.xsp.model.DataObject;
import com.ibm.xsp.model.domino.wrapped.DominoDocument;

import lotus.domino.*;

import com.ibm.xsp.extlib.util.ExtLibUtil;

public class DominoDocumentMap implements DataObject {

	@Override
	public DominoDocument getValue(Object key) {
		if(!(key instanceof String)) {
			throw new IllegalArgumentException("key must be a String.");
		}

		try {
			Database database = ExtLibUtil.getCurrentDatabase();
			Document doc = database.getDocumentByUNID((String)key);
			return DominoDocument.wrap(
			                           database.getFilePath(),	// databaseName
			                           doc,				// Document
			                           null,				// computeWithForm
			                           null,				// concurrencyMode
			                           false,			// allowDeletedDocs
			                           null,				// saveLinksAs
			                           null				// webQuerySaveAgent
					);
		} catch(NotesException ne) {
			ne.printStackTrace();
		}
		return null;
	}

	@Override
	public void setValue(Object key, Object value) { }

	@Override
	public Class<DominoDocument> getType(Object key) {
		return DominoDocument.class;
	}

	@Override
	public boolean isReadOnly(Object key) {
		return true;
	}
}
