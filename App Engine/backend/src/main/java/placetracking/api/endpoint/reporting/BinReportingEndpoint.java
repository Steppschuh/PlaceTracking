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
import placetracking.util.DateUtil;
import placetracking.util.StringUtils;
import placetracking.util.shortener.GoogleUrlShortener;

public class BinReportingEndpoint extends Endpoint {

    @Override
    public String getEndpointPath() {
        return EndpointManager.ENDPOINT_REPORTING_BINS;
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
            Map<User, List<Long>> bins = getDetailedBinsByTopic(request);
            if (readable) {
                response = getDetailedBinsByTopicAsReadableText(bins);
                results.add(response);
            } else {
                results.add(bins);
            }
            if (chart) {
                String chartUrl = getDetailedBinsByTopicAsLineChartUrl(bins);
                String shortChartUrl = GoogleUrlShortener.getShortUrlAsString(chartUrl);
                results.add(shortChartUrl);
            }
        } else {
            if (readable) {
                response = DeltaReportingEndpoint.getTotalDeltaByTopicAsReadableText(request);
                results.add(response);
            } else {
                List<Long> bins = getBinsByTopic(request);
                results.add(bins);
            }
        }

        return results;
    }

    public static List<Long> getBinsByTopic(WebsiteRequest request) throws Exception {
        Map<User, List<Long>> detailedBins = getDetailedBinsByTopic(request);

        List<Long> bins = new ArrayList<>();

        // iterate over all users
        for (Map.Entry<User, List<Long>> detailedBin : detailedBins.entrySet()) {
            // initialize bins with 0s
            if (bins.isEmpty()) {
                for (int i = 0; i < detailedBin.getValue().size(); i++) {
                    bins.add(0l);
                }
            }

            // add the delta to the total delta for each bin
            List<Long> deltas = detailedBin.getValue();
            for (int i = deltas.size() - 1; i >= 0; i--) {
                bins.set(i, bins.get(i) + deltas.get(i));
            }
        }

        return bins;
    }

    public static Map<User, List<Long>> getDetailedBinsByTopic(WebsiteRequest request) throws Exception {
        long topicId = request.getParameterAsLong("topicId", -1);
        List<User> users = GetUserEndpoint.getUsersByTopicId(topicId, request);

        long now = (new Date()).getTime();
        long timeFrame = request.getParameterAsLong("timeFrame", TimeUnit.DAYS.toMillis(30));

        // get matching actions
        WebsiteRequest actionsRequest = new WebsiteRequest(request.getServletRequest());
        actionsRequest.addParameter("minimumTimestamp", String.valueOf(now - timeFrame));
        List<Action> actions = GetActionEndpoint.getActionsWithFilters(actionsRequest);

        Map<User, List<Long>> bins = new HashMap<>();

        // iterate over all related users
        for (User user : users) {

            // get sessions for the current user
            WebsiteRequest binsRequest = new WebsiteRequest(request.getServletRequest());
            binsRequest.addParameter("frameMinimumTimestamp", String.valueOf(now - timeFrame));
            binsRequest.addParameter("userId", String.valueOf(user.getId()));
            List<Session> sessions = DeltaReportingEndpoint.getSessions(actions, binsRequest);

            // binnify sessions
            List<List<Session>> binnedSessions = binnifySessions(sessions, binsRequest);

            // keep track of the delta for each bin
            List<Long> deltas = new ArrayList<>();

            for (int i = 0; i < binnedSessions.size(); i++) {
                List<Session> binSessions = binnedSessions.get(i);

                // calculate the delta for the current user in the current bin
                WebsiteRequest deltaRequest = new WebsiteRequest(request.getServletRequest());
                deltaRequest.addParameter("userId", String.valueOf(user.getId()));
                long delta = DeltaReportingEndpoint.getTotalDeltaFromSessions(binSessions, deltaRequest);
                deltas.add(delta);
            }

            bins.put(user, deltas);
        }

        return bins;
    }

    public static String getDetailedBinsByTopicAsReadableText(Map<User, List<Long>> bins) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<User, List<Long>> bin : bins.entrySet()) {
            User user = bin.getKey();
            Long delta = DeltaReportingEndpoint.getTotalDelta(bin.getValue());
            String readableTime = StringUtils.millisToReadableTime(delta);

            if (sb.length() > 0) {
                sb.append("\r\n");
            }
            sb.append(user.getName() + ": ");
            sb.append(readableTime);
        }
        return sb.toString();
    }

    public static String getDetailedBinsByTopicAsLineChartUrl(Map<User, List<Long>> bins) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("http://chartspree.io/line.png?_style=light&_fill=true&_height=300px&_width=900px");
        for (Map.Entry<User, List<Long>> bin : bins.entrySet()) {
            User user = bin.getKey();
            List<Long> deltas = bin.getValue();

            StringBuilder pb = new StringBuilder();
            pb.append(URLEncoder.encode(user.getName(), "UTF-8") + "=");
            for (int i = 0; i < deltas.size(); i++) {
                long value = TimeUnit.MILLISECONDS.toMinutes(deltas.get(i));
                pb.append(String.valueOf(value));
                if (i < deltas.size() - 1) {
                    pb.append(",");
                }
            }

            sb.append("&");
            sb.append(pb.toString());
        }
        return sb.toString();
    }

    public static List<List<Session>> binnifySessions(List<Session> sessions, WebsiteRequest request) {
        WebsiteRequest binnifyRequest = getDefaultBinnifyRequest(request);

        long maximumTimestamp = binnifyRequest.getParameterAsLong("maximumTimestamp", -1);
        long minimumTimestamp = binnifyRequest.getParameterAsLong("minimumTimestamp", -1);
        long timestampDelta = binnifyRequest.getParameterAsLong("timestampDelta", -1);
        int binCount = (int) binnifyRequest.getParameterAsLong("binCount", -1);
        long binSize = binnifyRequest.getParameterAsLong("binSize", -1);

        List<List<Session>> bins = new ArrayList<>(binCount);
        for (int i = 0; i < binCount; i++) {
            bins.add(new ArrayList<Session>());
        }

        for (int i = 0; i < sessions.size(); i++) {
            Session session = sessions.get(i);

            // skip out of range sessions
            if (session.getStopTimestamp() < minimumTimestamp || session.getStartTimestamp() > maximumTimestamp) {
                continue;
            }

            // calculate in which bin the session starts
            long startTimestampOffset = Math.max(0, session.getStartTimestamp() - minimumTimestamp);
            int startBinIndex = (int) Math.floor(startTimestampOffset / binSize);

            // calculate in which bin the session stops
            long stopTimestampOffset = Math.min((binCount * binSize) - 1, session.getStopTimestamp() - minimumTimestamp);
            int stopBinIndex = (int) Math.floor(stopTimestampOffset / binSize);

            if (startBinIndex == stopBinIndex) {
                // the session doesn't overlap any bins
                long binStartTimestamp = minimumTimestamp + (startBinIndex * binSize);
                long binStopTimestamp = binStartTimestamp + binSize;

                Session adjustedSession = getAdjustedSession(session, binStartTimestamp, binStopTimestamp);
                bins.get(startBinIndex).add(adjustedSession);
            } else {
                // the session overlaps multiple bins, add a new session for every bin
                int overlappingBins = stopBinIndex - startBinIndex;
                for (int overlappedBinIndex = 0; overlappedBinIndex <= overlappingBins; overlappedBinIndex++) {
                    int binIndex = startBinIndex + overlappedBinIndex;

                    long binStartTimestamp = minimumTimestamp + (binIndex * binSize);
                    long binStopTimestamp = binStartTimestamp + binSize;

                    Session adjustedSession = getAdjustedSession(session, binStartTimestamp, binStopTimestamp);
                    bins.get(binIndex).add(adjustedSession);
                }
            }
        }

        return bins;
    }

    public static Session getAdjustedSession(Session originalSession, long minimumTimestamp, long maximumTimestamp) {
        Session adjustedSession = new Session(originalSession);
        adjustedSession.setMinimumStartTimestamp(minimumTimestamp);
        adjustedSession.setMaximumStopTimestamp(maximumTimestamp);
        adjustedSession.calculateDelta();
        return adjustedSession;
    }

    public static List<List<Action>> binnifyActions(List<Action> actions, WebsiteRequest request) {
        WebsiteRequest binnifyRequest = getDefaultBinnifyRequest(request);

        long maximumTimestamp = binnifyRequest.getParameterAsLong("maximumTimestamp", -1);
        long minimumTimestamp = binnifyRequest.getParameterAsLong("minimumTimestamp", -1);
        long timestampDelta = binnifyRequest.getParameterAsLong("timestampDelta", -1);
        int binCount = (int) binnifyRequest.getParameterAsLong("binCount", -1);
        long binSize = binnifyRequest.getParameterAsLong("binSize", -1);

        List<List<Action>> bins = new ArrayList<>(binCount);
        for (int i = 0; i < binCount; i++) {
            bins.add(new ArrayList<Action>());
        }

        // iterate over actions in reverse order to maintain order in bins
        for (int i = actions.size() - 1; i >= 0; i--) {
            // skip out of range actions
            Action action = actions.get(i);
            if (action.getTimestamp() < minimumTimestamp || action.getTimestamp() > maximumTimestamp) {
                continue;
            }

            // add action into matching bin
            long timestampOffset = action.getTimestamp() - minimumTimestamp;
            int binIndex = (int) Math.floor(timestampOffset / binSize);
            bins.get(binIndex).add(action);
        }

        return bins;
    }

    public static WebsiteRequest getDefaultBinnifyRequest(WebsiteRequest request) {
        long now = (new Date()).getTime();

        long defaultTimeFrame = TimeUnit.DAYS.toMillis(7);
        long timeFrame = request.getParameterAsLong("timeFrame", defaultTimeFrame);

        long defaultMaximumTimestamp;
        if (TimeUnit.MILLISECONDS.toDays(timeFrame) > 1) {
            defaultMaximumTimestamp = DateUtil.roundTimestampToNexDay(now);
        } else {
            defaultMaximumTimestamp = now;
        }

        long maximumTimestamp = request.getParameterAsLong("maximumTimestamp", defaultMaximumTimestamp);

        long defaultMinimumTimestamp = maximumTimestamp - timeFrame;
        long minimumTimestamp = request.getParameterAsLong("minimumTimestamp", defaultMinimumTimestamp);

        long timestampDelta = maximumTimestamp - minimumTimestamp;
        long defaultBinCount = getDefaultBinCountForDuration(timestampDelta);

        int binCount = (int) request.getParameterAsLong("binCount", defaultBinCount);
        long binSize = timestampDelta / binCount;

        WebsiteRequest defaultRequest = new WebsiteRequest(request.getServletRequest());
        defaultRequest.addParameter("maximumTimestamp", String.valueOf(maximumTimestamp));
        defaultRequest.addParameter("minimumTimestamp", String.valueOf(minimumTimestamp));
        defaultRequest.addParameter("binCount", String.valueOf(binCount));
        defaultRequest.addParameter("binSize", String.valueOf(binSize));
        defaultRequest.addParameter("timestampDelta", String.valueOf(timestampDelta));

        return defaultRequest;
    }

    public static long getDefaultBinCountForDuration(long duration) {
        long days = TimeUnit.MILLISECONDS.toDays(duration);
        long hours = TimeUnit.MILLISECONDS.toHours(duration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);

        if (days > 1) {
            if (days >= 60) {
                return days / 2;
            }
            if (days >= 14) {
                return days;
            }
            if (days >= 7) {
                return days * 2;
            }
            return days * 4;
        } else if (hours > 1) {
            if (hours >= 24) {
                return hours;
            }
            if (hours >= 12) {
                return hours * 2;
            }
            return hours * 4;
        } else {
            if (minutes >= 60) {
                return minutes / 2;
            }
            if (minutes >= 30) {
                return minutes;
            }
            return minutes * 2;
        }
    }

}
