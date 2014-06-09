package frostillicus.xsp.model.servlet.resources;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.validation.ConstraintViolationException;
import javax.validation.ConstraintViolation;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import lotus.domino.NotesException;
import frostillicus.xsp.model.ModelObject;
import frostillicus.xsp.model.Properties;

import org.openntf.domino.*;

import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonGenerator;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.commons.util.io.json.JsonParser;
import com.ibm.domino.commons.json.JsonMimeEntityAdapter;
import com.ibm.domino.commons.mime.MimeEntityHelper;
import com.ibm.domino.das.utils.ErrorHelper;
import com.ibm.domino.services.util.JsonWriter;
import com.ibm.xsp.model.domino.DominoUtils;


public enum ResourceUtils {
	;

	public static Response createErrorResponse(final Throwable t) {
		return createErrorResponse(t, null);
	}
	public static Response createErrorResponse(final Throwable t, final Response.Status status) {
		Response.Status useStatus = status == null ? Response.Status.INTERNAL_SERVER_ERROR : status;

		if(t instanceof WebApplicationException) {
			WebApplicationException e = (WebApplicationException)t;
			if(e.getResponse() != null) {
				return e.getResponse();
			} else {
				return ErrorHelper.createErrorResponse(e, useStatus);
			}
		} else if(t instanceof ConstraintViolationException) {
			ConstraintViolationException e = (ConstraintViolationException)t;
			useStatus = Response.Status.BAD_REQUEST;

			try {
				// Build a response similar in format to IBM's ErrorHelper, but including the constraint violations
				ResponseBuilder builder = Response.status(useStatus);
				StringWriter writer = new StringWriter();
				JsonWriter w = new JsonWriter(writer, false);

				try {
					w.startObject();

					ErrorHelper.writeProperty(w, "code", useStatus.getStatusCode());
					ErrorHelper.writeProperty(w, "text", useStatus.getReasonPhrase());

					ErrorHelper.writeProperty(w, "message", "Constraint violation");

					w.startProperty("violations");
					w.startArray();
					for(ConstraintViolation<?> v : e.getConstraintViolations()) {
						w.startArrayItem();
						w.startObject();

						ErrorHelper.writeProperty(w, "property", v.getPropertyPath().toString());
						ErrorHelper.writeProperty(w, "message", v.getMessage());

						w.endObject();
						w.endArrayItem();
					}
					w.endArray();
					w.endProperty();
				} finally {
					w.endObject();
				}

				builder.type(MediaType.APPLICATION_JSON_TYPE).entity(writer.getBuffer().toString());
				return builder.build();
			} catch(Throwable t2) {
				return createErrorResponse(t2);
			}
		} else if(t instanceof Exception) {
			return ErrorHelper.createErrorResponse((Exception)t, useStatus);
		} else {
			return ErrorHelper.createErrorResponse(t);
		}
	}

	public static Response createJSONResponse(final Object result, final boolean compact) {
		String resultJson;
		try {
			resultJson = JsonGenerator.toJson(JsonJavaFactory.instance, result, compact);
		} catch(Exception e) {
			return createErrorResponse(e);
		}

		ResponseBuilder builder = Response.ok();
		builder.type(MediaType.APPLICATION_JSON_TYPE).entity(resultJson);
		Response response = builder.build();

		return response;
	}

	public static void writeModelObject(final ModelObject model, final String managerName, final boolean includeSystem, final JsonWriter writer) throws NotesException, IOException, JsonException {
		writeSystemProperties(model, writer);

		for(String property : model.propertyNames(includeSystem)) {
			writer.startProperty(property);
			writeProperty(model.getValue(property), true, writer);
			writer.endProperty();
		}
	}

	protected static void writeSystemProperties(final ModelObject model, final JsonWriter writer) throws NotesException, IOException, JsonException {
		writer.startProperty("@modelClass");
		writer.outStringLiteral(model.getClass().getName());
		writer.endProperty();

		String position = model.viewRowPosition();
		if(StringUtil.isNotEmpty(position)) {
			writer.startProperty("@category");
			writer.outBooleanLiteral(model.category());
			writer.endProperty();

			writer.startProperty("@position");
			writer.outStringLiteral(position);
			writer.endProperty();
		}

		if(!model.category()) {
			writer.startProperty("@unid");
			writer.outStringLiteral(model.getId());
			writer.endProperty();

			writer.startProperty("@relativeUri");
			writer.outStringLiteral("/" + URLEncoder.encode(model.getClass().getName(), "UTF-8") + "/" + URLEncoder.encode(model.getId(), "UTF-8"));
			writer.endProperty();

			writer.startProperty("@authors");
			writeProperty(model.modifiedBy(), false, writer);
			writer.endProperty();

			writer.startProperty("@created");
			writeProperty(model.created(), false, writer);
			writer.endProperty();

			writer.startProperty("@modified");
			writeProperty(model.lastModified(), false, writer);
			writer.endProperty();
		}
	}

