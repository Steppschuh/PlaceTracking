package placetracking.api.endpoint;

import java.util.ArrayList;
import java.util.List;

import placetracking.WebsiteRequest;
import placetracking.api.endpoint.action.AddActionEndpoint;
import placetracking.api.endpoint.action.DeleteActionEndpoint;
import placetracking.api.endpoint.action.GetActionEndpoint;
import placetracking.api.endpoint.relation.AddRelationEndpoint;
import placetracking.api.endpoint.relation.GetRelationsEndpoint;
import placetracking.api.endpoint.topic.AddTopicEndpoint;
import placetracking.api.endpoint.topic.GetTopicEndpoint;
import placetracking.api.endpoint.user.AddUserEndpoint;
import placetracking.api.endpoint.user.GetUserEndpoint;

public final class EndpointManager {
    
    public static final String HOST_LOCAL_DEV_SERVER = "localhost:8888";
	public static final String HOST_APP_ENGINE = "placetracking.appspot.com";
    
    public static final String ENDPOINT_API = "api/";

    public static final String ENDPOINT_TOPICS = ENDPOINT_API + "topics/";
    public static final String ENDPOINT_TOPICS_GET = ENDPOINT_TOPICS + "get/";
    public static final String ENDPOINT_TOPICS_ADD = ENDPOINT_TOPICS + "add/";
    
    public static final String ENDPOINT_USERS = ENDPOINT_API + "users/";
    public static final String ENDPOINT_USERS_GET = ENDPOINT_USERS + "get/";
    public static final String ENDPOINT_USERS_ADD = ENDPOINT_USERS + "add/";
    
    public static final String ENDPOINT_ACTIONS = ENDPOINT_API + "actions/";
    public static final String ENDPOINT_ACTIONS_GET = ENDPOINT_ACTIONS + "get/";
    public static final String ENDPOINT_ACTIONS_ADD = ENDPOINT_ACTIONS + "add/";
    public static final String ENDPOINT_ACTIONS_DELETE = ENDPOINT_ACTIONS + "delete/";
    
    public static final String ENDPOINT_RELATIONS = ENDPOINT_API + "relations/";
    public static final String ENDPOINT_RELATIONS_GET = ENDPOINT_RELATIONS + "get/";
    public static final String ENDPOINT_RELATIONS_ADD = ENDPOINT_RELATIONS + "add/";
    
    public static final List<Endpoint> endpoints = getAvailableEndpoints();
    
    private EndpointManager() {
    	
    }
    
    private static List<Endpoint> getAvailableEndpoints() {
    	List<Endpoint> endpoints = new ArrayList<Endpoint>();
    	endpoints.add(new GetTopicEndpoint());
    	endpoints.add(new AddTopicEndpoint());
    	endpoints.add(new GetUserEndpoint());
    	endpoints.add(new AddUserEndpoint());
    	endpoints.add(new GetActionEndpoint());
    	endpoints.add(new AddActionEndpoint());
    	endpoints.add(new DeleteActionEndpoint());
    	endpoints.add(new GetRelationsEndpoint());
    	endpoints.add(new AddRelationEndpoint());
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
