/*
 * © Copyright OpenNTF 2013
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package org.openntf.xsp.extlib.renderkit;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.openntf.xsp.extlib.components.XspCanvas;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.renderkit.html_extended.HtmlTagRenderer;

public class HtmlCanvasRenderer extends HtmlTagRenderer {
	private static final String[] ATTRS = { "role" };
	private static final String TAG = "canvas";

	@Override
	protected void encodeAllAttributes(FacesContext context,
			UIComponent component, ResponseWriter writer, String[] properties)
			throws IOException {
		if (component instanceof XspCanvas) {
			XspCanvas canvas = (XspCanvas) component;
			encodeAttribute(writer, "ongesturechange", canvas
					.getOngesturechange(), "ongesturechange");
			encodeAttribute(writer, "ongestureend", canvas.getOngestureend(),
					"ongestureend");
			encodeAttribute(writer, "ongesturestart", canvas
					.getOngesturestart(), "ongesturestart");
			encodeAttribute(writer, "ontouchcancel", canvas.getOntouchcancel(),
					"ontouchcancel");
			encodeAttribute(writer, "ontouchend", canvas.getOntouchend(),
					"ontouchend");
			encodeAttribute(writer, "ontouchmove", canvas.getOntouchmove(),
					"ontouchmove");
			encodeAttribute(writer, "ontouchstart", canvas.getOntouchstart(),
					"ontouchstart");

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
