package frostillicus;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import lotus.domino.*;

import com.ibm.commons.util.SystemCache;
import com.ibm.xsp.FacesExceptionEx;
import com.ibm.xsp.extlib.builder.ControlBuilder.IControl;
import com.ibm.xsp.extlib.component.dynamicview.UIDynamicViewPanel;
import com.ibm.xsp.extlib.component.dynamicview.ViewColumnConverter;
import com.ibm.xsp.extlib.component.dynamicview.ViewDesign;
import com.ibm.xsp.extlib.component.dynamicview.DominoDynamicColumnBuilder.DominoViewCustomizer;
import com.ibm.xsp.extlib.component.dynamicview.ViewDesign.ColumnDef;
import com.ibm.xsp.extlib.component.dynamicview.ViewDesign.DefaultColumnDef;
import com.ibm.xsp.extlib.component.dynamicview.ViewDesign.DefaultViewDef;
import com.ibm.xsp.extlib.component.dynamicview.ViewDesign.ViewDef;
import com.ibm.xsp.extlib.component.dynamicview.ViewDesign.ViewFactory;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.util.FacesUtil;

import org.openntf.domino.utils.xml.XMLDocument;
import org.openntf.domino.utils.xml.XMLNode;


public class DynamicViewCustomizer extends DominoViewCustomizer implements Serializable {
	private static final long serialVersionUID = -5126984721484501732L;

	private String panelId = "";

	@Override
	public ViewFactory getViewFactory() {
		return new DynamicViewFactory();
	}

	public static class DynamicViewFactory implements ViewFactory, Serializable {
		private static final long serialVersionUID = 123034173761337005L;
		private SystemCache views = new SystemCache("View Definition", 16, "xsp.extlib.viewdefsize");

		private void writeObject(final ObjectOutputStream out) throws IOException {
			this.views = null;
			out.defaultWriteObject();
		}
		private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
			in.defaultReadObject();
			this.views = new SystemCache("View Definition", 16, "xsp.extlib.viewdefsize");
		}

		public ViewDef getViewDef(final View view) {
			if(view == null) {
				return null;
			}
			try {
				String viewKey = ViewDesign.getViewKey(view);
				DefaultViewDef viewDef = (DefaultViewDef)this.views.get(viewKey);
				if(viewDef == null) {
					// Read the view
					viewDef = new DefaultViewDef();
					if(view.isHierarchical()) { viewDef.flags |= DefaultViewDef.FLAG_HIERARCHICAL; }
					if(view.isCategorized()) { viewDef.flags |= DefaultViewDef.FLAG_CATEGORIZED; }

					viewDef.columns.addAll(this.getViewColumnInformation(view));
				}
				return viewDef;
			} catch(Exception ex) {
				throw new FacesExceptionEx(ex, "Error while accessing view {0}", view.toString());
			}
		}

