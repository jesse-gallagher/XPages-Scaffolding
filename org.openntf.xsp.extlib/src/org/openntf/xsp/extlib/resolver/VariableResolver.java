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
package org.openntf.xsp.extlib.resolver;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;

import org.openntf.xsp.extlib.Activator;

import com.ibm.xsp.util.Delegation;

public class VariableResolver extends javax.faces.el.VariableResolver {
	protected final javax.faces.el.VariableResolver _resolver;
	private final static boolean _debug = Activator._debug;

	public VariableResolver() throws FacesException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		this._resolver = ((javax.faces.el.VariableResolver) Delegation.getImplementation("variable-resolver"));
		if (_debug) {
			System.out.println(getClass().getName() + " created using " + (_resolver != null ? _resolver.getClass().getName() : "null"));
		}
	}

	public VariableResolver(javax.faces.el.VariableResolver resolver) {
		if (_debug) {
			System.out.println(getClass().getName() + " created from delegate " + resolver.getClass().getName());
		}
		_resolver = resolver;
	}

	@Override
	public Object resolveVariable(FacesContext paramFacesContext, String paramString) throws EvaluationException {
		// your code goes here
		return _resolver.resolveVariable(paramFacesContext, paramString);
	}

}
