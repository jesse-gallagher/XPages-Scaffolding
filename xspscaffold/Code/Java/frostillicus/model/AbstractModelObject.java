package frostillicus.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.DataModel;

import com.ibm.xsp.model.ViewRowData;

public abstract class AbstractModelObject extends DataModel implements ModelObject {
	private static final long serialVersionUID = 1L;

	private transient Map<String, Method> getterCache_ = new HashMap<String, Method>();
	private transient Map<String, List<Method>> setterCache_ = new HashMap<String, List<Method>>();

	/* **********************************************************************
	 * Hooks and utility methods for concrete classes
	 * These are named without "get" to avoid steeping on doc fields' toes
	 ************************************************************************/
	protected boolean querySave() {
		return true;
	}

	protected void postSave() {
	}

	protected boolean queryDelete() {
		return true;
	}

	protected void postDelete() {
	}

	/* **********************************************************************
	 * DataModel methods
	 ************************************************************************/
	@Override
	public int getRowCount() {
		return 1;
	}

	@Override
	public Object getRowData() {
		return this;
	}

	@Override
	public int getRowIndex() {
		return 0;
	}

	@Override
	public Object getWrappedData() {
		return this;
	}

	@Override
	public boolean isRowAvailable() {
		return true;
	}

	@Override
	public void setRowIndex(final int paramInt) {
		// NOP
	}

	@Override
	public void setWrappedData(final Object paramObject) {
		// NOP
	}

	/* **********************************************************************
	 * ViewRowData methods
	 ************************************************************************/
	public final Object getColumnValue(final String key) {
		return getValue(key);
	}

	public final void setColumnValue(final String key, final Object value) {
		setValue(key, value);
	}

	public final ViewRowData.ColumnInfo getColumnInfo(final String key) {
		return null;
	}

	public final boolean isReadOnly(final String key) {
		return isReadOnly((Object) key);
	}

	public final Object getValue(final String key) {
		return getValue((Object) key);
	}

	/* **********************************************************************
	 * Reflection seeker methods
	 ************************************************************************/
	protected final Method findGetter(final String key) {
		String lkey = key.toLowerCase();
		if (!getterCache_.containsKey(lkey)) {
			Method result = null;
			for (Method method : getClass().getMethods()) {
				String methodName = method.getName().toLowerCase();
				if (method.getParameterTypes().length == 0 && (methodName.equals("get" + lkey) || methodName.equals("is" + lkey))) {
					try {
						result = method;
						break;
					} catch (IllegalArgumentException e) {
						throw new RuntimeException(e);
					}
				}
			}
			getterCache_.put(lkey, result);
		}
		return getterCache_.get(lkey);
	}

	protected final List<Method> findSetters(final String key) {
		String lkey = key.toLowerCase();
		if (!setterCache_.containsKey(lkey)) {
			List<Method> result = new ArrayList<Method>();
			for (Method method : getClass().getMethods()) {
				Class<?>[] parameters = method.getParameterTypes();
				String methodName = method.getName().toLowerCase();
				if (parameters.length == 1 && methodName.equals("set" + lkey)) {
					result.add(method);
				}
			}
			setterCache_.put(lkey, result);
		}
		return setterCache_.get(lkey);
	}
}
