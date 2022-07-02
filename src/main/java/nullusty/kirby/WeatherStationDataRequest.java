package nullusty.kirby;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;

// immutable weather station data request
public class WeatherStationDataRequest {
    // HttpRequest
    private final HttpRequest request;
    // other request metadata
    private final String stationId;
    private final String ingestionBatchId;
    public WeatherStationDataRequest(String stationId, String ingestionBatchId) throws URISyntaxException {
        this.stationId = stationId;
        this.ingestionBatchId = ingestionBatchId;

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
    public String getIngestionBatchId() {
        return ingestionBatchId;
    }

    public String toString() {
        return "{request: " + request.toString() + ", station: " + stationId + "}";
    }
}
