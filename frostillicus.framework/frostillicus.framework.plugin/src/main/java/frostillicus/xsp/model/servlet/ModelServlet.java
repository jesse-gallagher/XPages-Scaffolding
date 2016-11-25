package frostillicus.xsp.model.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openntf.domino.AutoMime;
import org.openntf.domino.utils.Factory;
import org.openntf.domino.ext.Session.Fixes;
import org.openntf.domino.utils.Factory.ThreadConfig;

import com.ibm.domino.das.servlet.DasHttpResponseWrapper;
import com.ibm.domino.services.AbstractRestServlet;

/**
 * @since 1.0
 */
public class ModelServlet extends AbstractRestServlet {
	private static final long serialVersionUID = 1L;

	public static ModelServlet instance;

	@edu.umd.cs.findbugs.annotations.SuppressWarnings(
	                                                    value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
	                                                    justification="This is an intentional pattern")
	public ModelServlet() {
		instance = this;
	}

	@Override
	protected void doInit() throws ServletException {
		super.doInit();
	}

	@Override
	protected void doService(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		Factory.initThread(getDataServiceConfig());

		try {
			//Wrap the http response for Gzip/Deflate the output stream
			DasHttpResponseWrapper responseWrapper = new DasHttpResponseWrapper(request, response);

			super.doService(request, responseWrapper);
		} finally {
			Factory.termThread();
		}
	}

	protected ThreadConfig getDataServiceConfig() {
		Fixes[] fixes = Fixes.values();
		AutoMime autoMime = AutoMime.WRAP_32K;
		boolean bubbleExceptions = true;
		return new ThreadConfig(fixes, autoMime, bubbleExceptions);
	}
}