package placetracking.api.endpoint.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
		
		long delta = getDeltaBetweenActionTimestamps(request);
		if (readable) {
			String response;
			if (detailed) {
				response = getReadableDetailedDeltaByTopic(request);
			} else {
				response = getReadableDeltaByTopic(request);
			}
			log.info(response);
			results.add(response);
		} else {
			results.add(delta);
		}
		
		return results;
	}
	
	public static String getReadableDeltaByTopic(WebsiteRequest request) throws Exception {
		long delta = getDeltaBetweenActionTimestamps(request);
		String response = StringUtils.millisToReadableTime(delta);
		return response;
	}
	
	public static String getReadableDetailedDeltaByTopic(WebsiteRequest request) throws Exception {
		long topicId = request.getParameterAsLong("topicId", -1);
		List<User> users = GetUserEndpoint.getUsersByTopicId(topicId, request);
		
		StringBuilder sb = new StringBuilder();
		for (User user : users) {
			WebsiteRequest deltaRequest = new WebsiteRequest(request.getServletRequest());
			deltaRequest.addParameter("userId", String.valueOf(user.getId()));
			
			long delta = getDeltaBetweenActionTimestamps(deltaRequest);
			String readableTime = StringUtils.millisToReadableTime(delta);
			
			if (sb.length() > 0) {
				sb.append("\r\n");
			}
			sb.append(user.getName() + ": ");
			sb.append(readableTime);
		}
		
		return sb.toString();
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
