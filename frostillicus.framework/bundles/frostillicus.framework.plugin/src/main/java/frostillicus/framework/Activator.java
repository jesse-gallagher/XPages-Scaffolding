package frostillicus.framework;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * @since 1.0
 */
public class Activator extends Plugin {
	public static final boolean _debug = false;

	public static Activator instance;
	private static BundleContext context;

	@edu.umd.cs.findbugs.annotations.SuppressWarnings(
	                                                    value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
	                                                    justification="This is an intentional pattern")
	public Activator() {
		instance = this;
	}

	private static String version;

	public static String getVersion() {
		if (version == null) {
			version = (String) instance.getBundle().getHeaders().get("Bundle-Version");
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
