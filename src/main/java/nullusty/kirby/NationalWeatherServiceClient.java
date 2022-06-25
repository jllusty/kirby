package nullusty.kirby;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class NationalWeatherServiceClient implements WeatherClient {
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(Main.class);

    static HttpClient httpClient = HttpClient.newHttpClient();

    public NationalWeatherServiceClient() {

    }

    @Override
    public List<WeatherStationDataRequest> getWeatherStationDataRequests(String territoryAbbreviation) {
        try {
            // Generate Station Request, Generates Latest Requests
            HttpRequest.Builder builder = HttpRequest.newBuilder();
            builder.uri(new URI(String.format("https://api.weather.gov/stations?state=%s",territoryAbbreviation)));
            builder.setHeader("Accept", "application/geo+json");
            builder.setHeader("User-Agent", "(jotunheim.dev, jllusty@gmail.com)");
            HttpResponse<String> httpResponse = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return listStations(httpResponse.body());
        }
        catch(Exception e) {
            LOGGER.info("Failed to get weather data request objects:" + e.getMessage());
            return null;
        }
    }

    private List<WeatherStationDataRequest> listStations(String httpResponseBodyString) {
        List<WeatherStationDataRequest> weatherStationDataRequestList = new ArrayList<>();
        // root JSON contains a list of features
        try {
            Map<String,Object> rootJSONmap = new ObjectMapper().readValue(httpResponseBodyString, new TypeReference<Map<String,Object>>(){});
            List<Object> objectsList = new ObjectMapper().convertValue(rootJSONmap.get("features"), new TypeReference<List<Object>>(){} );
            for(int i = 0; i < objectsList.size(); ++i) {
                Object obj = objectsList.get(i);
                LOGGER.info("Parsing geoJSON feature for station " + String.valueOf(i+1) + " out of " + String.valueOf(objectsList.size()));
                // should catch this?
                Map<String,Object> objectMap = new ObjectMapper().convertValue(obj, new TypeReference<Map<String,Object>>(){});
                String stationURL = objectMap.get("id").toString();
                weatherStationDataRequestList.add(new WeatherStationDataRequest(stationURL));
            }
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }

        return weatherStationDataRequestList;
    }

    private WeatherStationData createWeatherStationData(HttpResponse<String> response, Long timeOfRequest) {
        try {
            Map<String,Object> featureJSONmap = new ObjectMapper().readValue(response.body(), new TypeReference<Map<String,Object>>(){});

            // get geometry
            Map<String,Object> geometryJSONmap = new ObjectMapper().convertValue(featureJSONmap.get("geometry"), new TypeReference<Map<String,Object>>(){});
            List<String> latLng = new ObjectMapper().convertValue(geometryJSONmap.get("coordinates"), new TypeReference<List<String>>(){});

            Map<String,Object> propertiesJSONmap = new ObjectMapper().convertValue(featureJSONmap.get("properties"),  new TypeReference<Map<String,Object>>(){});
            // get id
            String id = propertiesJSONmap.get("station").toString();
            // get temperature
            Map<String,Object> temperaturesJSONmap = new ObjectMapper().convertValue(propertiesJSONmap.get("temperature"), new TypeReference<Map<String,Object>>(){});
            Double temperature = Double.valueOf(temperaturesJSONmap.get("value").toString());
            // get pressure
            //Map<String,Object> pressuresJSONmap = new ObjectMapper().convertValue(propertiesJSONmap.get("barometricPressure"), new TypeReference<Map<String,Object>>(){});
            Double pressure = null; // Double.valueOf(pressuresJSONmap.get("value").toString());
            // get elevation, bruh
            // update weather station data
            Double lat =  Double.valueOf(latLng.get(0));
            Double lng =  Double.valueOf(latLng.get(1));
            return new WeatherStationData(id, temperature, 100.0, lat, lng, timeOfRequest);
        }
        catch(Exception e) {
            LOGGER.info("Failed to ingest weather station data: " + e.getMessage());
            return null;
        }

    }

    @Override
    public WeatherStationData getWeatherStationData(WeatherStationDataRequest weatherStationDataRequest) {
        try {
            // send request, get response
            Long timeOfRequest = Instant.now().getEpochSecond();
            HttpResponse<String> response = httpClient.send(weatherStationDataRequest.getBuilder(), HttpResponse.BodyHandlers.ofString());
            // map each response to a data object
            return createWeatherStationData(response,timeOfRequest);
        }
        catch(Exception e) {
            LOGGER.info("Weather Station Request Failure: "+ e.getMessage());
            return null;
        }

    }
}
