package frostillicus.framework;

import com.ibm.xsp.library.AbstractXspLibrary;

/**
 * @since 1.0
 */
public class FrostillicusLibrary extends AbstractXspLibrary {
	private final static String LIBRARY_ID = FrostillicusLibrary.class.getPackage().getName() + ".library";
	private final static boolean _debug = Activator._debug;

	static {
		if (_debug) {
			System.out.println(FrostillicusLibrary.class.getName() + " loaded");
		}
	}

	public FrostillicusLibrary() {
		if (_debug) {
			System.out.println(getClass().getName() + " created");
		}
	}

	@Override
	public String getLibraryId() {
		return LIBRARY_ID;
	}

	@Override
	public String getPluginId() {
		return Activator.getContext().getBundle().getSymbolicName();
	}

	@Override
	public String getTagVersion() {
		return "1.2.0";
	}

	@Override
	public String[] getDependencies() {
		return new String[] {
				"com.ibm.xsp.core.library",
				"com.ibm.xsp.extsn.library",
				"com.ibm.xsp.domino.library",
				"com.ibm.xsp.designer.library",
				"com.ibm.xsp.extlib.library"
		};
	}

	@Override
	public boolean isGlobalScope() {
		return false;
	}

	@Override
	public String[] getFacesConfigFiles() {
		return new String[] { "frostillicus/xsp/config/frostillicus-faces-config.xml" };
	}
	@Override
	public String[] getXspConfigFiles() {
		return new String[] {
				"frostillicus/xsp/config/modelDataSources.xsp-config",
				"frostillicus/xsp/config/converters.xsp-config"
		};
	}
}
