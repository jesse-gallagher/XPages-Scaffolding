package frostillicus.framework;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin {
	public static final String PLUGIN_ID = Activator.class.getPackage().getName();
	public static final boolean _debug = false;

	public static Activator instance;

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
	}

	public static Activator getDefault() {
		return instance;
	}
}
