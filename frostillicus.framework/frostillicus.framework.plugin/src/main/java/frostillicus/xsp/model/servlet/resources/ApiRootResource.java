package frostillicus.xsp.model.servlet.resources;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * @since 1.0
 */
@SuppressWarnings("unused")
@Path("")
public class ApiRootResource {

	@GET
	public Response getManagers(@Context final UriInfo uriInfo) {
		ResponseBuilder builder = Response.ok();
		builder.type(MediaType.APPLICATION_JSON_TYPE).entity("hey there");
		Response response = builder.build();

		return response;
	}
}
