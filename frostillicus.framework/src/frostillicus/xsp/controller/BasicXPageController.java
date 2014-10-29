package frostillicus.xsp.controller;

import javax.faces.event.PhaseEvent;

import frostillicus.xsp.util.FrameworkUtils;

public class BasicXPageController implements XPageController {
	private static final long serialVersionUID = 1L;
	private String navigationPath;
	private boolean showSearch = false;
	private boolean mobile; // set in ControlleringViewHandler

	private ComponentMap components_ = new ComponentMap("components");

	public BasicXPageController() {
	}

	@Override
	public void beforePageLoad() throws Exception {
	}

	@Override
	public void afterPageLoad() throws Exception {
		components_.initialize();
	}

	@Override
	public void beforeRenderResponse(final PhaseEvent event) throws Exception {
		components_.initialize();
	}

	@Override
	public void afterRenderResponse(final PhaseEvent event) throws Exception {
	}

	@Override
	public void afterRestoreView(final PhaseEvent event) throws Exception {
	}

	public void setNavigationPath(String navigationPath) {
		this.navigationPath = navigationPath;
	}

	public String getNavigationPath() {
		return navigationPath;
	}

	public void setShowSearch(boolean showSearch) {
		this.showSearch = showSearch;
	}

	public boolean isShowSearch() {
		return showSearch;
	}

	public boolean isMobile() {
		return mobile;
	}

	public void setMobile(boolean mobile) {
		this.mobile = mobile;
	}

	public String save() throws Exception {
		if (FrameworkUtils.getViewRoot().save()) {
			return "xsp-success";
		}
		return "xsp-failure";
	}

	public ComponentMap getComponents() {
		return components_;
	}
}
