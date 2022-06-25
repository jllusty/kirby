package nullusty.kirby;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.logging.Logger;

public class MySQLDao {
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(Main.class);
    Connection con;
    Long unixTime;

    public MySQLDao() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        con = DriverManager.getConnection("jdbc:mysql://localhost/kirby?user=kirby&password=kirby");
        unixTime = Instant.now().getEpochSecond();
    }

    public void insertWeatherData(WeatherStationData weatherStationData, String stateName) throws SQLException {
        if(weatherStationData == null) return;

        String query = String.format("INSERT INTO WEATHER (station,ts,temperature,pressure,lat,lng,state) VALUES ('%s',%s,%s,%s,%s,%s,'%s')",
                weatherStationData.getId(),
                weatherStationData.getObservationTimeMillis(),
                weatherStationData.getTemperature(),
                weatherStationData.getPressure(),
                weatherStationData.getLat(),
                weatherStationData.getLng(),
                stateName);
        LOGGER.info("Executing SQL Query: " + query);

        Statement stmt = con.createStatement();
        stmt.execute(query);
    }
}
