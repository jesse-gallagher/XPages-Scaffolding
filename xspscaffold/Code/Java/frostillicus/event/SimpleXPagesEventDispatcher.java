package frostillicus.event;

import java.util.*;

public class SimpleXPagesEventDispatcher implements XPagesEventDispatcher {
	private static final long serialVersionUID = 4297933143684795646L;

	private final Set<XPagesEventListener> listeners = new HashSet<XPagesEventListener>();

	@SuppressWarnings("unchecked")
	public void setListenerClasses(final List<String> listenerClassNames) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		for (String className : listenerClassNames) {
			Class<XPagesEventListener> listenerClass = (Class<XPagesEventListener>) Class.forName(className);
			this.addListener(listenerClass.newInstance());
		}
	}

	public void addListener(final XPagesEventListener listener) {
		this.listeners.add(listener);
	}

	public void dispatch(final XPagesEvent event) {
		for (XPagesEventListener listener : this.listeners) {
			listener.receiveEvent(event);
		}
	}

	public void dispatch(final String eventName) {
		this.dispatch(eventName, new Object[] {});
	}

	public void dispatch(final String eventName, final Object... eventPayload) {
		this.dispatch(new SimpleXPagesEvent(eventName, eventPayload));
	}
}