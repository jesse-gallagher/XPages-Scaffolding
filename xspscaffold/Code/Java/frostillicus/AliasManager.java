package frostillicus;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.Serializable;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import util.JSFUtil;

import lotus.domino.*;

public class AliasManager extends AbstractKeyMap<String, String> implements Serializable {
	private static final long serialVersionUID = 1L;

	private Map<String, Map<String, String>> cache;
	private Map<String, List<PatternPair>> patterns;

	@SuppressWarnings("unchecked")
	public AliasManager() throws NotesException {
		try {
			this.cache = new HashMap<String, Map<String, String>>();
			this.patterns = new HashMap<String, List<PatternPair>>();

			Database database = JSFUtil.getDatabase();
			View aliases = database.getView("Aliases");
			aliases.setAutoUpdate(false);
			ViewNavigator viewNav = aliases.createViewNav();
			viewNav.setBufferMaxEntries(400);
			ViewEntry entry = viewNav.getFirst();
			while (entry != null) {
				entry.setPreferJavaDates(true);
				List<Object> columnValues = entry.getColumnValues();

				String host = String.valueOf(columnValues.get(0));
				if (!this.patterns.containsKey(host)) {
					this.patterns.put(host, new ArrayList<PatternPair>());
				}

				List<PatternPair> patterns = this.patterns.get(host);
				String pattern = String.valueOf(columnValues.get(1));
				String replacement = String.valueOf(columnValues.get(2));
				patterns.add(new PatternPair(Pattern.compile(pattern), replacement));

				ViewEntry tempEntry = entry;
				entry = viewNav.getNext(entry);
				tempEntry.recycle();
			}
			viewNav.recycle();
			aliases.recycle();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public String get(final Object key) {
		if (!(key instanceof String)) {
			throw new IllegalArgumentException();
		}

		String url = (String) key;

		// Check if the URL is already cached for the current host
		Map<String, String> requestCache = getRequestCache();
		if (requestCache.containsKey(url)) {
			return requestCache.get(url);
		}

		// This URL wasn't found - do a search through the patterns for a match

		List<PatternPair> patternsList = getRequestPatterns();
		if (patternsList != null) {
			for (PatternPair pair : patternsList) {
				Matcher matcher = pair.pattern.matcher(url);
				if (matcher.matches()) {
					//String result = matcher.replaceAll(pair.replacement);
					String result = url.replaceAll(pair.pattern.pattern(), pair.replacement);
					requestCache.put(url, result);
					return result;
				}
			}
		}

		// If it wasn't found elsewhere, cache that fact and return the original
		requestCache.put(url, url);
		return url;
	}

	// Returns the list of pattern pairs appropriate for the current request's host
	private List<PatternPair> getRequestPatterns() {
		String server = getRequestServer();
		if (patterns.containsKey(server)) {
			return patterns.get(server);
		} else if (patterns.containsKey("*")) {
			return patterns.get("*");
		}
		return null;
	}

	// Returns the cache appropriate for the current request's host. Fills in "*" if it doesn't exist
	private Map<String, String> getRequestCache() {
		String server = getRequestServer();
		if (cache.containsKey(server)) {
			return cache.get(server);
		}

		if (!cache.containsKey("*")) {
			cache.put("*", new HashMap<String, String>());
		}
		return cache.get("*");
	}

	private String getRequestServer() {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		return request.getServerName();
	}

	protected static class PatternPair implements Serializable {
		private static final long serialVersionUID = 2450320758987994108L;

		public Pattern pattern;
		public String replacement;

		public PatternPair(final Pattern pattern, final String replacement) {
			this.pattern = pattern;
			this.replacement = replacement;
		}
	}
}
