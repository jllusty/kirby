package nullusty.kirby;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Do we really need more than one of these?
public class MySQLDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    Connection con;
    Long unixTime;

    // throw on construction
    public MySQLDao() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        con = DriverManager.getConnection("jdbc:mysql://localhost/kirby?user=kirby&password=kirby");
        unixTime = Instant.now().getEpochSecond();
    }

    public void insertWeatherData(WeatherStationData weatherStationData, String stateName) throws SQLException {
        if(weatherStationData == null) return;

        // convert optionals to null for SQL
        Double temperature = (!weatherStationData.getTemperature().isEmpty()) ? weatherStationData.getTemperature().get() : null;
        Double pressure = (!weatherStationData.getPressure().isEmpty()) ? weatherStationData.getPressure().get() : null;
        Double elevation = (!weatherStationData.getElevation().isEmpty()) ? weatherStationData.getElevation().get() : null;

        // create insertion query
        String query = String.format("INSERT INTO WEATHER (station,ts,lat,lng,elevation,temperature,pressure,state) VALUES ('%s',%s,%s,%s,%s,%s,%s,'%s')",
                weatherStationData.getId(),
                weatherStationData.getObservationTimeSeconds(),
                weatherStationData.getLatitude(),
                weatherStationData.getLongitude(),
                elevation,
                temperature,
                pressure,
                stateName);
        LOGGER.info("Executing SQL Query: " + query);

        Statement stmt = con.createStatement();
        stmt.execute(query);
    }
}
