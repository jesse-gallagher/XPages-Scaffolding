package org.openntf.xsp.extlib.servlet;

import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.openntf.xsp.extlib.Activator;

import com.ibm.commons.util.PathUtil;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.IServletFactory;
import com.ibm.designer.runtime.domino.adapter.ServletMatch;

public class ServletFactory implements IServletFactory {
	/*
	 * NTF - This class loads and constructs, but I have not yet figured out how to make it be the Factory for the primary servlets
	 */
	private final static boolean _debug = Activator._debug;
	static {
		if (_debug)
			System.out.println(ServletFactory.class.getName() + " loaded");
	}

	private final String _pathInfo1;
	private final String _pathInfo2;
	private final String _servletClass;
	private final String _servletName;

	private ComponentModule _module;
	private Servlet _servlet;

	public ServletFactory(String pathInfo, String servletClass, String servletName) {
		if (_debug) {
			System.out.println(getClass().getName() + " created with pathInfo " + pathInfo + ", servletClass: " + servletClass
					+ ", servletName: " + servletName);
		}
		pathInfo = PathUtil.concatPath("/xsp", pathInfo, '/'); // $NON-NLS-1$
		this._pathInfo1 = pathInfo;
		this._pathInfo2 = pathInfo + "/";
		this._servletClass = servletClass;
		this._servletName = servletName;
	}

	public ServletFactory() {
		if (_debug) {
			System.out.println(getClass().getName() + " created with defaults");
		}
		this._servletClass = "org.openntf.xsp.extlib.servlet.OpenNTFServlet";
		this._servletName = "OpenNTFServlet";
		this._pathInfo1 = "/xsp";
		this._pathInfo2 = "/xsp/";
	}

	public void init(ComponentModule paramComponentModule) {
		_module = paramComponentModule;
	}

	public ServletMatch getServletMatch(String contextPath, String path) throws ServletException {
		if (path.equals(_pathInfo1) || path.startsWith(_pathInfo2)) {
			String servletPath = _pathInfo1;
			String pathInfo = path.substring(_pathInfo1.length());
			return new ServletMatch(getServlet(), servletPath, pathInfo);
		}
		return null;
	}

	public Servlet getServlet() throws ServletException {
		if (_servlet == null) {
			synchronized (this) {
				if (_servlet == null) {
					_servlet = createServlet();
				}
			}
		}
		return _servlet;
	}

	protected Servlet createServlet() throws ServletException {
		if (_debug) {
			System.out.println(getClass().getName() + " creating servlet");
		}
		Servlet servlet = _module.createServlet(_servletClass, _servletName, (Map<String, String>) null /* params */);
		return servlet;
	}
}
