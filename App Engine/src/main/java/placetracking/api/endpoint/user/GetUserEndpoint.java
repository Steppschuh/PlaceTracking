package placetracking.api.endpoint.user;

import java.util.List;

import placetracking.WebsiteRequest;
import placetracking.api.ApiResponse;
import placetracking.api.endpoint.Endpoint;
import placetracking.api.endpoint.EndpointManager;
import placetracking.datastore.model.Topic;
import placetracking.datastore.model.User;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;

public class GetUserEndpoint extends Endpoint {
	
	@Override
	public String getEndpointPath() {
		return EndpointManager.ENDPOINT_USERS_GET;
	}
	
	@Override
	public ApiResponse generateRequestResponse(WebsiteRequest request) throws Exception {
		ApiResponse response = new ApiResponse();
		List<User> results = getRequestResponseEntries(request);
		response.setContent(results);
		return response;
	}
	
	public static List<User> getRequestResponseEntries(WebsiteRequest request) throws Exception {
		String name = request.getParameter("name");
		long id = request.getParameterAsLong("id", -1);
		
		List<User> results;
		if (name != null) {
			results = getUsersByName(name, request);
		} else {
			results = getUserById(id);
		}
		
		log.info("Found " + results.size() + " user(s)");
		return results;
	}
	
	public static List<User> getUsersByName(String name, WebsiteRequest request) {
		int offset = request.getParameterAsInt("offset", 0);
		int limit = request.getParameterAsInt("limit", 25);
		
		List<User> results = ObjectifyService.ofy()
                .load()
                .type(User.class)
                .limit(limit)
                .offset(offset)
                .filter("name", name)
                .list();
		
		return results;
	}
	
	public static List<User> getUserById(long id) {
		Key<User> key = Key.create(User.class, id);
		List<User> results = ObjectifyService.ofy()
                .load()
                .type(User.class)
                .filterKey(key)
                .list();
		
		return results;
	}
	
}
