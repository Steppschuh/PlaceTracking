package placetracking.api.endpoint.user;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import placetracking.WebsiteRequest;
import placetracking.api.ApiResponse;
import placetracking.api.endpoint.Endpoint;
import placetracking.api.endpoint.EndpointManager;
import placetracking.api.endpoint.relation.GetRelationsEndpoint;
import placetracking.datastore.model.Relation;
import placetracking.datastore.model.User;

public class GetUserEndpoint extends Endpoint {

    @Override
    public String getEndpointPath() {
        return EndpointManager.ENDPOINT_USERS;
    }

    @Override
    public Set<String> getEndpointMethods() {
        return Endpoint.METHOD_GET;
    }

    @Override
    public ApiResponse generateRequestResponse(WebsiteRequest request) throws Exception {
        ApiResponse response = new ApiResponse();
        List<User> results = getRequestResponseEntries(request);
        response.setContent(results);
        return response;
    }

    public static List<User> getRequestResponseEntries(WebsiteRequest request) throws Exception {
        long id = request.getParameterAsLong("id", -1);
        long topicId = request.getParameterAsLong("topicId", -1);

        List<User> results = new ArrayList<User>();
        if (topicId > -1) {
            results = getUsersByTopicId(topicId, request);
        } else if (id > -1) {
            results = getUserById(id);
        }

        log.info("Found " + results.size() + " user(s)");
        return results;
    }

    public static List<User> getUsersByName(String name, WebsiteRequest request) {
        int offset = request.getParameterAsInt("offset", 0);
        int count = request.getParameterAsInt("count", 25);

        List<User> results = ObjectifyService.ofy()
                .load()
                .type(User.class)
                .limit(count)
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

    public static List<User> getUsersByTopicId(long topicId, WebsiteRequest request) {
        List<Long> userIds = getUserIdsByTopicId(topicId, request);
        List<User> users = new ArrayList<User>();

        for (Long userId : userIds) {
            List<User> macthingUsers = getUserById(userId);
            if (macthingUsers.size() > 0) {
                users.add(macthingUsers.get(0));
            }
        }

        return users;
    }

    public static List<Long> getUserIdsByTopicId(long topicId, WebsiteRequest request) {
        List<Long> userIds = new ArrayList<Long>();

        try {
            WebsiteRequest relationsRequest = new WebsiteRequest(request.getServletRequest());
            relationsRequest.setAttribute("topicId", topicId);

            List<Relation> relations = GetRelationsEndpoint.getRelationsWithFilters(relationsRequest);
            for (Relation relation : relations) {
                if (!userIds.contains(relation.getUserId())) {
                    userIds.add(relation.getUserId());
                }
            }

            log.info("Found " + userIds.size() + " user(s) related to topic with id: " + topicId);
        } catch (Exception e) {
            log.warning("Unable to get userIds by topicId: " + topicId);
            e.printStackTrace();
        }

        return userIds;
    }

}
