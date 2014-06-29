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
package org.openntf.xsp.extlib.resources;

import java.io.IOException;
import java.util.List;

import javax.faces.context.FacesContext;

import com.ibm.xsp.context.RequestParameters;
import com.ibm.xsp.resource.Resource;

public class RequestCustomizer implements RequestParameters.ResourceProvider {

	public static final RequestCustomizer instance = new RequestCustomizer();

	private static List<Resource> resources;

	public List<Resource> getResources(FacesContext context) throws IOException {
		if (resources == null) {

		}
		return resources;
	}

}
