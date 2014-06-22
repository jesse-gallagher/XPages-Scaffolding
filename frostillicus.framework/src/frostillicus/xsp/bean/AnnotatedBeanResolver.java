package frostillicus.xsp.bean;

import java.io.Serializable;
import java.util.*;

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.VariableResolver;

import org.openntf.domino.*;
import org.openntf.domino.design.*;

import com.ibm.commons.util.StringUtil;

import frostillicus.xsp.bean.ApplicationScoped;
import frostillicus.xsp.bean.ManagedBean;
import frostillicus.xsp.bean.RequestScoped;
import frostillicus.xsp.bean.SessionScoped;
import frostillicus.xsp.bean.ViewScoped;

public class AnnotatedBeanResolver extends VariableResolver {

	private final VariableResolver delegate_;

	public AnnotatedBeanResolver(final VariableResolver resolver) {
		delegate_ = resolver;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object resolveVariable(final FacesContext facesContext, final String name) throws EvaluationException {
		if(name == null) {
			return null;
		}
		// Check the delegate first, since this adds the beans to the appropriate scope as needed
		if(delegate_ != null) {
			Object existing = delegate_.resolveVariable(facesContext, name);
			if(existing != null) {
				return existing;
			}
		}

		if(delegate_ != null) {
			try {
				// If the main resolver couldn't find it, check our annotated managed beans
				Map<String, Object> applicationScope = (Map<String, Object>)delegate_.resolveVariable(facesContext, "applicationScope");
				if(!applicationScope.containsKey("$$annotatedManagedBeanMap")) {
					Map<String, BeanInfo> beanMap = new HashMap<String, BeanInfo>();

					Database database = (Database)delegate_.resolveVariable(facesContext, "database");
					DatabaseDesign design = database.getDesign();
					for(String className : design.getJavaResourceClassNames()) {
						Class<?> loadedClass = facesContext.getContextClassLoader().loadClass(className);
						ManagedBean beanAnnotation = loadedClass.getAnnotation(ManagedBean.class);
						if(beanAnnotation != null) {
							BeanInfo info = new BeanInfo();
							info.className = loadedClass.getName();
							if(loadedClass.isAnnotationPresent(ApplicationScoped.class)) {
								info.scope = "application";
							} else if(loadedClass.isAnnotationPresent(SessionScoped.class)) {
								info.scope = "session";
							} else if(loadedClass.isAnnotationPresent(ViewScoped.class)) {
								info.scope = "view";
							} else if(loadedClass.isAnnotationPresent(RequestScoped.class)) {
								info.scope = "request";
							} else {
								info.scope = "none";
							}

							if(!StringUtil.isEmpty(beanAnnotation.name())) {
								beanMap.put(beanAnnotation.name(), info);
							} else {
								beanMap.put(loadedClass.getSimpleName(), info);
							}
						}
					}

					applicationScope.put("$$annotatedManagedBeanMap", beanMap);
				}

				// Now that we know we have a built map, look for the requested name
				Map<String, BeanInfo> beanMap = (Map<String, BeanInfo>)applicationScope.get("$$annotatedManagedBeanMap");
				if(beanMap.containsKey(name)) {
					BeanInfo info = beanMap.get(name);
					Class<?> loadedClass = facesContext.getContextClassLoader().loadClass(info.className);
					// Check its scope
					if("none".equals(info.scope)) {
						return loadedClass.newInstance();
					} else {
						Map<String, Object> scope = (Map<String, Object>)delegate_.resolveVariable(facesContext, info.scope + "Scope");
						if(!scope.containsKey(name)) {
							scope.put(name, loadedClass.newInstance());
						}
						return scope.get(name);
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		return null;
	}

	private static class BeanInfo implements Serializable {
		private static final long serialVersionUID = 1L;

		String className;
		String scope;
	}
}