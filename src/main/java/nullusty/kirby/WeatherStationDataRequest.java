package nullusty.kirby;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;

// immutable weather station data request
public class WeatherStationDataRequest {
    final HttpRequest.Builder builder;
    // other request metadata
    final String stationId;
    public WeatherStationDataRequest(String stationId) throws URISyntaxException {
        this.stationId = stationId;

        builder = HttpRequest.newBuilder();
        builder.uri(new URI(String.format("%s/%s",stationId,"observations/latest")));
        builder.setHeader("Accept", "application/geo+json");
        builder.setHeader("User-Agent", "(jotunheim.dev, jllusty@gmail.com)");
    }

    public HttpRequest getBuilder() {
        return builder.build();
    }

    public String toString() {
        return "{request: " + builder.build().toString() + ", station: " + stationId + "}";
    }
}
