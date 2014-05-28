package frostillicus.event;

import java.util.EventListener;
import java.io.Serializable;

public interface XPagesEventListener extends EventListener, Serializable {
	public void receiveEvent(final XPagesEvent event);
}