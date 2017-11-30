package de.tum.bgu.msm.moped.data;

import org.apache.log4j.Logger;

public class HouseholdType {

    private static final Logger logger = Logger.getLogger(HouseholdType.class);
    private final int hhTypeId;
    private String hhTypeName;
    private int kids;
    private int cars;
    private int workers;
    private int householdSize;
    private int income;
    private int age;

    public HouseholdType(String name, int id, int kids, int cars, int workers, int householdSize, int income, int age) {
        this.hhTypeName = name;
        this.hhTypeId = id;
        this.kids = kids;
        this.cars = cars;
        this.workers = workers;
        this.householdSize = householdSize;
        this.income = income;
        this.age = age;

    }

    public int getHhTypeId() {
        return hhTypeId;
    }

    public String getHhTypeName() {
        return hhTypeName;
    }

    public int getKids() {
        return kids;
    }

    public int getCars() {
        return cars;
    }

    public int getWorkers() {
        return workers;
    }

    public int getHouseholdSize() {
        return householdSize;
    }

    public int getIncome() {
        return income;
    }

    public int getAge() {
        return age;
    }

}
