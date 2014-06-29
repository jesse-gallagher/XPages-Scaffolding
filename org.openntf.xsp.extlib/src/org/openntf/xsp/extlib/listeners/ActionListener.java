package org.openntf.xsp.extlib.listeners;

import org.openntf.xsp.extlib.Activator;

import com.sun.faces.application.ActionListenerImpl;

public class ActionListener extends ActionListenerImpl {
	private final static boolean _debug = Activator._debug;
	static {
		if (_debug)
			System.out.println(ActionListener.class.getName() + " loaded");
	}

	public ActionListener() {
		if (_debug) {
			System.out.println(getClass().getName() + " created");
		}
	}

}
