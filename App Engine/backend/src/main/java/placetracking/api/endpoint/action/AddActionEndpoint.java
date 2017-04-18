package placetracking.api.endpoint.action;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;

import java.util.ArrayList;
import java.util.List;

import placetracking.WebsiteRequest;
import placetracking.api.ApiResponse;
import placetracking.api.endpoint.Endpoint;
import placetracking.api.endpoint.EndpointManager;
import placetracking.api.endpoint.relation.AddRelationEndpoint;
import placetracking.datastore.model.Action;

public class AddActionEndpoint extends Endpoint {

    @Override
    public String getEndpointPath() {
        return EndpointManager.ENDPOINT_ACTIONS_ADD;
    }

    @Override
    public List<String> getRequiredParameters(WebsiteRequest request) {
        List<String> params = super.getRequiredParameters(request);
        params.add("name");
        params.add("userId");
        params.add("topicId");
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
        String name = request.getParameter("name");
        long userId = request.getParameterAsLong("userId", -1);
        long topicId = request.getParameterAsLong("topicId", -1);

        // add a relation for the user to the topic, if not already set
        AddRelationEndpoint.addRelationIfNotYetSet(userId, topicId);

        // add the new action
        Action action = new Action(name)
                .byUser(userId)
                .onTopic(topicId);

        Key<Action> key = ObjectifyService.ofy()
                .save()
                .entity(action)
                .now();

        List<Action> results = new ArrayList<Action>();
        results.add(action);

        log.info("Added a new action with name: " + name + " and id: " + action.getId());
        return results;
    }

}
