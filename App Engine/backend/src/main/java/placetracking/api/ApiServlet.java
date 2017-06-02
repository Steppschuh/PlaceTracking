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

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        WebsiteRequest apiRequest = new WebsiteRequest(req);
        ApiResponse apiResponse = new ApiResponse();

        try {
            // Redirect API calls with invalid or old version codes
            redirectLegacyApiCalls(apiRequest, resp);

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

        apiResponse.send(resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    private static void redirectLegacyApiCalls(WebsiteRequest apiRequest, HttpServletResponse response) throws IOException {
        if (apiRequest.getUrl().contains("localhost")) {
            return;
        }
        int versionCode = extractDomainVersionCode(apiRequest.getUrl());
        if (versionCode < MINIMUM_VERSION_CODE) {
            // redirect to latest legacy version
            String redirectUrl = replaceDomainVersionCode(apiRequest.getFullUrl(), MINIMUM_VERSION_CODE - 1);
            response.sendRedirect(redirectUrl);
        }
    }

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
