package de.tum.bgu.msm.moped.data;


import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class MopedHousehold {
    private static final Logger logger = Logger.getLogger(MopedHousehold.class);
    private final int id;
    private final int income;
    private final int autos;
    private final MopedZone homeZone;
    private final int kids;
    private Map<Integer, MopedPerson> persons  = new HashMap<>();


    public MopedHousehold(int id, int income, int autos, int kids, MopedZone homeZone) {
        this.id = id;
        this.income = income;
        this.autos = autos;
        this.homeZone = homeZone;
        this.kids = kids;
    }

    public void addPerson(MopedPerson person) {
        MopedPerson test = this.persons.get(person.getId());
        if(test!= null) {
            if(test.equals(person)) {
                logger.warn("Person " + person.getId() + " was already added to household " + this.getId());
            } else {
                throw new IllegalArgumentException("Person id " + person.getId() + " already exists in household " + this.getId());
            }
        }
        this.persons.put(person.getId(), person);
    }

    public int getId() {
        return id;
    }

    public int getIncome() {
        return income;
    }

    public int getAutos() {
        return autos;
    }

    public MopedZone getHomeZone() {
        return homeZone;
    }

    public int getKids() {
        return kids;
    }

    public Map<Integer, MopedPerson> getPersons() {
        return persons;
    }
}
