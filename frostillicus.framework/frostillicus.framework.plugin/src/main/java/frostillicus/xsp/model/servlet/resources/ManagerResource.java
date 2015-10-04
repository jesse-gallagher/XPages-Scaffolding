package frostillicus.xsp.model.servlet.resources;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.HashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.openntf.domino.Database;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonGenerator;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.domino.commons.util.UriHelper;
import com.ibm.domino.das.utils.ErrorHelper;

import frostillicus.xsp.model.ModelManager;
import frostillicus.xsp.model.ModelObject;
import frostillicus.xsp.model.ModelUtils;
import frostillicus.xsp.util.FrameworkUtils;

/**
 * @since 1.0
 */
@SuppressWarnings("unused")
@Path("{managerName}")
public class ManagerResource {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getManager(@Context final UriInfo uriInfo, @PathParam("managerName") final String managerName) {
		try {
			Map<String, Object> result = new HashMap<String, Object>();
			Database database = FrameworkUtils.getDatabase();
			if(database == null) {
				result.put("status", "error");
				result.put("message", "Must be run in the context of a database.");
			} else {
				Class<? extends ModelManager<?>> managerClass = ModelUtils.findModelManager(database, managerName);
				if(managerClass == null) {
					result.put("status", "failure");
					result.put("message", "No manager found for name '" + managerName + "'");
				} else {
					result.put("status", "success");
					result.put("managerClass", managerClass.getName());
				}
			}

			return ResourceUtils.createJSONResponse(result, false);
		} catch (Throwable e) {
			return ResourceUtils.createErrorResponse(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createModel(final String requestEntity, @Context final UriInfo uriInfo, @PathParam("managerName") final String managerName) {

		Database database = FrameworkUtils.getDatabase();
		Class<? extends ModelManager<?>> managerClass = ModelUtils.findModelManager(database, managerName);
		if(managerClass == null) {
			return ErrorHelper.createErrorResponse("Manager '" + managerName + "' not found.", Response.Status.NOT_FOUND);
		}

		URI location;
		try {
			ModelManager<? extends ModelObject> manager = managerClass.newInstance();
			ModelObject model = manager.create();
			ResourceUtils.updateModelObject(requestEntity, model, false);

			location = UriHelper.appendPathSegment(uriInfo.getAbsolutePath(), model.getId());
		} catch(Throwable t) {
			return ResourceUtils.createErrorResponse(t);
		}

		ResponseBuilder builder = Response.created(location);
		Response response = builder.build();
		return response;
	}
}