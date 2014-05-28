package frostillicus.event;

import java.io.Serializable;

public interface XPagesEvent extends Serializable {
	public String getEventName();

	public Object[] getEventPayload();
}