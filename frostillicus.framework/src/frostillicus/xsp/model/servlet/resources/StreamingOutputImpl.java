package frostillicus.xsp.model.servlet.resources;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

public abstract class StreamingOutputImpl implements StreamingOutput {
	Response response_ = null;

	public void setResponse(final Response response) {
		response_ = response;
	}

}
