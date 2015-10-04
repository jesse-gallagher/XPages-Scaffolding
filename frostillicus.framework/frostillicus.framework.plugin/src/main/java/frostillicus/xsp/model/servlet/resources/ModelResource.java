package frostillicus.xsp.model.servlet.resources;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;

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

import org.apache.http.impl.cookie.DateParseException;
import org.openntf.domino.Database;

import com.ibm.commons.util.StringUtil;
import com.ibm.domino.services.util.ContentUtil;
import com.ibm.domino.services.util.JsonWriter;

import frostillicus.xsp.model.AbstractModelList;
import frostillicus.xsp.model.ModelManager;
import frostillicus.xsp.model.ModelObject;
import frostillicus.xsp.model.ModelUtils;
import frostillicus.xsp.util.FrameworkUtils;

/**
 * @since 1.0
 */
@Path("{managerName}/{key}")
public class ModelResource {

	@SuppressWarnings("unchecked")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResource(@Context final UriInfo uriInfo,
			@PathParam("managerName") final String managerName, @PathParam("key") final String key,
			@HeaderParam("If-Modified-Since") final String ifModifiedSince,
			@QueryParam("compact") final boolean compact,
			@QueryParam("start") final String startParam, @QueryParam("count") final String countParam,
			@HeaderParam("Range") final String range,
			@QueryParam("hidden") final boolean hidden) {

		try {
			Object contextObject = findContextObject(uriInfo, managerName, key, null);
			StreamingOutputImpl streamingJsonEntity;
			if(contextObject instanceof ModelObject) {
				streamingJsonEntity = getModel((ModelObject)contextObject, managerName, ifModifiedSince, hidden, compact);

			} else if(contextObject instanceof AbstractModelList) {
				streamingJsonEntity = getModelCollection((AbstractModelList<? extends ModelObject>)contextObject, managerName, hidden, compact,range, startParam, countParam);
			} else {
				throw new IllegalArgumentException("collectionKey must represent a model object or collection");
			}

			ResponseBuilder builder = Response.ok();
			builder.type(MediaType.APPLICATION_JSON_TYPE).entity(streamingJsonEntity);
			Response response = builder.build();
			streamingJsonEntity.setResponse(response);
			return response;
		} catch(Throwable t) {
			return ResourceUtils.createErrorResponse(t);
		}
	}

	@GET
	@Path("{collectionKey}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResourceByKey(@Context final UriInfo uriInfo,
			@PathParam("managerName") final String managerName, @PathParam("key") final String key,
			@PathParam("collectionKey") final String collectionKey,
			@HeaderParam("If-Modified-Since") final String ifModifiedSince,
			@QueryParam("compact") final boolean compact,
			@QueryParam("start") final String startParam, @QueryParam("count") final String countParam,
			@HeaderParam("Range") final String range,
			@QueryParam("hidden") final boolean hidden) {

		try {
			Object contextObject = findContextObject(uriInfo, managerName, key, collectionKey);
			if(!(contextObject instanceof ModelObject)) {
				throw new IllegalArgumentException("collectionKey must represent a model object in the collection");
			}

			StreamingOutputImpl streamingJsonEntity = getModel((ModelObject)contextObject, managerName, ifModifiedSince, hidden, compact);

			ResponseBuilder builder = Response.ok();
			builder.type(MediaType.APPLICATION_JSON_TYPE).entity(streamingJsonEntity);
			Response response = builder.build();
			streamingJsonEntity.setResponse(response);
			return response;
		} catch(Throwable t) {
			return ResourceUtils.createErrorResponse(t);
		}
	}

