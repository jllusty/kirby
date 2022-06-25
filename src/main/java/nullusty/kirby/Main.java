package nullusty.kirby;

import com.google.common.base.Preconditions;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        // Backend MySQL (kirby table) to dump weather data to
        MySQLDao mySQLDao = new MySQLDao();

        // for processing, all state boundaries from the US Census Bureau
        // implicitly sets the time that you initialize it for aggregating in the backend,
        // in the future we should associate the time of the query with what we populate in the backend,
        // however - it will be useful to have an aggregation field still in the backend
        ShapeDao shapeDao = new ShapeDao("C:\\Users\\jllus\\Downloads\\cb_2018_us_state_500k\\cb_2018_us_state_500k.shp");
        // for processing, all US territory names <=> US territory acronyms
        GeographyNerd jackie = new GeographyNerd("C:\\Users\\jllus\\Downloads\\statenames.txt");

        // NationalWeatherService (NWS) Client
        NationalWeatherServiceClient nwsClient = new NationalWeatherServiceClient();

        List<String> statesToIngest = Arrays.asList("North Carolina","South Carolina");

        // Run for 12 hours (should be parameters)
        long runTime = Duration.of(12, ChronoUnit.HOURS).toMillis();
        long startTime =  Instant.now().getEpochSecond();
        long endTime = startTime + runTime;
        while(Instant.now().getEpochSecond() < endTime) {
            // Ingest each US territories weather data
            for(String stateName : statesToIngest) {
                String territoryAbbreviation = jackie.getAbbreviationFromStateName(stateName);
                // Get List of Weather Station Requests
                List<WeatherStationDataRequest> weatherStationDataRequestsList = nwsClient.getWeatherStationDataRequests(territoryAbbreviation);
                for(WeatherStationDataRequest weatherStationDataRequest : weatherStationDataRequestsList) {
                    // Log Request + StationURL
                    LOGGER.info("HTTP Request: " + weatherStationDataRequest.toString());
                    // Get Weather Station Data
                    WeatherStationData weatherStationData = nwsClient.getWeatherStationData(weatherStationDataRequest);
                    if(weatherStationData == null) {
                        LOGGER.info("nwsClient returned null");
                        continue;
                    }

                    // Sanity Check
                    if(!shapeDao.doesStateContain(stateName, weatherStationData.getLat(), weatherStationData.getLng())) {
                        LOGGER.info(weatherStationData.getId() + " is not contained in " + stateName);
                    }
                    // Insert Weather Data into MySQL
                    mySQLDao.insertWeatherData(weatherStationData, territoryAbbreviation);
                }
            }
        }

    }
}
