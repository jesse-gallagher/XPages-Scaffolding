package frostillicus.xsp.darwino;

import com.ibm.xsp.library.AbstractXspLibrary;

public class FrostillicusDarwinoLibrary extends AbstractXspLibrary {
	private final static String LIBRARY_ID = FrostillicusDarwinoLibrary.class.getPackage().getName() + ".library"; //$NON-NLS-1$

	public FrostillicusDarwinoLibrary() {
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
		return "1.0.0"; //$NON-NLS-1$
	}

	@Override
	public String[] getDependencies() {
		return new String[] {
				"com.ibm.xsp.core.library", //$NON-NLS-1$
				"com.ibm.xsp.extsn.library", //$NON-NLS-1$
				"com.ibm.xsp.domino.library", //$NON-NLS-1$
				"com.ibm.xsp.designer.library", //$NON-NLS-1$
				"com.ibm.xsp.extlib.library", //$NON-NLS-1$
				"frostillicus.framework.library" //$NON-NLS-1$
		};
	}

	@Override
	public boolean isGlobalScope() {
		return false;
	}

	@Override
	public String[] getFacesConfigFiles() {
		return new String[] { 
				
		};
	}
	@Override
	public String[] getXspConfigFiles() {
		return new String[] {
				
		};
	}
}
