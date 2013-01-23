package frostillicus.controller;

import java.io.Serializable;
import javax.faces.event.PhaseEvent;

public interface XPageController extends Serializable {
	public void beforePageLoad() throws Exception;
	public void afterPageLoad() throws Exception;

	public void afterRestoreView(PhaseEvent event) throws Exception;

	public void beforeRenderResponse(PhaseEvent event) throws Exception;
	public void afterRenderResponse(PhaseEvent event) throws Exception;
}