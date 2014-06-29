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

import java.io.IOException;

import javax.faces.FacesException;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.openntf.xsp.extlib.Activator;

import com.ibm.xsp.application.StateManagerImpl;
import com.ibm.xsp.util.Delegation;

public class StateManager extends StateManagerImpl {
	private final javax.faces.application.StateManager _delegate;
	private final static boolean _debug = Activator._debug;

	public StateManager(javax.faces.application.StateManager paramStateManager) {
		super(paramStateManager);
		_delegate = paramStateManager;
		if (_debug) {
			System.out.println(getClass().getName() + " created using " + (_delegate != null ? _delegate.getClass().getName() : "null"));
		}
	}

	public StateManager() throws FacesException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		javax.faces.application.StateManager priorStateManager = ((javax.faces.application.StateManager) Delegation
				.getImplementation("state-manager"));
		this._delegate = priorStateManager;
		if (_debug) {
			System.out.println(getClass().getName() + " created empty using "
					+ (_delegate != null ? _delegate.getClass().getName() : "null"));
		}
	}

	@Override
	public void writeState(FacesContext paramFacesContext, StateManager.SerializedView paramSerializedView) throws IOException {
		// your code goes here
		super.writeState(paramFacesContext, paramSerializedView);
	}

	@Override
	public UIViewRoot restoreView(FacesContext paramFacesContext, String paramString1, String paramString2) {
		// your code goes here
		return super.restoreView(paramFacesContext, paramString1, paramString2);
	}
}
