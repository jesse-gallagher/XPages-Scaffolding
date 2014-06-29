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
package org.openntf.xsp.extlib.library;

import org.openntf.xsp.extlib.Activator;

import com.ibm.xsp.library.AbstractXspLibrary;

public class OpenNTFLibrary extends AbstractXspLibrary {
	private final static String LIBRARY_ID = OpenNTFLibrary.class.getName();
	// change this string to establish a namespace for your resources:
	public final static String LIBRARY_RESOURCE_NAMESPACE = "OpenNTF";
	public final static String LIBRARY_BEAN_PREFIX = "OpenNTF";
	private final static boolean _debug = Activator._debug;

	static {
		if (_debug) {
			System.out.println(OpenNTFLibrary.class.getName() + " loaded");
		}
	}

	public OpenNTFLibrary() {
		if (_debug) {
			System.out.println(OpenNTFLibrary.class.getName() + " created");
		}
	}

	public String getLibraryId() {
		return LIBRARY_ID;
	}

	@Override
	public String getPluginId() {
		return Activator.PLUGIN_ID;
	}

	@Override
	public String[] getDependencies() {
		return new String[] { "com.ibm.xsp.core.library",
				"com.ibm.xsp.extsn.library", "com.ibm.xsp.domino.library",
				"com.ibm.xsp.designer.library" };
	}

	@Override
	public String[] getXspConfigFiles() {
		String[] files = new String[] { "META-INF/openntf.xsp-config",
				"META-INF/html.xsp-config", "META-INF/canvas.xsp-config",
				"META-INF/uiRibbon.xsp-config",
				"META-INF/uiProgress.xsp-config",
				"META-INF/uiStackOverflowBadge.xsp-config",
				"META-INF/uiIOSBadge.xsp-config",
				"META-INF/html-list.xsp-config" };

		return files;
	}

	@Override
	public String[] getFacesConfigFiles() {
		String[] files = new String[] { "META-INF/openntf-faces-config.xml",
				"META-INF/canvas-faces-config.xml",
				"META-INF/uiRibbon-faces-config.xml",
				"META-INF/uiProgress-faces-config.xml",
				"META-INF/uiStackOverflowBadge-faces-config.xml",
				"META-INF/uiIOSBadge-faces-config.xml",
				"META-INF/html-list-faces-config.xml" };
		return files;
	}

	@Override
	public boolean isGlobalScope() {
		return false;
	}
}
