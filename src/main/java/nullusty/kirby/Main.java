package nullusty.kirby;

import java.io.IOException;
import java.sql.SQLException;
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
        // Backend MySQL (kirby table) to dump weather data to
        try (MySQLDao mySQLDao = new MySQLDao()) {
            execute(mySQLDao);
        }
    }

    public static void execute(MySQLDao mySQLDao) throws IOException {
        LOGGER.info("KIRBY (>'o')> starting!");
        TerritoryDataClient jackie = new FileTerritoryClient(
                "resources\\cb_2018_us_state_500k\\cb_2018_us_state_500k.shp",
                "resources\\statenames.txt");

        // NationalWeatherService (NWS) Client
        WeatherClient nwsClient = new NationalWeatherServiceClient();

        // Should be runtime args
        List<String> indicesToIngest = Arrays.asList("OR");

        // Run for 1 hours (should be parameters)
        Long runTime = Duration.of(1, ChronoUnit.HOURS).toSeconds();
        Long startTime =  Instant.now().getEpochSecond();
        Long endTime = startTime + runTime;
        Integer numBatches = 0;
        while(Instant.now().getEpochSecond() < endTime) {
            // Run identifier
            String runUUID = UUID.randomUUID().toString();
            LOGGER.info("starting batch of ingestions with id = " + runUUID);

            // Ingest each US territories weather data
            for(String index : indicesToIngest) {
                // TerritoryData for that state
                TerritoryData territoryData = jackie.getTerritoryByIndex(index);
                // Get List of Weather Station Requests
                List<WeatherStationDataRequest> weatherStationDataRequestsList = nwsClient.getWeatherStationDataRequests(index, runUUID);
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
                    if(!TerritoryDataHelper.doesTerritoryContainLatLong(territoryData, weatherStationData.getLatitude(), weatherStationData.getLongitude())) {
                        LOGGER.error(weatherStationData.getId() + " is not contained in " + index);
                    }

                    // Insert Weather Data into MySQL
                    try {
                        // todo: consider batching?
                        mySQLDao.insertWeatherData(weatherStationData, index);
                    }
                    catch(SQLException e) {
                        // todo: override toString method of weatherStationData and print diagnostic here
                        LOGGER.error("Could not insert weatherStationData : " + e.getMessage());
                    }
                }
            }
            numBatches++;
        }
        LOGGER.info("KIRBY (>'o'>) all done! did {} batches.",numBatches);
    }
}
