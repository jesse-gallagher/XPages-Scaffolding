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

import com.ibm.xsp.component.UIOutputEx;

public class OpenNTFComponent extends UIOutputEx {
	public static final String COMPONENT_FAMILY = "org.openntf.xsp.extlib";
	public static final String RENDERER_TYPE = "org.openntf.xsp.extlib.renderkit.OpenNTFRenderer";
	public static final String STYLEKIT_FAMILY = "OpenNTF.OpenNTF";

	public OpenNTFComponent() {
		setRendererType(RENDERER_TYPE);
	}

	@Override
	public void restoreState(FacesContext context, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(context, values[0]);
	}

	@Override
	public Object saveState(FacesContext context) {
		Object values[] = new Object[1];
		values[0] = super.saveState(context);
		return values;
	}

	@Override
	public String getFamily() {
		return COMPONENT_FAMILY;
	}

	@Override
	public String getStyleKitFamily() {
		return STYLEKIT_FAMILY;
	}
}
