package frostillicus.xsp.controller;


import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.event.PhaseEvent;

import frostillicus.xsp.util.FrameworkUtils;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.component.UIViewRootEx;

import org.openntf.domino.*;

import java.util.*;

/**
 * @since 1.0
 */
public class ControllingViewHandler extends com.ibm.xsp.application.ViewHandlerExImpl {
	public static final String BEAN_NAME = "controller";

	public ControllingViewHandler(final ViewHandler delegate) {
		super(delegate);
	}

	@SuppressWarnings("unchecked")
	@Override
	public UIViewRoot createView(final FacesContext context, final String pageName) {
		// Page name is in the format "/home"
		String truePageName = pageName;
		String pageClassName = pageName.substring(1);
		if(pageClassName.contains(".xsp")) {
			pageClassName = FrameworkUtils.strLeft(pageClassName, ".xsp");
		}

		if(pageClassName.equalsIgnoreCase("$$OpenDominoDocument")) {
			// Handle the virtual document page. There may be a better way to do this, but this will do for now
			Database database = FrameworkUtils.getDatabase();
			Map<String, String> param = (Map<String, String>)resolveVariable(context, "param");
			String documentId = param.get("documentId");
			if(StringUtil.isNotEmpty(documentId)) {
				Document doc = database.getDocumentByUNID(param.get("documentId"));
				if(doc == null) {
					// Could be a Note ID
					doc = database.getDocumentByID(documentId);
				}
				if(doc != null) {
					String formName = doc.getItemValueString("Form");
	
					// Now that we have the form, look for the XPageAlt
					Form form = database.getForm(formName);
					String formURL = form.getURL();
					String formUNID = FrameworkUtils.strRightBack(FrameworkUtils.strLeftBack(formURL, "?Open"), "/");
					Document formDoc = database.getDocumentByUNID(formUNID);
					String xpageAlt = formDoc.getItemValueString("$XPageAlt");
	
					if(StringUtil.isEmpty(xpageAlt)) {
						pageClassName = formName;
					} else {
						pageClassName = FrameworkUtils.strLeftBack(xpageAlt, ".xsp");
					}
					truePageName = "/" + pageClassName;
				}
			}
		}

		Class<? extends XPageController> controllerClass = null;
		try {
			controllerClass = (Class<? extends XPageController>)Class.forName("controller." + pageClassName, true, context.getContextClassLoader());
		} catch(ClassNotFoundException cnfe) {
			controllerClass = BasicXPageController.class;
		}
		UIViewRootEx root = null;
		try {
			XPageController pageController = controllerClass.newInstance();
			Map<String, Object> requestScope = (Map<String, Object>)resolveVariable(context, "requestScope");
			requestScope.put(BEAN_NAME, pageController);

			root = (UIViewRootEx)super.createView(FacesContext.getCurrentInstance(), truePageName);
			root.getViewMap().put(BEAN_NAME, pageController);
			requestScope.remove(BEAN_NAME);

			if(root.getBeforeRenderResponse() == null) {
				MethodBinding beforeRenderResponse = context.getApplication().createMethodBinding("#{" + BEAN_NAME + ".beforeRenderResponse}", new Class[] { PhaseEvent.class });
				root.setBeforeRenderResponse(beforeRenderResponse);
			}

			if(root.getAfterRenderResponse() == null) {
				MethodBinding afterRenderResponse = context.getApplication().createMethodBinding("#{" + BEAN_NAME + ".afterRenderResponse}", new Class[] { PhaseEvent.class });
				root.setAfterRenderResponse(afterRenderResponse);
			}

			if(root.getAfterRestoreView() == null) {
				MethodBinding afterRestoreView = context.getApplication().createMethodBinding("#{" + BEAN_NAME + ".afterRestoreView}", new Class[] { PhaseEvent.class });
				root.setAfterRestoreView(afterRestoreView);
			}
		} catch(Exception e) {
			e.printStackTrace();
			root = (UIViewRootEx)super.createView(context, truePageName);
		}

		return root;
	}

	private static Object resolveVariable(final FacesContext context, final String varName) {
		return context.getApplication().getVariableResolver().resolveVariable(context, varName);
	}
}