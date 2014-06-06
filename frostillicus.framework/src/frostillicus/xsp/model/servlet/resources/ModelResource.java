package frostillicus.xsp.model.servlet.resources;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
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
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.commons.util.io.json.JsonParser;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.designer.runtime.domino.adapter.servlet.LCDAdapterHttpSession;
import com.ibm.domino.osgi.core.context.ContextInfo;
import com.ibm.domino.xsp.module.nsf.NotesContext;

import frostillicus.xsp.model.ModelManager;
import frostillicus.xsp.model.ModelObject;
import frostillicus.xsp.model.Properties;
import frostillicus.xsp.model.servlet.ModelServlet;

@SuppressWarnings("unused")
@Path("{managerName}/{key}")
public class ModelResource {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResource(@Context final UriInfo uriInfo, @PathParam("managerName") final String managerName, @PathParam("key") final String key) {
		return new ModelObjectRunner(uriInfo, managerName, key) {
			@SuppressWarnings("unchecked")
			@Override
			protected void handle(final UriInfo uriInfo, final ModelManager<?> manager, final Object resultObject, final Map<String, Object> result) throws Exception {
				result.put("@status", "success");
				if(resultObject instanceof ModelObject) {
					ModelObject model = (ModelObject)resultObject;
					result.put("@type", "object");

					writeSystemProperties(model, result);

					for(String property : model.propertyNames()) {
						Object val = model.getValue(property);
						result.put(property, ResourceUtils.toJSONFriendly(val, true));
					}
				} else if(resultObject instanceof List) {
					result.put("@type", "collection");

					List<? extends ModelObject> modelList = (List<? extends ModelObject>)resultObject;
					List<Map<String, Object>> entries = new ArrayList<Map<String, Object>>();
					for(ModelObject model : modelList) {
						Map<String, Object> vals = new LinkedHashMap<String, Object>();

						writeSystemProperties(model, vals);

						for(String property : model.columnPropertyNames()) {
							Object val = model.getValue(property);
							vals.put(property, ResourceUtils.toJSONFriendly(val, true));
						}
						entries.add(vals);
					}
					result.put("@entries", entries);
				} else {
					result.put("@type", "unknown");
				}
			}

		}.call();
	}

	@PATCH
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response patchResource(final String requestEntity, @Context final UriInfo uriInfo, @PathParam("managerName") final String managerName, @PathParam("key") final String key) {
		return new ModelObjectRunner(uriInfo, managerName, key) {
			@Override
			protected void handle(final UriInfo uriInfo, final ModelManager<?> manager, final Object resultObject, final Map<String, Object> result) throws Exception {
				result.put("@status", "success");
				if(resultObject instanceof ModelObject) {
					ModelObject model = (ModelObject)resultObject;

					JsonJavaObject jsonItems = null;
					StringReader reader = new StringReader(requestEntity);
					try {
						jsonItems = (JsonJavaObject)JsonParser.fromJson(JsonJavaFactory.instanceEx, reader);
					} finally {
						reader.close();
					}

					Properties props = model.getClass().getAnnotation(Properties.class);
					boolean exhaustive = props != null && props.exhaustive();
					Set<String> propertyNames = exhaustive ? model.propertyNames() : null;

					List<String> updatedProperties = new ArrayList<String>();
					for(Map.Entry<String, Object> entry : jsonItems.entrySet()) {
						if((!exhaustive || propertyNames.contains(entry.getKey()) ) && !model.isReadOnly(entry.getKey())) {
							model.setValue(entry.getKey(), entry.getValue());
							updatedProperties.add(entry.getKey());
						}
					}
					model.save();
					result.put("@updatedProperties", updatedProperties);
				} else {
					throw new IllegalArgumentException("PATCH only applies to individual model objects");
				}
			}

		}.call();
	}

	protected void writeSystemProperties(final ModelObject model, final Map<String, Object> result) {
		result.put("@unid", model.getId());
		result.put("@modelClass", model.getClass().getName());
	}

	private static abstract class ModelObjectRunner implements Callable<Response> {
		private final UriInfo uriInfo_;
		private final String managerName_;
		private final String key_;

		public ModelObjectRunner(final UriInfo uriInfo, final String managerName, final String key) {
			uriInfo_ = uriInfo;
			managerName_ = managerName;
			key_ = key;
		}

		@Override
		public Response call() {
			try {
				Map<String, Object> result = new LinkedHashMap<String, Object>();
				Database database = ResourceUtils.getDatabase();
				if(database == null) {
					result.put("@status", "error");
					result.put("message", "Must be run in the context of a database.");
				} else {
					Class<? extends ModelManager<?>> managerClass = ResourceUtils.findManager(database, managerName_);
					if(managerClass == null) {
						result.put("@status", "failure");
						result.put("message", "No manager found for name '" + managerName_ + "'");
					} else {
						ModelManager<?> managerInstance = managerClass.newInstance();
						Object resultObject = managerInstance.getValue(key_);
						handle(uriInfo_, managerInstance, resultObject, result);
					}
				}
				return ResourceUtils.buildJSONResponse(result);
			} catch (Throwable e) {
				e.printStackTrace();
				return ResourceUtils.buildJSONResponse(e.toString());
			}
		}

		protected abstract void handle(UriInfo uriInfo, ModelManager<?> manager, Object resultObject, Map<String, Object> result) throws Exception;
	}
}
