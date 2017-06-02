package placetracking;

import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

public class WebsiteRequest {

    private static final Logger log = Logger.getLogger(WebsiteRequest.class.getName());

    private HttpServletRequest servletRequest;
    private String fullUrl;
    private String rootUrl;
    private String body;
    private JsonObject jsonBody;
    private Map<String, String> parameter = new HashMap<String, String>();

    public WebsiteRequest(HttpServletRequest req) {
        servletRequest = req;
        processRequestBody();
        fullUrl = getFullUrlFromRequest(req);
        rootUrl = getRootUrlFromRequest(req);
    }

    private void processRequestBody() {
        if (getMethod().equalsIgnoreCase(HTTPMethod.GET.name())) {
            return;
        }

        body = readBodyFromRequest(servletRequest);
        if (body == null || body.length() < 1) {
            return;
        }

        try {
            JsonParser jsonParser = new JsonParser();
            jsonBody = (JsonObject) jsonParser.parse(body);

            // add primitives from json body as request parameters
            for (Entry<String, JsonElement> jsonElementEntry : jsonBody.entrySet()) {
                if (jsonElementEntry.getValue().isJsonPrimitive()) {
                    addParameter(jsonElementEntry.getKey(), jsonElementEntry.getValue().getAsString());
                }
            }
        } catch (Exception e) {
            log.warning("Unable to process request body: " + e.getMessage());
        }
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

    private static String readBodyFromRequest(HttpServletRequest req) {
        try {
            Scanner s = new Scanner(req.getInputStream(), "UTF-8").useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        } catch (IOException e) {
            log.warning("Unable to read request body: " + e.getMessage());
        }
        return "";
    }

    /*
     * URL helper
     */
    private static String getRootUrlFromRequest(HttpServletRequest req) {
        String requestUrl = req.getRequestURL().toString();
        return requestUrl.substring(0, requestUrl.indexOf(req.getRequestURI())) + "/";
    }

    private List<String> getSeparatedPathStartingAt(String path) {
        List<String> paths = getSeparatedPathFromRequest(servletRequest);
        return paths.subList(paths.indexOf(path) + 1, paths.size());
    }

    private static List<String> getSeparatedPathFromRequest(HttpServletRequest req) {
        return Arrays.asList(req.getRequestURI().split("/"));
    }

    private static String getFullUrlFromRequest(HttpServletRequest request) {
        StringBuilder builder = new StringBuilder()
                .append(request.getRequestURL().toString())
                .append("?");

        for (Object parameter : request.getParameterMap().entrySet()) {
            Entry entry = (Entry) parameter;
            String key = (String) entry.getKey();
            String value = ((String[]) entry.getValue())[0];
            if (!key.equals("offset")) {
                builder.append(key).append("=").append(value).append("&");
            }
        }

        String url = builder.toString();
        url = url.substring(0, url.length() - 1);
        return url;
    }

    public String getNextFullUrlFromRequest() {
        StringBuilder builder = new StringBuilder()
                .append(servletRequest.getRequestURL().toString())
                .append("?");

        int offset = getParameterAsInt("offset", 0);
        int count = getParameterAsInt("count", 25);
        int nextOffset = offset + count;

        builder.append("offset=").append(nextOffset).append("&");

        for (Object parameter : servletRequest.getParameterMap().entrySet()) {
            Entry entry = (Entry) parameter;
            String key = (String) entry.getKey();
            String value = ((String[]) entry.getValue())[0];
            if (!key.equals("offset")) {
                builder.append(key).append("=").append(value).append("&");
            }
        }

        String url = builder.toString();
        url = url.substring(0, url.length() - 1);
        return url;
    }

    public String extractQueryFromPath(String sitePath) throws Exception {
        String query = "";
        List<String> paths = getSeparatedPathStartingAt(sitePath);
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

    public String getMethod() {
        return getServletRequest().getMethod().toLowerCase();
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

    public String getBody() {
        return body;
    }

    public JsonObject getJsonBody() {
        return jsonBody;
    }

}
