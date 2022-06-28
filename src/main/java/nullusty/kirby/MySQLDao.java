package nullusty.kirby;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.logging.Logger;

// Do we really need more than one of these?
public class MySQLDao {
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(Main.class);
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
        String query = String.format("INSERT INTO WEATHER (station,ts,lat,lng,temperature,pressure,state) VALUES ('%s',%s,%s,%s,%s,%s,'%s')",
                weatherStationData.getId(),
                weatherStationData.getObservationTimeSeconds(),
                weatherStationData.getLatitude(),
                temperature,
                pressure,
                elevation,
                stateName);
        LOGGER.info("Executing SQL Query: " + query);

        Statement stmt = con.createStatement();
        stmt.execute(query);
    }
}
