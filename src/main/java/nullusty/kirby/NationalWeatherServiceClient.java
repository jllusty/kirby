package nullusty.kirby;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NationalWeatherServiceClient implements WeatherClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(NationalWeatherServiceClient.class);

    static HttpClient httpClient = HttpClient.newHttpClient();

    // cache to avoid constantly re-creating weather station data objects
    private Map<String,WeatherStationDataRequest> stationDataRequestMap = new HashMap<>();

    public NationalWeatherServiceClient() {}

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
        // todo: should use more specific exception, and potentially not catch here, maybe in calling class
        catch(Exception e) {
            LOGGER.error("Failed to get weather data request objects:" + e.getMessage());
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
            LOGGER.error("Could not generate a list of queryable weather stations from HTTP Request Body = " + httpResponseBodyString + " : " + e.getMessage());
        }

        return weatherStationDataRequestList;
    }


    // tries to get an optional property from a Map<String,Object> propertiesJSONmap
    // is T extends Object a meaningful bound? I could have T extend a ObservationValue class
    // or something
private <T extends Object> Optional<T> getOptionalValueFromPropertiesJsonMap(Map<String, Object> propertiesJsonMap, String propertyName, Class<T> type) {
    if (propertiesJsonMap.containsKey(propertyName)) {
        try {
            Map<String, Object> temperaturesJSONmap = new ObjectMapper().convertValue(propertiesJsonMap.get("temperature"), new TypeReference<Map<String, Object>>() {});
            return Optional.of(type.cast(temperaturesJSONmap.get("value")));
        }
        catch(IllegalArgumentException e) {
            LOGGER.info("Could not fetch property = " + propertyName + " : " + e.getMessage());
        }
        catch(ClassCastException e) {
            LOGGER.info("Could not cast property = " + propertyName + " to type " + type.getName() + " : " + e.getMessage());
        }
    }
    return Optional.empty();
}

    // create WeatherStationData from a NWS Weather Station API Request
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

                // get optional properties
                Optional<Double> temperature = getOptionalValueFromPropertiesJsonMap(propertiesJSONmap,"temperature", Double.class);
                Optional<Double> pressure = getOptionalValueFromPropertiesJsonMap(propertiesJSONmap, "barometricPressure", Double.class);
                Optional<Double> elevation = getOptionalValueFromPropertiesJsonMap(propertiesJSONmap, "elevation", Double.class);
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
            LOGGER.error("Failed to ingest weather station data: " + e.getMessage());
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
            LOGGER.error("Weather Station Request Failure: "+ e.getMessage());
            return null;
        }

    }
}
