package frostillicus.xsp.model.servlet.resources;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import frostillicus.xsp.bean.ManagedBean;
import frostillicus.xsp.model.ModelManager;

import org.openntf.domino.*;
import org.openntf.domino.utils.Factory;
import org.openntf.domino.design.*;

import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.json.JsonGenerator;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.domino.osgi.core.context.ContextInfo;


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

	public static Response buildJSONResponse(final Object result) {
		String resultJson;
		try {
			resultJson = JsonGenerator.toJson(JsonJavaFactory.instance, result, false);
		} catch(Exception e) {
			resultJson = "\"" + e.toString().replace("\"", "\\\"") + "\"";
		}

		ResponseBuilder builder = Response.ok();
		builder.type(MediaType.APPLICATION_JSON_TYPE).entity(resultJson);
		Response response = builder.build();

		return response;
	}

	public static Object toJSONFriendly(final Object input, final boolean topLevel) {
		String type = null;
		Object result = null;
		if(input instanceof Iterable) {
			List<Object> listResult = new ArrayList<Object>();
			for(Object obj : (Iterable<?>)input) {
				listResult.add(toJSONFriendly(obj, false));
			}
			result = listResult;
		} else if(input instanceof Map) {
			type = "map";
			Map<String, Object> mapResult = new HashMap<String, Object>();
			for(Map.Entry<?, ?> entry : ((Map<?, ?>)input).entrySet()) {
				mapResult.put(String.valueOf(entry.getKey()), toJSONFriendly(entry.getValue(), false));
			}
			result = mapResult;
		} else {
			if(input instanceof DateTime) {
				if (((DateTime) input).getDateOnly().length() == 0) {
					// Time Only
					type = "timeonly";
					result = timeOnlyToString(((DateTime)input).toJavaDate());
				} else if (((DateTime) input).getTimeOnly().length() == 0) {
					// Date Only
					type = "dateonly";
					result = dateOnlyToString(((DateTime)input).toJavaDate());
				} else {
					type = "datetime";
					result = dateToString(((DateTime)input).toJavaDate(), true);
				}
			} else if(input instanceof Date) {
				type = "datetime";
				result = dateToString((Date)input, true);
			} else if(input instanceof Number) {
				result = input;
			} else if(input instanceof Boolean) {
				result = input;
			} else {
				result = String.valueOf(input);
			}
		}
		if(topLevel && type != null) {
			Map<String, Object> wrappedResult = new HashMap<String, Object>();
			wrappedResult.put("@type", type);
			wrappedResult.put("@value", result);
			return wrappedResult;
		} else {
			return result;
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

	private static String dateToString(final DateTime value, final boolean utc) throws IOException {
		return dateToString(value.toJavaDate(), utc);
	}
}