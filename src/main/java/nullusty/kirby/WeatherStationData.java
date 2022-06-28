package nullusty.kirby;

import org.checkerframework.checker.nullness.Opt;

import java.util.Optional;

// immutable weather station data (response)
// this is the mid-point between the geoJSON of an HttpResponse from a given weather station and
// a backend dump (SQL)
public class WeatherStationData {
    // Unique Identifier (Required)
    final private String id;

    // Time of Observation (Required)
    final private Long observationTimeSeconds;          // backend unit: Seconds (Unix Time)

    // Geographical Properties (Required)
    final private Double latitude;                        // backend unit: Latitude (degrees), and Longitude (degrees)
    final private Double longitude;

    // Geographical Properties (Optional?)
    final private Optional<Double> elevation;             // backend unit: Meters

    // Metrological Properties (Optional, availability depends on a number of uncontrollable factors)
    final private Optional<Double> pressure;              // backend unit: 1 Pascal (Pa) == 1 Newton per Square Meter (N/m^2)
    final private Optional<Double> temperature;           // backend unit: Celsius

    public static class WeatherStationDataBuilder {
        // Required Properties
        final private String id;
        final private Long observationTimeSeconds;
        final private Double latitude;
        final private Double longitude;

        // Optional Properties
        private Optional<Double> elevation = Optional.empty();
        private Optional<Double> temperature = Optional.empty();
        private Optional<Double> pressure = Optional.empty();

        public WeatherStationDataBuilder(String id, Long observationTimeSeconds, Double latitude, Double longitude) {
            this.id = id;
            this.observationTimeSeconds = observationTimeSeconds;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public WeatherStationDataBuilder setTemperature(Optional<Double> temperature) {
            this.temperature = temperature;
            return this;
        }
        public WeatherStationDataBuilder setPressure(Optional<Double> pressure) {
            this.pressure = pressure;
            return this;
        }
        public WeatherStationDataBuilder setElevation(Optional<Double> elevation) {
            this.elevation = elevation;
            return this;
        }

        public WeatherStationData build() {
            return new WeatherStationData(
                    this.id,
                    this.observationTimeSeconds,
                    this.latitude,
                    this.longitude,
                    this.elevation,
                    this.temperature,
                    this.pressure
            );
        }
    }

    private  WeatherStationData(String id,
                              Long observationTimeSeconds,
                              Double latitude,
                              Double longitude,
                              Optional<Double> elevation,
                              Optional<Double> temperature,
                              Optional<Double> pressure)
    {
        this.id = id;
        this.observationTimeSeconds = observationTimeSeconds;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.temperature = temperature;
        this.pressure = pressure;
    }
    public String getId() {
        return id;
    }
    public Long getObservationTimeSeconds() {
        return observationTimeSeconds;
    }
    public Double getLatitude() {
        return latitude;
    }
    public Double getLongitude() {
        return longitude;
    }
    public Optional<Double> getTemperature() {
        return temperature;
    }
    public Optional<Double> getPressure() {
        return pressure;
    }
    public Optional<Double> getElevation() {
        return elevation;
    }

    public String toString() {
        return String.format(
                "{\n\tid: %s,\n\tts: %s,\n\ttemperature: %s,\n\tpressure: %s,\n\tlatitude: %s,\n\tlongitude: %s\n}",
                id,
                observationTimeSeconds,
                temperature,
                pressure,
                latitude,
                longitude
        );
    }
}
