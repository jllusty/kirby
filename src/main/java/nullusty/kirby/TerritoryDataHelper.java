package nullusty.kirby;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

public class TerritoryDataHelper {
    private TerritoryDataHelper() {}

    public static Boolean doesTerritoryContainLatLong(TerritoryData territoryData, Double latitude, Double longitude) {
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        Point point = geometryFactory.createPoint(new Coordinate(latitude, longitude));
        return territoryData.getMultiPolygon().contains(point);
    }
}
