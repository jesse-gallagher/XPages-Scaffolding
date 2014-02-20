package frostillicus.controller;


import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.event.PhaseEvent;

import util.JSFUtil;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.component.UIViewRootEx;
import com.ibm.xsp.extlib.util.ExtLibUtil;
//import com.ibm.xsp.model.domino.DominoDocumentData;

import lotus.domino.*;
import java.util.*;

public class ControllingViewHandler extends com.ibm.xsp.application.ViewHandlerExImpl {

	public ControllingViewHandler(ViewHandler arg0) {
		super(arg0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public UIViewRoot createView(FacesContext context, String pageName) {
		// Page name is in the format "/home"
		String pageClassName = pageName.substring(1);

		if(pageClassName.equalsIgnoreCase("$$OpenDominoDocument")) {
			// Handle the virtual document page. There may be a better way to do this, but this will do for now
			try {
				Database database = ExtLibUtil.getCurrentDatabase();
				Map<String, String> param = (Map<String, String>)ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "param");
				Document doc = database.getDocumentByUNID(param.get("documentId"));
				if(doc != null) {
					String formName = doc.getItemValueString("Form");
					doc.recycle();

					// Now that we have the form, look for the XPageAlt
					Form form = database.getForm(formName);
					String formURL = form.getURL();
					String formUNID = JSFUtil.strRightBack(JSFUtil.strLeftBack(formURL, "?Open"), "/");
					Document formDoc = database.getDocumentByUNID(formUNID);
					String xpageAlt = formDoc.getItemValueString("$XPageAlt");
					formDoc.recycle();
					form.recycle();

					if(StringUtil.isEmpty(xpageAlt)) {
						pageClassName = formName;
					} else {
						pageClassName = JSFUtil.strLeftBack(xpageAlt, ".xsp");
					}
				}
			} catch(NotesException ne) { ne.printStackTrace();}
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
			ExtLibUtil.getRequestScope().put("pageController", pageController);

			root = (UIViewRootEx)super.createView(context, pageName);
			ExtLibUtil.getViewScope().put("pageController", pageController);
			ExtLibUtil.getRequestScope().remove("pageController");

			MethodBinding beforeRenderResponse = context.getApplication().createMethodBinding("#{pageController.beforeRenderResponse}", new Class[] { PhaseEvent.class });
			root.setBeforeRenderResponse(beforeRenderResponse);

			MethodBinding afterRenderResponse = context.getApplication().createMethodBinding("#{pageController.afterRenderResponse}", new Class[] { PhaseEvent.class });
			root.setAfterRenderResponse(afterRenderResponse);

			MethodBinding afterRestoreView = context.getApplication().createMethodBinding("#{pageController.afterRestoreView}", new Class[] { PhaseEvent.class });
			root.setAfterRestoreView(afterRestoreView);
		} catch(Exception e) {
			e.printStackTrace();
			root = (UIViewRootEx)super.createView(context, pageName);
		}

		return root;
	}
}
