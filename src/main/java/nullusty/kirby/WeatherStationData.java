package nullusty.kirby;

import com.google.common.base.Preconditions;
import org.checkerframework.checker.nullness.Opt;

import java.util.Optional;

// immutable weather station data (response)
// this is the mid-point between the geoJSON of an HttpResponse from a given weather station and
// a backend dump (SQL)
public class WeatherStationData {
    // Unique Identifier (Required)
    final private String id;

    // Time of Observation (Required)
    final private Long observationTimeSeconds;            // backend unit: Seconds (Unix Time)

    // Geographical Properties (Required)
    final private Double latitude;                        // backend unit: Latitude (degrees), and Longitude (degrees)
    final private Double longitude;

    // Geographical Properties (Optional?)
    final private Optional<Double> elevation;             // backend unit: Meters

    // Metrological Properties (Optional, availability depends on a number of uncontrollable factors)
    final private Optional<Double> pressure;              // backend unit: 1 Pascal (Pa) == 1 Newton per Square Meter (N/m^2)
    final private Optional<Double> temperature;           // backend unit: Celsius

    // Metadata Properties
    final private Optional<String> ingestionBatchId;

    public static class Builder {
        // Required Properties
        final private String id;
        final private Long observationTimeSeconds;
        final private Double latitude;
        final private Double longitude;

        // Optional Properties
        private Optional<Double> elevation = Optional.empty();
        private Optional<Double> temperature = Optional.empty();
        private Optional<Double> pressure = Optional.empty();
        private Optional<String> ingestionBatchId = Optional.empty();

        public Builder(String id, Long observationTimeSeconds, Double latitude, Double longitude) {
            // id is not null or empty string
            Preconditions.checkArgument(id != null, "id cannot be null");
            Preconditions.checkArgument(!id.isEmpty(), "id cannot be empty");

            // Observation Time not null
            Preconditions.checkArgument(observationTimeSeconds != null, "observationTimeSeconds cannot be null");

            // Longitude/Latitude not null
            Preconditions.checkArgument(latitude != null, "latitude cannot be null");
            Preconditions.checkArgument(longitude != null, "longitude cannot be null");
            // -90 <= Latitude <= 90
            Preconditions.checkArgument(Math.abs(latitude) <= 90.0, "latitude = %s cannot exceed 90.0 or subceed -90.0", latitude);
            // -180 <= Longitude <= 180
            Preconditions.checkArgument(Math.abs(longitude) <= 180.0, "longitude = %s cannot exceed 180.0 or subceed -180.0", longitude);

            this.id = id;
            this.observationTimeSeconds = observationTimeSeconds;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public Builder setTemperature(Optional<Double> temperature) {
            this.temperature = temperature;
            return this;
        }
        public Builder setPressure(Optional<Double> pressure) {
            this.pressure = pressure;
            return this;
        }
        public Builder setElevation(Optional<Double> elevation) {
            this.elevation = elevation;
            return this;
        }
        public Builder setIngestionBatchId(Optional<String> ingestionBatchId) {
            this.ingestionBatchId = ingestionBatchId;
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
                    this.pressure,
                    this.ingestionBatchId
            );
        }
    }

    private  WeatherStationData(String id,
                              Long observationTimeSeconds,
                              Double latitude,
                              Double longitude,
                              Optional<Double> elevation,
                              Optional<Double> temperature,
                              Optional<Double> pressure,
                              Optional<String> ingestionBatchId)
    {
        this.id = id;
        this.observationTimeSeconds = observationTimeSeconds;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.temperature = temperature;
        this.pressure = pressure;
        this.ingestionBatchId = ingestionBatchId;
    }
    public String getId() {
        return id;
    }
    public Long getObservationTimeSeconds() {
        return observationTimeSeconds;
    }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() {
        return longitude;
    }
    public Optional<String> getIngestionBatchId() { return ingestionBatchId; }
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
                "{\n\tid: %s,\n\tts: %s,\n\ttemperature: %s,\n\tpressure: %s,\n\tlatitude: %s,\n\tlongitude: %s\n,\n\tingestionBatchId: %s}",
                id,
                observationTimeSeconds,
                temperature,
                pressure,
                latitude,
                longitude,
                ingestionBatchId
        );
    }
}
