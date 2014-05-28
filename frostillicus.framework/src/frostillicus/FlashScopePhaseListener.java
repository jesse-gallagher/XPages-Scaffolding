package frostillicus;


import java.util.Map;
import javax.faces.context.*;
import javax.faces.event.*;
import javax.servlet.http.HttpServletRequest;

@SuppressWarnings("serial")
public class FlashScopePhaseListener implements PhaseListener {

	@Override
	public void afterPhase(PhaseEvent event) {
		FacesContext facesContext = event.getFacesContext();
		ExternalContext externalContext = facesContext.getExternalContext();
		HttpServletRequest request = (HttpServletRequest)externalContext.getRequest();
		if(!request.getMethod().equals("POST")) {
			Map<?, ?> flashScope = (Map<?, ?>)facesContext.getApplication().createValueBinding("#{flashScope}").getValue(facesContext);
			flashScope.clear();
		}
	}
	@Override
	public void beforePhase(PhaseEvent event) { }
	@Override
	public PhaseId getPhaseId() { return PhaseId.RENDER_RESPONSE; }
}
