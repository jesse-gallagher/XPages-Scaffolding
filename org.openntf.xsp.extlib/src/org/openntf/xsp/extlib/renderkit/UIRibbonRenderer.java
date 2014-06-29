package org.openntf.xsp.extlib.renderkit;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.openntf.xsp.extlib.Activator;
import org.openntf.xsp.extlib.components.UIRibbon;
import org.openntf.xsp.extlib.resources.Resources;

public class UIRibbonRenderer extends Renderer {
	private final static boolean _debug = Activator._debug;

	@Override
	public void encodeBegin(FacesContext context, UIComponent component) {
		if (_debug)
			System.out.println(getClass().getName() + " encodeBegin");
		try {
			Resources.addEncodeResource(context, Resources.uiRibbonStyle);
			UIRibbon tag = (UIRibbon) component;
			super.encodeBegin(context, component);
			ResponseWriter writer = context.getResponseWriter();
			writer.startElement("div", component);
			writer.writeAttribute("class", "ribbon-wrapper-outer", null);

			writer.startElement("div", component);
			writer.writeAttribute("class", "ribbon-wrapper-inner", null);
			
			writer.startElement("div", component);
			writer.writeAttribute("class", "ribbon-" + tag.getRibbonColor(),null);
			writer.append(tag.getRibbonText());
			writer.endElement("div");
			
			writer.endElement("div");
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