package org.openntf.xsp.extlib.components;

public class HtmlListItem extends AbstractHtmlTag {

	public static final String RENDERER_TYPE = "org.openntf.xsp.extlib.HtmlListItem";
	public static final String FAMILY = "org.openntf.xsp.extlib";

	public HtmlListItem() {
		setRendererType(RENDERER_TYPE);
	}

	public String getFamily() {
		return FAMILY;
	}

}
