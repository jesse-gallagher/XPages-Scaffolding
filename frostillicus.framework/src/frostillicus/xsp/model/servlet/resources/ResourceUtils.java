package frostillicus.xsp.model.servlet.resources;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import lotus.domino.NotesException;
import frostillicus.xsp.bean.ManagedBean;
import frostillicus.xsp.model.ModelManager;

import org.openntf.domino.*;
import org.openntf.domino.utils.Factory;
import org.openntf.domino.design.*;

import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonGenerator;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.domino.commons.json.JsonMimeEntityAdapter;
import com.ibm.domino.commons.mime.MimeEntityHelper;
import com.ibm.domino.osgi.core.context.ContextInfo;
import com.ibm.domino.services.util.JsonWriter;
import com.ibm.xsp.model.domino.DominoUtils;


public enum ResourceUtils {
	;

	public static Session getSession() {
		lotus.domino.Session lotusSession = ContextInfo.getUserSession();
		Session session = Factory.fromLotus(lotusSession, Session.SCHEMA, null);
		return session;
	}

	public static Database getDatabase() {
		lotus.domino.Database lotusDatabase = ContextInfo.getUserDatabase();
		if(lotusDatabase != null) {
			Session session = getSession();
			Database database = Factory.fromLotus(lotusDatabase, Database.SCHEMA, session);
			return database;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static Class<? extends ModelManager<?>> findManager(final Database database, final String managerName) {
		if(StringUtil.isEmpty(managerName)) { return null; }

		DatabaseDesign design = database.getDesign();
		ClassLoader loader = design.getDatabaseClassLoader(Thread.currentThread().getContextClassLoader());
		for(String className : design.getJavaResourceClassNames()) {
			try {
				Class<?> loadedClass = loader.loadClass(className);
				ManagedBean beanAnnotation = loadedClass.getAnnotation(ManagedBean.class);
				if(beanAnnotation != null) {
					if(managerName.equals(beanAnnotation.name())) {
						if(ModelManager.class.isAssignableFrom(loadedClass)) {
							return (Class<? extends ModelManager<?>>)loadedClass;
						} else {
							return null;
						}
					}
				}
			} catch(ClassNotFoundException cnfe) {
				// This likely shouldn't happen
				throw new RuntimeException(cnfe);
			}
		}
		return null;
	}

	public static Response buildJSONResponse(final Object result, final boolean compact) {
		String resultJson;
		try {
			resultJson = JsonGenerator.toJson(JsonJavaFactory.instance, result, compact);
		} catch(Exception e) {
			resultJson = "\"" + e.toString().replace("\"", "\\\"") + "\"";
		}

		ResponseBuilder builder = Response.ok();
		builder.type(MediaType.APPLICATION_JSON_TYPE).entity(resultJson);
		Response response = builder.build();

		return response;
	}

	public static void writeProperty(final Object input, final boolean topLevel, final JsonWriter writer) throws NotesException, IOException, JsonException {

		if(input instanceof Iterable) {
			writer.startArray();
			for(Object obj : (Iterable<?>)input) {
				writer.startArrayItem();
				writeProperty(obj, false, writer);
				writer.endArrayItem();
			}
			writer.endArray();
		} else if(input instanceof Map) {
			writer.startObject();
			if(topLevel) {
				writer.startProperty("type");
				writer.outStringLiteral("map");
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

			Session session = itemValue.getAncestorSession();
			boolean isConvertMime = session.isConvertMime();
			String itemName = itemValue.getName();

			List<JsonMimeEntityAdapter> adapters = new ArrayList<JsonMimeEntityAdapter>();
			lotus.domino.Document rawDoc = Factory.toLotus(itemValue.getAncestorDocument());
			MimeEntityHelper helper = new MimeEntityHelper(rawDoc, itemName);
			lotus.domino.MIMEEntity entity = helper.getFirstMimeEntity(true);
			if (entity != null) {
				JsonMimeEntityAdapter.addEntityAdapter(adapters, entity);
				entity.recycle();
			}
			writer.outLiteral(adapters);

			itemValue.getAncestorDocument().closeMIMEEntities();

			writer.endProperty();

			writer.endObject();

			session.setConvertMime(isConvertMime);
		} else {
			if(input instanceof DateTime) {
				writer.startObject();
				writer.startProperty("type");
				writer.outStringLiteral("datetime");
				writer.endProperty();
				writer.startProperty("value");
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
				writer.startProperty("value");
				writer.outStringLiteral(dateToString((Date)input, true));
				writer.endProperty();
				writer.endObject();
			} else if(input instanceof Number) {
				writer.outNumberLiteral(((Number)input).doubleValue());
			} else if(input instanceof Boolean) {
				writer.outBooleanLiteral((Boolean)input);
			} else if(input == null) {
				writer.outNull();
			} else {
				writer.outStringLiteral(String.valueOf(input));
			}
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