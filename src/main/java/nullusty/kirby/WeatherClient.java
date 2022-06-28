package nullusty.kirby;

import java.util.List;

public interface WeatherClient {
    // get possible weather station request objects
    // todo: current string is a hack, but we do need WeatherStationRequestParameters at some point
    List<WeatherStationDataRequest> getWeatherStationDataRequests(String territoryAbbreviation);
    // send weather station request objects, return weather station data objects
    WeatherStationData getWeatherStationData(WeatherStationDataRequest weatherStationDataRequest);
}
