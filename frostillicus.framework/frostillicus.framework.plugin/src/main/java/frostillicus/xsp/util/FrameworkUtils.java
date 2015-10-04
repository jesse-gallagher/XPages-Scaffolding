package frostillicus.xsp.util;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.openntf.domino.*;
import org.openntf.domino.utils.Factory;
import org.openntf.domino.utils.XSPUtil;

import com.ibm.domino.osgi.core.context.ContextInfo;
import com.ibm.xsp.component.UIViewRootEx2;

import lotus.domino.NotesException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * @since 1.0
 */
public enum FrameworkUtils {
	;

	public static Database getDatabase(final String server, final String filePath) {
		Map<String, Object> requestScope = getRequestScope();
		String key = "database-" + server + "!!" + filePath;
		if (!requestScope.containsKey(key)) {
			Session session = getSession();
			requestScope.put(key, session.getDatabase(server, filePath));
		}

		return (Database) requestScope.get(key);
	}

	public static Session getSession() {
		if(isFaces()) {
			Object session = resolveVariable("session");
			if(!(session instanceof Session)) {
				session = Factory.getWrapperFactory().fromLotus((lotus.domino.Session)session, Session.SCHEMA, null);
			}
			return (Session)session;
		} else {
			lotus.domino.Session lotusSession = ContextInfo.getUserSession();
			Session session;
			if(lotusSession == null) {
				session = Factory.getSession();
			} else {
				session = Factory.fromLotus(lotusSession, Session.SCHEMA, null);
			}
			session.setConvertMime(false);
			return session;
		}
	}
	public static Session getSessionAsSigner() {
		if(isFaces()) {
			return XSPUtil.getCurrentSessionAsSigner();
		} else {
			return getSession();
		}
	}

