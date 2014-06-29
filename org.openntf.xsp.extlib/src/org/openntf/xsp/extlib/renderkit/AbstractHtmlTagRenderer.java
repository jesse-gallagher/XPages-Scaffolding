package org.openntf.xsp.extlib.renderkit;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.openntf.xsp.extlib.components.AbstractHtmlTag;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.renderkit.html_extended.HtmlTagRenderer;

public abstract class AbstractHtmlTagRenderer extends HtmlTagRenderer {

	private static final String[] ATTRS = { "role" };
	private static final String TAG = null; // always override;

	@Override
	protected void encodeAllAttributes(FacesContext context,
			UIComponent component, ResponseWriter writer, String[] properties)
			throws IOException {
		if (component instanceof AbstractHtmlTag) {
			AbstractHtmlTag tag = (AbstractHtmlTag) component;
			encodeAttribute(writer, "dir", tag.getDir(), "dir");
			encodeAttribute(writer, "lang", tag.getLang(), "lang");
			encodeAttribute(writer, "onclick", tag.getOnclick(), "onclick");
			encodeAttribute(writer, "ondblclick", tag.getOndblclick(),
					"ondblclick");
			encodeAttribute(writer, "onkeydown", tag.getOnkeydown(),
					"onkeydown");
			encodeAttribute(writer, "onkeypress", tag.getOnkeypress(),
					"onkeypress");
			encodeAttribute(writer, "onkeyup", tag.getOnkeyup(), "onkeyup");
			encodeAttribute(writer, "onmousedown", tag.getOnmousedown(),
					"onmousedown");
			encodeAttribute(writer, "onmousemove", tag.getOnmousemove(),
					"onmousemove");
			encodeAttribute(writer, "onmouseout", tag.getOnmouseout(),
					"onmouseout");
			encodeAttribute(writer, "onmouseover", tag.getOnmouseover(),
					"onmouseover");
			encodeAttribute(writer, "onmouseup", tag.getOnmouseup(),
					"onmouseup");
			encodeAttribute(writer, "role", tag.getRole(), "role");
			encodeAttribute(writer, "style", tag.getStyle(), "style");
			encodeAttribute(writer, "class", tag.getStyleClass(), "styleClass");
			encodeAttribute(writer, "title", tag.getTitle(), "title");
			return;
		}
		super.encodeAllAttributes(context, component, writer, properties);
	}

	protected void encodeAttribute(ResponseWriter writer, String attrName,
			String value, String attrAlias) {
		if (StringUtil.isNotEmpty(value)) {
			try {
				writer.writeAttribute(attrName, value, attrAlias);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void encodeBegin(FacesContext context, UIComponent component)
			throws IOException {
		encodeBegin(context, component, TAG, ATTRS);
	}

	@Override
	public void encodeEnd(FacesContext context, UIComponent component)
			throws IOException {
		encodeEnd(context, component, TAG);
	}

}
