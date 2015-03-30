package frostillicus.xsp.model.servlet.resources;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

@SuppressWarnings("unused")
@Path("managers")
public class ManagersResource {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getManagers(@Context final UriInfo uriInfo) {
		ResponseBuilder builder = Response.ok();
		builder.type(MediaType.APPLICATION_JSON_TYPE).entity("[ 'asked for managers' ]");
		Response response = builder.build();

		return response;
	}
}
