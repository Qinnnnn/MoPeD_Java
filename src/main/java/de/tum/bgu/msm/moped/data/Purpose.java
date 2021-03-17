package de.tum.bgu.msm.moped.data;

public enum Purpose {
    HBW,
    HBE,
    HBS,
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
}
