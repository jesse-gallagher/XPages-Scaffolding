package frostillicus;

import com.ibm.xsp.model.domino.wrapped.DominoDocument;
import lotus.domino.*;
import com.ibm.xsp.extlib.util.ExtLibUtil;

public class DominoDocumentMap extends AbstractKeyMap<String, DominoDocument> {

	public DominoDocument get(Object key) {
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
}
