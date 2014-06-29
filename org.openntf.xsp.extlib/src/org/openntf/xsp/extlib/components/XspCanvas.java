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
package org.openntf.xsp.extlib.components;

import javax.faces.context.FacesContext;

public class XspCanvas extends AbstractHtmlTag {
	public static final String COMPONENT_TYPE = "org.openntf.xsp.Canvas";
	public static final String RENDERER_TYPE = "org.openntf.xsp.Canvas";
	private String ongesturechange;
	private String ongestureend;
	private String ongesturestart;
	private String ontouchcancel;
	private String ontouchend;
	private String ontouchmove;
	private String ontouchstart;

	public XspCanvas() {
		setRendererType(RENDERER_TYPE);
		setTagName("canvas");
	}

	public String getOngesturechange() {
		return getStringProperty("ongesturechange", this.ongesturechange);
	}

	public String getOngestureend() {
		return getStringProperty("ongestureend", this.ongestureend);
	}

	public String getOngesturestart() {
		return getStringProperty("ongesturestart", this.ongesturestart);
	}

	public String getOntouchcancel() {
		return getStringProperty("ontouchcancel", this.ontouchcancel);
	}

	public String getOntouchend() {
		return getStringProperty("ontouchend", this.ontouchend);
	}

	public String getOntouchmove() {
		return getStringProperty("ontouchmove", this.ontouchmove);
	}

	public String getOntouchstart() {
		return getStringProperty("ontouchstart", this.ontouchstart);
	}

	@Override
	public void restoreState(FacesContext context, Object state) {
		Object[] properties = (Object[]) state;
		int idx = 0;
		super.restoreState(context, properties[idx++]);
		this.ongesturechange = ((String) properties[idx++]);
		this.ongestureend = ((String) properties[idx++]);
		this.ongesturestart = ((String) properties[idx++]);
		this.ontouchcancel = ((String) properties[idx++]);
		this.ontouchend = ((String) properties[idx++]);
		this.ontouchmove = ((String) properties[idx++]);
		this.ontouchstart = ((String) properties[idx++]);
	}

	@Override
	public Object saveState(FacesContext context) {
		Object[] properties = new Object[8];
		int idx = 0;
		properties[idx++] = super.saveState(context);
		properties[idx++] = this.ongesturechange;
		properties[idx++] = this.ongestureend;
		properties[idx++] = this.ongesturestart;
		properties[idx++] = this.ontouchcancel;
		properties[idx++] = this.ontouchend;
		properties[idx++] = this.ontouchmove;
		properties[idx++] = this.ontouchstart;
		return properties;
	}

	public void setOngesturechange(String ongesturechange) {
		this.ongesturechange = ongesturechange;
	}

	public void setOngestureend(String ongestureend) {
		this.ongestureend = ongestureend;
	}

	public void setOngesturestart(String ongesturestart) {
		this.ongesturestart = ongesturestart;
	}

	public void setOntouchcancel(String ontouchcancel) {
		this.ontouchcancel = ontouchcancel;
	}

	public void setOntouchend(String ontouchend) {
		this.ontouchend = ontouchend;
	}

	public void setOntouchmove(String ontouchmove) {
		this.ontouchmove = ontouchmove;
	}

	public void setOntouchstart(String ontouchstart) {
		this.ontouchstart = ontouchstart;
	}
}
