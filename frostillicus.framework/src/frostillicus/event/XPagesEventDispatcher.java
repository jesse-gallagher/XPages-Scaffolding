package frostillicus.event;

import java.io.Serializable;

public interface XPagesEventDispatcher extends Serializable {
	public void dispatch(final XPagesEvent event);

	public void addListener(final XPagesEventListener listener);
}