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

public class ViewBean extends AbstractBean {

	private static final long serialVersionUID = -6588472053833575369L;

	public static ViewBean getCurrentInstance() {
		Object result = null;
		result = AbstractBean.getCurrentInstance("View");
		if (result instanceof ViewBean) {
			return (ViewBean) result;
		} else {
			return null;
		}
	}

	public ViewBean() {

	}
}
