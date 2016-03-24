package placetracking.api.endpoint.reporting;

import java.util.ArrayList;
import java.util.List;

import placetracking.WebsiteRequest;
import placetracking.api.ApiResponse;
import placetracking.api.endpoint.Endpoint;
import placetracking.api.endpoint.EndpointManager;
import placetracking.api.endpoint.user.GetUserEndpoint;
import placetracking.datastore.model.Topic;
import placetracking.datastore.model.User;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;

public class ReportingEndpoint extends Endpoint {
	
	@Override
	public String getEndpointPath() {
		return EndpointManager.ENDPOINT_REPORTING;
	}
	
	@Override
	public ApiResponse generateRequestResponse(WebsiteRequest request) throws Exception {
		ApiResponse response = new ApiResponse();
		List<Object> results = getRequestResponseEntries(request);
		response.setContent(results);
		return response;
	}
	
	public static List<Object> getRequestResponseEntries(WebsiteRequest request) throws Exception {
		long topicId = request.getParameterAsLong("topicId", -1);
		
		List<Object> results = new ArrayList<Object>();
		
		List<User> users = GetUserEndpoint.getUsersByTopicId(topicId, request);
		
		results.addAll(users);
		
		return results;
	}
	
	public static List<Topic> getTopicsByName(String name, WebsiteRequest request) {
		int offset = request.getParameterAsInt("offset", 0);
		int limit = request.getParameterAsInt("limit", 25);
		
		List<Topic> results = ObjectifyService.ofy()
                .load()
                .type(Topic.class)
                .limit(limit)
                .offset(offset)
                .filter("name", name)
                .list();
		
		return results;
	}
	
}
