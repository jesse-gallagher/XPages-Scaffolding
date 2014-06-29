package org.openntf.xsp.extlib.renderkit;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.openntf.xsp.extlib.Activator;
import org.openntf.xsp.extlib.components.UIProgress;
import org.openntf.xsp.extlib.resources.Resources;

public class UIProgressRenderer extends Renderer {
	private final static boolean _debug = Activator._debug;

	@Override
	public void encodeBegin(FacesContext context, UIComponent component) {
		if (_debug)
			System.out.println(getClass().getName() + " encodeBegin");
		try {
			Resources.addEncodeResource(context, Resources.uiProgress);
			UIProgress tag = (UIProgress) component;
			super.encodeBegin(context, component);
			
			ResponseWriter writer = context.getResponseWriter();
			
			
			
			String progressClass = "";
			progressClass = "progress";
			
			// Add in the color variation
			if (tag.getBarType() == "Info"){
				progressClass = progressClass + " progress-info";
			}
			if (tag.getBarType() == "Success"){
				progressClass = progressClass + " progress-success";
			}
			if (tag.getBarType() == "Warning"){
				progressClass = progressClass + " progress-warning";
			}
			if (tag.getBarType() == "Danger"){
				progressClass = progressClass + " progress-danger";
			}
			
			// Add in the special styles
			if (tag.getBarStyle() == "Striped"){
				progressClass = progressClass + " progress-striped";
			}
			if (tag.getBarStyle() == "Active"){
				progressClass = progressClass + " progress-striped active";
			}
			
			writer.startElement("div", component);
			writer.writeAttribute("class", progressClass, null);
			
			writer.startElement("div", component);
			writer.writeAttribute("class", "bar", null);
			writer.writeAttribute("style", "width:"+ tag.getBarWidth(), null);
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