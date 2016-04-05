package placetracking.api.endpoint.reporting;

import placetracking.util.StringUtils;

public class Session {

    private long startTimestamp;
    private long stopTimestamp;
    private long userId;
    private long topicId;
    private long delta;

    public Session() {
        startTimestamp = -1;
        stopTimestamp = -1;
        userId = -1;
        topicId = -1;
        delta = 0;
    }

    public Session(long userId, long topicId) {
        this();
        this.userId = userId;
        this.topicId = topicId;
    }

    public Session(Session session) {
        this.startTimestamp = session.getStartTimestamp();
        this.stopTimestamp = session.getStopTimestamp();
        this.userId = session.getUserId();
        this.topicId = session.getTopicId();
        this.delta = session.getDelta();
    }

    @Override
    public String toString() {
        return startTimestamp + " - " + stopTimestamp + " (" + StringUtils.millisToReadableTime(delta) + ")";
    }

    public long calculateDelta() {
        delta = calculateDelta(startTimestamp, stopTimestamp);
        return delta;
    }

    public static long calculateDelta(long startTimestamp, long stopTimestamp) {
        long delta;
        if (startTimestamp < 0 || stopTimestamp < 0) {
            delta = 0;
        } else {
            delta = Math.max(0, stopTimestamp - startTimestamp);
        }
        return delta;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public void setMinimumStartTimestamp(long startTimestamp) {
        this.startTimestamp = Math.max(this.startTimestamp, startTimestamp);
    }

    public long getStopTimestamp() {
        return stopTimestamp;
    }

    public void setStopTimestamp(long stopTimestamp) {
        this.stopTimestamp = stopTimestamp;
    }

    public void setMaximumStopTimestamp(long stopTimestamp) {
        this.stopTimestamp = Math.min(this.stopTimestamp, stopTimestamp);
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getTopicId() {
        return topicId;
    }

    public void setTopicId(long topicId) {
        this.topicId = topicId;
    }

    public long getDelta() {
        return delta;
    }

    public void setDelta(long delta) {
        this.delta = delta;
    }

}
