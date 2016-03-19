package placetracking.api.endpoint.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import placetracking.WebsiteRequest;
import placetracking.api.ApiResponse;
import placetracking.api.endpoint.Endpoint;
import placetracking.api.endpoint.EndpointManager;
import placetracking.datastore.model.Action;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;

public class DeleteActionEndpoint extends Endpoint {
	
	@Override
	public String getEndpointPath() {
		return EndpointManager.ENDPOINT_ACTIONS_DELETE;
	}
	
	@Override
	public List<String> getRequiredParameters(WebsiteRequest request) {
		List<String> params = super.getRequiredParameters(request);
		params.add("id");
		return params;
	}
	
	@Override
	public ApiResponse generateRequestResponse(WebsiteRequest request) throws Exception {
		ApiResponse response = new ApiResponse();
		List<Action> results = getRequestResponseEntries(request);
		response.setContent(results);
		return response;
	}
	
	public static List<Action> getRequestResponseEntries(WebsiteRequest request) throws Exception {
		long id = request.getParameterAsLong("id", -1);
		
		deleteActionById(id);
		
		log.info("Deleted action with id: " + id);
		return new ArrayList<Action>();
	}
	
	public static void deleteActionById(long id) {
		Key<Action> key = Key.create(Action.class, id);
		ObjectifyService.ofy()
                .delete()
                .key(key);
	}
	
}
