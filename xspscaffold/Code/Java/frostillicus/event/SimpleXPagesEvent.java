package frostillicus.event;

import java.io.Serializable;

public class SimpleXPagesEvent implements XPagesEvent, Serializable {
	private static final long serialVersionUID = 2494192698824356379L;

	private String eventName_;
	private Object[] eventPayload_;

	public SimpleXPagesEvent(final String eventName, final Object... eventPayload) {
		eventName_ = eventName;
		eventPayload_ = eventPayload;
	}

	public String getEventName() {
		return eventName_;
	}

	public Object[] getEventPayload() {
		return eventPayload_;
	}
}