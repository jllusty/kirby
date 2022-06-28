package nullusty.kirby;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;

// immutable weather station data request
public class WeatherStationDataRequest {
    // HttpRequest
    private final HttpRequest request;
    // other request metadata
    final String stationId;
    public WeatherStationDataRequest(String stationId) throws URISyntaxException {
        this.stationId = stationId;

        // build request at instantiation time
        this.request = HttpRequest.newBuilder()
                .uri(new URI(String.format("%s/%s",stationId,"observations/latest")))
                .setHeader("Accept", "application/geo+json")
                .setHeader("User-Agent", "(jotunheim.dev, jllusty@gmail.com)")
                .build();
    }

    public HttpRequest getHttpRequest() {
        return request;
    }

    public String toString() {
        return "{request: " + request.toString() + ", station: " + stationId + "}";
    }
}
