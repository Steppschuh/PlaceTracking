package placetracking.api.endpoint;

import java.util.ArrayList;
import java.util.List;

import placetracking.WebsiteRequest;
import placetracking.api.endpoint.topic.AddTopicEndpoint;
import placetracking.api.endpoint.topic.GetTopicEndpoint;
import placetracking.api.endpoint.user.AddUserEndpoint;
import placetracking.api.endpoint.user.GetUserEndpoint;

public final class EndpointManager {
    
    public static final String HOST_LOCAL_DEV_SERVER = "localhost:8888";
	public static final String HOST_APP_ENGINE = "placetracking.appspot.com/";
    
    public static final String ENDPOINT_API = "api/";

    public static final String ENDPOINT_TOPICS = ENDPOINT_API + "topics/";
    public static final String ENDPOINT_TOPICS_GET = ENDPOINT_TOPICS + "get/";
    public static final String ENDPOINT_TOPICS_ADD = ENDPOINT_TOPICS + "add/";
    
    public static final String ENDPOINT_USER = ENDPOINT_API + "user/";
    public static final String ENDPOINT_USER_GET = ENDPOINT_USER + "get/";
    public static final String ENDPOINT_USER_ADD = ENDPOINT_USER + "add/";
	
    public static final List<Endpoint> endpoints = getAvailableEndpoints();
    
    private EndpointManager() {
    	
    }
    
    private static List<Endpoint> getAvailableEndpoints() {
    	List<Endpoint> endpoints = new ArrayList<Endpoint>();
    	endpoints.add(new GetTopicEndpoint());
    	endpoints.add(new AddTopicEndpoint());
    	endpoints.add(new GetUserEndpoint());
    	endpoints.add(new AddUserEndpoint());
    	return endpoints;
    }
	
    public static Endpoint getEndpointForRequest(WebsiteRequest request) {
    	return getEndpointForRequest(request, endpoints);
    }
    
    public static Endpoint getEndpointForRequest(WebsiteRequest request, List<Endpoint> availableEndpoints) {
    	for (Endpoint endpoint : availableEndpoints) {
    		if (endpoint.shouldHandleRequest(request)) {
    			return endpoint;
    		}
    	}
    	return null;
    }
    
}
