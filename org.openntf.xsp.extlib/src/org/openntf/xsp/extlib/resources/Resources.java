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

import java.util.IdentityHashMap;
import javax.faces.context.FacesContext;

import com.ibm.xsp.component.UIViewRootEx;
import com.ibm.xsp.resource.DojoModuleResource;
import com.ibm.xsp.resource.Resource;
import com.ibm.xsp.resource.ScriptResource;
import com.ibm.xsp.resource.StyleSheetResource;

public class Resources {
	public static final DojoModuleResource openntfDojoModule1 = new DojoModuleResource("openntf.dojo.Module1");
	public static final DojoModuleResource openntfDojoModule2 = new DojoModuleResource("openntf.dojo.Module2");

	public static final StyleSheetResource openntfStyleSheet1 = new StyleSheetResource(ResourceProvider.RESOURCE_PATH
			+ "styles/openntfstyle1.css");
	public static final StyleSheetResource uiRibbonStyle = new StyleSheetResource(ResourceProvider.RESOURCE_PATH + "styles/ribbon.css");
	public static final StyleSheetResource uiProgress = new StyleSheetResource(ResourceProvider.RESOURCE_PATH + "styles/progress.css");
	public static final StyleSheetResource uiIOSBadge = new StyleSheetResource(ResourceProvider.RESOURCE_PATH + "styles/iosb.css");
	public static final StyleSheetResource czaruiBadges = new StyleSheetResource(ResourceProvider.RESOURCE_PATH + "styles/badges.css");
	
	public static final ScriptResource openntfClientLibrary1 = new ScriptResource(ResourceProvider.RESOURCE_PATH + "js/openntfLibrary1.js",
			true);
	public static final ScriptResource openntfClientLibrary2 = new ScriptResource(ResourceProvider.RESOURCE_PATH + "js/openntfLibrary2.js",
			true);
	public static final Resource[] openntfResourceCollection = { openntfDojoModule1, openntfDojoModule2, openntfStyleSheet1,
			openntfClientLibrary1, openntfClientLibrary2 };

	public static void addAllEncodeResources() {

	}

	public static void addEncodeResources(FacesContext context, Resource[] resources) {
		UIViewRootEx rootEx = (UIViewRootEx) context.getViewRoot();
		addEncodeResources(rootEx, resources);
	}

	public static void addEncodeResources(UIViewRootEx rootEx, Resource[] resources) {
		if (resources != null) {
			for (Resource resource : resources) {
				addEncodeResource(rootEx, resource);
			}
		}
	}

	public static void addEncodeResource(FacesContext context, Resource resource) {
		UIViewRootEx rootEx = (UIViewRootEx) context.getViewRoot();
		addEncodeResource(rootEx, resource);
	}

	@SuppressWarnings("unchecked")
	public static void addEncodeResource(UIViewRootEx rootEx, Resource resource) {
		IdentityHashMap<Resource, Boolean> m = (IdentityHashMap<Resource, Boolean>) rootEx.getEncodeProperty("genesis.EncodeResource");
		if (m == null) {
			m = new IdentityHashMap<Resource, Boolean>();
		} else {
			if (m.containsKey(resource)) {
				return;
			}
		}
		m.put(resource, Boolean.TRUE);

		rootEx.addEncodeResource(resource);
	}

}
