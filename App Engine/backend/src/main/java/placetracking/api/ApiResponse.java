package placetracking.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import placetracking.WebsiteRequest;
import placetracking.util.slack.SlackPayload;

public class ApiResponse {

    int statusCode = HttpServletResponse.SC_OK;
    String statusMessage = "OK";
    Object content;

    transient String response;
    transient boolean loadedFromCache = false;
    transient boolean asReadableResponse = false;

    public String generateJsonResponse() {
        if (response != null && response.length() > 0) {
            return response;
        }
        response = "{}";
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting()
                    //.serializeNulls()
                    .create();
            response = gson.toJson(this);
        } catch (Exception ex) {

        }
        return response;
    }

    public String generateReadableResponse() {
        if (response != null && response.length() > 0) {
            return response;
        }
        response = "";
        try {
            StringBuilder sb = new StringBuilder();
            if (content instanceof List<?>) {
                List contentItems = (List<Object>) content;
                for (Object item : contentItems) {
                    if (sb.length() > 0) {
                        sb.append("\r\n");
                    }
                    sb.append(item.toString());
                }
            } else {
                sb.append(content.toString());
            }
            response = sb.toString();
        } catch (Exception ex) {

        }
        return response;
    }

    public SlackPayload generateSlackPayload(WebsiteRequest request) {
        SlackPayload slackPayload = new SlackPayload();

        String channel = request.getParameter("slackChannel", "#tracking");
        slackPayload.setChannel(channel);
        slackPayload.setUsername("Tracking API");
        slackPayload.setIcon_emoji(":stopwatch:");

        if (asReadableResponse) {
            slackPayload.setText(generateReadableResponse());
        } else {
            slackPayload.setText(generateJsonResponse());
        }

        return slackPayload;
    }

    public void send(HttpServletResponse resp) throws IOException {
        resp.addHeader("Access-Control-Allow-Origin", "*");
        if (asReadableResponse) {
            resp.setContentType("text/plain");
            resp.getWriter().write(this.generateReadableResponse());
        } else {
            resp.setContentType("application/json");
            resp.getWriter().write(this.generateJsonResponse());
        }
        resp.getWriter().flush();
        resp.getWriter().close();
    }

    public void setException(Exception exception) {
        if (exception != null) {
            if (exception.getMessage() != null) {
                statusMessage = exception.getMessage();
            } else {
                statusMessage = exception.toString();
            }
        } else {
            statusMessage = "Unknown internal server error";
        }
        if (statusCode == HttpServletResponse.SC_OK) {
            statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public boolean isLoadedFromCache() {
        return loadedFromCache;
    }

    public void setLoadedFromCache(boolean loadedFromCache) {
        this.loadedFromCache = loadedFromCache;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public boolean isReadableResponse() {
        return asReadableResponse;
    }

    public void setAsReadableResponse(boolean asReadableResponse) {
        this.asReadableResponse = asReadableResponse;
    }


}
