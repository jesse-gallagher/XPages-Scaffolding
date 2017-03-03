package frostillicus.framework.test.models;

import java.util.Date;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.openntf.domino.*;

import frostillicus.framework.test.AllTests;
import frostillicus.xsp.bean.ApplicationScoped;
import frostillicus.xsp.bean.ManagedBean;
import frostillicus.xsp.model.domino.AbstractDominoManager;
import frostillicus.xsp.model.domino.AbstractDominoModel;
import frostillicus.xsp.util.FrameworkUtils;

public class Comment extends AbstractDominoModel {
	private static final long serialVersionUID = 1L;

	@NotEmpty String authorName;
	@NotEmpty @Email String authorEmailAddress;
	String authorURL;

	@Override
	public void initFromDatabase(final Database database) {
		super.initFromDatabase(database);

		setValue("Form", "Comment"); //$NON-NLS-1$ //$NON-NLS-2$
		Post post = (Post)FrameworkUtils.resolveVariable("post"); //$NON-NLS-1$
		if(post != null) {
			setValue("PostID", post.getValue("PostID")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	@Override
	protected boolean querySave() {
		if(isNew()) {
			setValue("Posted", new Date()); //$NON-NLS-1$
			setValue("CommentID", document().getUniversalID()); //$NON-NLS-1$

			HttpServletRequest req = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
			String remoteAddr = req.getRemoteAddr();
			String userAgent = req.getHeader("User-Agent"); //$NON-NLS-1$
			String referrer = req.getHeader("Referer"); //$NON-NLS-1$
			setValue("HTTP_Referer", referrer); //$NON-NLS-1$
			setValue("HTTP_User_Agent", userAgent); //$NON-NLS-1$
			setValue("Remote_Addr", remoteAddr); //$NON-NLS-1$
		}
		setValue("FullName", getValue("authorName")); //$NON-NLS-1$ //$NON-NLS-2$
		return super.querySave();
	}

	@Override
	protected void postSave() {

	}


	public Post getPost() {
		return (Post)Post.Manager.get().getValue(getValue("PostID")); //$NON-NLS-1$
	}


	@ManagedBean(name="Comments")
	@ApplicationScoped
	public static class Manager extends AbstractDominoManager<Comment> {
		private static final long serialVersionUID = 1L;

		public static Manager get() {
			Manager existing = (Manager)FrameworkUtils.resolveVariable(Manager.class.getAnnotation(ManagedBean.class).name());
			return existing == null ? new Manager() : existing;
		}

		@Override
		protected String getViewPrefix() {
			return "Comments\\"; //$NON-NLS-1$
		}
		@Override
		protected Database getDatabase() {
			return AllTests.session.getDatabase(AllTests.MODELS_DB_PATH);
		}
	}
}