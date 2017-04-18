package placetracking;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

public class WebsiteResponse {

    int statusCode = HttpServletResponse.SC_OK;
    String statusMessage = "OK";

    String content;
    boolean loadedFromCache = false;

    HttpServletResponse servletResponse;

    public WebsiteResponse(HttpServletResponse servletResponse) {
        this.servletResponse = servletResponse;
    }

    public void send(HttpServletResponse resp) throws IOException {
        resp.getWriter().write(content);
        resp.getWriter().flush();
        resp.getWriter().close();
    }

    public void redirect(String url) {
        try {
            servletResponse.sendRedirect(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public boolean isLoadedFromCache() {
        return loadedFromCache;
    }

    public void setLoadedFromCache(boolean loadedFromCache) {
        this.loadedFromCache = loadedFromCache;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String response) {
        this.content = response;
    }

    public HttpServletResponse getServletResponse() {
        return servletResponse;
    }

    public void setServletResponse(HttpServletResponse servletResponse) {
        this.servletResponse = servletResponse;
    }

}
