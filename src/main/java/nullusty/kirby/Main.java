package nullusty.kirby;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

// todo: Command Line Parsing

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        LOGGER.info("KIRBY (>'o')> starting...");

        // Backend MySQL (kirby table) to dump weather data to
        MySQLDao mySQLDao = new MySQLDao();

        // these should be merged - an object that knows statenames should know their boundaries
        // in the future we should associate the time of the query with what we populate in the backend,
        //
        // todo: put in local resource folder
        // for processing, all state boundaries from the US Census Bureau
        // implicitly sets the time that you initialize it for aggregating in the backend,
        ShapeDao shapeDao = new ShapeDao("C:\\Users\\jllus\\Downloads\\cb_2018_us_state_500k\\cb_2018_us_state_500k.shp");
        // for processing, all US territory names <=> US territory acronyms
        GeographyNerd jackie = new GeographyNerd("C:\\Users\\jllus\\Downloads\\statenames.txt");

        // NationalWeatherService (NWS) Client
        NationalWeatherServiceClient nwsClient = new NationalWeatherServiceClient();

        // Should be runtime args
        List<String> statesToIngest = Arrays.asList("Oregon");

        // Run for 1 hours (should be parameters)
        long runTime = Duration.of(9, ChronoUnit.HOURS).toSeconds();
        long startTime =  Instant.now().getEpochSecond();
        long endTime = startTime + runTime;
        while(Instant.now().getEpochSecond() < endTime) {
            // Run identifier (unused)
            String runUUID = UUID.randomUUID().toString();

            // Ingest each US territories weather data
            for(String stateName : statesToIngest) {
                String territoryAbbreviation = jackie.getAbbreviationFromStateName(stateName);
                // Get List of Weather Station Requests
                List<WeatherStationDataRequest> weatherStationDataRequestsList = nwsClient.getWeatherStationDataRequests(territoryAbbreviation, runUUID);
                for(WeatherStationDataRequest weatherStationDataRequest : weatherStationDataRequestsList) {
                    // Log Request + StationURL
                    LOGGER.info("HTTP Request: " + weatherStationDataRequest.toString());
                    // Get Weather Station Data
                    WeatherStationData weatherStationData = nwsClient.getWeatherStationData(weatherStationDataRequest);
                    if(weatherStationData == null) {
                        LOGGER.error("nwsClient returned null");
                        continue;
                    }

                    // Sanity Check
                    if(!shapeDao.doesStateContain(stateName, weatherStationData.getLatitude(), weatherStationData.getLongitude())) {
                        LOGGER.error(weatherStationData.getId() + " is not contained in " + stateName);
                    }
                    // Insert Weather Data into MySQL
                    mySQLDao.insertWeatherData(weatherStationData, territoryAbbreviation);
                }
            }
        }

    }
}
