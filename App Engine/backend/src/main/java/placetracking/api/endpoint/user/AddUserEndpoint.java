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
import placetracking.datastore.model.User;

public class AddUserEndpoint extends Endpoint {

    @Override
    public String getEndpointPath() {
        return EndpointManager.ENDPOINT_USERS;
    }

    @Override
    public Set<String> getEndpointMethods() {
        return Endpoint.METHOD_POST;
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
        List<User> results = getRequestResponseEntries(request);
        response.setContent(results);
        return response;
    }

    public static List<User> getRequestResponseEntries(WebsiteRequest request) throws Exception {
        String name = request.getParameter("name");

        User user = new User(name);

        Key<User> key = ObjectifyService.ofy()
                .save()
                .entity(user)
                .now();

        List<User> results = new ArrayList<User>();
        results.add(user);

        log.info("Added a new user with name: " + name + " and id: " + user.getId());
        return results;
    }

}
