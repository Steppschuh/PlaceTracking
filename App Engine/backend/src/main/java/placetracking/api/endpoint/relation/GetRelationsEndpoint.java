package placetracking.api.endpoint.relation;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;

import java.util.List;
import java.util.Set;

import placetracking.WebsiteRequest;
import placetracking.api.ApiResponse;
import placetracking.api.endpoint.Endpoint;
import placetracking.api.endpoint.EndpointManager;
import placetracking.datastore.model.Relation;

public class GetRelationsEndpoint extends Endpoint {

    @Override
    public String getEndpointPath() {
        return EndpointManager.ENDPOINT_RELATIONS;
    }

    @Override
    public Set<String> getEndpointMethods() {
        return Endpoint.METHOD_GET;
    }

    @Override
    public ApiResponse generateRequestResponse(WebsiteRequest request) throws Exception {
        ApiResponse response = new ApiResponse();
        List<Relation> results = getRequestResponseEntries(request);
        response.setContent(results);
        return response;
    }

    public static List<Relation> getRequestResponseEntries(WebsiteRequest request) throws Exception {
        long id = request.getParameterAsLong("id", -1);

        List<Relation> results;
        if (id > -1) {
            results = getRelationById(id);
        } else {
            results = getRelationsWithFilters(request);
        }

        log.info("Found " + results.size() + " relation(s)");
        return results;
    }

    public static List<Relation> getRelationsWithFilters(WebsiteRequest request) throws Exception {
        long userId = request.getParameterAsLong("userId", -1);
        long topicId = request.getParameterAsLong("topicId", -1);
        int offset = request.getParameterAsInt("offset", 0);
        int count = request.getParameterAsInt("count", 25);

        // make sure that no-one queries all relations that
        // aren't related to him / his topic
        if (userId < 0 && topicId < 0) {
            throw new Exception("No userId or topicId specified");
        }

        Query<Relation> query = ObjectifyService.ofy()
                .load()
                .type(Relation.class)
                .order("userId")
                .limit(count)
                .offset(offset);

        if (userId > -1) {
            query = query.filter("userId", userId);
        }

        if (topicId > -1) {
            query = query.filter("topicId", topicId);
        }

        List<Relation> results = query.list();
        return results;
    }

    public static List<Relation> getRelationById(long id) {
        Key<Relation> key = Key.create(Relation.class, id);
        List<Relation> results = ObjectifyService.ofy()
                .load()
                .type(Relation.class)
                .filterKey(key)
                .list();

        return results;
    }

    public static Relation getRelation(long userId, long topicId) {
        List<Relation> results = ObjectifyService.ofy()
                .load()
                .type(Relation.class)
                .filter("userId", userId)
                .filter("topicId", topicId)
                .list();

        if (results.size() > 0) {
            return results.get(0);
        } else {
            return null;
        }
    }

}
