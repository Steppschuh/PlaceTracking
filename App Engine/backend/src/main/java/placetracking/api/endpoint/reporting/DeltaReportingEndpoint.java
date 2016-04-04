package placetracking.api.endpoint.reporting;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
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
			Map<User, Long> deltas = getDetailedDeltaByTopic(request);
			if (readable) {
				response = getDetailedDeltaByTopicAsReadableText(deltas);
				results.add(response);
			}
			if (chart) {
				String chartUrl = getDetailedDeltaByTopicAsPieChartUrl(deltas);
				results.add(chartUrl);
			}
		} else {
			if (readable) {
				response = getDeltaByTopicAsReadableText(request);
				results.add(response);
			} else {
				long delta = getDeltaBetweenActionTimestamps(request);
				results.add(delta);
			}
		}
		
		return results;
	}
	
	public static String getDeltaByTopicAsReadableText(WebsiteRequest request) throws Exception {
		long delta = getDeltaBetweenActionTimestamps(request);
		String response = StringUtils.millisToReadableTime(delta);
		return response;
	}
	
	public static String getDetailedDeltaByTopicAsReadableText(WebsiteRequest request) throws Exception {
		Map<User, Long> deltas = getDetailedDeltaByTopic(request);
		return getDetailedDeltaByTopicAsReadableText(deltas);
	}
	
	public static String getDetailedDeltaByTopicAsReadableText(Map<User, Long> deltas) throws Exception {
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
	
	public static String getDetailedDeltaByTopicAsPieChartUrl(Map<User, Long> deltas) throws Exception {
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
	
	public static Map<User, Long> getDetailedDeltaByTopic(WebsiteRequest request) throws Exception {
		long topicId = request.getParameterAsLong("topicId", -1);
		List<User> users = GetUserEndpoint.getUsersByTopicId(topicId, request);		
		Map<User, Long> deltas = new HashMap<User, Long>();
		
		for (User user : users) {
			WebsiteRequest deltaRequest = new WebsiteRequest(request.getServletRequest());
			deltaRequest.addParameter("userId", String.valueOf(user.getId()));
			
			long delta = getDeltaBetweenActionTimestamps(deltaRequest);
			deltas.put(user, delta);
		}

        deltas = MapUtil.sortByValue(deltas);
		return deltas;
	}
	
	public static long getDeltaBetweenActionTimestamps(WebsiteRequest request) throws Exception {
		long now = (new Date()).getTime();
		long timeFrame = request.getParameterAsLong("timeFrame", TimeUnit.DAYS.toMillis(30));
		
		// get matching actions
		WebsiteRequest actionsRequest = new WebsiteRequest(request.getServletRequest());
		actionsRequest.addParameter("minimumTimestamp", String.valueOf(now - timeFrame));		
		List<Action> actions = GetActionEndpoint.getActionsWithFilters(actionsRequest);
		
		// calculate the delta
		long delta = getDeltaBetweenActionTimestamps(actions, request);
		return delta;
	}
	
	public static long getDeltaBetweenActionTimestamps(List<Action> actions, WebsiteRequest request) throws Exception {
		String startActionName = request.getParameter("startActionName", "start");
		String stopActionName = request.getParameter("stopActionName", "stop");
		long userId = request.getParameterAsLong("userId", -1);
		long topicId = request.getParameterAsLong("topicId", -1);
		
		long delta = 0;
		
		Action currentStartAction = null;
		Action currentStopAction = null;
		
		for (Action action : actions) {
			// skip unrelated users
			if (userId > -1 && userId != (long) action.getUserId()) {
				continue;
			}
			
			// skip unrelated topics
			if (topicId > -1 && topicId != (long) action.getTopicId()) {
				continue;
			}
			
			// set the current action
			if (action.getName().equals(startActionName)) {
				currentStartAction = action;
			} else if (action.getName().equals(stopActionName)) {
				currentStopAction = action;
			}
			
			// calculate deltas
			if (currentStartAction != null && currentStopAction == null && delta == 0) {
				// the latest action was a start action and has not been
				// stopped yet. Use current time as stopAction timestamp
				long now = (new Date()).getTime();
				delta += now - currentStartAction.getTimestamp();
				currentStartAction = null;
			} else if (currentStartAction == null && currentStopAction != null) {
				// we have a stop action, continue iterating until we get a start action
			} else if (currentStartAction != null && currentStopAction != null) {
				// we have a start and stop action, jay!
				if (currentStartAction.getTimestamp() > currentStopAction.getTimestamp()) {
					// someone messed up and added multiple start actions
					currentStartAction = null;
					continue;
				}
				delta += currentStopAction.getTimestamp() - currentStartAction.getTimestamp();
				currentStartAction = null;
				currentStopAction = null;
			}
		}
		
		return delta;
	}
	
}