	@SuppressWarnings("unchecked")
	public static void writeProperty(final Object input, final boolean topLevel, final JsonWriter writer) throws NotesException, IOException, JsonException {

		if(input instanceof Iterable) {
			Object first = ((Iterable<?>)input).iterator().next();
			if(first instanceof DateTime) {
				// Then it's a DateTime item
				writer.startObject();
				writer.startProperty("type");
				writer.outStringLiteral("datetime");
				writer.endProperty();
				writer.startProperty("data");
				writer.startArray();
				for(DateTime dt : (Iterable<DateTime>)input) {
					writer.startArrayItem();
					if (dt.getDateOnly().length() == 0) {
						// Time Only
						writer.outStringLiteral(timeOnlyToString(dt.toJavaDate()));
					} else if (dt.getTimeOnly().length() == 0) {
						// Date Only
						writer.outStringLiteral(dateOnlyToString(dt.toJavaDate()));
					} else {
						writer.outStringLiteral(dateToString(dt.toJavaDate(), true));
					}
					writer.endArrayItem();
				}
				writer.endArray();
				writer.endProperty();
				writer.endObject();
			} else {
				writer.startArray();
				for(Object obj : (Iterable<?>)input) {
					writer.startArrayItem();
					writeProperty(obj, false, writer);
					writer.endArrayItem();
				}
				writer.endArray();
			}
		} else if(input instanceof Map) {
			writer.startObject();
			if(topLevel) {
				writer.startProperty("type");
				writer.outStringLiteral("object");
				writer.endProperty();
				writer.startProperty("value");
				writer.startObject();
			}
			for(Map.Entry<?, ?> entry : ((Map<?, ?>)input).entrySet()) {
				writer.startProperty(String.valueOf(entry.getKey()));
				writeProperty(entry.getValue(), false, writer);
				writer.endProperty();
			}
			if(topLevel) {
				writer.endObject();
			}
			writer.endObject();
		} else if(input instanceof DominoUtils.HtmlConverterWrapper) {
			DominoUtils.HtmlConverterWrapper converter = (DominoUtils.HtmlConverterWrapper)input;
			writer.startObject();
			writer.startProperty("type");
			writer.outStringLiteral("richtext");
			writer.endProperty();
			writer.startProperty("contentType");
			writer.outStringLiteral("text/html");
			writer.endProperty();

			writer.startProperty("value");
			writer.outStringLiteral(converter.getConverterText());
			writer.endProperty();
			List<String> attachments = converter.getReferneceUrls();
			if(!attachments.isEmpty()) {
				writer.startProperty("attachments");
				writer.startArray();
				for (String attachment : converter.getReferneceUrls()) {
					writer.startArrayItem();
					writer.startObject();
					writer.startProperty("href");
					writer.outStringLiteral(attachment);
					writer.endProperty();
					writer.endObject();
					writer.endArrayItem();
				}
				writer.endArray();
				writer.endProperty();
			}
			writer.endObject();
		} else if(input instanceof Item && ((Item)input).getType() == Item.MIME_PART) {
			// TODO clean up this mess and make sure it doesn't crash the server

			Item itemValue = (Item)input;

			writer.startObject();
			writer.startProperty("type");
			writer.outStringLiteral("multipart");
			writer.endProperty();
			writer.startProperty("content");

			String itemName = itemValue.getName();

			List<JsonMimeEntityAdapter> adapters = new ArrayList<JsonMimeEntityAdapter>();
			MimeEntityHelper helper = new MimeEntityHelper(itemValue.getAncestorDocument(), itemName);
			MIMEEntity entity = (MIMEEntity)helper.getFirstMimeEntity(true);
			if (entity != null) {
				JsonMimeEntityAdapter.addEntityAdapter(adapters, entity);
			}
			writer.outLiteral(adapters);

			itemValue.getAncestorDocument().closeMIMEEntities();

			writer.endProperty();

			writer.endObject();
		} else if(input instanceof DateTime) {
			writer.startObject();
			writer.startProperty("type");
			writer.outStringLiteral("datetime");
			writer.endProperty();
			writer.startProperty("data");
			if (((DateTime) input).getDateOnly().length() == 0) {
				// Time Only
				writer.outStringLiteral(timeOnlyToString(((DateTime)input).toJavaDate()));
			} else if (((DateTime) input).getTimeOnly().length() == 0) {
				// Date Only
				writer.outStringLiteral(dateOnlyToString(((DateTime)input).toJavaDate()));
			} else {
				writer.outStringLiteral(dateToString(((DateTime)input).toJavaDate(), true));
			}
			writer.endProperty();
			writer.endObject();
		} else if(input instanceof Date) {
			writer.startObject();
			writer.startProperty("type");
			writer.outStringLiteral("datetime");
			writer.endProperty();
			writer.startProperty("data");
			writer.outStringLiteral(dateToString((Date)input, true));
			writer.endProperty();
			writer.endObject();
		} else if(input instanceof Number) {
			writer.outNumberLiteral(((Number)input).doubleValue());
		} else if(input instanceof Boolean) {
			writer.outBooleanLiteral((Boolean)input);
		} else if(input == null) {
			writer.outNull();
		} else if(input instanceof ModelObject) {
			writer.startObject();
			writer.startProperty("type");
			writer.outStringLiteral("object");
			writer.endProperty();
			writer.startProperty("value");
			writer.startObject();
			writeModelObject((ModelObject)input, input.getClass().getName(), false, writer);
			writer.endObject();
			writer.endProperty();
			writer.endObject();
		} else {
			writer.outStringLiteral(String.valueOf(input));
		}
	}

