package frostillicus.model;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public static Map<String, Object> columnValuesToMap(final List<Object> columnValues, final List<DominoColumnInfo> columnInfo) {
		Map<String, Object> result = new HashMap<String, Object>();

		for (int i = 0; i < columnValues.size(); i++) {
			result.put(columnInfo.get(i).getItemName(), columnValues.get(i));
		}

		return result;
	}

	public static List<DominoColumnInfo> getColumnInfo(final View view) {
		List<ViewColumn> columns = view.getColumns();
		List<DominoColumnInfo> result = new ArrayList<DominoColumnInfo>(columns.size());
		for (ViewColumn column : columns) {
			if (column.getColumnValuesIndex() < 65535) {
				result.add(new DominoColumnInfo(column));
			}
		}
		return result;
	}

	public static String strRightBack(final String input, final String delimiter) {
		return input.substring(input.lastIndexOf(delimiter) + delimiter.length());
	}

	public static String strLeft(final String input, final String delimiter) {
		return input.substring(0, input.indexOf(delimiter));
	}
}