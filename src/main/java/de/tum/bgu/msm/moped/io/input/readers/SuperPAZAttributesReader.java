package de.tum.bgu.msm.moped.io.input.readers;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.MopedZone;
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

public class SuperPAZAttributesReader extends CSVReader {
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
    private int whoIndex;

    private int pieIndex;
    private int slopeIndex;
    private int freewayIndex;
    //private int industrialIndex;
    private int parkIndex;
    private int index = 0;
    private int count = 0;
    private static final Logger logger = Logger.getLogger(SuperPAZAttributesReader.class);
    private QuadTree<SuperPAZ> superPAZSearchTree;
    private int clarkIndex;
    private int blockIndex;
    private int networkDensityIndex;


    public SuperPAZAttributesReader(DataSet dataSet) {
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
        idIndex = MoPeDUtil.findPositionInArray("superPAZ", header);
        householdIndex = MoPeDUtil.findPositionInArray("HH", header);
        totalEmplIndex = MoPeDUtil.findPositionInArray("EMP_TOT", header);
        financialIndex = MoPeDUtil.findPositionInArray("EMP_FIN", header);
        governmentIndex = MoPeDUtil.findPositionInArray("EMP_GOV", header);
        retailIndex = MoPeDUtil.findPositionInArray("EMP_RET", header);
        serviceIndex = MoPeDUtil.findPositionInArray("EMP_SER", header);
        pieIndex = MoPeDUtil.findPositionInArray("PIE_AVG", header);
        slopeIndex = MoPeDUtil.findPositionInArray("SLP_MEAN", header);
        freewayIndex = MoPeDUtil.findPositionInArray("FWY_IN_ZONE", header);
        parkIndex = MoPeDUtil.findPositionInArray("PRK", header);
        //industrialIndex = MoPeDUtil.findPositionInArray("EMP_INDUSTRIAL", header);
        afmIndex = MoPeDUtil.findPositionInArray("EMP_AFM", header);
        conIndex = MoPeDUtil.findPositionInArray("EMP_CON", header);
        mfgIndex = MoPeDUtil.findPositionInArray("EMP_MFG", header);
        tpuIndex = MoPeDUtil.findPositionInArray("EMP_TPU", header);
        whoIndex = MoPeDUtil.findPositionInArray("EMP_WHO", header);
       // clarkIndex = MoPeDUtil.findPositionInArray("Clark", header);
        blockIndex = MoPeDUtil.findPositionInArray("superPAZ_block_motorway", header);
        networkDensityIndex = MoPeDUtil.findPositionInArray("LENGTH", header);

    }

    @Override
    protected void processRecord(String[] record) {
        //System.out.println(count++);
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
        float pie = Float.parseFloat(record[pieIndex]);
        float slope = Float.parseFloat(record[slopeIndex]);
        int freeway = Integer.parseInt(record[freewayIndex]);
        int park = Integer.parseInt(record[parkIndex]);
        float afm = Float.parseFloat(record[afmIndex]);
        float con = Float.parseFloat(record[conIndex]);
        float mfg = Float.parseFloat(record[mfgIndex]);
        float tpu = Float.parseFloat(record[tpuIndex]);
        float who = Float.parseFloat(record[whoIndex]);
        //float industrial = Float.parseFloat(record[industrialIndex]);
       // int clark = Integer.parseInt(record[clarkIndex]);//internalIndex
        int block = Integer.parseInt(record[blockIndex]);
        double networkDensity = Double.parseDouble(record[networkDensityIndex]);


        dataSet.getSuperPAZ(superPAZId).setHousehold(household);
        dataSet.getSuperPAZ(superPAZId).setTotalEmpl(totalEmpl);
        dataSet.getSuperPAZ(superPAZId).setFinancial(financial);
        dataSet.getSuperPAZ(superPAZId).setGovernment(government);
        dataSet.getSuperPAZ(superPAZId).setRetail(retail);
        dataSet.getSuperPAZ(superPAZId).setService(service);
        dataSet.getSuperPAZ(superPAZId).setPie(pie);
        dataSet.getSuperPAZ(superPAZId).setSlope(slope);
        dataSet.getSuperPAZ(superPAZId).setFreeway(freeway);
        dataSet.getSuperPAZ(superPAZId).setPark(park);
        dataSet.getSuperPAZ(superPAZId).setIndustrial(afm+con+mfg);
        dataSet.getSuperPAZ(superPAZId).setAfm(afm);
        dataSet.getSuperPAZ(superPAZId).setConstruction(con);
        dataSet.getSuperPAZ(superPAZId).setManufacturing(mfg);
        dataSet.getSuperPAZ(superPAZId).setTpu(tpu);
        dataSet.getSuperPAZ(superPAZId).setWho(who);
        //dataSet.getSuperPAZ(superPAZId).setClark(clark==1?Boolean.TRUE:Boolean.FALSE);
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
        for (SimpleFeature feature: ShapeFileReader.getAllFeatures(Resources.INSTANCE.getString(Properties.SUPERPAZ_SHAPEFILE))) {
            int superPAZId = Integer.parseInt(feature.getAttribute("OBJECTID").toString());
            SuperPAZ superPAZ = dataSet.getSuperPAZ(superPAZId);
            if (superPAZ != null){
                superPAZ.setShapeFeature(feature);
                superPAZSearchTree.put(((Geometry)feature.getDefaultGeometry()).getCentroid().getX(),((Geometry)feature.getDefaultGeometry()).getCentroid().getY(),superPAZ);
            }else{
                logger.warn("superPAZId " + superPAZ + " doesn't exist in moped zone system");
            }
        }
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
