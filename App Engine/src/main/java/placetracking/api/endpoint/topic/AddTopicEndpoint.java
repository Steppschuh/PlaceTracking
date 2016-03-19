package placetracking.api.endpoint.topic;

import java.util.ArrayList;
import java.util.List;

import placetracking.WebsiteRequest;
import placetracking.api.ApiResponse;
import placetracking.api.endpoint.Endpoint;
import placetracking.api.endpoint.EndpointManager;
import placetracking.datastore.model.Topic;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;

public class AddTopicEndpoint extends Endpoint {
	
	@Override
	public String getEndpointPath() {
		return EndpointManager.ENDPOINT_TOPICS_ADD;
	}

	@Override
	public List<String> getRequiredParameters(WebsiteRequest request) {
		List<String> params = super.getRequiredParameters(request);
		params.add("name");
		return params;
	}
	
	@Override
	public ApiResponse generateRequestResponse(WebsiteRequest request) throws Exception {
		ApiResponse response = new ApiResponse();
		List<Topic> results = getRequestResponseEntries(request);
		response.setContent(results);
		return response;
	}
	
	public static List<Topic> getRequestResponseEntries(WebsiteRequest request) throws Exception {
		String name = request.getParameter("name");
		
		Topic topic = new Topic(name);
		
		Key<Topic> key = ObjectifyService.ofy()
                .save()
                .entity(topic)
                .now();
		
		List<Topic> results = new ArrayList<Topic>();
		results.add(topic);
		
		log.info("Added a new topic with name: " + name + " and id: " + topic.getId());
		return results;
	}
	
}
