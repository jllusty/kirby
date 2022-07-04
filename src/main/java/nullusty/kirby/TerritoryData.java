package nullusty.kirby;

import com.google.common.base.Preconditions;
import org.locationtech.jts.geom.MultiPolygon;

import java.util.List;

public class TerritoryData {
    // Strings that refer to this territory
    private final List<String> indices;
    // Polygons that refer to this territory
    private final MultiPolygon multiPolygon;
    public TerritoryData(List<String> indices, MultiPolygon multiPolygon) {
        // indices not null or empty
        Preconditions.checkArgument(indices != null, "indices cannot be null");
        Preconditions.checkArgument(indices.size() > 0, "indices cannot be empty");

        // multiPolygon not null or empty
        Preconditions.checkArgument(multiPolygon != null, "multiPolygon cannot be null");
        Preconditions.checkArgument(!multiPolygon.isEmpty(), "multiPolygon cannot be empty");

        this.indices = indices;
        this.multiPolygon = multiPolygon;
    }
    public List<String> getIndices() { return indices; }
    public MultiPolygon getMultiPolygon() { return multiPolygon; }
}