	@PATCH
	@Consumes(MediaType.APPLICATION_JSON)
	public Response patchResource(final String requestEntity, @Context final UriInfo uriInfo,
			@PathParam("managerName") final String managerName, @PathParam("key") final String key,
			@HeaderParam("If-Unmodified-Since") final String ifUnmodifiedSince) {

		return patchResourceByKey(requestEntity, uriInfo, managerName, key, null, ifUnmodifiedSince);
	}
	@PATCH
	@Path("{collectionKey}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response patchResourceByKey(final String requestEntity, @Context final UriInfo uriInfo,
			@PathParam("managerName") final String managerName, @PathParam("key") final String key,
			@PathParam("collectionKey") final String collectionKey,
			@HeaderParam("If-Unmodified-Since") final String ifUnmodifiedSince) {

		try {
			Object contextObject = findContextObject(uriInfo, managerName, key, collectionKey);
			if(!(contextObject instanceof ModelObject)) {
				throw new IllegalArgumentException("PATCH only applies to individual model objects");
			}
			replaceModel((ModelObject)contextObject, ifUnmodifiedSince, requestEntity);

			return Response.ok().build();
		} catch(Throwable t) {
			return ResourceUtils.createErrorResponse(t);
		}
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response putResource(final String requestEntity, @Context final UriInfo uriInfo,
			@PathParam("managerName") final String managerName, @PathParam("key") final String key,
			@HeaderParam("If-Unmodified-Since") final String ifUnmodifiedSince) {

		return putResourceByKey(requestEntity, uriInfo, managerName, key, null, ifUnmodifiedSince);
	}
	@PUT
	@Path("{collectionKey}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response putResourceByKey(final String requestEntity, @Context final UriInfo uriInfo,
			@PathParam("managerName") final String managerName, @PathParam("key") final String key,
			@PathParam("collectionKey") final String collectionKey,
			@HeaderParam("If-Unmodified-Since") final String ifUnmodifiedSince) {

		try {
			Object contextObject = findContextObject(uriInfo, managerName, key, collectionKey);
			if(!(contextObject instanceof ModelObject)) {
				throw new IllegalArgumentException("PUT only applies to individual model objects");
			}
			replaceModel((ModelObject)contextObject, ifUnmodifiedSince, requestEntity);

			return Response.ok().build();
		} catch(Throwable t) {
			return ResourceUtils.createErrorResponse(t);
		}
	}

	@DELETE
	public Response deleteResource(@Context final UriInfo uriInfo,
			@PathParam("managerName") final String managerName, @PathParam("key") final String key,
			@HeaderParam("If-Unmodified-Since") final String ifUnmodifiedSince) {

		return deleteResourceByKey(uriInfo, managerName, key, null, ifUnmodifiedSince);
	}
	@DELETE
	@Path("{collectionKey}")
	public Response deleteResourceByKey(@Context final UriInfo uriInfo,
			@PathParam("managerName") final String managerName, @PathParam("key") final String key,
			@PathParam("collectionKey") final String collectionKey,
			@HeaderParam("If-Unmodified-Since") final String ifUnmodifiedSince) {

		try {
			Object contextObject = findContextObject(uriInfo, managerName, key, collectionKey);
			if(!(contextObject instanceof ModelObject)) {
				throw new IllegalArgumentException("PUT only applies to individual model objects");
			}
			deleteModel((ModelObject)contextObject, ifUnmodifiedSince);

			return Response.ok().build();
		} catch(Throwable t) {
			return ResourceUtils.createErrorResponse(t);
		}
	}

	/* **********************************************************************
	 * Core action methods
	 ************************************************************************/
	public StreamingOutputImpl getModel(final ModelObject model, final String managerName, final String ifModifiedSince, final boolean hidden, final boolean compact) throws Exception {
		return new StreamingOutputImpl() {
			@Override
			public void write(final OutputStream outputStream) throws IOException, WebApplicationException {
				try {
					OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream);
					JsonWriter writer = new JsonWriter(streamWriter, compact);

					writer.startObject();

					writer.startProperty("@type");
					writer.outStringLiteral("object");
					writer.endProperty();


					String lastModifiedHeader = ifModifiedSince(model, ifModifiedSince);
					if(lastModifiedHeader != null) {
						response_.getMetadata().add("Last-Modified", lastModifiedHeader);
					}

					ResourceUtils.writeModelObject(model, managerName, hidden, writer);

					writer.endObject();

					writer.flush();

					streamWriter.close();
				} catch(Throwable t) {
					if(t instanceof RuntimeException) {
						throw (RuntimeException)t;
					} else {
						throw new RuntimeException(t);
					}
				}
			}
		};
	}

