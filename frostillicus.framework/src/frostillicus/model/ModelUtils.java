package frostillicus.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.openntf.domino.*;

import com.ibm.xsp.extlib.util.ExtLibUtil;

public enum ModelUtils {
	;

	public static Database getDatabase(final String server, final String filePath) {
		Map<String, Object> requestScope = ExtLibUtil.getRequestScope();
		String key = "database-" + server + "!!" + filePath;
		if (!requestScope.containsKey(key)) {
			Session session = (Session)ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "session");
			requestScope.put(key, session.getDatabase(server, filePath));
		}

		return (Database) requestScope.get(key);
	}

	public static boolean isUnid(final String value) {
		if (value.length() != 32)
			return false;
		return isHex(value);
	}

	public static boolean isHex(final String value) {
		String chk = value.trim().toLowerCase();
		for (int i = 0; i < chk.length(); i++) {
			char c = chk.charAt(i);
			boolean isHexDigit = Character.isDigit(c) || Character.isWhitespace(c) || c == 'a' || c == 'b' || c == 'c' || c == 'd' || c == 'e' || c == 'f';

			if (!isHexDigit) {
				return false;
			}

		}
		return true;
	}

	public static void publishException(final Exception e) {
		StringWriter out = new StringWriter();
		PrintWriter outWriter = new PrintWriter(out);
		e.printStackTrace(outWriter);
		outWriter.flush();

		FacesContext facesContext = FacesContext.getCurrentInstance();
		FacesMessage message = new FacesMessage(out.toString());
		message.setSeverity(FacesMessage.SEVERITY_ERROR);
		facesContext.addMessage("", message);
	}

	public static String strRightBack(final String input, final String delimiter) {
		return input.substring(input.lastIndexOf(delimiter) + delimiter.length());
	}

	public static String strLeft(final String input, final String delimiter) {
		return input.substring(0, input.indexOf(delimiter));
	}

	@SuppressWarnings("unchecked")
	public static ModelManager<?> findModelManager(final FacesContext context, final String managerName) throws IOException {
		Object managerObject = context.getApplication().getVariableResolver().resolveVariable(context, managerName);
		if(managerObject != null && !(managerObject instanceof ModelManager)) {
			throw new IllegalArgumentException("managerObject must be an instance of " + ModelManager.class.getName());
		}

		// If the object is null, assume that the managerName is a class name and instantiate anew
		if(managerObject == null) {
			try {
				Class<ModelManager<?>> managerClass = (Class<ModelManager<?>>)Class.forName(managerName);
				managerObject = managerClass.newInstance();
			} catch(ClassNotFoundException cnfe) {
				IOException ioe = new IOException("Error while instantiating manager object for name '" + managerName + "'");
				ioe.initCause(cnfe);
				throw ioe;
			} catch(InstantiationException ie) {
				IOException ioe = new IOException("Error while instantiating manager object for name '" + managerName + "'");
				ioe.initCause(ie);
				throw ioe;
			} catch(IllegalAccessException iae) {
				IOException ioe = new IOException("Error while instantiating manager object for name '" + managerName + "'");
				ioe.initCause(iae);
				throw ioe;
			}
		}

		return (ModelManager<?>)managerObject;
	}

	public static SortedSet<String> stringSet(final Collection<String> input) {
		SortedSet<String> result = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		if(input != null) result.addAll(input);
		return result;
	}
}