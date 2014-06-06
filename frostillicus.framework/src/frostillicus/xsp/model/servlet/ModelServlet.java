package frostillicus.xsp.model.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.designer.runtime.domino.adapter.servlet.LCDAdapterServletConfig;
import com.ibm.domino.xsp.module.nsf.NSFComponentModule;
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