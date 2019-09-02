package de.tum.bgu.msm.moped.util.concurrent;

import java.util.Random;
import java.util.concurrent.Callable;

public abstract class RandomizableConcurrentFunction<T> implements Callable<T>{

    protected final Random random;

    protected RandomizableConcurrentFunction(long randomSeed) {
        this.random = new Random(randomSeed);
    }
}
