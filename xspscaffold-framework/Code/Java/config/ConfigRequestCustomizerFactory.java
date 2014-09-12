package config;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import com.ibm.xsp.context.RequestCustomizerFactory;
import com.ibm.xsp.context.RequestParameters;

public class ConfigRequestCustomizerFactory extends RequestCustomizerFactory {

	@Override
	public void initializeParameters(final FacesContext facesContext, final RequestParameters parameters) {
		parameters.setUrlProcessor(ConfigUrlProcessor.INSTANCE);
	}

	public static final class ConfigUrlProcessor implements RequestParameters.UrlProcessor {
		public static final ConfigUrlProcessor INSTANCE = new ConfigUrlProcessor();

		private List<Map<String, Object>> aliases_;

		private ConfigUrlProcessor() { }

		public String processActionUrl(final String url) {
			List<Map<String, Object>> aliases = getAliases();
			String serverName = ((HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest()).getServerName();

			for(Map<String, Object> alias : aliases) {
				Pattern serverPattern = (Pattern)alias.get("serverPattern");
				if(serverPattern.matcher(serverName).matches()) {
					Pattern pattern = (Pattern)alias.get("pattern");
					Matcher matcher = pattern.matcher(url);
					if(matcher.matches()) {
						return matcher.replaceAll((String)alias.get("replacement"));
					}
				}
			}

			return url;
		}

		public String processGlobalUrl(final String url) {
			return url;
		}

		public String processResourceUrl(final String url) {
			List<Map<String, Object>> aliases = getAliases();
			String serverName = ((HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest()).getServerName();

			for(Map<String, Object> alias : aliases) {
				Pattern serverPattern = (Pattern)alias.get("serverPattern");
				if(serverPattern.matcher(serverName).matches()) {
					Pattern pattern = (Pattern)alias.get("pattern");
					Matcher matcher = pattern.matcher(url);
					if(matcher.matches()) {
						return matcher.replaceAll((String)alias.get("replacement"));
					}
				}
			}

			return url;
		}

		@SuppressWarnings("unchecked")
		private List<Map<String, Object>> getAliases() {
			if(aliases_ == null) {
				AppConfig appConfig = AppConfig.get();
				Object aliases = appConfig.getValue("aliases");
				if(aliases == null || aliases instanceof String) {
					aliases_ = Collections.emptyList();
				} else {
					aliases_ = new ArrayList<Map<String, Object>>();
					for(Map<String, Object> alias : (List<Map<String, Object>>)aliases) {
						aliases_.add(new HashMap<String, Object>(alias));
					}
				}



				// Compile the patterns
				String contextPath = Pattern.quote(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath());
				for(Map<String, Object> alias : aliases_) {
					String serverPattern = (String)alias.get("serverPattern");
					alias.put("serverPattern", Pattern.compile(serverPattern));
					String pattern = ((String)alias.get("pattern")).replace("DBPATH", contextPath);
					alias.put("pattern", Pattern.compile(pattern));
				}
			}
			return aliases_;
		}
	}
}
