package frostillicus.xsp.servlet;

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

/**
 * AbstractXSPServlet extends DesignerFacesServlet by wrapping the overridden service method through an abstracted
 * doService method, which is invoked by the former, to always enforce a closing of the OutputStream. The order of
 * invokation means that during your overriding of the abstracted doService method, you can set the Content-Type
 * to anything you like (defaults to 'text/plain'), along with any other FacesContext based work you need, such
 * as establishing a handle on the given user's lotus.domino.Session, as it passes the handle to the FacesContext
 * as a parameter in the abstracted doService method.
 * 
 * For further detail, please consult Jesse's blog post on the subject:
 * https://frostillic.us/blog/posts/D815DC7ED059395885257D6B00001006
 */
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
