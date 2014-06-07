package frostillicus.xsp.model.servlet.resources;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
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
import com.ibm.domino.das.utils.ErrorHelper;
import com.ibm.domino.services.util.ContentUtil;
import com.ibm.domino.services.util.JsonWriter;

import frostillicus.xsp.model.ModelManager;
import frostillicus.xsp.model.ModelObject;
import frostillicus.xsp.util.FrameworkUtils;

@Path("{managerName}/{key}")
public class ModelResource {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResource(@Context final UriInfo uriInfo,
			@PathParam("managerName") final String managerName, @PathParam("key") final String key,
			@HeaderParam("If-Modified-Since") final String ifModifiedSince,
			@QueryParam("compact") final boolean compact,
			@QueryParam("start") final String startParam, @QueryParam("count") final String countParam,
			@HeaderParam("Range") final String range,
			@QueryParam("hidden") final boolean hidden) {

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

						ResourceUtils.writeModelObject(model, managerName, hidden, writer);
					} else if(resultObject instanceof List) {
						List<? extends ModelObject> modelList = (List<? extends ModelObject>)resultObject;

						writer.startProperty("@type");
						writer.outStringLiteral("collection");
						writer.endProperty();

						// Figure out our looping limits
						int start = 1;
						int count = 20;
						if(StringUtil.isNotEmpty(range) && range.startsWith("items=")) {
							// Then use the header range
							int pos = "items=".length();
							int sep = range.indexOf(pos);
							start = Integer.valueOf(range.substring(pos, sep));
							int last = Integer.valueOf(range.substring(sep+1));
							count = last - start+1;
						} else {
							// Then check the URL
							try {
								start = Integer.parseInt(startParam);
							} catch(NumberFormatException nfe) {
								// Then ignore the params
							}
							try {
								count = Integer.parseInt(countParam);
							} catch(NumberFormatException nfe) {
								// Then ignore the params
							}
						}
						int end = start + count - 1;
						if(end > modelList.size()) { end = modelList.size(); }
						if(end < 1) { end = 1; }
						if(start > modelList.size()) { start = modelList.size(); }
						if(start < 1) { start = 1; }

						String rangeHeader = ContentUtil.getContentRangeHeaderString(start, end, modelList.size());
						response_.getMetadata().add("Content-Range", rangeHeader);

						writer.startProperty("@entries");
						writer.startArray();
						for(int i = start-1; i < end; i++) {
							ModelObject model = modelList.get(i);
							writer.startArrayItem();
							writer.startObject();

							ResourceUtils.writeModelObject(model, managerName, hidden, writer);

							writer.endObject();
							writer.endArrayItem();
						}
						writer.endArray();
						writer.endProperty();	// @entries
					} else {
						writer.startProperty("@type");
						writer.outStringLiteral("unknown");
						writer.endProperty();
					}

					writer.endObject();

					writer.flush();

					streamWriter.close();
				} catch(JsonException je) {
					je.printStackTrace();
					throw new WebApplicationException(je);
				} catch(NotesException ne) {
					ne.printStackTrace();
					throw new WebApplicationException(ne);
				} catch(Throwable t) {
					t.printStackTrace();
					throw new WebApplicationException(t);
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
	public Response patchResource(final String requestEntity, @Context final UriInfo uriInfo,
			@PathParam("managerName") final String managerName, @PathParam("key") final String key,
			@HeaderParam("If-Unmodified-Since") final String ifUnmodifiedSince) {

		Object resultObject = findContextObject(uriInfo, managerName, key);
		if(!(resultObject instanceof ModelObject)) {
			throw new IllegalArgumentException("PATCH only applies to individual model objects");
		}

		ModelObject model = (ModelObject)resultObject;

		ifUnmodifiedSince(model, ifUnmodifiedSince);

		return ResourceUtils.updateModelObject(requestEntity, model, false);
	}

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response putResource(final String requestEntity, @Context final UriInfo uriInfo,
			@PathParam("managerName") final String managerName, @PathParam("key") final String key,
			@HeaderParam("If-Unmodified-Since") final String ifUnmodifiedSince) {

		Object resultObject = findContextObject(uriInfo, managerName, key);
		if(!(resultObject instanceof ModelObject)) {
			throw new IllegalArgumentException("PUT only applies to individual model objects");
		}

		ModelObject model = (ModelObject)resultObject;

		ifUnmodifiedSince(model, ifUnmodifiedSince);

		return ResourceUtils.updateModelObject(requestEntity, model, true);
	}

	@DELETE
	public Response deleteResource(@Context final UriInfo uriInfo,
			@PathParam("managerName") final String managerName, @PathParam("key") final String key,
			@HeaderParam("If-Unmodified-Since") final String ifUnmodifiedSince) {

		Object resultObject = findContextObject(uriInfo, managerName, key);
		if(!(resultObject instanceof ModelObject)) {
			throw new IllegalArgumentException("DELETE only applies to individual model objects");
		}

		ModelObject model = (ModelObject)resultObject;

		ifUnmodifiedSince(model, ifUnmodifiedSince);

		if(!model.delete()) {
			throw new WebApplicationException(ErrorHelper.createErrorResponse("Object not deleted.", Response.Status.INTERNAL_SERVER_ERROR));
		}

		ResponseBuilder builder = Response.ok();
		Response response = builder.build();
		return response;
	}


	/* **********************************************************************
	 * Internal utility methods
	 ************************************************************************/

	// Based on ExtLib's DocumentResource#ifModifiedSince
	private static String ifModifiedSince(final ModelObject model, final String ifModifiedSince) {
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
	// Based on ExtLib's DocumentResource#ifUnmodifiedSince
	private static void ifUnmodifiedSince(final ModelObject model, final String ifUnmodifiedSince) {
		if(ifUnmodifiedSince == null) {
			return;
		}

		Date modified = model.lastModified();
		if(modified != null) {
			String lastModifiedHeader = org.apache.http.impl.cookie.DateUtils.formatDate(modified);
			if(lastModifiedHeader != null) {
				if(!ifUnmodifiedSince.equalsIgnoreCase(lastModifiedHeader)) {
					try {
						Date ifUnmodifiedSinceDate = org.apache.http.impl.cookie.DateUtils.parseDate(ifUnmodifiedSince);
						if(modified.after(ifUnmodifiedSinceDate)) {
							throw new WebApplicationException(Response.Status.PRECONDITION_FAILED);
						}
					} catch(DateParseException e) {
						throw new WebApplicationException(Response.Status.PRECONDITION_FAILED);
					}
				}
			}
		}
	}

	public Object findContextObject(final UriInfo uriInfo, final String managerName, final String key) {
		try {
			Database database = FrameworkUtils.getDatabase();
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
}
