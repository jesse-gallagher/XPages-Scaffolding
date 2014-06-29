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

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ValueChangeEvent;

import org.openntf.xsp.extlib.Activator;

public class ValueChangeListener extends AbstractListener implements javax.faces.event.ValueChangeListener {
	private final static boolean _debug = Activator._debug;
	static {
		if (_debug)
			System.out.println(ValueChangeListener.class.getName() + " loaded");
	}

	public ValueChangeListener() {
		_debugOut("created");
	}

	public void processValueChange(ValueChangeEvent paramValueChangeEvent) throws AbortProcessingException {
		// TODO Auto-generated method stub

	}

}
