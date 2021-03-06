package placetracking.api.endpoint;

import java.util.ArrayList;
import java.util.List;

import placetracking.WebsiteRequest;
import placetracking.api.endpoint.action.AddActionEndpoint;
import placetracking.api.endpoint.action.DeleteActionEndpoint;
import placetracking.api.endpoint.action.GetActionEndpoint;
import placetracking.api.endpoint.relation.AddRelationEndpoint;
import placetracking.api.endpoint.relation.GetRelationsEndpoint;
import placetracking.api.endpoint.reporting.ReportingEndpoint;
import placetracking.api.endpoint.topic.AddTopicEndpoint;
import placetracking.api.endpoint.topic.GetTopicEndpoint;
import placetracking.api.endpoint.user.AddUserEndpoint;
import placetracking.api.endpoint.user.GetUserEndpoint;

public final class EndpointManager {

    public static final String HOST_LOCAL_DEV_SERVER = "localhost:8080";
    public static final String HOST_APP_ENGINE = "placetracking.appspot.com";

    public static final String ENDPOINT_API = "api/";

    public static final String ENDPOINT_TOPICS = "topics/";
    public static final String ENDPOINT_USERS = "users/";
    public static final String ENDPOINT_ACTIONS = "actions/";
    public static final String ENDPOINT_RELATIONS = "relations/";
    public static final String ENDPOINT_REPORTING = "reports/";
    public static final String ENDPOINT_REPORTING_DELTA = ENDPOINT_REPORTING + "delta/";
    public static final String ENDPOINT_REPORTING_BINS = ENDPOINT_REPORTING + "bins/";

    private static final List<Endpoint> endpoints = getAvailableEndpoints();

    private EndpointManager() {
    }

    private static List<Endpoint> getAvailableEndpoints() {
        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        endpoints.add(new ReportingEndpoint());

        endpoints.add(new GetActionEndpoint());
        endpoints.add(new AddActionEndpoint());
        endpoints.add(new DeleteActionEndpoint());

        endpoints.add(new GetTopicEndpoint());
        endpoints.add(new AddTopicEndpoint());

        endpoints.add(new GetUserEndpoint());
        endpoints.add(new AddUserEndpoint());

        endpoints.add(new GetRelationsEndpoint());
        endpoints.add(new AddRelationEndpoint());
        return endpoints;
    }

    public static Endpoint getEndpointForRequest(WebsiteRequest request) {
        return getEndpointForRequest(request, endpoints);
    }

    public static Endpoint getEndpointForRequest(WebsiteRequest request, List<Endpoint> availableEndpoints) {
        for (Endpoint endpoint : availableEndpoints) {
            if (endpoint.shouldHandleRequest(request)) {
                return endpoint;
            }
        }
        return null;
    }

}
