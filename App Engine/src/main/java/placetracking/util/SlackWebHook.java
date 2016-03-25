package placetracking.util;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

import com.google.appengine.repackaged.com.google.api.client.http.GenericUrl;
import com.google.appengine.repackaged.com.google.api.client.http.HttpRequestFactory;
import com.google.appengine.repackaged.com.google.api.client.http.HttpTransport;
import com.google.appengine.repackaged.com.google.api.client.http.UrlEncodedContent;
import com.google.appengine.repackaged.com.google.api.client.http.javanet.NetHttpTransport;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SlackWebHook {

	transient private final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	transient private final HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory();
    
	private SlackPayload payload;
	transient private String url;
	
	public SlackWebHook(String url) {
		super();
		this.url = url;
	}

	public String getPayloadAsJson() {
		Gson gson = new GsonBuilder().create();
		return gson.toJson(payload);
	}
	
	public void post() {
		try {
			HashMap<Object, Object> payloadToSend = Maps.newHashMap();
	        payloadToSend.put("payload", getPayloadAsJson());

	        requestFactory.buildPostRequest(new GenericUrl(url), new UrlEncodedContent(payloadToSend))
	                .execute();
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	}

	public SlackPayload getPayload() {
		return payload;
	}

	public void setPayload(SlackPayload payload) {
		this.payload = payload;
	}
	
	
	
}