		@SuppressWarnings("unchecked")
		private List<ColumnDef> getViewColumnInformation(final View view) throws Exception {
			Database database = view.getParent();

			/* Generate the DXL */
			Document viewDoc = database.getDocumentByUNID(view.getUniversalID());
			String dxl = viewDoc.generateXML();
			InputStream is = new ByteArrayInputStream(dxl.getBytes(Charset.forName("UTF-8")));
			XMLDocument dxlDoc = new XMLDocument();
			dxlDoc.loadInputStream(is);
			viewDoc.recycle();

			/*
			 * Fetch both types of column information since some properties are
			 * much easier to deal with via the standard API
			 */
			List<ViewColumn> viewColumns = view.getColumns();
			List<XMLNode> dxlColumns = dxlDoc.selectNodes("//column");

			// Figure out if we're going to extend the last column
			boolean extendLastColumn = dxlDoc.selectSingleNode("//view").getAttribute("extendlastcolumn").equals("true");

			Document contextDoc = database.createDocument();
			List<ColumnDef> columns = new Vector<ColumnDef>();
			String activeColorColumn = "";
			for(int i = 0; i < dxlColumns.size(); i++) {
				XMLNode columnNode = dxlColumns.get(i);
				ViewColumn viewColumn = viewColumns.get(i);

				ExtendedColumnDef column = new ExtendedColumnDef();
				column.replicaId = view.getParent().getReplicaID();

				if(columnNode.getAttribute("hidden").equals("true")) {
					column.flags |= DefaultColumnDef.FLAG_HIDDEN;
				} else {
					// Check to see if it's hidden by a hide-when formula
					XMLNode hideWhen = columnNode.selectSingleNode("code[@event='hidewhen']");
					if(hideWhen != null) {
						if(hideWhen.getAttribute("enabled") == null || !hideWhen.getAttribute("enabled").equals("false")) {
							String hideWhenFormula = hideWhen.getText();
							if(hideWhenFormula.length() > 0) {
								List<Object> evalResult = ExtLibUtil.getCurrentSession().evaluate(hideWhenFormula, contextDoc);
								if(evalResult.size() > 0 && evalResult.get(0) instanceof Double && (Double)evalResult.get(0) == 1) {
									column.flags |= DefaultColumnDef.FLAG_HIDDEN;
								}
							}
						}
					}
				}

				column.name = columnNode.getAttribute("itemname");

				if(columnNode.getAttribute("showascolor").equals("true")) {
					activeColorColumn = column.name;
				}
				column.colorColumn = activeColorColumn;

				// Get the header information
				XMLNode header = columnNode.selectSingleNode("columnheader");
				column.title = columnNode.selectSingleNode("columnheader").getAttribute("title");
				if(header.getAttribute("align").equals("center")) {
					column.flags |= DefaultColumnDef.FLAG_HALIGNCENTER;
				} else if(header.getAttribute("align").equals("right")) {
					column.flags |= DefaultColumnDef.FLAG_HALIGNRIGHT;
				}

				column.width = new Float(columnNode.getAttribute("width")).intValue();
				column.actualWidth = Double.parseDouble(columnNode.getAttribute("width"));

				if(columnNode.getAttribute("responsesonly").equals(true)) {
					column.flags |= DefaultColumnDef.FLAG_RESPONSE;
				}
				if(columnNode.getAttribute("categorized").equals("true")) {
					column.flags |= DefaultColumnDef.FLAG_CATEGORIZED;
				}
				if(columnNode.getAttribute("sort").length() > 0) {
					column.flags |= DefaultColumnDef.FLAG_SORTED;
				}
				if(columnNode.getAttribute("resort").equals("ascending") || columnNode.getAttribute("resort").equals("both")) {
					column.flags |= DefaultColumnDef.FLAG_RESORTASC;
				}
				if(columnNode.getAttribute("resort").equals("descending") || columnNode.getAttribute("resort").equals("both")) {
					column.flags |= DefaultColumnDef.FLAG_RESORTDESC;
				}
				if(columnNode.getAttribute("align").equals("center")) {
					column.flags |= DefaultColumnDef.FLAG_ALIGNCENTER;
				} else if(columnNode.getAttribute("align").equals("right")) {
					column.flags |= DefaultColumnDef.FLAG_ALIGNRIGHT;
				}
				if(columnNode.getAttribute("showaslinks").equals("true")) {
					column.flags |= DefaultColumnDef.FLAG_LINK | DefaultColumnDef.FLAG_ONCLICK | DefaultColumnDef.FLAG_CHECKBOX;
				}

				column.numberFmt = viewColumn.getNumberFormat();
				column.numberDigits = viewColumn.getNumberDigits();
				column.numberAttrib = viewColumn.getNumberAttrib();
				if(viewColumn.isNumberAttribParens()) {
					column.flags |= DefaultColumnDef.FLAG_ATTRIBPARENS;
				}
				if(viewColumn.isNumberAttribPercent()) {
					column.flags |= DefaultColumnDef.FLAG_ATTRIBPERCENT;
				}
				if(viewColumn.isNumberAttribPunctuated()) {
					column.flags |= DefaultColumnDef.FLAG_ATTRIBPUNC;
				}
				column.timeDateFmt = viewColumn.getTimeDateFmt();
				column.dateFmt = viewColumn.getDateFmt();
				column.timeFmt = viewColumn.getTimeFmt();
				column.timeZoneFmt = viewColumn.getTimeZoneFmt();
				column.listSep = viewColumn.getListSep();

				column.fontFace = viewColumn.getFontFace();
				column.fontStyle = viewColumn.getFontStyle();
				column.fontPointSize = viewColumn.getFontPointSize();
				column.fontColor = viewColumn.getFontColor();

				column.headerFontFace = viewColumn.getHeaderFontFace();
				column.headerFontStyle = viewColumn.getHeaderFontStyle();
				column.headerFontPointSize = viewColumn.getHeaderFontPointSize();
				column.headerFontColor = viewColumn.getHeaderFontColor();

				if(columnNode.getAttribute("showasicons").equals("true")) {
					column.flags |= DefaultColumnDef.FLAG_ICON;
				}
				if(columnNode.getAttribute("twisties").equals("true")) {
					column.flags |= DefaultColumnDef.FLAG_INDENTRESP;
				}

				// Find any twistie image
				XMLNode twistieImage = columnNode.selectSingleNode("twistieimage/imageref");
				if(twistieImage != null) {
					if(twistieImage.getAttribute("database").equals("0000000000000000")) {
						column.twistieReplicaId = database.getReplicaID();
					} else {
						column.twistieReplicaId = twistieImage.getAttribute("database");
					}

					// Make sure that the referenced database is available on
					// the current server
					boolean setTwistie = true;
					if(!column.twistieReplicaId.equalsIgnoreCase(database.getReplicaID())) {
						Database twistieDB = ExtLibUtil.getCurrentSession().getDatabase("", "");
						twistieDB.openByReplicaID("", column.twistieReplicaId);
						if(!twistieDB.isOpen()) {
							setTwistie = false;
						}
						twistieDB.recycle();
					}
					if(setTwistie) {
						column.twistieImage = twistieImage.getAttribute("name");
					}

				}

				// Support extending the column width to the full window.
				// In the client, "extend last column" takes priority
				if(extendLastColumn && i == dxlColumns.size() - 1) {
					column.extendColumn = true;
				} else if(!extendLastColumn && columnNode.getAttribute("extwindowwidth").equals("true")) {
					column.extendColumn = true;
				}

				columns.add(column);

				viewColumn.recycle();
			}
			contextDoc.recycle();

			database.recycle();

			return columns;
		}

