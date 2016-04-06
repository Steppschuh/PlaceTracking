package placetracking.util.shortener;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.urlshortener.Urlshortener;
import com.google.api.services.urlshortener.UrlshortenerScopes;
import com.google.api.services.urlshortener.model.Url;
import com.google.api.services.urlshortener.model.UrlHistory;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class GoogleUrlShortener {

    public static final Logger log = Logger.getLogger(GoogleUrlShortener.class.getSimpleName());

    public static Urlshortener getUrlShortener() {
        AppIdentityCredential credential = new AppIdentityCredential(Arrays.asList(UrlshortenerScopes.URLSHORTENER));
        return new Urlshortener.Builder(new UrlFetchTransport(), new JacksonFactory(), credential)
                .setApplicationName("Tracking API")
                .build();
    }

    public static List<Url> getHistoryItems() throws Exception {
        Urlshortener shortener = getUrlShortener();
        UrlHistory history = shortener.url().list().execute();
        return history.getItems();
    }

    public static Url getShortUrl(String url) throws Exception {
        Urlshortener shortener = getUrlShortener();
        Url longUrl = new Url().setLongUrl(url);
        Url shortUrl = shortener.url().insert(longUrl).execute();
        return shortUrl;
    }

    public static String getShortUrlAsString(String url) {
        try {
            Url shortUrl = getShortUrl(url);
            if (shortUrl.getId() != null) {
                return shortUrl.getId();
            }
        } catch (Exception ex) {
            log.severe("Unable to shorten URL: " + ex.getMessage() + " Long URL: " + url);
        }
        return url;
    }

}
