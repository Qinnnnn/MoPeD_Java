package de.tum.bgu.msm.moped.data;

import java.util.ArrayList;
import java.util.List;

public enum Purpose {
    HBW,
    HBE,
    HBS,
    HBR,
    HBO,
    NHBO,
    NHBW,
    //additional purpose for moped standalone
    HBREC,
    HBSHOP,
    HBOTH,
    HBSCH,
    HBCOLL,
    NHBNW;

    public static Purpose[] purposeSetForStandAlone() {
        Purpose[] set = { HBW, HBSHOP, HBREC, HBOTH, NHBW, NHBNW };
        return set;
    }

    public static List<Purpose> getMandatoryPurposes(){
        List<Purpose> list = new ArrayList<>();
        list.add(HBW);
        list.add(HBE);
        return list;
    }

    public static List<Purpose> getHomeBasedDiscretionaryPurposes() {
        List<Purpose> list = new ArrayList<>();
        list.add(HBS);
        list.add(HBR);
        list.add(HBO);
        return list;
    }

    public static List<Purpose> getNonHomeBasedPurposes() {
        List<Purpose> list = new ArrayList<>();
        list.add(NHBO);
        list.add(NHBW);
        return list;
    }
}
