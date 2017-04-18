package placetracking.api.endpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import placetracking.WebsiteRequest;
import placetracking.api.ApiResponse;

/**
 * Abstract endpoint that every API endpoint needs to extend.
 * Contains all basic methods and some that may need to be overwritten.
 */
public abstract class Endpoint {

    public static final Logger log = Logger.getLogger(Endpoint.class.getSimpleName());

    /**
     * Used for assigning requests to endpoints
     */
    public String getEndpointPath() {
        return EndpointManager.ENDPOINT_API;
    }

    /**
     * Checks if the requested URL can be handled by an endpoint
     */
    public boolean shouldHandleRequest(WebsiteRequest request) {
        if (request.getUrl().contains(getEndpointPath())) {
            return true;
        }
        return false;
    }

    /**
     * Main method for actually handling an request. Avoid overwriting this,
     * use @generateRequestResponse() instead
     */
    public ApiResponse processRequest(WebsiteRequest request) {
        ApiResponse response = new ApiResponse();

        try {
            checkParameters(request);
        } catch (Exception ex) {
            log.warning("Malformed request: " + ex.getMessage());
            response.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
            response.setException(ex);
            return response;
        }

        try {
            response = generateRequestResponse(request);
        } catch (Exception ex) {
            log.warning("Unable to handle request: " + ex.getMessage());
            ex.printStackTrace();
            response.setStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setException(ex);
            return response;
        }

        return response;
    }

    /**
     * Overwrite if the endpoint requires some mandatory parameters to be set.
     * An exception will be thrown if one of the parameters is not set
     */
    public List<String> getRequiredParameters(WebsiteRequest request) {
        return new ArrayList<String>();
    }

    /**
     * Checks if all parameters specified in @getRequiredParameters() are set
     */
    private void checkParameters(WebsiteRequest request) throws Exception {
        for (String parameter : getRequiredParameters(request)) {
            if (request.getParameter(parameter) == null) {
                throw new Exception("Required parameter missing: " + parameter);
            }
        }
    }

    /**
     * Overwrite this to do the endpoint specific work and return some @ApiResponse object
     */
    public ApiResponse generateRequestResponse(WebsiteRequest request) throws Exception {
        throw new Exception("Endpoint not implemenetd");
    }

}
