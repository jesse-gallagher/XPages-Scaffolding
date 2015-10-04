package frostillicus.xsp.event;

import java.util.EventListener;
import java.io.Serializable;

/**
 * @since 1.0
 */
public interface XPagesEventListener extends EventListener, Serializable {
	public void receiveEvent(final XPagesEvent event);
}