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
package org.openntf.xsp.extlib.ssjs;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.ibm.commons.util.io.StreamUtil;
import com.ibm.jscript.ILibraryFactory;
import com.ibm.jscript.InterpretException;

public class OpenNTFFactory implements ILibraryFactory {
	private final static Map<String, String> _libs = new HashMap<String, String>();
	static {
		_libs.put("OpenNTF.jss", "/org/openntf/xsp/extlib/ssjs/OpenNTF.jss");
	}

	public String loadLibrary(String libName) throws InterpretException {
		String result = null;
		if (_libs.containsKey(libName)) {
			InputStream is = null;
			try {
				is = getClass().getClassLoader().getResourceAsStream(_libs.get(libName));
				result = StreamUtil.readString(is);
			} catch (IOException ex) {
				throw new InterpretException(ex);
			} finally {
				try {
					is.close();
				} catch (Exception e) {
				}
			}
		}
		return result;
	}

}
