package de.tum.bgu.msm.moped.data;


public class MopedHousehold {
    private final int hhId;
    private final int income;
    private final int autos;
    private final MopedZone homeZone;
    private final int kids;


    public MopedHousehold(int id, int income, int autos, int kids, MopedZone homeZone) {
        this.hhId = id;
        this.income = income;
        this.autos = autos;
        this.homeZone = homeZone;
        this.kids = kids;
    }


}
