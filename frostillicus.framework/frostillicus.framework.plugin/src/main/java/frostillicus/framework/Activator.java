package frostillicus.framework;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin {
	public static final String PLUGIN_ID = "frostillicus.framework.plugin";
	public static final boolean _debug = false;

	public static Activator instance;

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
	}

	public static Activator getDefault() {
		return instance;
	}
}
