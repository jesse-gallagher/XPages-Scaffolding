package frostillicus.controller;


import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.event.PhaseEvent;

import util.JSFUtil;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.component.UIViewRootEx;

import org.openntf.domino.*;
import java.util.*;

public class ControllingViewHandler extends com.ibm.xsp.application.ViewHandlerExImpl {

	public ControllingViewHandler(final ViewHandler delegate) {
		super(delegate);
	}

	@SuppressWarnings("unchecked")
	@Override
	public UIViewRoot createView(final FacesContext context, final String pageName) {
		// Page name is in the format "/home"
		String pageClassName = pageName.substring(1);

		if(pageClassName.equalsIgnoreCase("$$OpenDominoDocument")) {
			// Handle the virtual document page. There may be a better way to do this, but this will do for now
			Database database = (Database)resolveVariable(context, "database");
			Map<String, String> param = (Map<String, String>)resolveVariable(context, "param");
			Document doc = database.getDocumentByUNID(param.get("documentId"));
			if(doc != null) {
				String formName = doc.getItemValueString("Form");

				// Now that we have the form, look for the XPageAlt
				Form form = database.getForm(formName);
				String formURL = form.getURL();
				String formUNID = JSFUtil.strRightBack(JSFUtil.strLeftBack(formURL, "?Open"), "/");
				Document formDoc = database.getDocumentByUNID(formUNID);
				String xpageAlt = formDoc.getItemValueString("$XPageAlt");

				if(StringUtil.isEmpty(xpageAlt)) {
					pageClassName = formName;
				} else {
					pageClassName = JSFUtil.strLeftBack(xpageAlt, ".xsp");
				}
			}
		}

		Class<? extends XPageController> controllerClass = null;
		try {
			controllerClass = (Class<? extends XPageController>)Class.forName("controller." + pageClassName);
		} catch(ClassNotFoundException cnfe) {
			controllerClass = BasicXPageController.class;
		}
		UIViewRootEx root = null;
		try {
			XPageController pageController = controllerClass.newInstance();
			Map<String, Object> requestScope = (Map<String, Object>)resolveVariable(context, "requestScope");
			requestScope.put("controller", pageController);

			root = (UIViewRootEx)super.createView(context, pageName);
			root.getViewMap().put("controller", pageController);
			requestScope.remove("controller");

			MethodBinding beforeRenderResponse = context.getApplication().createMethodBinding("#{controller.beforeRenderResponse}", new Class[] { PhaseEvent.class });
			root.setBeforeRenderResponse(beforeRenderResponse);

			MethodBinding afterRenderResponse = context.getApplication().createMethodBinding("#{controller.afterRenderResponse}", new Class[] { PhaseEvent.class });
			root.setAfterRenderResponse(afterRenderResponse);

			MethodBinding afterRestoreView = context.getApplication().createMethodBinding("#{controller.afterRestoreView}", new Class[] { PhaseEvent.class });
			root.setAfterRestoreView(afterRestoreView);
		} catch(Exception e) {
			e.printStackTrace();
			root = (UIViewRootEx)super.createView(context, pageName);
		}

		return root;
	}

	private static Object resolveVariable(final FacesContext context, final String varName) {
		return context.getApplication().getVariableResolver().resolveVariable(context, varName);
	}
}