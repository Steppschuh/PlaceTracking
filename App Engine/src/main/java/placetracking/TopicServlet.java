package placetracking;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import placetracking.datastore.model.Topic;

import com.googlecode.objectify.ObjectifyService;

public class TopicServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(TopicServlet.class.getSimpleName());

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        List<Topic> topics = ObjectifyService.ofy()
                .load()
                .type(Topic.class)
                .limit(15)
                .list();

        log.info("Found " + topics.size() + " topic(s)");
        for (Topic topic : topics) {
            log.info(" - " + topic.name);
        }
        
        resp.setContentType("text/plain");
        resp.getWriter().println("{ \"name\": \"Topic\" }");
    }

}
