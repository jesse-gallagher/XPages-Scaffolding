package frostillicus.xsp.model;

import java.io.Serializable;
import java.util.*;

import com.ibm.xsp.model.TabularDataModel;

public abstract class AbstractModelList<E extends ModelObject> extends TabularDataModel implements Serializable, List<E> {
	private static final long serialVersionUID = 1L;

	private String sortColumn_;
	private boolean ascending_;
	private Class<E> clazz_;

	public AbstractModelList(final Class<E> clazz) {
		clazz_ = clazz;
	}

	public abstract E getByKey(final Object key);

	public abstract void search(final String searchQuery);

	protected Class<E> getClazz() {
		return clazz_;
	}

	/* **********************************************************************
	 * List methods
	 ************************************************************************/
	@Override
	public ListIterator<E> listIterator() {
		return new ModelListIterator<E>();
	}

	@Override
	public ListIterator<E> listIterator(final int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E remove(final int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(final Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E set(final int index, final E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<E> subList(final int fromIndex, final int toIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean add(final E e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(final int index, final E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(final Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(final Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		for (Object o : c) {
			if (!contains(o)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Iterator<E> iterator() {
		return new ModelListIterator<E>();
	}

	@Override
	public int indexOf(final Object o) {
		if(o == null || !o.getClass().equals(clazz_)) {
			return -1;
		}

		for (int i = 0; i < size(); i++) {
			if (get(i).equals(o)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public int lastIndexOf(final Object o) {
		if(o == null || !o.getClass().equals(clazz_)) {
			return -1;
		}

		for (int i = size() - 1; i >= 0; i--) {
			if (get(i).equals(o)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public Object[] toArray() {
		Object[] result = new Object[size()];
		for (int i = 0; i < size(); i++) {
			result[i] = get(i);
		}
		return result;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Object> T[] toArray(final T[] a) {
		return (T[]) toArray();
	};



	/* **********************************************************************
	 * TabularDataModel methods
	 ************************************************************************/
	@Override
	public boolean isColumnSortable(final String columnName) {
		return getResortType(columnName) != TabularDataModel.RESORT_NONE;
	}
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
	protected void setResortColumn(final String sortColumn) {
		sortColumn_ = sortColumn;
	}
	protected boolean isAscending() {
		return ascending_;
	}
	protected void setAscending(final boolean ascending) {
		ascending_ = ascending;
	}

	@Override
	public int getResortState(final String columnName) {
		if (sortColumn_ != null && sortColumn_.equalsIgnoreCase(columnName)) {
			return ascending_ ? TabularDataModel.RESORT_ASCENDING : TabularDataModel.RESORT_DESCENDING;
		}
		return TabularDataModel.RESORT_NONE;
	}

	@Override
	public int getRowType() {
		if (get(getRowIndex()).category()) {
			return TabularDataModel.TYPE_CATEGORY;
		}
		return TabularDataModel.TYPE_ENTRY;
	}

	/* ******************************************************************************
	 * Internal iterator class
	 ********************************************************************************/
	private class ModelListIterator<K> implements ListIterator<K> {
		private int index_ = 0;

		@Override
		public void add(final K value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasNext() {
			return index_ < size();
		}

		@Override
		public boolean hasPrevious() {
			return index_ > 0;
		}

		@Override
		@SuppressWarnings("unchecked")
		public K next() {
			if (index_ == size()) {
				throw new NoSuchElementException();
			}
			return (K) get(index_++);
		}

		@Override
		public int nextIndex() {
			return index_ == size() ? index_ : index_ + 1;
		}

		@Override
		@SuppressWarnings("unchecked")
		public K previous() {
			if (index_ == 0) {
				throw new NoSuchElementException();
			}
			return (K) get(index_--);
		}

		@Override
		public int previousIndex() {
			return index_ == 0 ? index_ : index_ - 1;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void set(final K value) {
			throw new UnsupportedOperationException();
		}
	}
}
