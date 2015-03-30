package org.openntf.xsp.extlib.query;

import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

public class ClientIdEqualsFilter extends QueryFilter {
	protected static Logger _logger;
	static {
		_logger = Logger.getLogger(ClientIdEqualsFilter.class.getName());
	}

	public ClientIdEqualsFilter(final String propertyName, final Object content) {
		super(propertyName, content);
	}

	@Override
	public boolean matches(final UIComponent component, final FacesContext context) {
		String clientId = component.getClientId(context);
		//_logger.log(Level.FINEST, "Comparing component client id " + clientId + " to " + getContent());
		if(clientId == null || getContent() == null) {
			return false;
		} else {
			return component.getClientId(context).equalsIgnoreCase(getContent().toString());
		}
	}

}
