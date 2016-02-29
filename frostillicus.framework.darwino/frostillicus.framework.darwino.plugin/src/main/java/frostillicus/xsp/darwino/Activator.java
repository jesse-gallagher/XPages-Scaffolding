package frostillicus.xsp.darwino;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import com.darwino.commons.log.Logger;

/**
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class Activator extends Plugin {
	public static final boolean _debug = false;

	public static Activator instance;
	private static BundleContext context;
	public static Logger log = com.darwino.commons.Platform.logService().getLogMgr(Activator.class.getPackage().getName());
	static {
		log.setLogLevel(Logger.LOG_TRACE_LEVEL);
	}

	public Activator() {
		instance = this;
	}

	private static String version;

	public static String getVersion() {
		if (version == null) {
			version = instance.getBundle().getHeaders().get("Bundle-Version"); //$NON-NLS-1$
		}
		return version;
	}

	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		Activator.context = context;
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		Activator.context = null;
	}
	
	public static BundleContext getContext() {
		return context;
	}

	public static Activator getDefault() {
		return instance;
	}
}
