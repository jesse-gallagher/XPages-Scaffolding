package frostillicus.xsp.controller;

import java.util.Map;

import javax.faces.context.FacesContext;

import org.openntf.domino.Document;

import com.ibm.xsp.designer.context.XSPUrl;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.domino.wrapped.DominoDocument;

import frostillicus.xsp.util.FrameworkUtils;

public class BasicDocumentController extends BasicXPageController implements DocumentController {
	private static final long serialVersionUID = 1L;
	private String cancelPath;

	/**
	 * @return String path to use for xsp-cancel. Allows computation of going to
	 *         DeletedDocuments or relevant view
	 */
	public String getCancelPath() {
		return cancelPath;
	}

	/**
	 * @param cancelPath
	 *            String to set cancelPath
	 */
	public void setCancelPath(String cancelPath) {
		this.cancelPath = cancelPath;
	}

	@Override
	public void queryNewDocument() throws Exception {
	}

	@Override
	public void postNewDocument() throws Exception {
	}

	@Override
	public void queryOpenDocument() throws Exception {
	}

	@Override
	public void postOpenDocument() throws Exception {
		DominoDocument doc = this.getDoc();
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		viewScope.put("$REF", doc.getValue("$REF"));
	}

	@Override
	public void querySaveDocument() throws Exception {
	}

	@Override
	public void postSaveDocument() throws Exception {
	}

	@Override
	public String save() throws Exception {
		DominoDocument doc = this.getDoc();

		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		viewScope.put("$REF", doc.getValue("$REF"));

		boolean isNewNote = doc.isNewNote();
		if (doc.save()) {
			lotus.domino.Database database = doc.getParentDatabase();
			if (database.isFTIndexed()) {
				database.updateFTIndex(false);
			}
			FrameworkUtils.addMessage("confirmation", doc.getValue("Form") + " " + (isNewNote ? "created" : "updated") + " successfully.");
			return "xsp-success";
		} else {
			FrameworkUtils.addMessage("error", "Save failed");
			return "xsp-failure";
		}
	}

	public String save(boolean goToView) throws Exception {
		String retVal_ = save();
		if ("xsp-success".equals(retVal_)) {
			if (!goToView) {
				retVal_ = "xsp-current";
			}
		}
		return retVal_;
	}

	public String cancel() throws Exception {
		return "xsp-cancel";
	}

	public String delete() throws Exception {
		DominoDocument doc = this.getDoc();

		String formName = (String) doc.getValue("Form");
		doc.getDocument(true).remove(true);
		FrameworkUtils.addMessage("confirmation", formName + " deleted.");
		return "xsp-success";
	}

	@Override
	public String getDocumentId() {
		try {
			return this.getDoc().getDocument().getUniversalID();
		} catch (Exception e) {
			return "";
		}
	}

	public String getCurrentPath() {
		XSPUrl currUrl = ExtLibUtil.getXspContext().getUrl();
		return currUrl.getSiteRelativeAddress(ExtLibUtil.getXspContext()) + "?documentId=" + getDocumentId() + "&action=openDocument";
	}

	@Override
	public boolean isEditable() {
		return this.getDoc().isEditable();
	}

	protected DominoDocument getDoc() {
		return (DominoDocument) ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "doc");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see frostillicus.controller.DocumentController#isDeleted() boolean if
	 * MarkForDeletion field is "1" (String)
	 */
	public boolean isDeleted() {
		try {
			Document doc = (Document) getDoc().getDocument();
			if (doc.isDeleted() || !doc.isValid()) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see frostillicus.controller.DocumentController#getEditSummary()
	 */
	public String getEditSummary() {
		String retVal_ = "";
		try {
			if (getDoc().isNewNote()) {
				return "";
			}
			StringBuilder s = new StringBuilder();
			Document mainDoc = (Document) getDoc().getDocument();
			s.append("Created by ");
			s.append(mainDoc.getAuthors().firstElement());
			s.append(" at ");
			s.append(mainDoc.getCreatedDate().toLocaleString());
			s.append("<br/>Last Updated By ");
			s.append(mainDoc.getAuthors().lastElement());
			s.append(" at ");
			s.append(mainDoc.getLastModifiedDate().toLocaleString());
			retVal_ = s.toString();
		} catch (Throwable t) {

		}
		return retVal_;
	}

	/*
	 * Default, to edit using the current page (non-Javadoc)
	 * 
	 * @see frostillicus.controller.DocumentController#getEditPage()
	 */
	public String getEditPage() {
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see frostillicus.controller.DocumentController#preventEdit()
	 */
	public boolean preventEdit() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see frostillicus.controller.DocumentController#isNewNote()
	 */
	public boolean isNewNote() {
		boolean retVal_ = false;
		try {
			retVal_ = this.getDoc().isNewNote();
		} catch (Throwable t) {

		}
		return retVal_;
	}
}
