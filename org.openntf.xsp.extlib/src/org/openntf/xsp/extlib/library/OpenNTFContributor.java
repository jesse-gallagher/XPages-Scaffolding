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

import org.openntf.xsp.extlib.el.OpenNTFELBindingFactoryImpl;
import org.openntf.xsp.extlib.implicit.ImplicitObjectFactory;

import com.ibm.xsp.library.XspContributor;

public class OpenNTFContributor extends XspContributor {
	public static final String OPENNTF_IMPLICITOBJECTS_FACTORY = "org.openntf.xsp.extlib.implicit.IMPLICIT_OBJECT_FACTORY";
	public static final String OPENNTF_ELBINDING_FACTORY = "org.openntf.xsp.extlib.implicit.ELBINDING_FACTORY";

	public OpenNTFContributor() {

	}

	@Override
	public Object[][] getFactories() {
		return new Object[][] { { OPENNTF_IMPLICITOBJECTS_FACTORY, ImplicitObjectFactory.class },
				{ OPENNTF_ELBINDING_FACTORY, OpenNTFELBindingFactoryImpl.class } };
	}
}
