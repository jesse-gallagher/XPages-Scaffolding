package frostillicus.xsp.controller;

import javax.faces.event.PhaseEvent;

import frostillicus.xsp.util.FrameworkUtils;

public class BasicXPageController implements XPageController {
	private static final long serialVersionUID = 1L;

	public BasicXPageController() { }

	@Override
	public void beforePageLoad() throws Exception { }
	@Override
	public void afterPageLoad() throws Exception { }

	@Override
	public void beforeRenderResponse(final PhaseEvent event) throws Exception { }
	@Override
	public void afterRenderResponse(final PhaseEvent event) throws Exception { }

	@Override
	public void afterRestoreView(final PhaseEvent event) throws Exception { }

	public String save() throws Exception {
		if(FrameworkUtils.getViewRoot().save()) {
			return "xsp-success";
		}
		return "xsp-failure";
	}
}
