package placetracking.api.endpoint.relation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import placetracking.WebsiteRequest;
import placetracking.api.ApiResponse;
import placetracking.api.endpoint.Endpoint;
import placetracking.api.endpoint.EndpointManager;
import placetracking.datastore.model.Action;
import placetracking.datastore.model.Relation;
import placetracking.datastore.model.Topic;
import placetracking.datastore.model.User;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;

public class AddRelationEndpoint extends Endpoint {
	
	@Override
	public boolean shouldHandleRequest(WebsiteRequest request) {
		// this endpoint is for internal use only
		return false;
	}

	@Override
	public List<String> getRequiredParameters(WebsiteRequest request) {
		List<String> params = super.getRequiredParameters(request);
		params.add("userId");
		params.add("topicId");
		return params;
	}
	
	@Override
	public ApiResponse generateRequestResponse(WebsiteRequest request) throws Exception {
		ApiResponse response = new ApiResponse();
		List<Relation> results = getRequestResponseEntries(request);
		response.setContent(results);
		return response;
	}
	
	public static List<Relation> getRequestResponseEntries(WebsiteRequest request) throws Exception {
		long userId = request.getParameterAsLong("userId", -1);
		long topicId = request.getParameterAsLong("topicId", -1);
		
		Relation relation = new Relation(userId, topicId);
		addRelation(relation);
		
		List<Relation> results = new ArrayList<Relation>();
		results.add(relation);
		return results;
	}
	
	public static void addRelationIfNotYetSet(long userId, long topicId) {
		Relation relation = GetRelationsEndpoint.getRelation(userId, topicId);
		if (relation != null) {
			return;
		}
		
		relation = new Relation(userId, topicId);
		addRelation(relation);
	}
	
	public static Key<Relation> addRelation(Relation relation) {
		Key<Relation> key = ObjectifyService.ofy()
		        .save()
		        .entity(relation)
		        .now();
		
		log.info("Added a new relation between user: " + relation.getUserId() + " and topic: " + relation.getTopicId());
		return key;
	}
	
}
