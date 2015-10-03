package frostillicus.xsp.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.openntf.domino.Database;
import org.openntf.domino.design.DatabaseDesign;
import org.openntf.domino.design.FacesConfig;

import com.ibm.commons.util.StringUtil;

import frostillicus.xsp.bean.ManagedBean;
import frostillicus.xsp.util.FrameworkUtils;

public enum ModelUtils {
	;

	public static Map<String, Object> getCacheScope() {
		Map<String, Object> scope = FrameworkUtils.getViewScope();
		return scope == null ? new HashMap<String, Object>() : scope;
	}

	public static boolean isUnid(final String value) {
		if (value.length() != 32) {
			return false;
		}
		return isHex(value);
	}

	public static boolean isHex(final String value) {
		String chk = value.trim().toLowerCase();
		for (int i = 0; i < chk.length(); i++) {
			char c = chk.charAt(i);
			boolean isHexDigit = Character.isDigit(c) || Character.isWhitespace(c) || c == 'a' || c == 'b' || c == 'c' || c == 'd' || c == 'e' || c == 'f';

			if (!isHexDigit) {
				return false;
			}

		}
		return true;
	}

	public static void publishException(final Exception e) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if(facesContext != null) {
			StringWriter out = new StringWriter();
			PrintWriter outWriter = new PrintWriter(out);
			e.printStackTrace(outWriter);
			outWriter.flush();

			FacesMessage message = new FacesMessage(out.toString());
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			facesContext.addMessage("", message);
		} else {
			if(e instanceof RuntimeException) {
				throw (RuntimeException)e;
			} else {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * 
	 * @param database
	 * 		The database to search for the model objects
	 * @param managerName
	 * 		Either the name of a Manager by class or managed bean name or the name of a Model class
	 * @return
	 * 		A Class object representing a Manager for the given name, or null if not found
	 */
	@SuppressWarnings("unchecked")
	public static Class<? extends ModelManager<?>> findModelManager(final Database database, final String managerName) {
		if(StringUtil.isEmpty(managerName)) { return null; }

		DatabaseDesign design = database.getDesign();
		ClassLoader loader;
		if(FrameworkUtils.isFaces()) {
			loader = FacesContext.getCurrentInstance().getContextClassLoader();
		} else {
			loader = design.getDatabaseClassLoader(Thread.currentThread().getContextClassLoader());
		}

		// First, see if we're dealing with a class name or bean name
		Class<?> referencedClass = null;
		if(managerName.contains(".")) {
			try {
				referencedClass = loader.loadClass(managerName);
				if(ModelManager.class.isAssignableFrom(referencedClass)) {
					// Then we're done immediately
					return (Class<? extends ModelManager<?>>)referencedClass;
				}
				// Otherwise, keep the referenced class around in case it's a model object
			} catch(ClassNotFoundException cnfe) {
				// Though we're likely doomed at this point, soldier on to the later tests
			}
		}

		// Now, search through the classes in the DB for any @ManagedBean-annotated classes that match
		for(String className : design.getJavaResourceClassNames()) {
			try {
				Class<?> loadedClass = loader.loadClass(className);
				if(isManager(loadedClass, managerName, referencedClass)) {
					return (Class<? extends ModelManager<?>>)loadedClass;
				}

			} catch(ClassNotFoundException cnfe) {
				// This happens when the note in the NSF contains an old class name - ignore
			}
		}

		// Now, look through faces-config-declared managed beans
		FacesConfig facesConfig = design.getFacesConfig();
		if(facesConfig != null) {
			for(FacesConfig.ManagedBean bean : facesConfig.getManagedBeans()) {
				if(StringUtil.isNotEmpty(bean.getClassName())) {
					try {
						Class<?> loadedClass = loader.loadClass(bean.getClassName());
						if(isManager(loadedClass, managerName, referencedClass)) {
							return (Class<? extends ModelManager<?>>)loadedClass;
						}
					} catch(ClassNotFoundException cnfe) {
						// This would be a config problem for the app, but we don't care here
					}
				}
			}
		}

		return null;
	}

	private static boolean isManager(final Class<?> loadedClass, final String managerName, final Class<?> referencedClass) {
		// There are two ways to identify the manager: by bean name or by an @ManagerFor annotation for a model class name

		// Check the bean name first
		ManagedBean beanAnnotation = loadedClass.getAnnotation(ManagedBean.class);
		if(beanAnnotation != null) {
			if(managerName.equals(beanAnnotation.name())) {
				if(ModelManager.class.isAssignableFrom(loadedClass)) {
					return true;
				} else {
					return false;
				}
			}
		}

		// Now check for a @ManagerFor annotation with the referenced class or bean name
		ManagerFor manFor = loadedClass.getAnnotation(ManagerFor.class);
		if(manFor != null) {
			if(referencedClass != null && referencedClass.equals(manFor.value())) {
				return true;
			} else if(managerName.equals(manFor.name())) {
				return true;
			}
		}
		return false;
	}

	// TODO integrate this with findModelManager
	@SuppressWarnings("unchecked")
	public static ModelManager<?> findManagerInstance(final FacesContext context, final String managerName) throws IOException {
		if(managerName == null) {
			return null;
		}

		Object managerObject = FrameworkUtils.resolveVariable(managerName);
		if(managerObject != null && !(managerObject instanceof ModelManager)) {
			throw new IllegalArgumentException("managerObject must be an instance of " + ModelManager.class.getName());
		}

		// If the object is null, assume that the managerName is a class name and instantiate anew
		if(managerObject == null) {
			try {
				Class<ModelManager<?>> managerClass = (Class<ModelManager<?>>)FacesContext.getCurrentInstance().getContextClassLoader().loadClass(managerName);
				managerObject = managerClass.newInstance();
			} catch(ClassNotFoundException cnfe) {
				IOException ioe = new IOException("Error while instantiating manager object for name '" + managerName + "'");
				ioe.initCause(cnfe);
				throw ioe;
			} catch(InstantiationException ie) {
				IOException ioe = new IOException("Error while instantiating manager object for name '" + managerName + "'");
				ioe.initCause(ie);
				throw ioe;
			} catch(IllegalAccessException iae) {
				IOException ioe = new IOException("Error while instantiating manager object for name '" + managerName + "'");
				ioe.initCause(iae);
				throw ioe;
			}
		}

		return (ModelManager<?>)managerObject;
	}

	public static SortedSet<String> stringSet(final Collection<String> input) {
		SortedSet<String> result = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		if(input != null) {
			result.addAll(input);
		}
		return result;
	}
}