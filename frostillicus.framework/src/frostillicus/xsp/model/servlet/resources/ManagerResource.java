package frostillicus.xsp.model.servlet.resources;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import javax.ws.rs.GET;
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
import com.ibm.domino.das.utils.ErrorHelper;

import frostillicus.xsp.model.ModelManager;
import frostillicus.xsp.util.FrameworkUtils;

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
				Class<? extends ModelManager<?>> managerClass = ResourceUtils.findManager(database, managerName);
				if(managerClass != null) {
					result.put("status", "success");
					result.put("managerClass", managerClass.getName());
				} else {
					result.put("status", "failure");
					result.put("message", "No manager found for name '" + managerName + "'");
				}
			}

			return ResourceUtils.buildJSONResponse(result, false);
		} catch (Throwable e) {
			throw new WebApplicationException(ErrorHelper.createErrorResponse(e));
			//return ResourceUtils.buildJSONResponse(e.toString(), false);
		}
	}
}