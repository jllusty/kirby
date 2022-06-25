package nullusty.kirby;

// immutable weather station data
public class WeatherStationData {
    // these come in multiple possible reported units, how to address this?
    final private Double pressure;
    final private Double temperature;
    final private String id;
    final private Double lat, lng;
    final private Long observationTimeMillis;
    // other location data
    public WeatherStationData(String id, Double temperature, Double pressure, Double latitude, Double longitude, Long observationTimeMillis) {
        this.temperature = temperature;
        this.pressure = pressure;
        this.id = id;
        this.lat = latitude;
        this.lng = longitude;
        this.observationTimeMillis = observationTimeMillis;
    }
    public Double getTemperature() {
        return temperature;
    }
    public String getId() {
        return id;
    }
    public Double getLat() {
        return lat;
    }
    public Double getLng() {
        return lng;
    }
    public Double getPressure() {
        return pressure;
    }
    public Long getObservationTimeMillis() { return observationTimeMillis; }
    public String toString() {
        return String.format("{\n\tid: %s,\n\tts: %s,\n\ttemperature: %s,\n\tpressure: %s,\n\tlatitude: %s,\n\tlongitude: %s\n}",
                id, observationTimeMillis, pressure, lat, lng);
    }
}
