package placetracking;

import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

public class WebsiteRequest {
	
	private static final Logger log = Logger.getLogger(WebsiteRequest.class.getName());
	
	HttpServletRequest servletRequest;
	String fullUrl;
	String rootUrl;
	Map<String, String> parameter = new HashMap<String, String>();
	
	public WebsiteRequest (HttpServletRequest req) {
		servletRequest = req;
		fullUrl = getFullUrlFromRequest(req);
		rootUrl = getRootUrlFromRequest(req);
	}
	
	public void setAttribute(String key, Object value) {
		servletRequest.setAttribute(key, value);
	}
	
	/**
	 * Parameter helper
	 */
	public void addParameter(String key, String value) {
		parameter.put(key, value);
	}
	
	public String getParameter(String key) {
		if (parameter.containsKey(key)) {
			return parameter.get(key);
		} else {
			return servletRequest.getParameter(key);
		}
	}
	
	public String getParameter(String key, String defaultValue) {
		String value = getParameter(key);
		if (value != null && value.length() > 0) {
			return value;
		} else {
			return defaultValue;
		}
	}
	
	public long getParameterAsLong(String key, long defaultValue) {
		try {
			return Long.parseLong(getParameter(key));
		} catch (Exception ex) {
			return defaultValue;
		}
	}
	
	public int getParameterAsInt(String key, int defaultValue) {
		try {
			return Integer.parseInt(getParameter(key));
		} catch (Exception ex) {
			return defaultValue;
		}
	}
	
	public boolean getParameterAsBoolean(String key, boolean defaultValue) {
		try {
			return getParameter(key).equals("true");
		} catch (Exception ex) {
			return defaultValue;
		}
	}
	
	public float getParameterAsFloat(String key, float defaultValue) {
		try {
			return Float.parseFloat(getParameter(key));
		} catch (Exception ex) {
			return defaultValue;
		}
	}
	
	/*
	 * URL helper
	 */
	public static String getRootUrlFromRequest(HttpServletRequest req) {
		String requestUrl = req.getRequestURL().toString();
        return requestUrl.substring( 0, requestUrl.indexOf(req.getRequestURI())) + "/";
	}
	
	public List<String> getSeperatedPath() {
		return getSeperatedPathFromRequest(servletRequest);
	}
	
	public List<String> getSeperatedPathStartingAt(String path) {
		List<String> paths = getSeperatedPathFromRequest(servletRequest);
		return paths.subList(paths.indexOf(path) + 1, paths.size());
	}
	
	public static List<String> getSeperatedPathFromRequest(HttpServletRequest req) {
		List<String> paths = Arrays.asList(req.getRequestURI().split("/"));
		return paths;
	}
	
	public static String getFullUrlFromRequest(HttpServletRequest req) {
    	StringBuilder builder = new StringBuilder();			
		builder.append(req.getRequestURL().toString() + "?");
		
		Iterator entries = req.getParameterMap().entrySet().iterator();
		while (entries.hasNext()) {
		  Entry thisEntry = (Entry) entries.next();
		  String key = (String) thisEntry.getKey();
		  String value = ((String[]) thisEntry.getValue())[0];
		  builder.append(key + "=" + value + "&");
		}
		
		String url = builder.toString();
		url = url.substring(0, url.length() - 1);
		return url;
    }
	
	public String getNextFullUrlFromRequest() {
    	StringBuilder builder = new StringBuilder();
		builder.append(servletRequest.getRequestURL().toString() + "?");
		
		int offset = getParameterAsInt("offset", 0);
		int count = getParameterAsInt("count", 25);
		int nextOffset = offset + count;
		
		builder.append("offset=" + nextOffset + "&");
		
		Iterator entries = servletRequest.getParameterMap().entrySet().iterator();
		while (entries.hasNext()) {
			  Entry thisEntry = (Entry) entries.next();
			  String key = (String) thisEntry.getKey();
			  String value = ((String[]) thisEntry.getValue())[0];
			  if (!key.equals("offset")) {
				  builder.append(key + "=" + value + "&");  
			  }
		}
		
		String url = builder.toString();
		url = url.substring(0, url.length() - 1);
		return url;
    }
	
	public String extractQueryFromPath(String sitePath) throws Exception {
		String query = "";
		List<String> paths = getSeperatedPathStartingAt(sitePath);
		if (paths.size() > 0) {
			query = paths.get(0);
			query = URLDecoder.decode(query, "UTF-8");
		}
		
		if (query == null || query.length() < 1) {
			throw new Exception("Invalid query path specified");
		}
		
		return query;
	}
	
	public String getUrl() {
		return servletRequest.getRequestURL().toString();
	}
	
	public String getUri() {
		return servletRequest.getRequestURI();
	}
	
	public String getHost() {
		return servletRequest.getHeader("Host");
	}
	
	public String getCity() {
		return servletRequest.getHeader("X-AppEngine-City");
	}
	
	public String getCountry() {
		return servletRequest.getHeader("X-AppEngine-Country");
	}
	
	public HttpServletRequest getServletRequest() {
		return servletRequest;
	}

	public String getFullUrl() {
		return fullUrl;
	}	
	
	public String getRootUrl() {
		return rootUrl;
	}

}
