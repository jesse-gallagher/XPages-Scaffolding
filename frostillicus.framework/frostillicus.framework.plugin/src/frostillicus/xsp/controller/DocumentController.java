package frostillicus.xsp.controller;

public interface DocumentController extends XPageController {
	public void queryNewDocument() throws Exception;
	public void postNewDocument() throws Exception;
	public void queryOpenDocument() throws Exception;
	public void postOpenDocument() throws Exception;
	public void querySaveDocument() throws Exception;
	public void postSaveDocument() throws Exception;

	public String getDocumentId();
	
	/**
	 * Returns brief edit history details using format:<br/>
	 * Created by Paul Withers at 25/10/2013 17:20:00<br/>
	 * Last edited by Paul Withers at 25/10/2013 17:20:00
	 * 
	 * @return String summary of creator and last editor
	 */
	public String getEditSummary();

	/**
	 * Gets the page to use for editing the main document. Or return an empty string to use the current page
	 * 
	 * @return String of page to open to edit the current document
	 */
	public String getEditPage();
	public boolean isEditable();	
	
	/**
	 * @return boolean whether or not the datasource is deleted
	 */
	public boolean isDeleted();

	/**
	 * @return boolean whether or not the datasource is a new doc
	 */
	public boolean isNewNote();

	/**
	 * @return boolean whether Edit button should be hidden
	 */
	public boolean preventEdit();
}
