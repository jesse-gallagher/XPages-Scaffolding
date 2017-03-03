package frostillicus.xsp.controller;

import java.io.PrintStream;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @since 1.0
 */
public abstract class BasicXAgentController extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	@Override
	public void beforeRenderResponse(final PhaseEvent event) throws Exception {
		super.beforeRenderResponse(event);

		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExternalContext externalContext = facesContext.getExternalContext();
		HttpServletResponse res = (HttpServletResponse)externalContext.getResponse();
		HttpServletRequest req = (HttpServletRequest)externalContext.getRequest();

		try {
			execute(facesContext, req, res);
		} catch(Throwable t) {
			t.printStackTrace();
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			res.setContentType("text/plain");
			t.printStackTrace(new PrintStream(res.getOutputStream()));
		} finally {
			facesContext.responseComplete();
		}
	}

	public abstract void execute(final FacesContext facesContext, final HttpServletRequest req, final HttpServletResponse res) throws Exception;
}