	public StreamingOutputImpl getModelCollection(final AbstractModelList<? extends ModelObject> modelList, final String managerName, final boolean hidden, final boolean compact, final String range, final String startParam, final String countParam) {
		return new StreamingOutputImpl() {
			@Override
			public void write(final OutputStream outputStream) throws IOException, WebApplicationException {
				try {
					OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream);
					JsonWriter writer = new JsonWriter(streamWriter, compact);

					writer.startObject();

					writer.startProperty("@type");
					writer.outStringLiteral("collection");
					writer.endProperty();

					// Figure out our looping limits
					int start = 1;
					int count = 20;
					if(StringUtil.isNotEmpty(range) && range.startsWith("items=")) {
						// Then use the header range
						int pos = "items=".length();
						int sep = range.indexOf('-', pos);
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
					if(!modelList.isEmpty()) {
						for(int i = start-1; i < end; i++) {
							ModelObject model = modelList.get(i);
							writer.startArrayItem();
							writer.startObject();

							ResourceUtils.writeModelObject(model, managerName, hidden, writer);

							writer.endObject();
							writer.endArrayItem();
						}
					}
					writer.endArray();
					writer.endProperty();	// @entries

					writer.endObject();

					writer.flush();

					streamWriter.close();
				} catch(Throwable t) {
					if(t instanceof RuntimeException) {
						throw (RuntimeException)t;
					} else {
						throw new RuntimeException(t);
					}
				}
			}
		};
	}

	public void updateModel(final ModelObject model, final String ifUnmodifiedSince, final String requestEntity) throws Exception {
		ifUnmodifiedSince(model, ifUnmodifiedSince);
		ResourceUtils.updateModelObject(requestEntity, model, true);
	}

	public void replaceModel(final ModelObject model, final String ifUnmodifiedSince, final String requestEntity) throws Exception {
		// TODO actually implement replace
		updateModel(model, ifUnmodifiedSince, requestEntity);
	}

	public void deleteModel(final ModelObject model, final String ifUnmodifiedSince) throws Exception {
		ifUnmodifiedSince(model, ifUnmodifiedSince);
		if(!model.delete()) {
			throw new WebApplicationException(new Exception("Object not deleted."), Response.Status.BAD_REQUEST);
		}
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

	@SuppressWarnings("unchecked")
	public Object findContextObject(final UriInfo uriInfo, final String managerName, final String key, final String collectionKey) {
		try {
			Database database = FrameworkUtils.getDatabase();
			if(database == null) {
				throw new IllegalStateException("Must be run in the context of a database.");
			} else {
				Class<? extends ModelManager<?>> managerClass = ModelUtils.findModelManager(database, managerName);
				if(managerClass == null) {
					throw new NullPointerException("No manager found for name '" + managerName + "'");
				} else {
					ModelManager<?> managerInstance = managerClass.newInstance();
					Object resultObject = managerInstance.getValue(key);
					if(collectionKey == null) {
						return resultObject;
					} else {
						if(resultObject instanceof AbstractModelList) {
							AbstractModelList<? extends ModelObject> modelList = (AbstractModelList<? extends ModelObject>)resultObject;
							return modelList.getByKey(collectionKey);
						} else {
							throw new IllegalArgumentException("collectionKey not allowed for non-collection contexts");
						}
					}
				}
			}
		} catch(Throwable t) {
			if(t instanceof RuntimeException) {
				throw (RuntimeException)t;
			} else {
				throw new RuntimeException(t);
			}
		}
	}
}
