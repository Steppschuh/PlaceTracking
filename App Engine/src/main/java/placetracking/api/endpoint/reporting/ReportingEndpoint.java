package placetracking.api.endpoint.reporting;

import java.util.ArrayList;
import java.util.List;

import placetracking.WebsiteRequest;
import placetracking.api.ApiResponse;
import placetracking.api.endpoint.Endpoint;
import placetracking.api.endpoint.EndpointManager;

public class ReportingEndpoint extends Endpoint {
	
	public static final List<Endpoint> endpoints = getAvailableEndpoints();
	
	@Override
	public String getEndpointPath() {
		return EndpointManager.ENDPOINT_REPORTING;
	}
	
	@Override
	public ApiResponse processRequest(WebsiteRequest request) {
		Endpoint endpoint = EndpointManager.getEndpointForRequest(request, endpoints);
		if (endpoint != null) {
			return endpoint.processRequest(request);
		} else {
			return super.processRequest(request);
		}
	}
	
	private static List<Endpoint> getAvailableEndpoints() {
    	List<Endpoint> endpoints = new ArrayList<Endpoint>();
    	endpoints.add(new DeltaReportingEndpoint());
    	return endpoints;
    }
	
}
