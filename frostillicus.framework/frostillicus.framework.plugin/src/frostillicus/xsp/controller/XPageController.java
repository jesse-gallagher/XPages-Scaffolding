package frostillicus.xsp.controller;

import java.io.Serializable;

import javax.faces.event.PhaseEvent;

public interface XPageController extends Serializable {
	public void beforePageLoad() throws Exception;

	public void afterPageLoad() throws Exception;

	public void afterRestoreView(PhaseEvent event) throws Exception;

	public void beforeRenderResponse(PhaseEvent event) throws Exception;

	public void afterRenderResponse(PhaseEvent event) throws Exception;

	/**
	 * Sets the navigation path property, for use in layout
	 * 
	 * @param navigationPath
	 *            String matching the pageTreeNode's selection property
	 */
	public void setNavigationPath(String navigationPath);

	/**
	 * Gets the navigation path, for use in layout
	 * 
	 * @return String navigation path programmatic name in a Navigator to
	 *         highlight
	 */
	public String getNavigationPath();

	public boolean isMobile();

	public void setMobile(boolean mobile);

}