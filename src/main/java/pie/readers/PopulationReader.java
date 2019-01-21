package pie.readers;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.io.input.CSVReader;
import de.tum.bgu.msm.moped.util.MoPeDUtil;

public class PopulationReader extends CSVReader {
    //private int idIndex;
    private int nameIndex;
    private int xIndex;
    private int yIndex;
    private int popIndex;
    private int id;


    public PopulationReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {

        super.read("F:/Qin/MoPeD/MunichPIE/data/population/pop_muc_100m_3035.csv", ",");

    }

    @Override
    protected void processHeader(String[] header) {
        //idIndex = MoPeDUtil.findPositionInArray("id", header);
        id = 1;
        nameIndex = MoPeDUtil.findPositionInArray("Gitter_ID_100m", header);
        xIndex = MoPeDUtil.findPositionInArray("x_mp_100m", header);
        yIndex = MoPeDUtil.findPositionInArray("y_mp_100m", header);
        popIndex = MoPeDUtil.findPositionInArray("Einwohner", header);

    }

    @Override
    protected void processRecord(String[] record) {
        //int id = Integer.parseInt(record[idIndex]);
        String name = record[nameIndex];
        long x = Long.parseLong(record[xIndex]);
        long y = Long.parseLong(record[yIndex]);
        int pop = Integer.parseInt(record[popIndex]);
        if(pop == -1){
            pop = 0;
        }
        //BlockGroup mucZone = new BlockGroup(id, name, new Coordinate(x,y), pop);
        //MucPIECaculator.zoneMap.put(id,mucZone);
        //MucPIECaculator.popTree.put(x,y,mucZone);

        id++;

    }



}
