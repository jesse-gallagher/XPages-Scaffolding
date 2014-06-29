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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openntf.xsp.extlib.Activator;
import org.openntf.xsp.extlib.util.LibraryUtils;

import com.ibm.xsp.webapp.FacesResourceServlet;
import com.ibm.xsp.webapp.resources.BundleResourceProvider;
import com.ibm.xsp.webapp.resources.URLResourceProvider;

public class ResourceProvider extends BundleResourceProvider {
	private final static boolean _debug = Activator._debug;
	static {
		if (_debug)
			System.out.println(ResourceProvider.class.getName() + " loaded");
	}
	public static final String BUNDLE_RES_PATH = "/resources/web/";
	public static final String OPENNTF_PREFIX = "." + ModulePath.NAMESPACE
			+ "/" + Activator.getVersion();

	public static final String RESOURCE_PATH = FacesResourceServlet.RESOURCE_PREFIX
			+ ResourceProvider.OPENNTF_PREFIX + "/";
	public static final String DOJO_PATH = FacesResourceServlet.RESOURCE_PREFIX
			+ ResourceProvider.OPENNTF_PREFIX;
	private static final long LAST_MODDATE = Activator.instance.getBundle()
			.getLastModified();
	protected final Map<String, CacheableResource> resources = new HashMap<String, CacheableResource>();

	public ResourceProvider() {
		super(Activator.instance.getBundle(), OPENNTF_PREFIX);
		if (_debug) {
			System.out.println(getClass().getName() + " created");
		}
	}

	@Override
	protected boolean shouldCacheResources() {
		return true;
	}

	@Override
	protected URL getResourceURL(HttpServletRequest request, String name) {
		String path = BUNDLE_RES_PATH + name;
		URL resourcePath = LibraryUtils.getResourceURL(getBundle(), path);
		return resourcePath;
	}

	@Override
	public synchronized URLResource addResource(String paramString, URL paramURL) {
		CacheableResource localURLResource = new CacheableResource(paramString,
				paramURL);
		if (shouldCacheResources()) {
			this.resources.put(paramString, localURLResource);
		}
		return localURLResource;
	}

	protected static long getLastModificationDate() {
		return LAST_MODDATE;
	}

	protected class CacheableResource extends URLResourceProvider.URLResource {

		protected CacheableResource(String paramString, URL paramURL) {
			super(paramString, paramURL);
		}

		@Override
		protected long getLastModificationDate() {
			return ResourceProvider.getLastModificationDate();
		}

		@Override
		protected boolean isResourcesModifiedSince(long paramLong) {
			long l = getLastModificationDate();
			if (l >= 0L) {
				return (paramLong < l);
			}
			return true;
		}

	}

}
