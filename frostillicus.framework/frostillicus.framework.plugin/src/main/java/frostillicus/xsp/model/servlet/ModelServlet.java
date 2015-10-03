package frostillicus.xsp.model.servlet;

import javax.servlet.ServletException;
import com.ibm.domino.services.AbstractRestServlet;

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


}