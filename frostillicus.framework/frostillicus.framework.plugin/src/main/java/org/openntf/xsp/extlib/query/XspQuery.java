package org.openntf.xsp.extlib.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitHint;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;

import com.ibm.xsp.context.FacesContextEx;

public class XspQuery extends ArrayList<UIComponent> {
	private static final long serialVersionUID = 1L;
	protected static Logger _logger;
	static {
		_logger = Logger.getLogger(XspQuery.class.getName());
	}
	private List<QueryFilter> filters;
	private List<VisitCallback> callbacks;
	private ICacheContainer cacheContainer;

	public XspQuery() {

	}

	public XspQuery addCallback(final VisitCallback callback) {
		getCallbacks().add(callback);
		return this;
	}

	public XspQuery addContains(final String propertyName, final Object content) {
		getFilters().add(new ContainsFilter(propertyName, content));
		return this;
	}

	public XspQuery addEndsWith(final String propertyName, final String content) {
		getFilters().add(new EndsWithFilter(propertyName, content));
		return this;
	}

	public XspQuery addEquals(final String propertyName, final Object content) {
		getFilters().add(new EqualsFilter(propertyName, content));
		return this;
	}

	public XspQuery addClientIdFilter(final String propertyName, final Object content) {
		getFilters().add(new ClientIdEqualsFilter(propertyName, content));
		return this;
	}

	public XspQuery addInstanceOf(final Class<? extends UIComponent> klass) {
		getFilters().add(new InstanceFilter(klass));
		return this;
	}

	public XspQuery addStartsWith(final String propertyName, final String content) {
		getFilters().add(new StartsWithFilter(propertyName, content));
		return this;
	}

	public UIComponent byId(final String id) {
		return byId(id, null);
	}

	public UIComponent byId(final String id, final UIComponent root) {
		UIComponent result = null;
		addEquals("id", id);
		List<UIComponent> matches = locate(root);
		if (matches.size() > 0) {
			_logger.log(Level.FINER, "Found a component match by id: " + id);
			result = matches.get(0);
		} else {
			_logger.log(Level.FINER, "Unable to find a component match by id: "
					+ id);
		}
		return result;
	}

	public UIComponent byClientId(final String id) {
		return byClientId(id, null);
	}

	public UIComponent byClientId(final String id, UIComponent root) {
		_logger.finer("Component requested by client id " + id);
		UIComponent result = null;
		if (getCacheContainer() != null) {
			_logger
			.finer("Cache container exists, requesting cached component");
			result = getCacheContainer().get(id);
		} else {
			_logger.warning("This org.openntf.xsp.extlib.query has a null cache container");
		}
		if (result == null) {
			_logger.log(Level.FINER, "Did not find result in cache.");
			if (root == null) {
				root = FacesContext.getCurrentInstance().getViewRoot();
			}
			addClientIdFilter("clientId", id);
			List<UIComponent> matches = locate(root);
			if (matches.size() > 0) {
				result = matches.get(0);
			}
		} else {
			_logger.log(Level.FINER, "Found result in cache.");
		}
		return result;
	}

	protected List<VisitCallback> getCallbacks() {
		if (this.callbacks == null) {
			this.callbacks = new ArrayList<VisitCallback>();
		}
		return this.callbacks;
	}

	protected List<QueryFilter> getFilters() {
		if (this.filters == null) {
			this.filters = new ArrayList<QueryFilter>();
		}
		return this.filters;
	}

	public List<UIComponent> locate() {
		return locate(null);
	}

	public List<UIComponent> locate(UIComponent root) {
		if (root == null) {
			root = FacesContext.getCurrentInstance().getViewRoot();
		}
		safeVisitTree(new QueryVisitContext(), new QueryVisitCallback(), root);
		return this;
	}

	public UIInput locateField(final String id) {
		return locateField(id, null);
	}

