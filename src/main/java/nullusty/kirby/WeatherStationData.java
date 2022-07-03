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
    final private Optional<Double> temperature;           // backend unit: Celsius
    final private Optional<Double> barometricPressure;    // backend unit: 1 Pascal (Pa) == 1 Newton per Square Meter (N/m^2)
    final private Optional<Double> seaLevelPressure;
    final private Optional<Double> windSpeed;
    final private Optional<Double> windDirection;
    final private Optional<Double> relativeHumidity;
    final private Optional<Double> dewpoint;

    // Metadata Properties (Required)
    final private String ingestionBatchId;

    public static class Builder {
        // Required Properties
        final private String id;
        final private Long observationTimeSeconds;
        final private Double latitude;
        final private Double longitude;
        final private String ingestionBatchId;

        // Optional Metrological Properties
        private Optional<Double> elevation = Optional.empty();
        private Optional<Double> temperature = Optional.empty();
        private Optional<Double> barometricPressure = Optional.empty();
        private Optional<Double> seaLevelPressure = Optional.empty();
        private Optional<Double> windSpeed = Optional.empty();
        private Optional<Double> windDirection = Optional.empty();
        private Optional<Double> relativeHumidity = Optional.empty();
        private Optional<Double> dewpoint = Optional.empty();

        public Builder(String id, Long observationTimeSeconds, Double latitude, Double longitude, String ingestionBatchId) {
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

            // Ingestion Batch Id not null or empty
            Preconditions.checkArgument(ingestionBatchId != null, "ingestionBatchId cannot be null");
            Preconditions.checkArgument(!ingestionBatchId.isEmpty(), "ingestionBatchId cannot be empty");

            this.id = id;
            this.observationTimeSeconds = observationTimeSeconds;
            this.latitude = latitude;
            this.longitude = longitude;
            this.ingestionBatchId = ingestionBatchId;
        }

        public Builder setTemperature(Optional<Double> temperature) {
            this.temperature = temperature;
            return this;
        }
        public Builder setBarometricPressure(Optional<Double> barometricPressure) {
            this.barometricPressure = barometricPressure;
            return this;
        }
        public Builder setElevation(Optional<Double> elevation) {
            this.elevation = elevation;
            return this;
        }
        public Builder setRelativeHumidity(Optional<Double> relativeHumidity) {
            this.relativeHumidity = relativeHumidity;
            return this;
        }
        public Builder setSeaLevelPressure(Optional<Double> seaLevelPressure) {
            this.seaLevelPressure = seaLevelPressure;
            return this;
        }
        public Builder setWindSpeed(Optional<Double> windSpeed) {
            this.windSpeed = windSpeed;
            return this;
        }
        public Builder setWindDirection(Optional<Double> windDirection) {
            this.windDirection = windDirection;
            return this;
        }
        public Builder setDewpoint(Optional<Double> dewpoint) {
            this.dewpoint = dewpoint;
            return this;
        }

        public WeatherStationData build() {
            return new WeatherStationData(
                    this.id,
                    this.observationTimeSeconds,
                    this.latitude,
                    this.longitude,
                    this.ingestionBatchId,
                    this.elevation,
                    this.temperature,
                    this.barometricPressure,
                    this.seaLevelPressure,
                    this.dewpoint,
                    this.relativeHumidity,
                    this.windSpeed,
                    this.windDirection
            );
        }
    }

    private WeatherStationData(String id,
                              Long observationTimeSeconds,
                              Double latitude,
                              Double longitude,
                              String ingestionBatchId,
                              Optional<Double> elevation,
                              Optional<Double> temperature,
                              Optional<Double> barometricPressure,
                              Optional<Double> seaLevelPressure,
                              Optional<Double> dewpoint,
                              Optional<Double> relativeHumidity,
                              Optional<Double> windSpeed,
                              Optional<Double> windDirection)
    {
        this.id = id;
        this.observationTimeSeconds = observationTimeSeconds;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.temperature = temperature;
        this.barometricPressure = barometricPressure;
        this.seaLevelPressure = seaLevelPressure;
        this.ingestionBatchId = ingestionBatchId;
        this.dewpoint = dewpoint;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.relativeHumidity = relativeHumidity;
    }
    public String getId() { return id; }
    public Long getObservationTimeSeconds() {
        return observationTimeSeconds;
    }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() {
        return longitude;
    }
    public String getIngestionBatchId() { return ingestionBatchId; }
    public Optional<Double> getTemperature() {
        return temperature;
    }
    public Optional<Double> getBarometricPressure() { return barometricPressure; }
    public Optional<Double> getWindSpeed() { return windSpeed; }
    public Optional<Double> getWindDirection() { return windDirection; }
    public Optional<Double> getSeaLevelPressure() { return seaLevelPressure; }
    public Optional<Double> getDewpoint() { return dewpoint; }
    public Optional<Double> getRelativeHumidity() { return relativeHumidity; }
    public Optional<Double> getElevation() {
        return elevation;
    }
}
