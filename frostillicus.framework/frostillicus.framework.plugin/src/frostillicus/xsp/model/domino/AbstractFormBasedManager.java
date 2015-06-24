package frostillicus.xsp.model.domino;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.faces.context.FacesContext;
import javax.persistence.Table;

import org.openntf.domino.Database;
import org.openntf.domino.Document;
import org.openntf.domino.ViewEntry;
import org.openntf.domino.design.DatabaseDesign;

import com.ibm.commons.util.StringUtil;

import frostillicus.xsp.model.domino.AbstractDominoModel;
import frostillicus.xsp.model.domino.AbstractDominoManager;
import frostillicus.xsp.model.domino.DominoColumnInfo;
import frostillicus.xsp.model.domino.DominoModelList;
import frostillicus.xsp.util.FrameworkUtils;

public abstract class AbstractFormBasedManager extends AbstractDominoManager<AbstractDominoModel> {
	private static final long serialVersionUID = 1L;

	@Override
	protected Class<AbstractDominoModel> getModelClass() {
		return AbstractDominoModel.class;
	}

	@Override
	public DominoModelList<AbstractDominoModel> getNamedCollection(final String name, final String category) {
		return new FormBasedModelList(getDatabase(), getViewPrefix() + name, category, getModelClass());
	}

	public static class FormBasedModelList extends DominoModelList<AbstractDominoModel> {
		private static final long serialVersionUID = 1L;

		public FormBasedModelList(final Database database, final String viewName, final String category, final Class<AbstractDominoModel> clazz) {
			super(database, viewName, category, clazz);
		}

		@Override
		protected AbstractDominoModel createFromViewEntry(final ViewEntry entry, final List<DominoColumnInfo> columnInfo) {
			if(entry.isCategory()) {
				AbstractDominoModel model = new CategoryDominoModel();
				model.initFromViewEntry(entry, columnInfo);
				return model;
			}

			String form = null;
			for(DominoColumnInfo col : columnInfo) {
				if(col.getItemName().equals("Form")) {
					form = entry.getColumnValue("Form", String.class);
					break;
				}
			}
			if(form == null) {
				Document doc = entry.getDocument();
				form = doc.getFormName();
			}
			if(StringUtil.isNotEmpty(form)) {
				Map<String, Class<? extends AbstractDominoModel>> formClassMap = getFormClassMap();
				if(formClassMap.containsKey(form)) {
					try {
						AbstractDominoModel result = formClassMap.get(form).newInstance();
						result.initFromViewEntry(entry, columnInfo);
						return result;
					} catch (SecurityException e) {
						throw new RuntimeException(e);
					} catch (IllegalArgumentException e) {
						throw new RuntimeException(e);
					} catch (InstantiationException e) {
						throw new RuntimeException(e);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
			}

			AbstractDominoModel model = new GenericDominoModel();
			model.initFromViewEntry(entry, columnInfo);
			return model;
		}

		@SuppressWarnings("unchecked")
		private Map<String, Class<? extends AbstractDominoModel>> getFormClassMap() {
			Map<String, Object> applicationScope = FrameworkUtils.getApplicationScope();
			String cacheKey = AbstractFormBasedManager.class.getName() + "_formClassMap";
			if(!applicationScope.containsKey(cacheKey)) {
				// Build a Map of form names to model classes
				Map<String, Class<? extends AbstractDominoModel>> result = new TreeMap<String, Class<? extends AbstractDominoModel>>(String.CASE_INSENSITIVE_ORDER);
				DatabaseDesign design = FrameworkUtils.getDatabase().getDesign();

				ClassLoader loader;
				if(FrameworkUtils.isFaces()) {
					loader = FacesContext.getCurrentInstance().getContextClassLoader();
				} else {
					loader = design.getDatabaseClassLoader(getClass().getClassLoader());
				}
				for(String className : design.getJavaResourceClassNames()) {
					try {
						Class<?> clazz = loader.loadClass(className);
						Table tableAnnotation = clazz.getAnnotation(Table.class);
						if(tableAnnotation != null) {
							if(StringUtil.isNotEmpty(tableAnnotation.name())) {
								result.put(tableAnnotation.name(), (Class<? extends AbstractDominoModel>)clazz);
							}
						}
					} catch(ClassNotFoundException e) {
						// Ignore
					} catch (SecurityException e) {
						throw new RuntimeException(e);
					}
				}

				applicationScope.put(cacheKey, result);
			}
			return (Map<String, Class<? extends AbstractDominoModel>>)applicationScope.get(cacheKey);
		}
	}
}