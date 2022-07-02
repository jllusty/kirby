package nullusty.kirby;

import com.google.common.base.Preconditions;

import javax.print.attribute.URISyntax;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.Map;

// immutable weather station data request
public class WeatherStationDataRequest {
    // HttpRequest
    private final HttpRequest request;

    // other request metadata
    private final String stationId;
    private final String ingestionBatchId;

    // Builder
    public static class Builder {
        // cache stationId -> request objects
        private static Map<String,HttpRequest> stationIdToHttpRequestMap = new HashMap<>();

        // HttpRequest (required)
        private final HttpRequest httpRequest;

        // other request metadata (currently required)
        private final String stationId;
        private final String ingestionBatchId;

        public Builder(String stationId, String ingestionBatchId) throws URISyntaxException {
            // stationId + ingestionBatchId cannot be null
            Preconditions.checkArgument(stationId != null, "stationId cannot be null");
            Preconditions.checkArgument(ingestionBatchId != null, "ingestionBatchId cannot be null");

            // cache HttpRequest Objects by stationId
            if(stationIdToHttpRequestMap.containsKey(stationId)) {
                this.httpRequest = stationIdToHttpRequestMap.get(stationId);
            }
            else {
                this.httpRequest = HttpRequest.newBuilder()
                            .uri(new URI(String.format("%s/%s",stationId,"observations/latest")))
                            .setHeader("Accept", "application/geo+json")
                            .setHeader("User-Agent", "(jotunheim.dev, jllusty@gmail.com)")
                            .build();
                stationIdToHttpRequestMap.put(stationId,this.httpRequest);
            }
            this.stationId = stationId;
            this.ingestionBatchId = ingestionBatchId;
        }

        public WeatherStationDataRequest build() {
            return new WeatherStationDataRequest(stationId,ingestionBatchId,httpRequest);
        }
    }

    private WeatherStationDataRequest(String stationId, String ingestionBatchId, HttpRequest httpRequest) {
        this.stationId = stationId;
        this.ingestionBatchId = ingestionBatchId;
        this.request = httpRequest;
    }

    public HttpRequest getHttpRequest() {
        return request;
    }
    public String getStationId() { return stationId; }
    public String getIngestionBatchId() {
        return ingestionBatchId;
    }

    public String toString() {
        return String.format("{\n\trequest: %s,\n\tstation: %s,\n\tingestionBatchId: %s\n}",
                request.toString(), stationId, ingestionBatchId);
    }
}
