package placetracking.api;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import placetracking.WebsiteRequest;
import placetracking.api.endpoint.Endpoint;
import placetracking.api.endpoint.EndpointManager;

public class ApiServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ApiServlet.class.getSimpleName());

    @Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		WebsiteRequest apiRequest = new WebsiteRequest(req);
		ApiResponse apiResponse = new ApiResponse();

		try {			
			// Find an endpoint that can handle the request
			Endpoint endpoint = EndpointManager.getEndpointForRequest(apiRequest);
			if (endpoint == null) {
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
		// this is here for Slack integrations
		doGet(req, resp);
	}
    
}
