package nullusty.kirby;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

public class NationalWeatherServiceClient implements WeatherClient {
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(Main.class);

    static HttpClient httpClient = HttpClient.newHttpClient();

    // cache to avoid constantly re-creating weather station data objects
    private Map<String,WeatherStationDataRequest> stationDataRequestMap = new HashMap<>();

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
                // don't re-create unnecessary immutable weather request objects
                if(stationDataRequestMap.containsKey(stationURL)) {
                    weatherStationDataRequestList.add(stationDataRequestMap.get(stationURL));
                }
                else {
                    stationDataRequestMap.put(stationURL, new WeatherStationDataRequest(stationURL));
                    weatherStationDataRequestList.add(stationDataRequestMap.get(stationURL));
                }
            }
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }

        return weatherStationDataRequestList;
    }


    // should refactor this into smaller pieces - what about the methods that get optionals? can we get
    // a useful generic subroutine to handle that process?
    private WeatherStationData createWeatherStationData(HttpResponse<String> response, Long timeOfRequest) {
        try {
            // root features of geoJSON
            Map<String,Object> featureJSONmap = new ObjectMapper().readValue(response.body(), new TypeReference<Map<String,Object>>(){});

            // want to throw if these fail to parse, since they are required fields
            // get geometry
            Map<String,Object> geometryJSONmap = new ObjectMapper().convertValue(featureJSONmap.get("geometry"), new TypeReference<Map<String,Object>>(){});
            List<String> latLng = new ObjectMapper().convertValue(geometryJSONmap.get("coordinates"), new TypeReference<List<String>>(){});
            Double lat =  Double.valueOf(latLng.get(0));
            Double lng =  Double.valueOf(latLng.get(1));

            // get properties
            if(featureJSONmap.containsKey("properties")) {
                Map<String,Object> propertiesJSONmap = new ObjectMapper().convertValue(featureJSONmap.get("properties"),  new TypeReference<Map<String,Object>>(){});
                // required property
                String id = propertiesJSONmap.get("station").toString();
                // todo: should check required properties for nullness? not here, in the weather data builder method

                // try to get temperature
                Optional<Double> temperature = Optional.empty();
                if(propertiesJSONmap.containsKey("temperature")) {
                    try {
                        Map<String,Object> temperaturesJSONmap = new ObjectMapper().convertValue(propertiesJSONmap.get("temperature"), new TypeReference<Map<String,Object>>(){});
                        temperature = Optional.of(Double.valueOf(temperaturesJSONmap.get("value").toString()));
                    }
                    catch(IllegalArgumentException e) {
                        // todo: add error handler for logs
                        LOGGER.info("Could not fetch temperature property: " + e.getMessage());
                    }
                }
                // try to get pressure
                Optional<Double> pressure = Optional.empty();
                if(propertiesJSONmap.containsKey("barometricPressure")) {
                    try {
                        Map<String,Object> pressuresJSONmap = new ObjectMapper().convertValue(propertiesJSONmap.get("barometricPressure"), new TypeReference<Map<String,Object>>(){});
                        pressure = Optional.of(Double.valueOf(pressuresJSONmap.get("value").toString()));
                    }
                    catch(IllegalArgumentException e) {
                        // todo: add error handler for logs
                        LOGGER.info("Could not fetch pressure property: " + e.getMessage());
                    }
                }
                // todo: refactor the above into testable functions and add parsing for elevation
                Optional<Double> elevation = Optional.empty();
                return new WeatherStationData.WeatherStationDataBuilder(id, timeOfRequest, lat, lng)
                        .setTemperature(temperature)
                        .setPressure(pressure)
                        .setElevation(elevation)
                        .build();
            }
            else {
                // should throw something more specific
                throw new Exception("HTTP Response's geoJSON does not contain properties!");
            }
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
            HttpResponse<String> response = httpClient.send(weatherStationDataRequest.getHttpRequest(), HttpResponse.BodyHandlers.ofString());
            // map each response to a data object
            return createWeatherStationData(response,timeOfRequest);
        }
        catch(Exception e) {
            LOGGER.info("Weather Station Request Failure: "+ e.getMessage());
            return null;
        }

    }
}
