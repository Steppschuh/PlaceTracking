package placetracking.api;

import java.io.IOException;
import java.util.Date;

import javax.cache.Cache;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ApiResponse {

	int statusCode = HttpServletResponse.SC_OK;
	String statusMessage = "OK";
	Object content;

	transient String response;
	transient boolean loadedFromCache = false;
	
	public String generateJsonResponse() {
		if (response != null && response.length() > 0) {
			return response;
		}
		response = "{}";
		try {
			Gson gson = new GsonBuilder()
			        //.serializeNulls()
			        .create();
			response = gson.toJson(this);
		} catch (Exception ex) {
			
		}
		return response;
	}
	
	public void send(HttpServletResponse resp) throws IOException {
		resp.setContentType("application/json");
		resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.getWriter().write(this.generateJsonResponse());
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
	
}
