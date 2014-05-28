package frostillicus.controller;

public interface DocumentController extends XPageController {
	public void queryNewDocument() throws Exception;
	public void postNewDocument() throws Exception;
	public void queryOpenDocument() throws Exception;
	public void postOpenDocument() throws Exception;
	public void querySaveDocument() throws Exception;
	public void postSaveDocument() throws Exception;

	public String getDocumentId();
	public boolean isEditable();
}
