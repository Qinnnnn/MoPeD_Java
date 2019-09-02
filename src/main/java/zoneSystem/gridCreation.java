package zoneSystem;

import com.vividsolutions.jts.geom.*;
import org.geotools.data.*;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.grid.Grids;
//import org.geotools.util.URLs;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class gridCreation {




    public void createGrid() throws IOException {

        // Load the outline of Australia from a shapefile
        String urlAsString = "file:/F:/Qin/MoPeD/MunichPIE/shapefiles/boundary.shp";
        URL url = new URL(urlAsString);
        FileDataStore dataStore = FileDataStoreFinder.getDataStore(url);
        SimpleFeatureSource ozMapSource = dataStore.getFeatureSource();

        // Set the grid size (1 degree) and create a bounding envelope
        // that is neatly aligned with the grid size
        double sideLen = 100;
        //double sideLen = 402.336804674938/5*60;
        //ReferencedEnvelope gridBounds = Envelopes.expandToInclude(ozMapSource.getBounds(), 0);
        //org.opengis.referencing.crs.CoordinateReferenceSystem crs = ozMapSource.getBounds().getCoordinateReferenceSystem();
        ReferencedEnvelope gridBounds = new ReferencedEnvelope(ozMapSource.getBounds().getMinX(), ozMapSource.getBounds().getMaxX()+450,ozMapSource.getBounds().getMinY(), ozMapSource.getBounds().getMaxY()+1,ozMapSource.getBounds().getCoordinateReferenceSystem());

//        ReferencedEnvelope gridBounds = new ReferencedEnvelope(ozMapSource.getBounds());
//        gridBounds.expandToInclude(ozMapSource.getBounds().getMinX(), ozMapSource.getBounds().getMaxX());
//        gridBounds.expandToInclude(ozMapSource.getBounds().getMinY(), ozMapSource.getBounds().getMaxY());


//        ReferencedEnvelope gridBounds = new ReferencedEnvelope(
//               2266398.59,136225.91,2409630.49,267790.04,ozMapSource.getBounds().getCoordinateReferenceSystem());




        //Create a feature type
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName("grid");
        tb.setCRS(gridBounds.getCoordinateReferenceSystem());
        tb.add("the_geom", Polygon.class);
        tb.add("id", Integer.class);
        SimpleFeatureType TYPE = tb.buildFeatureType();

        //Build the grid the custom feature builder class
        // GridFeatureBuilder builder = new IntersectionBuilder(TYPE, ozMapSource);
        SimpleFeatureSource grid = Grids.createSquareGrid(gridBounds, sideLen,-1);

        String output = "file:/F:/Qin/MoPeD/MunichPIE/shapefiles";
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
        FeatureWriter<SimpleFeatureType, SimpleFeature> writer = ds.getFeatureWriter("grid", Transaction.AUTO_COMMIT);
        SimpleFeatureIterator iterator = grid.getFeatures().features();
        SimpleFeature feature1;
        int id = 1;
        while (iterator.hasNext()) {
            feature1 = iterator.next();
            SimpleFeature feature2 = writer.next();
            feature2.setAttribute("the_geom", feature1.getDefaultGeometry());
            feature2.setAttribute("id", id);
            id++;
        }
        writer.write();
        writer.close();
        ds.dispose();


        //exportToShapefile2(grid,TYPE.getTypeName(),file);


//        String output = "file:/C:/Users/Qin/Documents/GitHub/Qin/MoPeD_Java";
//        URL urlOutput = new URL(output);
//        File file = null;
//        try {
//            file = new File(urlOutput.toURI());
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }

//        try {
//            Map<String, Serializable> params = new HashMap<String, Serializable>();
//            params.put( ShapefileDataStoreFactory.URLP.key, file.toURI().toURL() );
//            DataStore ds = new ShapefileDataStoreFactory().createNewDataStore(params);
//            //定义图形信息和属性信息
//            SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
//            tb.setCRS(DefaultGeographicCRS.WGS84);
//            tb.setName("shapefile");
//            tb.add("the_geom", Point.class);
//            tb.add("POIID", Long.class);
//            tb.add("NAMEC", String.class);
//            ds.createSchema(tb.buildFeatureType());
//            //ds.setCharset(Charset.forName("GBK"));
//            //设置Writer
//            FeatureWriter<SimpleFeatureType, SimpleFeature> writer = ds.getFeatureWriter("shapefile", Transaction.AUTO_COMMIT);
//            //写下一条
//            SimpleFeature feature = writer.next();
//            feature.setAttribute("the_geom", new GeometryFactory().createPoint(new Coordinate(116.123, 39.345)));
//            feature.setAttribute("POIID", 1234567890l);
//            feature.setAttribute("NAMEC", "某兴趣点1");
//            feature = writer.next();
//            feature.setAttribute("the_geom", new GeometryFactory().createPoint(new Coordinate(116.456, 39.678)));
//            feature.setAttribute("POIID", 1234567891l);
//            feature.setAttribute("NAMEC", "某兴趣点2");
//            writer.write();
//            writer.close();
//            ds.dispose();
//
////            //读取刚写完shape文件的图形信息
////            ShpFiles shpFiles = new ShpFiles(output);
////            ShapefileReader reader = new ShapefileReader(shpFiles, false, true, new GeometryFactory(), false);
////            try {
////                while (reader.hasNext()) {
////                    System.out.println(reader.nextRecord().shape());
////                }
////            } finally {
////                reader.close();
////            }
//        } catch (Exception e) {    }




    }




    DataStore exportToShapefile2( SimpleFeatureSource grid, String typeName, File directory)
            throws IOException {
        // existing feature source from MemoryDataStore
        SimpleFeatureType ft = grid.getSchema();

        String fileName = ft.getTypeName();
        File file = new File(directory, fileName + ".shp");

        Map<String, Serializable> creationParams = new HashMap<>();
        //creationParams.put("url", URLs.fileToUrl(file));

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
