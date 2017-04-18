package placetracking.api.endpoint.reporting;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import placetracking.WebsiteRequest;
import placetracking.api.ApiResponse;
import placetracking.api.endpoint.Endpoint;
import placetracking.api.endpoint.EndpointManager;
import placetracking.api.endpoint.action.GetActionEndpoint;
import placetracking.api.endpoint.user.GetUserEndpoint;
import placetracking.datastore.model.Action;
import placetracking.datastore.model.User;
import placetracking.util.MapUtil;
import placetracking.util.StringUtils;
import placetracking.util.shortener.GoogleUrlShortener;

public class DeltaReportingEndpoint extends Endpoint {

    @Override
    public String getEndpointPath() {
        return EndpointManager.ENDPOINT_REPORTING_DELTA;
    }

    @Override
    public ApiResponse generateRequestResponse(WebsiteRequest request) throws Exception {
        boolean readable = request.getParameterAsBoolean("readable", false);
        ApiResponse response = new ApiResponse();
        List<Object> results = getRequestResponseEntries(request);
        response.setContent(results);
        response.setAsReadableResponse(readable);
        return response;
    }

    public static List<Object> getRequestResponseEntries(WebsiteRequest request) throws Exception {
        List<Object> results = new ArrayList<Object>();
        boolean readable = request.getParameterAsBoolean("readable", false);
        boolean detailed = request.getParameterAsBoolean("detailed", false);
        boolean chart = request.getParameterAsBoolean("chart", false);

        String response;
        if (detailed) {
            Map<User, Long> deltas = getDetailedTotalDeltaByTopic(request);
            if (readable) {
                response = getDetailedTotalDeltaByTopicAsReadableText(deltas);
                results.add(response);
            } else {
                results.add(deltas);
            }
            if (chart) {
                String chartUrl = getDetailedTotalDeltaByTopicAsPieChartUrl(deltas);
                String shortChartUrl = GoogleUrlShortener.getShortUrlAsString(chartUrl);
                results.add(shortChartUrl);
            }
        } else {
            if (readable) {
                response = getTotalDeltaByTopicAsReadableText(request);
                results.add(response);
            } else {
                long delta = getTotalDeltaBetweenActionTimestamps(request);
                results.add(delta);
            }
        }

        return results;
    }

    public static String getTotalDeltaByTopicAsReadableText(WebsiteRequest request) throws Exception {
        long delta = getTotalDeltaBetweenActionTimestamps(request);
        String response = StringUtils.millisToReadableTime(delta);
        return response;
    }

    public static String getDetailedTotalDeltaByTopicAsReadableText(WebsiteRequest request) throws Exception {
        Map<User, Long> deltas = getDetailedTotalDeltaByTopic(request);
        return getDetailedTotalDeltaByTopicAsReadableText(deltas);
    }

