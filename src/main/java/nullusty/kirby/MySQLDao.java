package nullusty.kirby;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Do we really need more than one of these?
public class MySQLDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    Connection con;

    // throw on construction
    public MySQLDao() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        con = DriverManager.getConnection("jdbc:mysql://localhost/kirby?user=kirby&password=kirby");
        LOGGER.info("initialized");
    }

    public void insertWeatherData(WeatherStationData weatherStationData, String stateName) throws SQLException {
        if(weatherStationData == null) return;

        // if optional is empty, convert to value or null
        Function<Optional<Double>,Double> toValueOrNull = (x) -> (!x.isEmpty()) ? x.get() : null;

        // convert optionals to null for SQL
        Double temperature = toValueOrNull.apply(weatherStationData.getTemperature());
        Double seaLevelPressure = toValueOrNull.apply(weatherStationData.getSeaLevelPressure());
        Double barometricPressure = toValueOrNull.apply(weatherStationData.getBarometricPressure());
        Double elevation = toValueOrNull.apply(weatherStationData.getElevation());
        Double dewpoint = toValueOrNull.apply(weatherStationData.getDewpoint());
        Double relativeHumidity = toValueOrNull.apply(weatherStationData.getRelativeHumidity());
        Double windSpeed = toValueOrNull.apply(weatherStationData.getWindSpeed());
        Double windDirection = toValueOrNull.apply(weatherStationData.getWindDirection());

        // create insertion query
        String query = String.format("INSERT INTO WEATHER (station,ts,lat,lng,elevation,temperature,barometric_pressure,sea_level_pressure,wind_speed,wind_direction,dewpoint,relative_humidity,state,ingestion_batch_id) VALUES ('%s',%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,'%s','%s')",
                weatherStationData.getId(),
                weatherStationData.getObservationTimeSeconds(),
                weatherStationData.getLatitude(),
                weatherStationData.getLongitude(),
                elevation,
                temperature,
                barometricPressure,
                seaLevelPressure,
                windSpeed,
                windDirection,
                dewpoint,
                relativeHumidity,
                stateName,
                weatherStationData.getIngestionBatchId());
        LOGGER.info("Executing SQL Query: " + query);

        Statement stmt = con.createStatement();
        stmt.execute(query);
    }
}