	// TODO make replace actually work
	protected static void updateModelObject(final String requestEntity, final ModelObject model, final boolean replace) throws Exception {

		JsonJavaObject jsonItems = null;
		StringReader reader = new StringReader(requestEntity);
		try {
			jsonItems = (JsonJavaObject)JsonParser.fromJson(JsonJavaFactory.instanceEx, reader);
		} finally {
			reader.close();
		}

		Properties props = model.getClass().getAnnotation(Properties.class);
		boolean exhaustive = props != null && props.exhaustive();
		Set<String> propertyNames = exhaustive ? model.propertyNames(false) : null;

		List<String> updatedProperties = new ArrayList<String>();
		for(Map.Entry<String, Object> entry : jsonItems.entrySet()) {
			if((!exhaustive || propertyNames.contains(entry.getKey()) ) && !model.isReadOnly(entry.getKey())) {
				model.setValue(entry.getKey(), entry.getValue());
				updatedProperties.add(entry.getKey());
			}
		}
		if(!model.save()) {
			throw new WebApplicationException(ErrorHelper.createErrorResponse("Model not saved.", Response.Status.BAD_REQUEST));
		}
	}

	// From ExtLib's com.ibm.domino.services.util.JsonWriter

	private static SimpleDateFormat ISO8601_UTC = null;
	private static SimpleDateFormat ISO8601_DT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	private static SimpleDateFormat ISO8601_DO = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat ISO8601_TO = new SimpleDateFormat("HH:mm:ss");

	private static String dateToString(final Date value, final boolean utc) {
		String result = null;

		if ( utc ) {
			if ( ISO8601_UTC == null ) {
				// Initialize the UTC formatter once
				TimeZone tz = TimeZone.getTimeZone("UTC");
				ISO8601_UTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
				ISO8601_UTC.setTimeZone(tz);
			}

			result = ISO8601_UTC.format(value);
		}
		else {
			result = ISO8601_DT.format(value);
		}

		return result;
	}
	private static String dateOnlyToString(final Date javaDate) {
		return ISO8601_DO.format(javaDate);
	}

	private static String timeOnlyToString(final Date javaDate) {
		return ISO8601_TO.format(javaDate);
	}

	@SuppressWarnings("unused")
	private static String dateToString(final DateTime value, final boolean utc) throws IOException {
		return dateToString(value.toJavaDate(), utc);
	}
}