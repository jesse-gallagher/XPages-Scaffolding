package org.openntf.xsp.extlib.servlet;

import org.openntf.xsp.extlib.Activator;

import com.ibm.xsp.webapp.DesignerFacesServlet;

public class OpenNTFServlet extends DesignerFacesServlet {
	private final static boolean _debug = Activator._debug;
	static {
		if (_debug)
			System.out.println(OpenNTFServlet.class.getName() + " loaded");
	}

	public OpenNTFServlet() {
		if (_debug) {
			System.out.println(getClass().getName() + " created");
		}
	}

}
