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

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import static tec.units.ri.unit.Units.*;

public class NationalWeatherServiceClient implements WeatherClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(NationalWeatherServiceClient.class);

    static HttpClient httpClient = HttpClient.newHttpClient();

    public NationalWeatherServiceClient() {}

    @Override
    public List<WeatherStationDataRequest> getWeatherStationDataRequests(String territoryAbbreviation, String ingestionBatchId) {
        try {
            // Generate Station Request, Generates Latest Requests
            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(new URI(String.format("https://api.weather.gov/stations?state=%s",territoryAbbreviation)))
                .setHeader("Accept", "application/geo+json")
                .setHeader("User-Agent", "(jotunheim.dev, jllusty@gmail.com)")
                .build();
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return listStations(httpResponse.body(), ingestionBatchId);
        }
        // todo: should use more specific exception, and potentially not catch here, maybe in calling class
        catch(Exception e) {
            LOGGER.error("Failed to get weather data request objects:" + e.getMessage());
            return null;
        }
    }

    private List<WeatherStationDataRequest> listStations(String httpResponseBodyString, String ingestionBatchId) {
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
                weatherStationDataRequestList.add(new WeatherStationDataRequest.Builder(stationURL, ingestionBatchId).build());
            }
        }
        catch(Exception e) {
            LOGGER.error("Could not generate a list of queryable weather stations from HTTP Request Body = " + httpResponseBodyString + " : " + e.getMessage());
        }

        return weatherStationDataRequestList;
    }

    // tries to get an optional property from a Map<String,Object> propertiesJSONmap in specific units
    private <Q extends Quantity<Q>> Optional<Double> getOptionalValueFromPropertiesJsonMap(Map<String, Object> propertiesJsonMap, String propertyName, Unit<Q> desiredUnit) {
        if (propertiesJsonMap.containsKey(propertyName)) {
            try {
                Map<String, Object> valuesJSONmap = new ObjectMapper().convertValue(propertiesJsonMap.get(propertyName), new TypeReference<Map<String, Object>>() {});
                if(valuesJSONmap.containsKey("unitCode")) {
                    // todo: consider unitCodes other than wmo:unitCode if necessary
                    String unitCode = valuesJSONmap.get("unitCode").toString();
                    // correct units, no transformation necessary
                    if(desiredUnit == WMOUnits.convertWMOUnitCodeStringToUnit(unitCode)) {
                        return Optional.of(Double.valueOf(valuesJSONmap.get("value").toString()));
                    }
                    // units do not match our backend
                    else {
                        // todo: are the units compatible i.e. of same dimension? if so, transform and put that in the backend
                        // i.e. if(desiredUnit.isCompatible(WMOUnits.convertWMOUnitCodeStringToUnit(unitCode))) { ... }
                        throw new IllegalArgumentException("propertiesJSON map reported a unit that doesn't match our backend");
                    }
                }
                return Optional.of(Double.valueOf(valuesJSONmap.get("value").toString()));
            }
            catch(ClassNotFoundException e) {
                LOGGER.info(e.getMessage());
            }
            catch(NumberFormatException e) {
                LOGGER.info("Value = %s for property = %s could not be converted to a Double : " + e.getMessage());
            }
            catch(IllegalArgumentException e) {
                LOGGER.info("Could not fetch property = " + propertyName + " : " + e.getMessage());
            }
            catch(ClassCastException e) {
                LOGGER.info("Could not cast property = " + propertyName + " to Double : " + e.getMessage());
            }
            catch(NullPointerException e) {
                LOGGER.info("value key for property = " + propertyName + " is missing!");
            }
        }
        return Optional.empty();
    }

    // create WeatherStationData from a NWS Weather Station API Request
    // todo: should this be moved into a WeatherStationData.Builder?
    private WeatherStationData createWeatherStationData(HttpResponse<String> response, Long timeOfRequest, String ingestionBatchId) {
        try {
            // root features of geoJSON
            Map<String,Object> featureJSONmap = new ObjectMapper().readValue(response.body(), new TypeReference<Map<String,Object>>(){});

            // want to throw if these fail to parse, since they are required fields
            // get geometry
            Map<String,Object> geometryJSONmap = new ObjectMapper().convertValue(featureJSONmap.get("geometry"), new TypeReference<Map<String,Object>>(){});
            List<String> latLng = new ObjectMapper().convertValue(geometryJSONmap.get("coordinates"), new TypeReference<List<String>>(){});
            Double lat =  Double.valueOf(latLng.get(1));
            Double lng =  Double.valueOf(latLng.get(0));

            // get properties
            if(featureJSONmap.containsKey("properties")) {
                Map<String,Object> propertiesJSONmap = new ObjectMapper().convertValue(featureJSONmap.get("properties"),  new TypeReference<Map<String,Object>>(){});
                // required property
                String id = propertiesJSONmap.get("station").toString();

                // get optional properties
                Optional<Double> temperature = getOptionalValueFromPropertiesJsonMap(propertiesJSONmap, "temperature", CELSIUS);
                Optional<Double> barometricPressure = getOptionalValueFromPropertiesJsonMap(propertiesJSONmap, "barometricPressure", PASCAL);
                Optional<Double> dewpoint = getOptionalValueFromPropertiesJsonMap(propertiesJSONmap, "dewpoint", CELSIUS);
                Optional<Double> windSpeed = getOptionalValueFromPropertiesJsonMap(propertiesJSONmap, "windSpeed", KILOMETRE_PER_HOUR);
                // todo: need unit for "wmounit:degree_(angle)", but we only have RADIAN :( in this implementation
                Optional<Double> seaLevelPressure = getOptionalValueFromPropertiesJsonMap(propertiesJSONmap, "seaLevelPressure", PASCAL);
                Optional<Double> relativeHumidity = getOptionalValueFromPropertiesJsonMap(propertiesJSONmap, "relativeHumidity", PASCAL);
                Optional<Double> elevation = getOptionalValueFromPropertiesJsonMap(propertiesJSONmap, "elevation", METRE);
                return new WeatherStationData.Builder(id, timeOfRequest, lat, lng, ingestionBatchId)
                        .setTemperature(temperature)
                        .setDewpoint(dewpoint)
                        .setSeaLevelPressure(seaLevelPressure)
                        .setBarometricPressure(barometricPressure)
                        .setRelativeHumidity(relativeHumidity)
                        .setWindSpeed(windSpeed)
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
            return createWeatherStationData(response,timeOfRequest,weatherStationDataRequest.getIngestionBatchId());
        }
        catch(Exception e) {
            LOGGER.error("Weather Station Request Failure: "+ e.getMessage());
            return null;
        }

    }
}
