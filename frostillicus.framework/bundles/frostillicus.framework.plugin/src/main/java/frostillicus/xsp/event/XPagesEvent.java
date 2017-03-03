package frostillicus.xsp.event;

import java.io.Serializable;

/**
 * @since 1.0
 */
public interface XPagesEvent extends Serializable {
	public String getEventName();

	public Object[] getEventPayload();
}