package frostillicus.xsp.servlet;

import javax.servlet.ServletException;

import com.ibm.designer.runtime.domino.adapter.*;

/**
 * @since 1.0
 */
public class ServletFactory implements IServletFactory {
	private ComponentModule module_;

	@Override
	public ServletMatch getServletMatch(final String contextPath, final String path) throws ServletException {
		String[] pieces = path.split("/");
		if(pieces.length >= 3) {
			// This matches servlet requests like foo.nsf/xsp/someServletClass => servlet.SomeServletClass

			String servletName = pieces[2];
			servletName = servletName.substring(0, 1).toUpperCase() + servletName.substring(1);

			try {
				Class<?> clazz = module_.getModuleClassLoader().loadClass("servlet." + servletName);
				return new ServletMatch(module_.createServlet(clazz.getName(), servletName, null), "", path);
			} catch(ClassNotFoundException e) {
				//System.out.println("servlet not found: " + servletName);
			}

		}
		return null;
	}

	@Override
	public void init(final ComponentModule module) {
		module_ = module;
	}

}