		public static class ExtendedColumnDef extends DefaultColumnDef implements Serializable {
			private static final long serialVersionUID = 5158008403553374867L;

			public String colorColumn;
			public String twistieImage = "";
			public String twistieReplicaId = "";
			public boolean extendColumn = false;
			public String replicaId = "";
			public double actualWidth;

			public String fontFace = "";
			public int fontPointSize = 0;
			public int fontStyle = 0;
			public int fontColor = 0;

			public String headerFontFace = "";
			public int headerFontPointSize = 0;
			public int headerFontStyle = 0;
			public int headerFontColor = 0;
		}
	}
	@Override
	public IControl createColumn(final FacesContext context, final UIDynamicViewPanel panel, final int index, final ColumnDef colDef) {
		this.panelId = panel.getId();
		return super.createColumn(context, panel, index, colDef);
	}

	@Override
	public void afterCreateColumn(final FacesContext context, final int index, final ColumnDef colDef, final IControl column) {
		UIDynamicViewPanel panel = (UIDynamicViewPanel)FacesUtil.getComponentFor(context.getViewRoot(), panelId);

		// Patch in a converter to handle special text and icon columns
		UIDynamicViewPanel.DynamicColumn col = (UIDynamicViewPanel.DynamicColumn)column.getComponent();
		if(colDef.isIcon()) {
			// For icons, override the default behavior so it can handle
			// string-based ones
			col.setValueBinding("iconSrc", null);
			col.setDisplayAs("");
			col.setConverter(new IconColumnConverter(null, colDef, panel));
		} else {
			col.setConverter(new ExtendedViewColumnConverter(null, colDef, panel));
		}

		// Apply a general style class to indicate that it's not just some
		// normal view panel column
		// Many style attributes will be class-based both for flexibility and
		// because headers can't have style applied directly
		String styleClass = " notesViewColumn";
		String headerStyleClass = "";

		// Add an extra class for category columns
		if(colDef.isCategorized()) {
			styleClass += " notesViewCategory";
		}

		// We'll handle escaping the HTML manually, to support
		// [<b>Notes-style</b>] pass-through-HTML and icon columns
		col.setContentType("html");

		// Deal with any twistie images and color columns
		if(colDef instanceof DynamicViewFactory.ExtendedColumnDef) {
			DynamicViewFactory.ExtendedColumnDef extColDef = (DynamicViewFactory.ExtendedColumnDef)colDef;

			if(extColDef.twistieImage.length() > 0) {
				// Assume that it's a multi-image well for now
				col.setCollapsedImage("/.ibmxspres/domino/__" + extColDef.twistieReplicaId + ".nsf/" + extColDef.twistieImage.replaceAll("\\\\", "/") + "?Open&ImgIndex=2");
				col.setExpandedImage("/.ibmxspres/domino/__" + extColDef.twistieReplicaId + ".nsf/" + extColDef.twistieImage.replaceAll("\\\\", "/") + "?Open&ImgIndex=1");
			}

			// The style applies to the contents of the column as well as the
			// column itself, which messes with icon columns
			// For now, don't apply it at all to those columns
			String style = "";
			//			if(!extColDef.extendColumn) {
			//				style = "max-width: " + (extColDef.actualWidth * extColDef.fontPointSize * 1.3) + "px; min-width: " + (extColDef.actualWidth * extColDef.fontPointSize * 1.3) + "px";
			//			} else {
			//				style = "width: 100%";
			//			}

			// Check for left or right alignment
			switch(extColDef.getAlignment()) {
				case ViewColumn.ALIGN_CENTER:
					styleClass += " notesViewAlignCenter";
					break;
				case ViewColumn.ALIGN_RIGHT:
					styleClass += " notesViewAlignRight";
					break;
			}

			// Add font information
			styleClass += this.fontStyleToStyleClass(extColDef.fontStyle);
			headerStyleClass += this.fontStyleToStyleClass(extColDef.headerFontStyle);
			style += "; color: " + this.notesColorToCSS(extColDef.fontColor);

			if(extColDef.colorColumn.length() > 0) {
				String styleFormula = "#{javascript:'" + style.replace("'", "\\'") + ";' + " + this.getClass().getName() + ".colorColumnToStyle(" + panel.getVar() + ".getColumnValue('"
				+ extColDef.colorColumn + "'))}";
				ValueBinding styleBinding = context.getApplication().createValueBinding(styleFormula);
				col.setValueBinding("style", styleBinding);
			} else {
				col.setStyle(style);
			}

		}
		col.setStyleClass((col.getStyleClass() == null ? "" : col.getStyleClass()) + styleClass);
		col.setHeaderClass((col.getHeaderClass() == null ? "" : col.getHeaderClass()) + headerStyleClass);
	}

	public static class ExtendedViewColumnConverter extends ViewColumnConverter {
		private ColumnDef colDef;
		private String panelId;

		// For loading the state
		public ExtendedViewColumnConverter() {}

		public ExtendedViewColumnConverter(final ViewDef viewDef, final ColumnDef colDef, final UIDynamicViewPanel panel) {
			super(viewDef, colDef);
			this.colDef = colDef;
			this.panelId = panel.getId();
		}

		@Override
		public String getAsString(final FacesContext context, final UIComponent component, final Object value) {
			UIDynamicViewPanel panel = (UIDynamicViewPanel)FacesUtil.getComponentFor(context.getViewRoot(), panelId);

			// First, apply any column-color info needed
			ViewEntry entry = this.resolveViewEntry(context, panel.getVar());
			try {
				if(value instanceof DateTime) {
					return this.getValueDateTimeAsString(context, component, ((DateTime)value).toJavaDate());
				}
				if(value instanceof Date) {
					return this.getValueDateTimeAsString(context, component, (Date)value);
				}
				if(value instanceof Number) {
					return this.getValueNumberAsString(context, component, (Number)value);
				}
			} catch(NotesException ex) {}

			String stringValue = value.toString();

			try {
				stringValue = specialTextDecode(stringValue, entry);
			} catch(NotesException ne) {}

			// Process the entry as Notes-style pass-through-HTML
			try {
				if(!entry.isCategory()) {
					stringValue = this.handlePassThroughHTML(stringValue);
				}
			} catch(NotesException e) { }

			// Add in some text for empty categories
			try {
				if(entry.isCategory() && stringValue.length() == 0) {
					stringValue = "(Not Categorized)";
				}
			} catch(NotesException e) { }

			// Include a &nbsp; to avoid weird styling problems when the content
			// itself is empty or not visible
			return stringValue;
		}

		private String handlePassThroughHTML(String cellData) {
			if(cellData.contains("[<") && cellData.contains(">]")) {
				String[] cellChunks = cellData.split("\\[\\<", -2);
				cellData = "";
				for(String chunk : cellChunks) {
					if(chunk.contains(">]")) {
						String[] smallChunks = chunk.split(">]", -2);
						cellData += "<" + smallChunks[0] + ">" + xmlEncode(smallChunks[1]);
					} else {
						cellData += xmlEncode(chunk);
					}
				}
			} else {
				cellData = xmlEncode(cellData);
			}
			return cellData;
		}

		private ViewEntry resolveViewEntry(final FacesContext context, final String var) {
			return (ViewEntry)context.getApplication().getVariableResolver().resolveVariable(context, var);
		}

		@Override
		public Object saveState(final FacesContext context) {
			Object[] superState = (Object[])super.saveState(context);
			Object[] state = new Object[3];
			state[0] = superState;
			state[1] = this.colDef;
			state[2] = this.panelId;
			return state;
		}

		@Override
		public void restoreState(final FacesContext context, final Object value) {
			Object[] state = (Object[])value;
			super.restoreState(context, state[0]);
			this.colDef = (ColumnDef)state[1];
			this.panelId = (String)state[2];
		}
	}

	public static class IconColumnConverter extends ViewColumnConverter {
		private ColumnDef colDef;

		// For loading the state
		public IconColumnConverter() {}

		public IconColumnConverter(final ViewDef viewDef, final ColumnDef colDef, final UIDynamicViewPanel panel) {
			super(viewDef, colDef);
			this.colDef = colDef;
		}

		@SuppressWarnings("unchecked")
		@Override
		public String getAsString(final FacesContext context, final UIComponent component, final Object value) {
			List<Object> listValue;
			if(value instanceof List) {
				listValue = (List<Object>)value;
			} else {
				listValue = new Vector<Object>();
				listValue.add(value);
			}
			StringBuilder result = new StringBuilder();

			result.append("<span style='white-space: nowrap'>");
			for(Object node : listValue) {
				// Handle a zero-value icon specially
				if(node instanceof Double && ((Double)node == 0 || (Double)node == 999)) {
					result.append("<img class='notesViewIconCustom notesViewIconBlank' src='/icons/ecblank.gif' />");
				} else if(node instanceof Double) {
					result.append("<img class='notesViewIconStandard' src='/icons/vwicn");
					Double num = (Double)node;
					if(num < 10) {
						result.append("00");
					} else if(num < 100) {
						result.append("0");
					}
					result.append(num.intValue());
					result.append(".gif' />");
				} else {
					if(String.valueOf(value).length() > 0 && !String.valueOf(value).equals("null")) {
						DynamicViewFactory.ExtendedColumnDef col = (DynamicViewFactory.ExtendedColumnDef)this.colDef;
						try {
							result.append("<img class='notesViewIconCustom' src='");
							result.append("/__" + col.replicaId + ".nsf/" + java.net.URLEncoder.encode(String.valueOf(node), "UTF-8"));
							result.append("' />");
						} catch(Exception e) {}
					}
				}
			}
			result.append("</span>");

			return result.toString();
		}

		@Override
		public Object saveState(final FacesContext context) {
			Object[] superState = (Object[])super.saveState(context);
			Object[] state = new Object[2];
			state[0] = superState;
			state[1] = this.colDef;
			return state;
		}

		@Override
		public void restoreState(final FacesContext context, final Object value) {
			Object[] state = (Object[])value;
			super.restoreState(context, state[0]);
			this.colDef = (ColumnDef)state[1];
		}

	}

	@SuppressWarnings("unchecked")
	public static String colorColumnToStyle(final Object colorValuesObj) {
		String cellStyle = "";
		if(colorValuesObj instanceof List) {
			List<Double> colorValues = (List<Double>)colorValuesObj;
			if(colorValues.size() > 3) {
				// Then we have a background color
				if(colorValues.get(0) != -1) {
					// Then the background is not pass-through
					cellStyle = "background-color: rgb(" + colorValues.get(0).intValue() + ", " + colorValues.get(1).intValue() + ", " + colorValues.get(2).intValue() + ");";
				} else {
					cellStyle = "";
				}
				if(colorValues.get(3) != -1) {
					// Then main color is not pass-through
					cellStyle += "color: rgb(" + colorValues.get(3).intValue() + ", " + colorValues.get(4).intValue() + ", " + colorValues.get(5).intValue() + ");";
				}
			} else {
				// Then it's just a text color
				if(colorValues.get(0) != -1) {
					cellStyle += "color: rgb(" + colorValues.get(0).intValue() + ", " + colorValues.get(1).intValue() + ", " + colorValues.get(2).intValue() + ");";
				}
			}
		}
		return cellStyle;
	}

	private String fontStyleToStyleClass(final int fontStyle) {
		StringBuilder result = new StringBuilder();

		if((fontStyle & ViewColumn.FONT_PLAIN) != 0) {
			result.append(" notesViewPlain");
		}
		if((fontStyle & ViewColumn.FONT_BOLD) != 0) {
			result.append(" notesViewBold");
		}
		if((fontStyle & ViewColumn.FONT_UNDERLINE) != 0) {
			result.append(" notesViewUnderline");
		}
		if((fontStyle & ViewColumn.FONT_STRIKETHROUGH) != 0) {
			result.append(" notesViewStrikethrough");
		}
		if((fontStyle & ViewColumn.FONT_ITALIC) != 0) {
			result.append(" notesViewItalic");
		}

		return result.toString();
	}

	private String notesColorToCSS(final int notesColor) {
		try {
			Session session = ExtLibUtil.getCurrentSession();
			ColorObject colorObject = session.createColorObject();
			colorObject.setNotesColor(notesColor);

			StringBuilder result = new StringBuilder();
			result.append("rgb(");
			result.append(colorObject.getRed());
			result.append(",");
			result.append(colorObject.getGreen());
			result.append(",");
			result.append(colorObject.getBlue());
			result.append(")");

			return result.toString();
		} catch(NotesException ne) {
			return "";
		}
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

	public static String xmlEncode(final String text) {
		StringBuilder result = new StringBuilder();

		for(int i = 0; i < text.length(); i++) {
			char currentChar = text.charAt(i);
			if(!((currentChar >= 'a' && currentChar <= 'z') || (currentChar >= 'A' && currentChar <= 'Z') || (currentChar >= '0' && currentChar <= '9'))) {
				result.append("&#" + (int)currentChar + ";");
			} else {
				result.append(currentChar);
			}
		}

		return result.toString();
	}

	public static String specialTextDecode(final String specialText, final ViewEntry viewEntry) throws NotesException {
		String result = specialText;

		String specialStart = (char)127 + "";
		String specialEnd = (char)160 + "";

		// First, find the start and end of the special text
		int start_pos = result.indexOf(specialStart);
		int end_pos = result.indexOf(specialEnd);

		int loopStopper = 1;
		while(start_pos > -1 && end_pos > start_pos && loopStopper < 100) {
			loopStopper++;

			String working = result.substring(start_pos+1, end_pos);
			String midResult = "";
			String[] choices;
			int offset, length, parameterCount;

			switch(working.charAt(0)) {
				case 'C':
					// @DocChildren
					parameterCount = Integer.parseInt(working.substring(1, 2));
					switch(parameterCount) {
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
							choices[0] = working.substring(working.indexOf("=", offset)+1, working.indexOf("=", offset)+1+length);

							offset = working.indexOf("=", offset) + 1 + length;
							choices[1] = working.substring(working.indexOf("=", offset)+1, working.indexOf("=", offset)+1+length);

							if(viewEntry.getChildCount() == 0) {
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
							choices[0] = working.substring(working.indexOf("=", offset)+1, working.indexOf("=", offset)+1+length);

							offset = working.indexOf("=", offset) + 2 + length;
							length = Integer.parseInt(working.substring(offset, working.indexOf("=", offset)));
							choices[1] = working.substring(working.indexOf("=", offset)+1, working.indexOf("=", offset)+1+length);

							offset = working.indexOf("=", offset) + 2 + length;
							length = Integer.parseInt(working.substring(offset, working.indexOf("=", offset)));
							choices[2] = working.substring(working.indexOf("=", offset)+1, working.indexOf("=", offset)+1+length);

							if(viewEntry.getChildCount() == 0) {
								midResult = choices[0].replaceAll("%", "0");
							} else if(viewEntry.getChildCount() == 1) {
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
					switch(parameterCount) {
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
							choices[0] = working.substring(working.indexOf("=", offset)+1, working.indexOf("=", offset)+1+length);

							offset = working.indexOf("=", offset) + 1 + length;
							choices[1] = working.substring(working.indexOf("=", offset)+1, working.indexOf("=", offset)+1+length);

							if(viewEntry.getDescendantCount() == 0) {
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
							choices[0] = working.substring(working.indexOf("=", offset)+1, working.indexOf("=", offset)+1+length);

							offset = working.indexOf("=", offset) + 2 + length;
							length = Integer.parseInt(working.substring(offset, working.indexOf("=", offset)));
							choices[1] = working.substring(working.indexOf("=", offset)+1, working.indexOf("=", offset)+1+length);

							offset = working.indexOf("=", offset) + 2 + length;
							length = Integer.parseInt(working.substring(offset, working.indexOf("=", offset)));
							choices[2] = working.substring(working.indexOf("=", offset)+1, working.indexOf("=", offset)+1+length);

							if(viewEntry.getDescendantCount() == 0) {
								midResult = choices[0].replaceAll("%", "0");
							} else if(viewEntry.getDescendantCount() == 1) {
								midResult = choices[1].replaceAll("%", "1");
							} else {
								midResult = choices[2].replaceAll("%", viewEntry.getDescendantCount() + "");
							}

							break;
					}
					break;
				case 'H':
					// @DocLevel
					midResult = (viewEntry.getIndentLevel()+1) + ""; 
					break;
				case 'A':
					// @DocNumber
					parameterCount = Integer.parseInt(working.substring(1, 2));
					switch(parameterCount) {
						case 0:
							midResult = viewEntry.getPosition('.');
							break;
						case 1:
							String delimiter = strRight(working, "=");
							if(delimiter.length() == 0) {
								midResult = strRightBack(viewEntry.getPosition('.'), ".");
							} else if(delimiter.length() > 1) {
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
					if(viewEntry.getIndentLevel() == 0) {
						midResult = "";
					} else {
						parameterCount = Integer.parseInt(working.substring(1, 2));
						switch(parameterCount) {
							case 0:
								midResult = strLeftBack(viewEntry.getPosition('.'), ".");
								break;
							case 1:
								String delimiter = strRight(working, "=");
								if(delimiter.length() == 0) {
									midResult = strRightBack(strLeftBack(viewEntry.getPosition('.'), "."), ".");
								} else if(delimiter.length() > 1) {
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
					parameterCount = Integer.parseInt(working.substring(1, 2));
					switch(parameterCount) {
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
							choices[0] = working.substring(working.indexOf("=", offset)+1, working.indexOf("=", offset)+1+length);

							offset = working.indexOf("=", offset) + 2 + length;
							length = Integer.parseInt(working.substring(offset, working.indexOf("=", offset)));
							choices[1] = working.substring(working.indexOf("=", offset)+1, working.indexOf("=", offset)+1+length);

							midResult = viewEntry.isCategory() ? choices[0] : choices[1];

							break;
					}

					break;
				case 'G':
					// @IsExpandable
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
}