	public UIInput locateField(final String id, final UIComponent root) {
		UIInput result = null;
		addEquals("id", id);
		List<UIInput> inputs = locateInputs(root);
		if (inputs.size() > 0) {
			result = inputs.get(0);
		}
		return result;
	}

	public List<UIInput> locateInputs() {
		return locateInputs(null);
	}

	public List<UIInput> locateInputs(final UIComponent root) {
		List<UIInput> inputs = new ArrayList<UIInput>();
		addInstanceOf(UIInput.class);
		List<UIComponent> components = locate(null);
		for (UIComponent component : components) {
			inputs.add((UIInput) component);
		}
		return inputs;
	}

	@SuppressWarnings("unchecked")
	public static boolean safeVisitTree(final VisitContext context,
			final VisitCallback callback, final UIComponent component) {
		if (!(isVisitable(context, component))) {
			return false;
		}
		VisitResult res = context.invokeVisitCallback(component, callback);
		if (res == VisitResult.COMPLETE) {
			return true;
		}
		if ((res == VisitResult.ACCEPT)
				&& (((component.getChildCount() > 0) || (component
						.getFacetCount() > 0)))) {
			for (Iterator<UIComponent> it = component.getFacetsAndChildren(); it.hasNext();) {
				UIComponent c = it.next();
				if (safeVisitTree(context, callback, c)) {
					return true;
				}
			}
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	private static boolean isVisitable(final VisitContext context,
			final UIComponent component) {
		Collection hints = context.getHints();
		if ((hints.contains(VisitHint.SKIP_TRANSIENT))
				&& (component.isTransient())) {
			return false;
		}
		return ((!(hints.contains(VisitHint.SKIP_UNRENDERED))) || (component
				.isRendered()));
	}

	public XspQuery setCacheContainer(final ICacheContainer cacheContainer) {
		this.cacheContainer = cacheContainer;
		return this;
	}

	protected ICacheContainer getCacheContainer() {
		return this.cacheContainer;
	}

	private class QueryVisitCallback implements VisitCallback {

		public VisitResult visit(final VisitContext context, final UIComponent component) {
			refreshCache(context, component);
			boolean valid = true;
			for (QueryFilter filter : getFilters()) {
				if (!filter.matches(component, FacesContext
				                    .getCurrentInstance())) {
					valid = false;
					break;
				}
			}
			if (valid) {
				List<VisitCallback> forEach = getCallbacks();
				for (VisitCallback each : forEach) {
					each.visit(context, component);
				}
				add(component);
				/*
				 * List<VisitCallback> forEach = getCallbacks(); for
				 * (VisitCallback each : forEach) { VisitResult thisResult =
				 * each.visit(context, component); switch
				 * (thisResult.compareTo(VisitResult.ACCEPT)) { case 1: //
				 * REJECT return thisResult; case 2: // COMPLETE add(component);
				 * return thisResult; } add(component); }
				 */
			}
			return VisitResult.ACCEPT;
		}

		private void refreshCache(final VisitContext context, final UIComponent component) {
			ICacheContainer cache = getCacheContainer();
			if (!(cache == null)) {
				String clientId = component.getClientId(context
				                                        .getFacesContext());
				if (!(cache.containsKey(clientId))) {
					cache.put(clientId, component);
				}
			}
		}

	}

	private class QueryVisitContext extends VisitContext {

		@Override
		public FacesContext getFacesContext() {
			return FacesContextEx.getCurrentInstance();
		}

		@Override
		public Set<VisitHint> getHints() {
			Set<VisitHint> hints = new HashSet<VisitHint>();
			hints.add(VisitHint.SKIP_TRANSIENT);
			return hints;
		}

		@Override
		public Collection<String> getIdsToVisit() {
			return null;
		}

		@Override
		public Collection<String> getSubtreeIdsToVisit(final UIComponent component) {
			return null;
		}

		@Override
		public VisitResult invokeVisitCallback(final UIComponent component,
				final VisitCallback callback) {
			return callback.visit(this, component);
		}

	}

}