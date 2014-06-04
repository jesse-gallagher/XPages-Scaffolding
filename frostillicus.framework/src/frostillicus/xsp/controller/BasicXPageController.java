package frostillicus.xsp.controller;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;

import com.ibm.xsp.extlib.util.ExtLibUtil;

public class BasicXPageController implements XPageController {
	private static final long serialVersionUID = 1L;

	public BasicXPageController() { }

	@Override
	public void beforePageLoad() throws Exception { }
	@Override
	public void afterPageLoad() throws Exception { }

	@Override
	public void beforeRenderResponse(PhaseEvent event) throws Exception { }
	@Override
	public void afterRenderResponse(PhaseEvent event) throws Exception { }

	@Override
	public void afterRestoreView(PhaseEvent event) throws Exception { }

	protected static Object resolveVariable(String varName) {
		return ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), varName);
	}
}
