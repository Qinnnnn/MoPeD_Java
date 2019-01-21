package de.tum.bgu.msm.moped.data;

import org.apache.log4j.Logger;


public class MopedPerson {
    private static final Logger logger = Logger.getLogger(MopedPerson.class);

    private final int id;
    private MopedZone occupationZone;
    private final int age;
    private MopedHousehold household;


    public MopedPerson(int id, int age) {
        this.id = id;
        this.age = age;
    }

    public void setOccupationZone(MopedZone occupationZone) {
        this.occupationZone = occupationZone;
    }

    public MopedZone getOccupationZone() {
        return occupationZone;
    }

    public int getAge() {
        return age;
    }

}
