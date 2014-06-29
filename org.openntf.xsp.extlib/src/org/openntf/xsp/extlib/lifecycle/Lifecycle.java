package org.openntf.xsp.extlib.lifecycle;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseListener;

import org.openntf.xsp.extlib.Activator;

import com.sun.faces.lifecycle.LifecycleImpl;

public class Lifecycle extends LifecycleImpl {
	private javax.faces.lifecycle.Lifecycle delegate;
	private final static boolean _debug = Activator._debug;
	static {
		if (_debug)
			System.out.println(Lifecycle.class.getName() + " loaded");
	}

	public Lifecycle(javax.faces.lifecycle.Lifecycle standardLifecycle) {
		delegate = standardLifecycle;
		if (_debug)
			System.out.println(getClass().getName() + " created");
	}

	javax.faces.lifecycle.Lifecycle getDelegate() {
		return delegate;
	}

	@Override
	public void addPhaseListener(PhaseListener listener) {
		getDelegate().addPhaseListener(listener);
	}

	@Override
	public void execute(FacesContext context) throws FacesException {
		getDelegate().execute(context);
		// try {
		// getDelegate().execute(context);
		// } catch (FacesException intercepted) {
		// intercepted.printStackTrace();
		// FacesContext.getCurrentInstance().renderResponse();
		// throw intercepted;
		// }
	}

	@Override
	public PhaseListener[] getPhaseListeners() {
		return getDelegate().getPhaseListeners();
	}

	@Override
	public void removePhaseListener(PhaseListener listener) {
		getDelegate().removePhaseListener(listener);
	}

	@Override
	public void render(FacesContext context) throws FacesException {
		getDelegate().render(context);
		// try {
		// getDelegate().render(context);
		// } catch (FacesException intercepted) {
		// intercepted.printStackTrace();
		// throw intercepted;
		// }
	}

}
