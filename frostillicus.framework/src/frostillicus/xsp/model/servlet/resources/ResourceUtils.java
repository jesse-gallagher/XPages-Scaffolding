package frostillicus.xsp.model.servlet.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public static Object toJSONFriendly(final Object input) {
		if(input instanceof Iterable) {
			List<Object> result = new ArrayList<Object>();
			for(Object obj : (Iterable<?>)input) {
				result.add(toJSONFriendly(obj));
			}
			return result;
		} else if(input instanceof Map) {
			Map<String, Object> result = new HashMap<String, Object>();
			for(Map.Entry<?, ?> entry : ((Map<?, ?>)input).entrySet()) {
				result.put(String.valueOf(entry.getKey()), toJSONFriendly(entry.getValue()));
			}
			return result;
		} else {
			if(input instanceof DateTime) {
				return ((DateTime)input).toJavaDate().toString();
			} else if(input instanceof Number) {
				return input;
			} else if(input instanceof Boolean) {
				return input;
			}
			return String.valueOf(input);
		}
	}
}