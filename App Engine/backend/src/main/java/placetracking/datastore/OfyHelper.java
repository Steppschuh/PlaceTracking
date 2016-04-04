package placetracking.datastore;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import placetracking.datastore.model.Action;
import placetracking.datastore.model.Relation;
import placetracking.datastore.model.Topic;
import placetracking.datastore.model.User;

import com.googlecode.objectify.ObjectifyService;

public class OfyHelper implements ServletContextListener {
	
	public void contextInitialized(ServletContextEvent event) {
        // This will be invoked as part of a warmup request, or the first user request if no warmup
        // request.
        ObjectifyService.register(Topic.class);
        ObjectifyService.register(User.class);
        ObjectifyService.register(Action.class);
        ObjectifyService.register(Relation.class);
    }

    public void contextDestroyed(ServletContextEvent event) {
        // App Engine does not currently invoke this method.
    }

}