    public static String getDetailedTotalDeltaByTopicAsReadableText(Map<User, Long> deltas) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<User, Long> delta : deltas.entrySet()) {
            User user = delta.getKey();
            String readableTime = StringUtils.millisToReadableTime(delta.getValue());

            if (sb.length() > 0) {
                sb.append("\r\n");
            }
            sb.append(user.getName() + ": ");
            sb.append(readableTime);
        }
        return sb.toString();
    }

    public static String getDetailedTotalDeltaByTopicAsPieChartUrl(Map<User, Long> deltas) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("http://chartspree.io/pie.png?_style=light&_height=300px&_width=600px");
        for (Map.Entry<User, Long> delta : deltas.entrySet()) {
            User user = delta.getKey();
            long miniutes = TimeUnit.MILLISECONDS.toMinutes(delta.getValue());

            String parameter = URLEncoder.encode(user.getName(), "UTF-8") + "=" + miniutes;

            sb.append("&");
            sb.append(parameter);
        }
        return sb.toString();
    }

    public static Map<User, Long> getDetailedTotalDeltaByTopic(WebsiteRequest request) throws Exception {
        long topicId = request.getParameterAsLong("topicId", -1);
        List<User> users = GetUserEndpoint.getUsersByTopicId(topicId, request);
        Map<User, Long> deltas = new HashMap<User, Long>();

        for (User user : users) {
            WebsiteRequest deltaRequest = new WebsiteRequest(request.getServletRequest());
            deltaRequest.addParameter("userId", String.valueOf(user.getId()));

            long delta = getTotalDeltaBetweenActionTimestamps(deltaRequest);
            deltas.put(user, delta);
        }

        deltas = MapUtil.sortByValue(deltas);
        return deltas;
    }

    public static long getTotalDeltaBetweenActionTimestamps(WebsiteRequest request) throws Exception {
        long now = (new Date()).getTime();
        long timeFrame = request.getParameterAsLong("timeFrame", TimeUnit.DAYS.toMillis(30));

        // get matching actions
        WebsiteRequest actionsRequest = new WebsiteRequest(request.getServletRequest());
        actionsRequest.addParameter("minimumTimestamp", String.valueOf(now - timeFrame));
        List<Action> actions = GetActionEndpoint.getActionsWithFilters(actionsRequest);

        // calculate the delta
        long delta = getTotalDeltaBetweenActionTimestamps(actions, request);
        return delta;
    }

    /**
     * Maps a list of deltas to values between 0 and 100
     */
    public static List<Long> getRelativeDeltas(List<Long> deltas) {
        long maximumDelta = getMaximumDelta(deltas);
        List<Long> relativeDeltas = new ArrayList<>(deltas.size());
        for (int i = 0; i < deltas.size(); i++) {
            relativeDeltas.add((long) Math.round((100 * deltas.get(i)) / maximumDelta));
        }
        return relativeDeltas;
    }

    /**
     * Returns the largest value in a list of longs
     */
    public static long getMaximumDelta(List<Long> deltas) {
        long maximumDelta = 0;
        for (Long delta : deltas) {
            maximumDelta = Math.max(maximumDelta, delta);
        }
        return maximumDelta;
    }

    public static List<Session> getSessions(List<Action> actions, WebsiteRequest request) throws Exception {
        String startActionName = request.getParameter("startActionName", "start");
        String stopActionName = request.getParameter("stopActionName", "stop");
        long userId = request.getParameterAsLong("userId", -1);
        long topicId = request.getParameterAsLong("topicId", -1);

        long frameMinimumTimestamp = request.getParameterAsLong("frameMinimumTimestamp", -1);
        long frameMaximumTimestamp = request.getParameterAsLong("frameMaximumTimestamp", (new Date()).getTime());

        List<Session> sessions = new ArrayList<>();

        Action currentStartAction = null;
        Action currentStopAction = null;

        for (int i = 0; i < actions.size(); i++) {
            Action action = actions.get(i);

            // skip unrelated users
            if (userId > -1 && userId != action.getUserId()) {
                continue;
            }

            // skip unrelated topics
            if (topicId > -1 && topicId != action.getTopicId()) {
                continue;
            }

            // set the current action
            if (action.getName().equals(startActionName)) {
                currentStartAction = action;
            } else if (action.getName().equals(stopActionName)) {
                currentStopAction = action;
            }

            // calculate deltas
            if (currentStartAction != null && currentStopAction == null && i == 0) {
                // the latest action was a start action and has not been
                // stopped yet.
                Session session = new Session(action.getUserId(), action.getTopicId());
                session.setStartTimestamp(currentStartAction.getTimestamp());
                session.setStopTimestamp(frameMaximumTimestamp);
                session.calculateDelta();
                sessions.add(session);

                currentStartAction = null;
            } else if (currentStartAction == null && currentStopAction != null && i == actions.size() - 1) {
                // the oldest action was a stop action and has not been
                // started yet.
                if (frameMinimumTimestamp > -1) {
                    Session session = new Session(action.getUserId(), action.getTopicId());
                    session.setStartTimestamp(frameMinimumTimestamp);
                    session.setStopTimestamp(currentStopAction.getTimestamp());
                    session.calculateDelta();
                    sessions.add(session);

                    currentStopAction = null;
                }
            } else if (currentStartAction == null && currentStopAction != null) {
                // we have a stop action, continue iterating until we get a start action
            } else if (currentStartAction != null && currentStopAction == null) {
                // we have a start action that has not been stopped, ignore it
                currentStartAction = null;
            } else if (currentStartAction != null && currentStopAction != null) {
                // we have a start and stop action, jay!
                if (currentStartAction.getTimestamp() > currentStopAction.getTimestamp()) {
                    // someone messed up and added multiple start actions
                    currentStartAction = null;
                    continue;
                }

                Session session = new Session(action.getUserId(), action.getTopicId());
                session.setStartTimestamp(currentStartAction.getTimestamp());
                session.setStopTimestamp(currentStopAction.getTimestamp());
                session.calculateDelta();
                sessions.add(session);

                currentStartAction = null;
                currentStopAction = null;
            }
        }

        return sessions;
    }

    public static long getTotalDeltaBetweenActionTimestamps(List<Action> actions, WebsiteRequest request) throws Exception {
        List<Session> sessions = getSessions(actions, request);
        return getTotalDeltaFromSessions(sessions, request);
    }

    public static long getTotalDeltaFromSessions(List<Session> sessions, WebsiteRequest request) throws Exception {
        long userId = request.getParameterAsLong("userId", -1);
        long topicId = request.getParameterAsLong("topicId", -1);

        long totalDelta = 0;

        for (Session session : sessions) {
            // skip unrelated users
            if (userId > -1 && userId != session.getUserId()) {
                continue;
            }

            // skip unrelated topics
            if (topicId > -1 && topicId != session.getTopicId()) {
                continue;
            }

            totalDelta += session.getDelta();
        }

        return totalDelta;
    }

    public static Long getTotalDelta(List<Long> deltas) {
        long total = 0;
        for (Long delta : deltas) {
            total += delta;
        }
        return total;
    }

}
