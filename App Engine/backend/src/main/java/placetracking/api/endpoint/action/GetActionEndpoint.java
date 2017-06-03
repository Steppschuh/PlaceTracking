package placetracking.api.endpoint.action;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;

import java.util.Date;
import java.util.List;
import java.util.Set;

import placetracking.WebsiteRequest;
import placetracking.api.ApiResponse;
import placetracking.api.endpoint.Endpoint;
import placetracking.api.endpoint.EndpointManager;
import placetracking.datastore.model.Action;

public class GetActionEndpoint extends Endpoint {

    public static final int MAXIMUM_ACTIONS_COUNT = 200;

    @Override
    public String getEndpointPath() {
        return EndpointManager.ENDPOINT_ACTIONS;
    }

    @Override
    public Set<String> getEndpointMethods() {
        return Endpoint.METHOD_GET;
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

        List<Action> results;
        if (id > -1) {
            results = getActionById(id);
        } else {
            results = getActionsWithFilters(request);
        }

        if (results.size() >= MAXIMUM_ACTIONS_COUNT) {
            throw new Exception("The result exceeded the maximum actions count (" + MAXIMUM_ACTIONS_COUNT + ")," +
                    "please use the limit and offset parameter or choose a smaller time frame");
        }

        log.info("Found " + results.size() + " action(s)");
        return results;
    }

    public static List<Action> getActionsWithFilters(WebsiteRequest request) throws Exception {
        long now = (new Date()).getTime();

        String name = request.getParameter("name");
        long userId = request.getParameterAsLong("userId", -1);
        long topicId = request.getParameterAsLong("topicId", -1);
        long minimumTimestamp = request.getParameterAsLong("minimumTimestamp", 0);
        long maximumTimestamp = request.getParameterAsLong("maximumTimestamp", now);
        int offset = request.getParameterAsInt("offset", 0);
        int count = request.getParameterAsInt("count", MAXIMUM_ACTIONS_COUNT / 2);
        count = Math.min(count, MAXIMUM_ACTIONS_COUNT);

        // make sure that no-one queries all actions that
        // aren't related to him / his topic
        if (userId < 0 && topicId < 0) {
            throw new Exception("No userId or topicId specified");
        }

        Query<Action> query = ObjectifyService.ofy()
                .load()
                .type(Action.class)
                .order("-timestamp")
                .limit(count)
                .offset(offset);

        if (name != null && name.length() > 0) {
            query = query.filter("name", name);
        }

        if (userId > -1) {
            query = query.filter("userId", userId);
        }

        if (topicId > -1) {
            query = query.filter("topicId", topicId);
        }

        if (minimumTimestamp > 0) {
            query = query.filter("timestamp >=", minimumTimestamp);
        }

        if (maximumTimestamp < now) {
            query = query.filter("timestamp <=", maximumTimestamp);
        }

        List<Action> results = query.list();
        return results;
    }

    public static List<Action> getActionById(long id) {
        Key<Action> key = Key.create(Action.class, id);
        List<Action> results = ObjectifyService.ofy()
                .load()
                .type(Action.class)
                .filterKey(key)
                .list();

        return results;
    }

}
