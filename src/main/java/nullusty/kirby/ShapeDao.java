package nullusty.kirby;

import org.geotools.App;
import org.geotools.data.DataUtilities;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.collection.SpatialIndexFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ShapeDao {
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(ShapeDao.class);

    private final Map<String, MultiPolygon> stateNameToMultipolygonMap = new HashMap<>();

    public ShapeDao(String filepath) throws IOException {
        LOGGER.info("ingesting shapefile data at: " + filepath);
        File file = new File(filepath);

        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        SimpleFeatureSource featureSource = store.getFeatureSource();
        // Cache entire shapefile into memory
        SimpleFeatureSource cachedSource =
                DataUtilities.source(
                        new SpatialIndexFeatureCollection(featureSource.getFeatures()));

        SimpleFeatureCollection featureCollection = cachedSource.getFeatures();
        LOGGER.info(featureCollection.getSchema().toString());
        SimpleFeatureIterator simpleFeatureIterator = featureCollection.features();
        while(simpleFeatureIterator.hasNext()) {
            SimpleFeature simpleFeature = simpleFeatureIterator.next();
            String stateName = (String) simpleFeature.getAttribute("NAME");
            String LSAD = (String) simpleFeature.getAttribute("LSAD");
            MultiPolygon multiPolygon = (MultiPolygon) simpleFeature.getAttribute("the_geom");
            stateNameToMultipolygonMap.put(stateName, multiPolygon);
            LOGGER.info(String.format("Loaded <stateName: %s, LSAD: %s>", stateName, LSAD));
        }
    }

    public Boolean doesStateContain(String stateName, Double latitude, Double longitude) {
        LOGGER.info(String.format("Calling doesStateContain for state '%s', Coords '%s,%s'",stateName,latitude,longitude));
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        Point point = geometryFactory.createPoint(new Coordinate(latitude, longitude));
        if(stateNameToMultipolygonMap.containsKey(stateName)) {
            return stateNameToMultipolygonMap.get(stateName).contains(point);
        }
        else return false;
    }
}
