package org.geotools;


import java.io.File;
import java.util.logging.Logger;

import org.geotools.data.DataUtilities;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.collection.SpatialIndexFeatureCollection;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.locationtech.jts.geom.*;
import org.opengis.feature.simple.SimpleFeature;

public class App 
{
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(App.class);

    public static void main( String[] args ) throws Exception
    {
        // display a data store file chooser dialog for shapefiles
        LOGGER.info( "Quickstart");
        LOGGER.config( "Welcome Developers");
        LOGGER.info("java.util.logging.config.file="+System.getProperty("java.util.logging.config.file"));
        File file = JFileDataStoreChooser.showOpenFile("shp", null);
        if (file == null) {
            return;
        }
        LOGGER.config("File selected "+file);

        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        SimpleFeatureSource featureSource = store.getFeatureSource();
        // Cache entire shapefile into memory
        SimpleFeatureSource cachedSource =
                DataUtilities.source(
                        new SpatialIndexFeatureCollection(featureSource.getFeatures()));
        LOGGER.info(cachedSource.getSchema().toString());
        SimpleFeatureCollection featureCollection = cachedSource.getFeatures();
        SimpleFeatureIterator simpleFeatureIterator = featureCollection.features();
        while(simpleFeatureIterator.hasNext()) {
            SimpleFeature simpleFeature = simpleFeatureIterator.next();
            String statens = (String) simpleFeature.getAttribute("NAME");
            MultiPolygon multiPolygon = (MultiPolygon) simpleFeature.getAttribute("the_geom");
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
            Point point = geometryFactory.createPoint(new Coordinate(-105.144863, 39.939094 ));
            if(multiPolygon.contains(point)) {
                LOGGER.info(statens + ": contains Superior");
            }
            else {
                LOGGER.info(statens + ": does not contain Superior");
            }
        }

        // Create a map content and add our shapefile to it
        MapContent map = new MapContent();
        map.setTitle("Quickstart");

        Style style = SLD.createSimpleStyle(featureSource.getSchema());
        Layer layer = new FeatureLayer(featureSource, style);
        map.addLayer(layer);

        // Now display the map
        JMapFrame.showMap(map);
    }
}
