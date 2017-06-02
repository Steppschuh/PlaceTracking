package placetracking.api;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import placetracking.WebsiteRequest;
import placetracking.api.endpoint.Endpoint;
import placetracking.api.endpoint.EndpointManager;

public class ApiServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ApiServlet.class.getSimpleName());

    private static final int MINIMUM_VERSION_CODE = 3;
    private static final Pattern apiVersionPattern = Pattern.compile(".*(/api/v)(\\d)+(/).*");
    private static final Pattern domainVersionPattern = Pattern.compile(".*(://)(\\d)+(-dot-).*");

    private static void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        WebsiteRequest apiRequest = new WebsiteRequest(request);
        ApiResponse apiResponse = new ApiResponse();

        try {
            // Redirect API calls with invalid or old version codes
            redirectLegacyApiCalls(apiRequest, response);

            // Find an endpoint that can handle the request
            Endpoint endpoint = EndpointManager.getEndpointForRequest(apiRequest);
            if (endpoint == null) {
                apiResponse.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
                throw new Exception("Unknown endpoint called");
            }

            // Process the request
            apiResponse = endpoint.processRequest(apiRequest);
        } catch (Exception e) {
            log.severe(e.getMessage());
            apiResponse.setException(e);
        }

        apiResponse.send(response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        handleRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleRequest(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleRequest(request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleRequest(request, response);
    }

    /**
     * Redirects API requests to the latest supported legacy version in case that
     * the requested API version is too old or not specified.
     *
     * Won't redirect requests against the dev server.
     */
    private static void redirectLegacyApiCalls(WebsiteRequest apiRequest, HttpServletResponse response) throws IOException {
        if (apiRequest.getUrl().contains(EndpointManager.HOST_LOCAL_DEV_SERVER)) {
            return;
        }
        int versionCode = extractDomainVersionCode(apiRequest.getUrl());
        if (versionCode < MINIMUM_VERSION_CODE) {
            String redirectUrl = replaceDomainVersionCode(apiRequest.getFullUrl(), MINIMUM_VERSION_CODE - 1);
            response.sendRedirect(redirectUrl);
        }
    }

    /**
     * Given an url like "http://placetracking.appspot.com/api/v3/users/", it will return 3.
     */
    private static int extractApiVersionCode(String requestUrl) {
        int version = MINIMUM_VERSION_CODE - 1;
        try {
            Matcher matcher = apiVersionPattern.matcher(requestUrl);
            if (matcher.find()) {
                String versionString = matcher.group(2);
                version = Integer.parseInt(versionString);
            }
        } catch (Exception e) {
            log.warning("Unable to extract version from request: " + requestUrl + "\n" + e.getMessage());
        }
        return version;
    }

    private static String replaceApiVersionCode(String requestUrl, int targetVersionCode) {
        Matcher matcher = apiVersionPattern.matcher(requestUrl);
        String target = "/api/";
        String replacement = target + "v" + targetVersionCode + "/";
        if (matcher.find()) {
            String versionString = matcher.group(2);
            target += "v" + versionString + "/";
        }
        return requestUrl.replace(target, replacement);
    }

    /**
     * Given an url like "http://3-dot-placetracking.appspot.com/api/users/", it will return 3.
     */
    private static int extractDomainVersionCode(String requestUrl) {
        int version = MINIMUM_VERSION_CODE - 1;
        try {
            Matcher matcher = domainVersionPattern.matcher(requestUrl);
            if (matcher.find()) {
                String versionString = matcher.group(2);
                version = Integer.parseInt(versionString);
            }
        } catch (Exception e) {
            log.warning("Unable to extract version from request: " + requestUrl + "\n" + e.getMessage());
        }
        return version;
    }

    private static String replaceDomainVersionCode(String requestUrl, int targetVersionCode) {
        Matcher matcher = domainVersionPattern.matcher(requestUrl);
        String target = "://";
        String replacement = target + targetVersionCode + "-dot-";
        if (matcher.find()) {
            String versionString = matcher.group(2);
            target += versionString + "-dot-";
        }
        return requestUrl.replace(target, replacement);
    }

}
