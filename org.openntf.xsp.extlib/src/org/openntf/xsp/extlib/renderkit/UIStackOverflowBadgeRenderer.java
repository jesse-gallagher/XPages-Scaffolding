package org.openntf.xsp.extlib.renderkit;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.openntf.xsp.extlib.Activator;
import org.openntf.xsp.extlib.components.UIStackOverflowBadge;
import org.openntf.xsp.extlib.resources.Resources;

public class UIStackOverflowBadgeRenderer extends Renderer {
	private final static boolean _debug = Activator._debug;

	@Override
	public void encodeBegin(FacesContext context, UIComponent component) {
		if (_debug)
			System.out.println(getClass().getName() + " encodeBegin");
		try {
			Resources.addEncodeResource(context, Resources.czaruiBadges);
			UIStackOverflowBadge tag = (UIStackOverflowBadge) component;
			super.encodeBegin(context, component);
			
			ResponseWriter writer = context.getResponseWriter();
			
			writer.startElement("div", component);
			writer.writeAttribute("class", "badge-wrapper", null);
			
			writer.startElement("a", component);
			writer.writeAttribute("href", "#", null);
			writer.writeAttribute("title", "Bronze Badge", null);
			
			if (tag.getBadgeType()){
				writer.writeAttribute("class", "badge tag", null);
			} else {
				writer.writeAttribute("class", "badge", null);
			}
			
			writer.startElement("span", component);
			writer.writeAttribute("class", tag.getBadgeColor(), null);
			writer.endElement("span");
			
			writer.append(tag.getBadgeText());
			
			writer.endElement("a");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void encodeChildren(FacesContext context, UIComponent component) {
		if (_debug)
			System.out.println(getClass().getName() + " encodeChildren");
		try {
			super.encodeChildren(context, component);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void encodeEnd(FacesContext context, UIComponent component) {
		if (_debug)
			System.out.println(getClass().getName() + " encodeEnd");
		try {
			super.encodeEnd(context, component);
			ResponseWriter writer = context.getResponseWriter();
			writer.endElement("div");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}