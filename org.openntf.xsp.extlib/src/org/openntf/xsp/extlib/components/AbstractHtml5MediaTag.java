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

public class AbstractHtml5MediaTag extends AbstractHtmlTag {
	private String onabort;
	private String oncanplay;
	private String oncanplaythrough;
	private String ondurationchange;
	private String onemptied;
	private String onended;
	private String onerror;
	private String onloadstart;
	private String onloadeddata;
	private String onloadedmetadata;
	private String onplaying;
	private String onprogress;
	private String onpause;
	private String onplay;
	private String onratechange;
	private String onseeked;
	private String onseeking;
	private String onstalled;
	private String onsuspend;
	private String ontimeupdate;
	private String onvolumechange;
	private String onwaiting;

	public String getOnabort() {
		return getStringProperty("onabort", this.onabort);
	}

	public String getOncanplay() {
		return getStringProperty("oncanplay", this.oncanplay);
	}

	public String getOncanplaythrough() {
		return getStringProperty("oncanplaythrough", this.oncanplaythrough);
	}

	public String getOndurationchange() {
		return getStringProperty("ondurationchange", this.ondurationchange);
	}

	public String getOnemptied() {
		return getStringProperty("onemptied", this.onemptied);
	}

	public String getOnended() {
		return getStringProperty("onended", this.onended);
	}

	public String getOnerror() {
		return getStringProperty("onerror", this.onerror);
	}

	public String getOnloadeddata() {
		return getStringProperty("onloadeddata", this.onloadeddata);
	}

	public String getOnloadedmetadata() {
		return getStringProperty("onloadedmetadata", this.onloadedmetadata);
	}

	public String getOnloadstart() {
		return getStringProperty("onloadstart", this.onloadstart);
	}

	public String getOnpause() {
		return getStringProperty("onpause", this.onpause);
	}

	public String getOnplay() {
		return getStringProperty("onplay", this.onplay);
	}

	public String getOnplaying() {
		return getStringProperty("onplaying", this.onplaying);
	}

	public String getOnprogress() {
		return getStringProperty("onprogress", this.onprogress);
	}

	public String getOnratechange() {
		return getStringProperty("onratechange", this.onratechange);
	}

	public String getOnseeked() {
		return getStringProperty("onseeked", this.onseeked);
	}

	public String getOnseeking() {
		return getStringProperty("onseeking", this.onseeking);
	}

	public String getOnstalled() {
		return getStringProperty("onstalled", this.onstalled);
	}

	public String getOnsuspend() {
		return getStringProperty("onsuspend", this.onsuspend);
	}

	public String getOntimeupdate() {
		return getStringProperty("ontimeupdate", this.ontimeupdate);
	}

	public String getOnvolumechange() {
		return getStringProperty("onvolumechange", this.onvolumechange);
	}

	public String getOnwaiting() {
		return getStringProperty("onwaiting", this.onwaiting);
	}

	@Override
	public void restoreState(FacesContext context, Object state) {
		Object[] properties = (Object[]) state;
		int idx = 0;
		super.restoreState(context, properties[idx++]);
		this.onabort = ((String) properties[idx++]);
		this.oncanplay = ((String) properties[idx++]);
		this.oncanplaythrough = ((String) properties[idx++]);
		this.ondurationchange = ((String) properties[idx++]);
		this.onemptied = ((String) properties[idx++]);
		this.onended = ((String) properties[idx++]);
		this.onerror = ((String) properties[idx++]);
		this.onloadeddata = ((String) properties[idx++]);
		this.onloadedmetadata = ((String) properties[idx++]);
		this.onloadstart = ((String) properties[idx++]);
		this.onpause = ((String) properties[idx++]);
		this.onplay = ((String) properties[idx++]);
		this.onplaying = ((String) properties[idx++]);
		this.onprogress = ((String) properties[idx++]);
		this.onratechange = ((String) properties[idx++]);
		this.onseeked = ((String) properties[idx++]);
		this.onseeking = ((String) properties[idx++]);
		this.onstalled = ((String) properties[idx++]);
		this.onsuspend = ((String) properties[idx++]);
		this.ontimeupdate = ((String) properties[idx++]);
		this.onvolumechange = ((String) properties[idx++]);
		this.onwaiting = ((String) properties[idx++]);
	}

	@Override
	public Object saveState(FacesContext context) {
		Object[] properties = new Object[8];
		int idx = 0;
		properties[idx++] = super.saveState(context);
		properties[idx++] = this.onabort;
		properties[idx++] = this.oncanplay;
		properties[idx++] = this.oncanplaythrough;
		properties[idx++] = this.ondurationchange;
		properties[idx++] = this.onemptied;
		properties[idx++] = this.onended;
		properties[idx++] = this.onerror;
		properties[idx++] = this.onloadeddata;
		properties[idx++] = this.onloadedmetadata;
		properties[idx++] = this.onloadstart;
		properties[idx++] = this.onpause;
		properties[idx++] = this.onplay;
		properties[idx++] = this.onplaying;
		properties[idx++] = this.onprogress;
		properties[idx++] = this.onratechange;
		properties[idx++] = this.onseeked;
		properties[idx++] = this.onseeking;
		properties[idx++] = this.onstalled;
		properties[idx++] = this.onsuspend;
		properties[idx++] = this.ontimeupdate;
		properties[idx++] = this.onvolumechange;
		properties[idx++] = this.onwaiting;
		return properties;
	}

	public void setOnabort(String onabort) {
		this.onabort = onabort;
	}

	public void setOncanplay(String oncanplay) {
		this.oncanplay = oncanplay;
	}

	public void setOncanplaythrough(String oncanplaythrough) {
		this.oncanplaythrough = oncanplaythrough;
	}

	public void setOndurationchange(String ondurationchange) {
		this.ondurationchange = ondurationchange;
	}

	public void setOnemptied(String onemptied) {
		this.onemptied = onemptied;
	}

	public void setOnended(String onended) {
		this.onended = onended;
	}

	public void setOnerror(String onerror) {
		this.onerror = onerror;
	}

	public void setOnloadeddata(String onloadeddata) {
		this.onloadeddata = onloadeddata;
	}

	public void setOnloadedmetadata(String onloadedmetadata) {
		this.onloadedmetadata = onloadedmetadata;
	}

	public void setOnloadstart(String onloadstart) {
		this.onloadstart = onloadstart;
	}

	public void setOnpause(String onpause) {
		this.onpause = onpause;
	}

	public void setOnplay(String onplay) {
		this.onplay = onplay;
	}

	public void setOnplaying(String onplaying) {
		this.onplaying = onplaying;
	}

	public void setOnprogress(String onprogress) {
		this.onprogress = onprogress;
	}

	public void setOnratechange(String onratechange) {
		this.onratechange = onratechange;
	}

	public void setOnseeked(String onseeked) {
		this.onseeked = onseeked;
	}

	public void setOnseeking(String onseeking) {
		this.onseeking = onseeking;
	}

	public void setOnstalled(String onstalled) {
		this.onstalled = onstalled;
	}

	public void setOnsuspend(String onsuspend) {
		this.onsuspend = onsuspend;
	}

	public void setOntimeupdate(String ontimeupdate) {
		this.ontimeupdate = ontimeupdate;
	}

	public void setOnvolumechange(String onvolumechange) {
		this.onvolumechange = onvolumechange;
	}

	public void setOnwaiting(String onwaiting) {
		this.onwaiting = onwaiting;
	}
}
