package de.tum.bgu.msm.moped.io.input.readers;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.SuperPAZ;
import de.tum.bgu.msm.moped.io.input.CSVReader;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import de.tum.bgu.msm.moped.util.MoPeDUtil;
import org.apache.log4j.Logger;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.Geometry;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import java.io.File;
import java.io.IOException;

public class MitoSuperPAZReader extends CSVReader {
    private int idIndex;
    private int householdIndex;
    private int totalEmplIndex;
    private int financialIndex;
    private int governmentIndex;
    private int retailIndex;
    private int serviceIndex;
    private int afmIndex;
    private int conIndex;
    private int mfgIndex;
    private int tpuIndex;
    private int utlIndex;
    private int whoIndex;

    private int parkIndex;
    private int index = 0;

    private static final Logger logger = Logger.getLogger(MitoSuperPAZReader.class);
    private QuadTree<SuperPAZ> superPAZSearchTree;

    private int blockIndex;
    private int networkDensityIndex;


    public MitoSuperPAZReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        logger.info("reading superPAZ attributes");
        super.read(Resources.INSTANCE.getString(Properties.SUPERPAZATTRIBUTE), ",");
        mapFeaturesToSuperPAZs(dataSet);
    }

    @Override
    protected void processHeader(String[] header) {
        idIndex = MoPeDUtil.findPositionInArray("superPAZId", header);
        householdIndex = MoPeDUtil.findPositionInArray("pop", header);
        totalEmplIndex = MoPeDUtil.findPositionInArray("TotalEmpl", header);
        financialIndex = MoPeDUtil.findPositionInArray("Finc", header);
        governmentIndex = MoPeDUtil.findPositionInArray("Admn", header);
        retailIndex = MoPeDUtil.findPositionInArray("Retl", header);
        serviceIndex = MoPeDUtil.findPositionInArray("Serv", header);
        afmIndex = MoPeDUtil.findPositionInArray("Agri", header);
        conIndex = MoPeDUtil.findPositionInArray("Cons", header);
        mfgIndex = MoPeDUtil.findPositionInArray("Mnft", header);
        tpuIndex = MoPeDUtil.findPositionInArray("Trns", header);
        utlIndex = MoPeDUtil.findPositionInArray("Util", header);
        whoIndex = MoPeDUtil.findPositionInArray("Rlst", header);
        parkIndex = MoPeDUtil.findPositionInArray("PRK", header);
        blockIndex = MoPeDUtil.findPositionInArray("block_barrier", header);
        networkDensityIndex = MoPeDUtil.findPositionInArray("LENGTH", header);

    }

    @Override
    protected void processRecord(String[] record) {
        int superPAZId = Integer.parseInt(record[idIndex]);

        SuperPAZ superPAZ = dataSet.getSuperPAZ(superPAZId);
        if (superPAZ == null){
            superPAZ = new SuperPAZ(superPAZId, "ORIGIN");
            dataSet.addSuperPAZ(superPAZ);
        }

        float household = Float.parseFloat(record[householdIndex]);
        float totalEmpl = Float.parseFloat(record[totalEmplIndex]);
        float financial = Float.parseFloat(record[financialIndex]);
        float government = Float.parseFloat(record[governmentIndex]);
        float retail = Float.parseFloat(record[retailIndex]);
        float service = Float.parseFloat(record[serviceIndex]);
        int park = Integer.parseInt(record[parkIndex]);
        float afm = Float.parseFloat(record[afmIndex]);
        float con = Float.parseFloat(record[conIndex]);
        float mfg = Float.parseFloat(record[mfgIndex]);
        float tpu = Float.parseFloat(record[tpuIndex]);
        float util = Float.parseFloat(record[utlIndex]);
        float who = Float.parseFloat(record[whoIndex]);
        int block = Integer.parseInt(record[blockIndex]);
        double networkDensity = Double.parseDouble(record[networkDensityIndex]);


        dataSet.getSuperPAZ(superPAZId).setHousehold(household);
        dataSet.getSuperPAZ(superPAZId).setTotalEmpl(totalEmpl);
        dataSet.getSuperPAZ(superPAZId).setFinancial(financial);
        dataSet.getSuperPAZ(superPAZId).setGovernment(government);
        dataSet.getSuperPAZ(superPAZId).setRetail(retail);
        dataSet.getSuperPAZ(superPAZId).setService(service);
        dataSet.getSuperPAZ(superPAZId).setPark(park);
        dataSet.getSuperPAZ(superPAZId).setIndustrial(afm+con+mfg);
        dataSet.getSuperPAZ(superPAZId).setAfm(afm);
        dataSet.getSuperPAZ(superPAZId).setConstruction(con);
        dataSet.getSuperPAZ(superPAZId).setManufacturing(mfg);
        dataSet.getSuperPAZ(superPAZId).setTpu(tpu+util);
        dataSet.getSuperPAZ(superPAZId).setWho(who);
        dataSet.getSuperPAZ(superPAZId).setBlock(block);
        dataSet.getSuperPAZ(superPAZId).setNetworkDesnity((float) (networkDensity/1000.));
        if (totalEmpl != 0){
            dataSet.addDestinationSuperPAZ(index, superPAZ);
            superPAZ.setIndex(index);
            index++;
        }
    }

    //TODO: like the one in MITO
    private void mapFeaturesToSuperPAZs(DataSet dataSet) {
        setSuperPAZSearchTree();
        int counter = 0;
        for (SimpleFeature feature: ShapeFileReader.getAllFeatures(Resources.INSTANCE.getString(Properties.SUPERPAZ_SHAPEFILE))) {
            int superPAZId = Integer.parseInt(feature.getAttribute("id").toString());
            SuperPAZ superPAZ = dataSet.getSuperPAZ(superPAZId);
            if (superPAZ != null){
                superPAZ.setShapeFeature(feature);
                superPAZSearchTree.put(((Geometry)feature.getDefaultGeometry()).getCentroid().getX(),((Geometry)feature.getDefaultGeometry()).getCentroid().getY(),superPAZ);
            }else{
                //logger.warn("superPAZId " + superPAZ + " doesn't exist in moped zone system");
                counter++;
            }
        }
        logger.warn(counter + " superPAZ in shapefile doesn't exist in superPAZ.csv");

        dataSet.setSuperPAZSearchTree(superPAZSearchTree);
    }

    private void setSuperPAZSearchTree() {
        ReferencedEnvelope bounds = loadEnvelope();
        double minX = bounds.getMinX()-1;
        double minY = bounds.getMinY()-1;
        double maxX = bounds.getMaxX()+1;
        double maxY = bounds.getMaxY()+1;
        this.superPAZSearchTree = new QuadTree<>(minX,minY,maxX,maxY);
    }

    private ReferencedEnvelope loadEnvelope() {
        File zonesShapeFile = new File(Resources.INSTANCE.getString(Properties.SUPERPAZ_SHAPEFILE));
        try {
            FileDataStore dataStore = FileDataStoreFinder.getDataStore(zonesShapeFile);
            return dataStore.getFeatureSource().getBounds();
        } catch (IOException e) {
            logger.error("Error reading file " + zonesShapeFile);
            throw new RuntimeException(e);
        }
    }
}
