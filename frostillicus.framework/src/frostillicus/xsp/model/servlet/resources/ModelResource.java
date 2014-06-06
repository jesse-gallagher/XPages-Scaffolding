package frostillicus.xsp.model.servlet.resources;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URI;
import java.util.Date;
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
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import lotus.domino.NotesException;

import org.apache.http.impl.cookie.DateParseException;
import org.openntf.domino.Database;

import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonGenerator;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.commons.util.io.json.JsonParser;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.designer.runtime.domino.adapter.servlet.LCDAdapterHttpSession;
import com.ibm.domino.osgi.core.context.ContextInfo;
import com.ibm.domino.services.util.JsonWriter;
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
	public Response getResource(@Context final UriInfo uriInfo,
			@PathParam("managerName") final String managerName, @PathParam("key") final String key,
			@HeaderParam("If-Modified-Since") final String ifModifiedSince,
			@QueryParam("compact") final boolean compact) {

		Map<String, Object> result = new LinkedHashMap<String, Object>();
		ResponseBuilder builder = Response.ok();

		StreamingOutputImpl streamingJsonEntity = new StreamingOutputImpl() {
			@SuppressWarnings("unchecked")
			@Override
			public void write(final OutputStream outputStream) throws IOException, WebApplicationException {
				try {
					OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream);
					JsonWriter writer = new JsonWriter(streamWriter, compact);

					Object resultObject = findContextObject(uriInfo, managerName, key);

					writer.startObject();

					if(resultObject instanceof ModelObject) {
						ModelObject model = (ModelObject)resultObject;

						writer.startProperty("@type");
						writer.outStringLiteral("object");
						writer.endProperty();


						String lastModifiedHeader = ifModifiedSince(model, ifModifiedSince);
						if(lastModifiedHeader != null) {
							response_.getMetadata().add("Last-Modified", lastModifiedHeader);
						}

						writeSystemProperties(model, writer);

						for(String property : model.propertyNames()) {
							writer.startProperty(property);
							ResourceUtils.writeProperty(model.getValue(property), compact, writer);
							writer.endProperty();
						}
					} else if(resultObject instanceof List) {

						writer.startProperty("@type");
						writer.outStringLiteral("collection");
						writer.endProperty();

						List<? extends ModelObject> modelList = (List<? extends ModelObject>)resultObject;
						List<Map<String, Object>> entries = new ArrayList<Map<String, Object>>();
						for(ModelObject model : modelList) {
							Map<String, Object> vals = new LinkedHashMap<String, Object>();

							writeSystemProperties(model, writer);

							for(String property : model.columnPropertyNames()) {
								writer.startProperty(property);
								ResourceUtils.writeProperty(model.getValue(property), compact, writer);
								writer.endProperty();
							}
							entries.add(vals);
						}
						writer.startProperty("@entries");
						writer.outArrayLiteral(entries);
						writer.endProperty();
					} else {
						writer.startProperty("@type");
						writer.outStringLiteral("unknown");
						writer.endProperty();
					}

					writer.endObject();

					writer.flush();

					streamWriter.close();
				} catch(JsonException je) {
					throw new WebApplicationException(je);
				} catch(NotesException ne) {
					throw new WebApplicationException(ne);
				}
			}
		};

		builder.type(MediaType.APPLICATION_JSON_TYPE).entity(streamingJsonEntity);
		Response response = builder.build();
		streamingJsonEntity.setResponse(response);
		return response;
	}

	@PATCH
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response patchResource(final String requestEntity, @Context final UriInfo uriInfo, @PathParam("managerName") final String managerName, @PathParam("key") final String key) {
		return new ModelObjectRunner(uriInfo, managerName, key, false) {
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

	protected void writeSystemProperties(final ModelObject model, final JsonWriter writer) throws IOException {
		writer.startProperty("@unid");
		writer.outStringLiteral(model.getId());
		writer.endProperty();

		writer.startProperty("@modelClass");
		writer.outStringLiteral(model.getClass().getName());
		writer.endProperty();
	}



	/* **********************************************************************
	 * Internal utility methods
	 ************************************************************************/
	// Based on ExtLib's DocumentResource#ifModifiedSince
	protected static String ifModifiedSince(final ModelObject model, final String ifModifiedSince) {
		String lastModifiedHeader = null;
		if(StringUtil.isNotEmpty(ifModifiedSince)) {
			Date lastModified = model.lastModified();
			if(lastModified != null) {
				lastModifiedHeader = org.apache.http.impl.cookie.DateUtils.formatDate(lastModified);
				if(lastModifiedHeader != null) {
					if(ifModifiedSince.equalsIgnoreCase(lastModifiedHeader)) {
						throw new WebApplicationException(Response.Status.NOT_MODIFIED);
					}
					try {
						Date ifModifiedSinceDate = org.apache.http.impl.cookie.DateUtils.parseDate(ifModifiedSince);
						if(ifModifiedSinceDate.equals(lastModified) || ifModifiedSinceDate.after(lastModified)) {
							throw new WebApplicationException(Response.Status.NOT_MODIFIED);
						}
					} catch(DateParseException dpe) {
						// NOP
					}
				}
			}
		}
		return lastModifiedHeader;
	}

	public Object findContextObject(final UriInfo uriInfo, final String managerName, final String key) {
		try {
			Map<String, Object> result = new LinkedHashMap<String, Object>();
			Database database = ResourceUtils.getDatabase();
			if(database == null) {
				throw new IllegalStateException("Must be run in the context of a database.");
			} else {
				Class<? extends ModelManager<?>> managerClass = ResourceUtils.findManager(database, managerName);
				if(managerClass == null) {
					throw new NullPointerException("No manager found for name '" + managerName + "'");
				} else {
					ModelManager<?> managerInstance = managerClass.newInstance();
					return managerInstance.getValue(key);
				}
			}
		} catch (Throwable e) {
			throw new WebApplicationException(e);
		}
	}

	/* **********************************************************************
	 * Internal utility class to save on code
	 ************************************************************************/
	private static abstract class ModelObjectRunner implements Callable<Response> {
		private final UriInfo uriInfo_;
		private final String managerName_;
		private final String key_;
		private final boolean compact_;

		public ModelObjectRunner(final UriInfo uriInfo, final String managerName, final String key, final boolean compact) {
			uriInfo_ = uriInfo;
			managerName_ = managerName;
			key_ = key;
			compact_ = compact;
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
				return ResourceUtils.buildJSONResponse(result, compact_);
			} catch(WebApplicationException e) {
				throw e;
			} catch (Throwable e) {
				e.printStackTrace();
				return ResourceUtils.buildJSONResponse(e.toString(), compact_);
			}
		}

		protected abstract void handle(UriInfo uriInfo, ModelManager<?> manager, Object resultObject, Map<String, Object> result) throws Exception;
	}
}
