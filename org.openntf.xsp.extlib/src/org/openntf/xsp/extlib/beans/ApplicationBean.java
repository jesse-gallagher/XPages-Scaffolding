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
package org.openntf.xsp.extlib.beans;

import java.io.Serializable;

import org.openntf.xsp.extlib.application.XspRegistry;

public class ApplicationBean extends AbstractBean {

	private static final long serialVersionUID = 8889085634851461653L;
	private XspRegistry _xspRegistry;

	public static ApplicationBean getCurrentInstance() {
		Object result = null;
		result = AbstractBean.getCurrentInstance("Application");
		if (result instanceof ApplicationBean) {
			return (ApplicationBean) result;
		} else {
			return null;
		}
	}

	public ApplicationBean() {

	}

	public XspRegistry getXspRegistry() {
		if (_xspRegistry == null) {
			_xspRegistry = new XspRegistry();
		}
		return _xspRegistry;
	}

	public ServerBean getServerScope() {
		return ServerBean.getCurrentInstance();
	}

	public Serializable getServerVar(Object key) {
		return getServerScope().get(key);
	}

	public Serializable putServerVar(Object key, Serializable value) {
		return getServerScope().put(key, value);
	}

}
