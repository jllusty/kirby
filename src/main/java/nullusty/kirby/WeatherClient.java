package nullusty.kirby;

import java.util.List;

public interface WeatherClient {
    // get possible weather station request objects
    List<WeatherStationDataRequest> getWeatherStationDataRequests(String territoryAbbreviation);
    // send weather station request objects, return weather station data objects
    WeatherStationData getWeatherStationData(WeatherStationDataRequest weatherStationDataRequest);
}