	public static Database getDatabase() {
		if(isFaces()) {
			lotus.domino.Database lotusDatabase = (lotus.domino.Database)resolveVariable("database");
			Database database;
			if(lotusDatabase instanceof Database) {
				database = (Database)lotusDatabase;
			} else {
				try {
					Session session;
					lotus.domino.Session lotusSession = lotusDatabase.getParent();
					if(lotusSession instanceof Session) {
						session = (Session)lotusSession;
					} else {
						session = Factory.getWrapperFactory().fromLotus(lotusSession, Session.SCHEMA, null);
					}
					database = Factory.getWrapperFactory().fromLotus(lotusDatabase, Database.SCHEMA, session);
				} catch(NotesException ne) {
					throw new RuntimeException(ne);
				}
			}
			return database;
		} else {
			Session session = getSession();
			lotus.domino.Database lotusDatabase = ContextInfo.getUserDatabase();
			Database database;
			if(lotusDatabase == null) {
				database = session.getCurrentDatabase();
			} else {
				database = Factory.fromLotus(lotusDatabase, Database.SCHEMA, session);
			}
			return database;
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> getApplicationScope() {
		if(isFaces()) {
			return (Map<String, Object>)resolveVariable("applicationScope");
		} else {
			return new HashMap<String, Object>();
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> getSessionScope() {
		if(isFaces()) {
			return (Map<String, Object>)resolveVariable("applicationScope");
		} else {
			return new HashMap<String, Object>();
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> getViewScope() {
		if(isFaces()) {
			return (Map<String, Object>)resolveVariable("viewScope");
		} else {
			return new HashMap<String, Object>();
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> getRequestScope() {
		if(isFaces()) {
			return (Map<String, Object>)resolveVariable("requestScope");
		} else {
			return new HashMap<String, Object>();
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<Object, Object> getFlashScope() {
		if(isFaces()) {
			return (Map<Object, Object>)resolveVariable("flashScope");
		} else {
			return new HashMap<Object, Object>();
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String> getParam() {
		if(isFaces()) {
			return (Map<String, String>)resolveVariable("param");
		} else {
			return new HashMap<String, String>();
		}
	}

	public static boolean isFaces() {
		return FacesContext.getCurrentInstance() != null;
	}

	public static Object getBindingValue(final String ref) {
		if(isFaces()) {
			FacesContext context = FacesContext.getCurrentInstance();
			Application application = context.getApplication();
			return application.createValueBinding(ref).getValue(context);
		} else {
			return null;
		}
	}

	public static void setBindingValue(final String ref, final Object newObject) {
		if(isFaces()) {
			FacesContext context = FacesContext.getCurrentInstance();
			Application application = context.getApplication();
			ValueBinding binding = application.createValueBinding(ref);
			binding.setValue(context, newObject);
		}
	}

	public static Object resolveVariable(final String varName) {
		if(isFaces()) {
			FacesContext context = FacesContext.getCurrentInstance();
			return context.getApplication().getVariableResolver().resolveVariable(context, varName);
		} else {
			return null;
		}
	}

	public static String getUserName() {
		return getSession().getEffectiveUserName();
	}

	public static UIViewRootEx2 getViewRoot() {
		return (UIViewRootEx2) resolveVariable("view");
	}

	public static String pluralize(final String input) {
		if (input.endsWith("s")) {
			return input + "es";
		} else if (input.endsWith("y")) {
			return input.substring(0, input.length() - 2) + "ies";
		}
		return input + "s";
	}

	public static String singularize(final String input) {
		if (input.endsWith("ses")) {
			return input.substring(0, input.length() - 2);
		} else if (input.endsWith("ies")) {
			return input.substring(0, input.length() - 3) + "y";
		} else if (input.endsWith("s")) {
			return input.substring(0, input.length() - 1);
		}
		return input;
	}

	public String fetchURL(final String urlString) throws Exception {
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestProperty("User-Agent", "Firefox/2.0");

		BufferedReader in = new BufferedReader(new InputStreamReader((InputStream) conn.getContent()));
		StringWriter resultWriter = new StringWriter();
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			resultWriter.write(inputLine);
		}
		in.close();

		return resultWriter.toString().replace("<HTTP-EQUIV", "<meta http-equiv");

	}

	public static String xor(final String input, final Vector<?> key) {
		StringBuilder output = new StringBuilder();

		for (int i = 0; i < input.length(); i++) {
			int character = input.codePointAt(i);
			int keyNode = ((Double) key.get(i % key.size())).intValue();

			int onePass = character ^ keyNode;

			output.append((char) onePass);
		}

		return output.toString();
	}

	public static String xmlEncode(final String text) {
		StringBuilder result = new StringBuilder();

		for (int i = 0; i < text.length(); i++) {
			char currentChar = text.charAt(i);
			if (!((currentChar >= 'a' && currentChar <= 'z') || (currentChar >= 'A' && currentChar <= 'Z') || (currentChar >= '0' && currentChar <= '9'))) {
				result.append("&#" + (int) currentChar + ";");
			} else {
				result.append(currentChar);
			}
		}

		return result.toString();
	}

	public static String strLeft(final String input, final String delimiter) {
		return input.substring(0, input.indexOf(delimiter));
	}

	public static String strRight(final String input, final String delimiter) {
		return input.substring(input.indexOf(delimiter) + delimiter.length());
	}

	public static String strLeftBack(final String input, final String delimiter) {
		return input.substring(0, input.lastIndexOf(delimiter));
	}

	public static String strLeftBack(final String input, final int chars) {
		return input.substring(0, input.length() - chars);
	}

	public static String strRightBack(final String input, final String delimiter) {
		return input.substring(input.lastIndexOf(delimiter) + delimiter.length());
	}

	public static String strRightBack(final String input, final int chars) {
		return input.substring(input.length() - chars);
	}

	public static List<String> toStringList(final Object columnValue) {
		List<String> result = new Vector<String>();
		if (columnValue.getClass().getName().equals("java.util.Vector")) {
			for (Object reader : (Vector<?>) columnValue) {
				result.add((String) reader);
			}
		} else if (((String) columnValue).length() > 0) {
			result.add((String) columnValue);
		}
		return result;
	}

	public static List<Integer> toIntegerList(final Object columnValue) {
		List<Integer> result = new Vector<Integer>();
		if (columnValue.getClass().getName().equals("java.util.Vector")) {
			for (Object element : (Vector<?>) columnValue) {
				result.add(((Double) element).intValue());
			}
		} else {
			result.add(((Double) columnValue).intValue());
		}
		return result;
	}

	public static int toInteger(final Object columnValue) {
		int result = 0;
		if (columnValue.getClass().getName().equals("java.lang.String")) {
			result = 0;
		} else {
			result = ((Double) columnValue).intValue();
		}
		return result;
	}

	public static Date toDate(final Object columnValue) throws NotesException {
		return ((DateTime) columnValue).toJavaDate();
	}

	public static boolean isSpecialText(final String specialText) {
		return specialText.contains((char)127 + "");
	}

	public static String specialTextDecode(final String specialText, final ViewEntry viewEntry) throws NotesException {
		String result = specialText;
		//if(true) return result;

		String specialStart = (char)127 + "";
		String specialEnd = (char)160 + "";

		// First, find the start and end of the special text
		int start_pos = result.indexOf(specialStart);
		int end_pos = result.indexOf(specialEnd);

		// This is just in case things get out of hand - no need to have broken code
		//	result in an infinite loop on the server
		int loopStopper = 1;
		while (start_pos > -1 && end_pos > start_pos && loopStopper < 100) {
			loopStopper++;

			// "working" holds the text we're going to replace, minus the delimiters
			// "result" holds the text we're going to replace working and the delimiters with
			String working = result.substring(start_pos + 1, end_pos);
			String midResult = "";
			String[] choices;
			int offset, length, parameterCount;

			switch (working.charAt(0)) {
			case 'C':
				// @DocChildren
				parameterCount = Integer.parseInt(working.substring(1, 2));
				switch (parameterCount) {
				case 0:
					midResult = viewEntry.getChildCount() + "";
					break;
				case 1:
					midResult = strRight(working, "=").replaceAll("%", viewEntry.getChildCount() + "");
					break;
				case 2:
					// For convenience, I'll break the string into each option, even if I only use one
					choices = new String[] { "", "" };

					// I can cheat a bit on the first one to find the length
					offset = 0;
					length = Integer.parseInt(strLeft(strRight(working, ";"), "="));
					choices[0] = working.substring(working.indexOf("=", offset) + 1, working.indexOf("=", offset) + 1 + length);

					offset = working.indexOf("=", offset) + 1 + length;
					choices[1] = working.substring(working.indexOf("=", offset) + 1, working.indexOf("=", offset) + 1 + length);

					if (viewEntry.getChildCount() == 0) {
						midResult = choices[0].replaceAll("%", "0");
					} else {
						midResult = choices[1].replaceAll("%", viewEntry.getChildCount() + "");
					}

					break;
				case 3:
					// For convenience, I'll break the string into each option, even if I only use one
					choices = new String[] { "", "", "" };

					// I can cheat a bit on the first one to find the length
					offset = 0;
					length = Integer.parseInt(strLeft(strRight(working, ";"), "="));
					choices[0] = working.substring(working.indexOf("=", offset) + 1, working.indexOf("=", offset) + 1 + length);

					offset = working.indexOf("=", offset) + 2 + length;
					length = Integer.parseInt(working.substring(offset, working.indexOf("=", offset)));
					choices[1] = working.substring(working.indexOf("=", offset) + 1, working.indexOf("=", offset) + 1 + length);

					offset = working.indexOf("=", offset) + 2 + length;
					length = Integer.parseInt(working.substring(offset, working.indexOf("=", offset)));
					choices[2] = working.substring(working.indexOf("=", offset) + 1, working.indexOf("=", offset) + 1 + length);

					if (viewEntry.getChildCount() == 0) {
						midResult = choices[0].replaceAll("%", "0");
					} else if (viewEntry.getChildCount() == 1) {
						midResult = choices[1].replaceAll("%", "1");
					} else {
						midResult = choices[2].replaceAll("%", viewEntry.getChildCount() + "");
					}

					break;
				}
				break;
			case 'D':
				// @DocDescendants
				parameterCount = Integer.parseInt(working.substring(1, 2));
				switch (parameterCount) {
				case 0:
					midResult = viewEntry.getDescendantCount() + "";
					break;
				case 1:
					midResult = strRight(working, "=").replaceAll("%", viewEntry.getDescendantCount() + "");
					break;
				case 2:
					// For convenience, I'll break the string into each option, even if I only use one
					choices = new String[] { "", "" };

					// I can cheat a bit on the first one to find the length
					offset = 0;
					length = Integer.parseInt(strLeft(strRight(working, ";"), "="));
					choices[0] = working.substring(working.indexOf("=", offset) + 1, working.indexOf("=", offset) + 1 + length);

					offset = working.indexOf("=", offset) + 1 + length;
					choices[1] = working.substring(working.indexOf("=", offset) + 1, working.indexOf("=", offset) + 1 + length);

					if (viewEntry.getDescendantCount() == 0) {
						midResult = choices[0].replaceAll("%", "0");
					} else {
						midResult = choices[1].replaceAll("%", viewEntry.getDescendantCount() + "");
					}

					break;
				case 3:
					// For convenience, I'll break the string into each option, even if I only use one
					choices = new String[] { "", "", "" };

					// I can cheat a bit on the first one to find the length
					offset = 0;
					length = Integer.parseInt(strLeft(strRight(working, ";"), "="));
					choices[0] = working.substring(working.indexOf("=", offset) + 1, working.indexOf("=", offset) + 1 + length);

					offset = working.indexOf("=", offset) + 2 + length;
					length = Integer.parseInt(working.substring(offset, working.indexOf("=", offset)));
					choices[1] = working.substring(working.indexOf("=", offset) + 1, working.indexOf("=", offset) + 1 + length);

					offset = working.indexOf("=", offset) + 2 + length;
					length = Integer.parseInt(working.substring(offset, working.indexOf("=", offset)));
					choices[2] = working.substring(working.indexOf("=", offset) + 1, working.indexOf("=", offset) + 1 + length);

					if (viewEntry.getDescendantCount() == 0) {
						midResult = choices[0].replaceAll("%", "0");
					} else if (viewEntry.getDescendantCount() == 1) {
						midResult = choices[1].replaceAll("%", "1");
					} else {
						midResult = choices[2].replaceAll("%", viewEntry.getDescendantCount() + "");
					}

					break;
				}
				break;
			case 'H':
				// @DocLevel
				midResult = (viewEntry.getIndentLevel() + 1) + "";
				break;
			case 'A':
				// @DocNumber
				/* Three forms:
				 * @DocNumber - all levels separated by "."
				 * @DocNumber("") - only the least significant level
				 * @DocNumber(char) - all levels separated by char. Note: the formula accepts a multi-character string, but
				 * 	displays it as just the string, not the doc level
				 */
				parameterCount = Integer.parseInt(working.substring(1, 2));
				switch (parameterCount) {
				case 0:
					midResult = viewEntry.getPosition('.');
					break;
				case 1:
					String delimiter = strRight(working, "=");
					if (delimiter.length() == 0) {
						midResult = strRightBack(viewEntry.getPosition('.'), ".");
					} else if (delimiter.length() > 1) {
						// Mimic formula's weird behavior for multi-character strings
						midResult = delimiter;
					} else {
						midResult = viewEntry.getPosition(delimiter.charAt(0));
					}
					break;
				}
				break;
			case 'J':
				// @DocParentNumber
				// Same as above, just for the parent, so do the same thing and chomp off the last bit
				if (viewEntry.getIndentLevel() == 0) {
					midResult = "";
				} else {
					parameterCount = Integer.parseInt(working.substring(1, 2));
					switch (parameterCount) {
					case 0:
						midResult = strLeftBack(viewEntry.getPosition('.'), ".");
						break;
					case 1:
						String delimiter = strRight(working, "=");
						if (delimiter.length() == 0) {
							midResult = strRightBack(strLeftBack(viewEntry.getPosition('.'), "."), ".");
						} else if (delimiter.length() > 1) {
							// Mimic formula's weird behavior for multi-character strings
							midResult = delimiter;
						} else {
							midResult = strLeftBack(viewEntry.getPosition(delimiter.charAt(0)), delimiter);
						}
						break;
					}
				}
				break;
			case 'B':
				// @DocSiblings
				midResult = (viewEntry.getSiblingCount()) + "";
				break;
			case 'I':
				// @IsCategory
				/* Three forms:
				 * @IsCategory - "*" if it's a category, "" otherwise
				 * @IsCategory(string) - string if it's a category, "" otherwise
				 * @IsCategory(string1, string 2) - string1 if it's a category, string2 otherwise
				 */
				parameterCount = Integer.parseInt(working.substring(1, 2));
				switch (parameterCount) {
				case 0:
					midResult = viewEntry.isCategory() ? "*" : "";
					break;
				case 1:
					midResult = viewEntry.isCategory() ? strRight(working, "=") : "";
					break;
				case 2:
					// For convenience, I'll break the string into each option, even if I only use one
					choices = new String[] { "", "" };
					offset = 0;
					length = Integer.parseInt(strLeft(strRight(working, ";"), "="));
					choices[0] = working.substring(working.indexOf("=", offset) + 1, working.indexOf("=", offset) + 1 + length);

					offset = working.indexOf("=", offset) + 2 + length;
					length = Integer.parseInt(working.substring(offset, working.indexOf("=", offset)));
					choices[1] = working.substring(working.indexOf("=", offset) + 1, working.indexOf("=", offset) + 1 + length);

					midResult = viewEntry.isCategory() ? choices[0] : choices[1];

					break;
				}

				break;
			case 'G':
				// @IsExpandable
				// This is a UI function that changes based on the expanded/collapsed state of the entry in
				//	the Notes client. This kind of behavior could be better done without @functions on the web,
				//	so it's not really worth implementing
				midResult = "";
				break;
			default:
				midResult = working;
				break;
			}

			result = result.replaceAll(specialStart + working + specialEnd, midResult);

			start_pos = result.indexOf(specialStart);
			end_pos = result.indexOf(specialEnd);
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public static void flashMessage(final String type, final String message) {
		Map<Object, Object> flashScope = getFlashScope();
		List<Object> messages = (List<Object>) flashScope.get(type + "Messages");
		if (messages == null) {
			messages = new ArrayList<Object>();
			flashScope.put(type + "Messages", messages);
		}
		messages.add(message);
	}

	public static void addMessage(final String summary) {
		if(isFaces()) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(summary));
		}
	}
	public static void addMessage(final String summary, final String detail) {
		if(isFaces()) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(summary, detail));
		}
	}
	public static void addMessage(final FacesMessage.Severity severity, final String summary, final String detail) {
		if(isFaces()) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
		}
	}

	/**
	 * @param url
	 * 	The URL of a resource in the application in the style used for XSP component URLs, e.g. "/foo.js"
	 * @return
	 * 	The server-relative URL of the resource inside the application
	 */
	public static String getResourceURL(final String url) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		return facesContext.getExternalContext().encodeResourceURL(facesContext.getApplication().getViewHandler().getResourceURL(facesContext, url));
	}

	public static String getActionURL(final String url) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		return facesContext.getExternalContext().encodeActionURL(facesContext.getApplication().getViewHandler().getActionURL(facesContext, url));
	}
}
