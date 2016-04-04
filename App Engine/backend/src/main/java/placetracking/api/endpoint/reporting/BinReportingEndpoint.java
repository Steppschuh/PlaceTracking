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
import placetracking.util.StringUtils;

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
                results.add(chartUrl);
            }
        } else {
            if (readable) {
                response = DeltaReportingEndpoint.getDeltaByTopicAsReadableText(request);
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

        // move actions into bins
        WebsiteRequest binsRequest = new WebsiteRequest(request.getServletRequest());
        binsRequest.addParameter("minimumTimestamp", String.valueOf(now - timeFrame));
        List<List<Action>> binnedActions = binnifyActions(actions, binsRequest);

        Map<User, List<Long>> bins = new HashMap<>();

        // iterate over all related users
        for (User user : users) {
            // keep track of the delta for each bin
            List<Long> deltas = new ArrayList<>();

            for (List<Action> binActions : binnedActions) {
                // calculate the delta for the current user in the current bin
                WebsiteRequest deltaRequest = new WebsiteRequest(request.getServletRequest());
                deltaRequest.addParameter("userId", String.valueOf(user.getId()));

                long delta = DeltaReportingEndpoint.getDeltaBetweenActionTimestamps(binActions, deltaRequest);
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
        sb.append("http://chartspree.io/bar.png?_style=light&_height=300px&_width=1200px");
        for (Map.Entry<User, List<Long>> bin : bins.entrySet()) {
            User user = bin.getKey();
            List<Long> deltas = bin.getValue();

            StringBuilder pb = new StringBuilder();
            pb.append(URLEncoder.encode(user.getName(), "UTF-8") + "=");
            for (int i = 0; i < deltas.size(); i++) {
                long value = TimeUnit.MILLISECONDS.toHours(deltas.get(i));
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

    public static List<List<Action>> binnifyActions(List<Action> actions, WebsiteRequest request) {
        long defaultMaximumTimestamp = (new Date()).getTime();
        long maximumTimestamp = request.getParameterAsLong("maximumTimestamp", defaultMaximumTimestamp);

        long defaultMinimumTimestamp = maximumTimestamp - TimeUnit.DAYS.toMillis(7);
        long minimumTimestamp = request.getParameterAsLong("minimumTimestamp", defaultMinimumTimestamp);

        long timestampDelta = maximumTimestamp - minimumTimestamp;
        long defaultBinCount = getDefaultBinCountForDuration(timestampDelta);

        int binCount = (int) request.getParameterAsLong("binCount", defaultBinCount);
        long binSize = timestampDelta / binCount;

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
            bins.set(binIndex, actions);
        }

        return bins;
    }

    public static long getDefaultBinCountForDuration(long duration) {
        long days = TimeUnit.MILLISECONDS.toDays(duration);
        long hours = TimeUnit.MILLISECONDS.toHours(duration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);

        if (days > 1) {
            if (days >= 60) {
                return days / 2;
            }
            if (days >= 30) {
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
