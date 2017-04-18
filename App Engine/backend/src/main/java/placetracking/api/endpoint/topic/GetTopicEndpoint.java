package placetracking.api.endpoint.topic;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;

import java.util.List;

import placetracking.WebsiteRequest;
import placetracking.api.ApiResponse;
import placetracking.api.endpoint.Endpoint;
import placetracking.api.endpoint.EndpointManager;
import placetracking.datastore.model.Topic;

public class GetTopicEndpoint extends Endpoint {

    @Override
    public String getEndpointPath() {
        return EndpointManager.ENDPOINT_TOPICS_GET;
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
        long id = request.getParameterAsLong("id", -1);

        List<Topic> results;
        if (name != null) {
            results = getTopicsByName(name, request);
        } else {
            results = getTopicById(id);
        }

        log.info("Found " + results.size() + " topic(s)");
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

    public static List<Topic> getTopicById(long id) {
        Key<Topic> key = Key.create(Topic.class, id);
        List<Topic> results = ObjectifyService.ofy()
                .load()
                .type(Topic.class)
                .filterKey(key)
                .list();

        return results;
    }

}
