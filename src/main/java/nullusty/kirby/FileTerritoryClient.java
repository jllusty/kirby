package nullusty.kirby;

import org.geotools.data.DataUtilities;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.collection.SpatialIndexFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.locationtech.jts.geom.MultiPolygon;
import org.opengis.feature.simple.SimpleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileTerritoryClient implements TerritoryDataClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileTerritoryClient.class);

    private Map<String,TerritoryData> indexToTerritoryDataMap = new HashMap<>();

    public FileTerritoryClient(String shapeFilePath, String indicesFilePath) throws IOException, AssertionError {
        // read equivalent indices into map from globally unique index -> list of equivalent indices
        HashMap<String,ArrayList<String>> mapOfEquivalentIndices = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(
                indicesFilePath))) {

            String line = reader.readLine();
            while (line != null) {
                // split CSV line into array of Strings
                String[] fields = line.split(",");
                if (fields.length > 0) {
                    // iterate over list of indices, accumulate into a list
                    ArrayList<String> equivalentIndices = new ArrayList<>();
                    for (int i = 0; i < fields.length; i++) {
                        equivalentIndices.add(fields[i]);
                    }
                    // iterate back over list of indices, populate map
                    for (int i = 0; i < fields.length; i++) {
                        // if this is not a globally unique field, throw
                        if(mapOfEquivalentIndices.containsKey(fields[i])) {
                            throw new AssertionError(String.format("Each index is expected to be globally unique! Found more than one occurrence of index: %s",fields[i]));
                        }
                        mapOfEquivalentIndices.put(fields[i],equivalentIndices);
                    }
                    LOGGER.info(String.format("Loaded indices: %s",equivalentIndices));
                }
                // read next line
                line = reader.readLine();
            }
        }

        // read shapefiles (must include at least one equivalent index)
        LOGGER.info("ingesting shapefile data at: " + shapeFilePath);
        File file = new File(shapeFilePath);

        // todo: move this into the second resource of the above try-with-resources
        //   split logic into two functions for each step
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
            // Extract Features
            SimpleFeature simpleFeature = simpleFeatureIterator.next();
            String shapeFileIndex = (String) simpleFeature.getAttribute("NAME");
            // todo: WTF is LSAD??
            // String LSAD = (String) simpleFeature.getAttribute("LSAD");
            MultiPolygon multiPolygon = (MultiPolygon) simpleFeature.getAttribute("the_geom");
            // if not found in our map of equivalent indices, skip
            if(mapOfEquivalentIndices.containsKey(shapeFileIndex)) {
                LOGGER.error("Could not identify any indices for shapeFileIndex = " + shapeFileIndex);
                // Create Territory Data
                TerritoryData territoryData = new TerritoryData(mapOfEquivalentIndices.get(shapeFileIndex), multiPolygon);
                // Populate Indices Referencing that TerritoryData
                for(String index : mapOfEquivalentIndices.get(shapeFileIndex)) {
                    indexToTerritoryDataMap.put(index, territoryData);
                }
            }
        }
    }

    public TerritoryData getTerritoryByIndex(String index) {
        return indexToTerritoryDataMap.get(index);
    }

    public List<TerritoryData> getAllTerritories() {
        return indexToTerritoryDataMap.values().stream().toList();
    }
}
