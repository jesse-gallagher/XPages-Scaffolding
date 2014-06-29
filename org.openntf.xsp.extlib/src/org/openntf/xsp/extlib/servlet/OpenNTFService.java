package org.openntf.xsp.extlib.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletRequestAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletResponseAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpSessionAdapter;

public class OpenNTFService extends HttpService {

	public OpenNTFService(LCDEnvironment paramLCDEnvironment) {
		super(paramLCDEnvironment);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void getModules(List<ComponentModule> paramList) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean doService(String paramString1, String paramString2, HttpSessionAdapter paramHttpSessionAdapter,
			HttpServletRequestAdapter paramHttpServletRequestAdapter, HttpServletResponseAdapter paramHttpServletResponseAdapter)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		return false;
	}

}
