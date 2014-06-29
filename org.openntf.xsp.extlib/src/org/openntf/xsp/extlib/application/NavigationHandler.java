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

package org.openntf.xsp.extlib.application;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;

import org.openntf.xsp.extlib.Activator;

import com.ibm.xsp.application.NavigationHandlerImpl;
import com.ibm.xsp.util.Delegation;

public class NavigationHandler extends javax.faces.application.NavigationHandler {
	private final javax.faces.application.NavigationHandler _delegate;
	private final static boolean _debug = Activator._debug;

	public NavigationHandler(javax.faces.application.NavigationHandler handler) throws FacesException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		if (handler == null) {
			javax.faces.application.NavigationHandler priorHandler = ((javax.faces.application.NavigationHandler) Delegation
					.getImplementation("navigation-handler"));
			if (priorHandler == null) {
				priorHandler = new NavigationHandlerImpl(new com.sun.faces.application.NavigationHandlerImpl());
			}
			_delegate = priorHandler;
		} else {
			_delegate = handler;
		}
		if (_debug) {
			System.out.println(getClass().getName() + " created using " + (_delegate != null ? _delegate.getClass().getName() : "null"));
		}
	}

	@Override
	public void handleNavigation(FacesContext paramFacesContext, String paramString1, String paramString2) {
		// your code goes here
		_delegate.handleNavigation(paramFacesContext, paramString1, paramString2);

	}

}
