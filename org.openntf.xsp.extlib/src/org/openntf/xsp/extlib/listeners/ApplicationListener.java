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
package org.openntf.xsp.extlib.listeners;

import org.openntf.xsp.extlib.Activator;

import com.ibm.xsp.application.ApplicationEx;

public class ApplicationListener implements com.ibm.xsp.application.events.ApplicationListener {
	public final static boolean ATTACH_LISTENER = true; // change this to false if you don't want to bother.
	private final static boolean _debug = Activator._debug;
	static {
		if (_debug)
			System.out.println(ApplicationListener.class.getName() + " loaded");
	}

	public ApplicationListener() {
		_debugOut("created");
	}

	public void applicationCreated(ApplicationEx paramApplicationEx) {
		_debugOut("applicationCreated triggered " + paramApplicationEx.getApplicationId());
		// your code goes here
	}

	public void applicationDestroyed(ApplicationEx paramApplicationEx) {
		_debugOut("applicationDestroyed triggered " + paramApplicationEx.getApplicationId());
		// your code goes here
	}

	private void _debugOut(String message) {
		if (_debug)
			System.out.println(getClass().getName() + " " + message);
	}

}
