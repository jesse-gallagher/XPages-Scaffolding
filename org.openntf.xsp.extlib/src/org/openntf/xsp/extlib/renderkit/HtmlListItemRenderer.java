package org.openntf.xsp.extlib.renderkit;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

public class HtmlListItemRenderer extends AbstractHtmlTagRenderer {

	private static final String TAG = "li";
	private static final String[] ATTRS = { "role" };

	@Override
	protected void encodeAllAttributes(FacesContext context, UIComponent component, ResponseWriter writer,
			String[] properties) throws IOException {
		super.encodeAllAttributes(context, component, writer, properties);
	}

	@Override
	protected void encodeAttribute(ResponseWriter writer, String attrName, String value, String attrAlias) {
		super.encodeAttribute(writer, attrName, value, attrAlias);
	}

	@Override
	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
		encodeBegin(context, component, TAG, ATTRS);
	}

	@Override
	public void encodeChildren(FacesContext paramFacesContext, UIComponent paramUIComponent) throws IOException {
		super.encodeChildren(paramFacesContext, paramUIComponent);
	}

	@Override
	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
		encodeEnd(context, component, TAG);
	}
}
