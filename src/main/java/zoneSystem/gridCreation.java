package zoneSystem;

import com.google.common.math.LongMath;
import com.vividsolutions.jts.geom.Polygon;
import de.tum.bgu.msm.moped.modules.agentBased.AgentBasedModel;
import org.apache.log4j.Logger;
import org.geotools.data.*;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.grid.Grids;
import org.geotools.util.IntegerList;
import org.geotools.util.URLs;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class gridCreation {

    private final static Logger logger = Logger.getLogger(AgentBasedModel.class);
    public static void main(String[] args) {
        try {
            editShapefile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void createGrid() throws IOException {

        File zonesShapeFile = new File("F:/models/mitoMunich/input/trafficAssignment/zonesShapefile/zonesNew.shp");

        ReferencedEnvelope gridBounds;
        try {
            FileDataStore dataStore = FileDataStoreFinder.getDataStore(zonesShapeFile);
            ReferencedEnvelope referencedBound = dataStore.getFeatureSource().getBounds();
            gridBounds = new ReferencedEnvelope(referencedBound.getMinX(),referencedBound.getMaxX()+100,referencedBound.getMinY(),referencedBound.getMaxY(),referencedBound.getCoordinateReferenceSystem());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        double sideLen = 100;

        //Create a feature type
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        //tb.setNamespaceURI("http://www.opengis.net/gml");
        tb.setName("grid");
        tb.setCRS(gridBounds.getCoordinateReferenceSystem());
        tb.add("the_geom", Polygon.class);
        tb.add("id", Integer.class);
        SimpleFeatureType TYPE = tb.buildFeatureType();

        //Build the grid the custom feature builder class
        //GridFeatureBuilder builder = new IntersectionBuilder(TYPE, ozMapSource);
        SimpleFeatureSource grid = Grids.createSquareGrid(gridBounds, sideLen,-1);

        String output = "file:/F:/models/mitoMunich/input/moped/grid.shp";
        URL urlOutput = new URL(output);
        File file = null;
        try {
            file = new File(urlOutput.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put( ShapefileDataStoreFactory.URLP.key, file.toURI().toURL() );
        DataStore ds = new ShapefileDataStoreFactory().createNewDataStore(params);
        FeatureWriter<SimpleFeatureType, SimpleFeature> writer = ds.getFeatureWriter(TYPE.getTypeName(), Transaction.AUTO_COMMIT);
        SimpleFeatureIterator iterator = grid.getFeatures().features();
        SimpleFeature feature1;
        int id = 1;
        while (iterator.hasNext()) {
            feature1 = iterator.next();
            SimpleFeature feature2 = writer.next();
            feature2.setAttribute("the_geom", feature1.getDefaultGeometry());
            feature2.setAttribute("id", id);
            id++;
            writer.write();
        }
        writer.close();
        ds.dispose();


        //exportToShapefile2(grid,TYPE.getTypeName(),urlOutput);
    }


    public static void editShapefile() throws IOException {

        FileDataStore ds = FileDataStoreFinder.getDataStore(new File("C:/Users/qin/Desktop/test.shp"));
        SimpleFeatureType schema = ds.getSchema();
        // create new schema
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(schema.getName());
        builder.setSuperType((SimpleFeatureType) schema.getSuper());
        builder.addAll(schema.getAttributeDescriptors());
        // add new attribute(s)
        builder.add("zoneID", Integer.class);
        // build new schema
        SimpleFeatureType nSchema = builder.buildFeatureType();


        List<SimpleFeature> features = new ArrayList<>();
        int id = 1;
        int total = ds.getFeatureSource().getFeatures().size();
        SimpleFeatureIterator itr = ds.getFeatureSource().getFeatures().features();
        try{
            while (itr.hasNext()) {
                SimpleFeature f = itr.next();
                SimpleFeature f2 = DataUtilities.reType(nSchema, f);
                f2.setAttribute("zoneID", id);
                if(LongMath.isPowerOfTwo(id)) {
                    logger.info(id + " in " + total);
                };
                features.add(f2);
                id++;
            }
        }finally {
            itr.close();
            ds.dispose();
        }

        String output = "file:/C:/Users/qin/Desktop/test_new.shp";
        URL urlOutput = new URL(output);
        File file = null;
        try {
            file = new File(urlOutput.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put( ShapefileDataStoreFactory.URLP.key, file.toURI().toURL() );

        DataStore dataStore = DataStoreFinder.getDataStore(params);
        System.out.println(dataStore.getTypeNames()[0]);
        SimpleFeatureSource source = dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
        if (source instanceof SimpleFeatureStore) {
            SimpleFeatureStore store = (SimpleFeatureStore) source;
            store.addFeatures(DataUtilities.collection(features));
        } else {
            System.err.println("Unable to write to database");
        }
        //exportToShapefile2(source,dataStore.getTypeNames()[0],file.toURI().toURL());
    }




    static DataStore exportToShapefile2(SimpleFeatureSource grid, String typeName, URL urlOutput)
            throws IOException {
        // existing feature source from MemoryDataStore
        SimpleFeatureType ft = grid.getSchema();
//
//        String fileName = ft.getTypeName();
//        File file = new File(directory, fileName + ".shp");

        Map<String, Serializable> creationParams = new HashMap<>();
        creationParams.put("url", urlOutput);

        FileDataStoreFactorySpi factory = FileDataStoreFinder.getDataStoreFactory("shp");
        DataStore dataStore = factory.createNewDataStore(creationParams);

        dataStore.createSchema(ft);

        //SimpleFeatureStore featureStore = (SimpleFeatureStore) dataStore.getFeatureSource(typeName);

        Transaction t = new DefaultTransaction();
        try {
            SimpleFeatureCollection collection = grid.getFeatures(); // grab all features

            FeatureWriter<SimpleFeatureType, SimpleFeature> writer = dataStore.getFeatureWriter(
                    typeName, t);

            SimpleFeatureIterator iterator = collection.features();
            SimpleFeature feature;
            try {
                while (iterator.hasNext()) {
                    feature = iterator.next();

                    // Step1: create a new empty feature on each call to next
                    SimpleFeature aNewFeature = (SimpleFeature) writer.next();
                    // Step2: copy the values in
                    aNewFeature.setAttributes(feature.getAttributes());
                    // Step3: write out the feature
                    writer.write();
                }

            } catch (IOException eek) {
                eek.printStackTrace();
                try {
                    t.rollback();
                } catch (IOException doubleEeek) {
                    // rollback failed?
                }
            }
        } finally {
            t.close();
        }
        return dataStore;
    }
}