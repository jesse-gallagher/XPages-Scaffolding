package frostillicus.framework.test.models;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.validation.constraints.NotNull;

import org.openntf.domino.*;

import frostillicus.framework.test.AllTests;
import frostillicus.xsp.bean.ApplicationScoped;
import frostillicus.xsp.bean.ManagedBean;
import frostillicus.xsp.model.domino.AbstractDominoManager;
import frostillicus.xsp.model.domino.AbstractDominoModel;
import frostillicus.xsp.util.FrameworkUtils;

public class Post extends AbstractDominoModel {
	private static final long serialVersionUID = 1L;

	@NotNull Date posted;
	@NotNull PostStatus status;
	String thread;
	//	List<String> tags;


	@Override
	public void initFromDatabase(final Database database) {
		super.initFromDatabase(database);

		setValue("Form", "Post");  //$NON-NLS-1$//$NON-NLS-2$
		setValue("Posted", new Date()); //$NON-NLS-1$
		setValue("$$Creator", FrameworkUtils.getUserName()); //$NON-NLS-1$
		setValue("Status", PostStatus.Draft); //$NON-NLS-1$
	}

	public List<Comment> getComments() {
		Comment.Manager comments = Comment.Manager.get();
		return comments.getNamedCollection("By PostID", String.valueOf(getValue("PostID"))); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public int getCommentCount() {
		return getComments().size();
	}

	@Override
	protected boolean querySave() {
		if(isNew()) {
			setValue("PostID", document().getUniversalID()); //$NON-NLS-1$
		}
		return super.querySave();
	}

	@ManagedBean(name="Posts")
	@ApplicationScoped
	public static class Manager extends AbstractDominoManager<Post> {
		private static final long serialVersionUID = 1L;

		public static final ThreadLocal<DateFormat> MONTH_LABEL_FORMAT = new ThreadLocal<DateFormat>() {
			@Override
			protected DateFormat initialValue() {
				return new SimpleDateFormat("MMMM yyyy"); //$NON-NLS-1$
			}
		};
		public static final ThreadLocal<DateFormat> MONTH_CONVERT_FORMAT = new ThreadLocal<DateFormat>() {
			@Override
			protected DateFormat initialValue() {
				return new SimpleDateFormat("yyyy-MM"); //$NON-NLS-1$
			}
		};

		public static Manager get() {
			Manager existing = (Manager)FrameworkUtils.resolveVariable(Manager.class.getAnnotation(ManagedBean.class).name());
			return existing == null ? new Manager() : existing;
		}

		@SuppressWarnings({ "unchecked"})
		@Override
		public Object getValue(final Object keyObject) {

			if("archiveMonths".equals(keyObject)) { //$NON-NLS-1$
				View view = getDatabase().getView("Posts\\By Month"); //$NON-NLS-1$
				if(view != null) {
					List<Object> months = view.getColumnValues(0);
					List<Map<String, Object>> result = new ArrayList<Map<String, Object>>(months.size());
					for(Object month : months) {
						try {
							Date monthDate = MONTH_CONVERT_FORMAT.get().parse(String.valueOf(month));
							Map<String, Object> node = new HashMap<String, Object>();
							node.put("label", MONTH_LABEL_FORMAT.get().format(monthDate)); //$NON-NLS-1$
							node.put("queryString", month); //$NON-NLS-1$
							result.add(node);
						} catch (ParseException e) {
							throw new RuntimeException(e);
						}
					}
					return result;
				} else {
					return Collections.emptyList();
				}
			} else if("knownTags".equals(keyObject)) { //$NON-NLS-1$
				View view = getDatabase().getView("Tags"); //$NON-NLS-1$
				Set<String> uniques = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);


				uniques.addAll((List<String>)(List<?>)view.getColumnValues(0));


				return new ArrayList<String>(uniques);
			}

			return super.getValue(keyObject);
		}

		@Override
		protected String getViewPrefix() {
			return "Posts\\"; //$NON-NLS-1$
		}

		@Override
		protected Database getDatabase() {
			return AllTests.session.getDatabase(AllTests.MODELS_DB_PATH);
		}
	}

	public static enum PostStatus {
		Draft, Posted;
	}
}
