package frostillicus.xsp.controller;


import javax.faces.context.FacesContext;

import frostillicus.xsp.util.FrameworkUtils;
import lotus.domino.*;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.domino.wrapped.DominoDocument;

import java.util.Map;

public class BasicDocumentController extends BasicXPageController implements DocumentController {
	private static final long serialVersionUID = 1L;

	@Override
	public void queryNewDocument() throws Exception { }
	@Override
	public void postNewDocument() throws Exception { }
	@Override
	public void queryOpenDocument() throws Exception { }
	@Override
	public void postOpenDocument() throws Exception {
		DominoDocument doc = this.getDoc();
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		viewScope.put("$REF", doc.getValue("$REF"));
	}
	@Override
	public void querySaveDocument() throws Exception { }
	@Override
	public void postSaveDocument() throws Exception { }

	@Override
	public String save() throws Exception {
		DominoDocument doc = this.getDoc();

		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		viewScope.put("$REF", doc.getValue("$REF"));

		boolean isNewNote = doc.isNewNote();
		if(doc.save()) {
			Database database = doc.getParentDatabase();
			if(database.isFTIndexed()) {
				database.updateFTIndex(false);
			}
			FrameworkUtils.addMessage("confirmation", doc.getValue("Form") + " " + (isNewNote ? "created" : "updated") + " successfully.");
			return "xsp-success";
		} else {
			FrameworkUtils.addMessage("error", "Save failed");
			return "xsp-failure";
		}
	}
	public String cancel() throws Exception {
		return "xsp-cancel";
	}
	public String delete() throws Exception {
		DominoDocument doc = this.getDoc();

		String formName = (String)doc.getValue("Form");
		doc.getDocument(true).remove(true);
		FrameworkUtils.addMessage("confirmation", formName + " deleted.");
		return "xsp-success";
	}

	@Override
	public String getDocumentId() {
		try {
			return this.getDoc().getDocument().getUniversalID();
		} catch(Exception e) { return ""; }
	}

	@Override
	public boolean isEditable() { return this.getDoc().isEditable(); }

	protected DominoDocument getDoc() {
		return (DominoDocument)ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "doc");
	}
}
