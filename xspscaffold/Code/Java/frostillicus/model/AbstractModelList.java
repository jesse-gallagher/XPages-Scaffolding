package frostillicus.model;

import java.io.Serializable;
import java.util.*;
import com.ibm.xsp.model.TabularDataModel;

public abstract class AbstractModelList<E extends ModelObject> extends TabularDataModel implements Serializable, List<E> {
	private static final long serialVersionUID = 1L;

}
