package placetracking.datastore.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.Date;

@Entity
public class Action {

    public static final String ACTION_START = "start";
    public static final String ACTION_STOP = "stop";

    public static final String ACTION_PAUSE = "pause";
    public static final String ACTION_RESUME = "resume";

    public static final String ACTION_ARRIVE = "arrive";
    public static final String ACTION_LEAVE = "leave";

    @Id
    private Long id;

    @Index
    private Long userId;
    @Index
    private Long topicId;
    @Index
    private String name;
    @Index
    private Long timestamp;

    public Action() {
        timestamp = (new Date()).getTime();
    }

    public Action(String name) {
        this();
        this.name = name;
    }

    public Action(Long userId, Long topicId, String name) {
        super();
        this.userId = userId;
        this.topicId = topicId;
        this.name = name;
    }

    public Action byUser(long userId) {
        this.userId = userId;
        return this;
    }

    public Action onTopic(long topicId) {
        this.topicId = topicId;
        return this;
    }

    @Override
    public String toString() {
        return "name: " + name + " timestamp: " + timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}