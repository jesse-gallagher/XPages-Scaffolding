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

public class XspVideo extends AbstractHtml5MediaTag {
	public static final String COMPONENT_TYPE = "org.openntf.xsp.Video";
	public static final String RENDERER_TYPE = "org.openntf.xsp.Video";

	public XspVideo() {
		setRendererType(RENDERER_TYPE);
		setTagName("video");
	}

	@Override
	public void restoreState(FacesContext context, Object state) {
		Object[] properties = (Object[]) state;
		int idx = 0;
		super.restoreState(context, properties[idx++]);
	}

	@Override
	public Object saveState(FacesContext context) {
		Object[] properties = new Object[1];
		int idx = 0;
		properties[idx++] = super.saveState(context);
		return properties;
	}

}
