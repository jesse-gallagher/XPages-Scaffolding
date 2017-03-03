package frostillicus.xsp.event;

import java.io.Serializable;

/**
 * @since 1.0
 */
public interface XPagesEventDispatcher extends Serializable {
	public void dispatch(final XPagesEvent event);

	public void addListener(final XPagesEventListener listener);
}