package frostillicus.xsp.model.servlet;

import javax.servlet.ServletException;
import com.ibm.domino.services.AbstractRestServlet;

public class ModelServlet extends AbstractRestServlet {
	private static final long serialVersionUID = 1L;

	public static ModelServlet instance;

	public ModelServlet() {
		instance = this;
	}

	@Override
	protected void doInit() throws ServletException {
		super.doInit();
	}


}