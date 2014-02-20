package servlet;

import java.io.IOException;
import java.io.PrintStream;

import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.ibm.xsp.webapp.DesignerFacesServlet;

public abstract class AbstractXSPServlet extends DesignerFacesServlet {
	@Override
	public void service(final ServletRequest servletRequest, final ServletResponse servletResponse) throws ServletException, IOException {
		HttpServletResponse res = (HttpServletResponse) servletResponse;
		HttpServletRequest req = (HttpServletRequest) servletRequest;

		res.setContentType("text/plain");
		ServletOutputStream out = res.getOutputStream();
		FacesContext facesContext = this.getFacesContext(req, res);

		try {
			doService(req, res, facesContext, out);
		} catch (Exception e) {
			e.printStackTrace(new PrintStream(out));
		} finally {
			facesContext.responseComplete();
			facesContext.release();
			out.close();
		}
	}

	protected abstract void doService(final HttpServletRequest req, final HttpServletResponse res, final FacesContext facesContext, final ServletOutputStream out) throws Exception;
}
