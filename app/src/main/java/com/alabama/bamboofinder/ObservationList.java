package com.alabama.bamboofinder;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Michael W. on 4/4/2015.
 */
public class ObservationList {

    private List<Observation> mObservationList;

    public ObservationList() {
        mObservationList = new LinkedList<>();
    }

    // adds an observation to the observation list

    public void addObservation(Observation observation) {
        mObservationList.add(observation);
    }

    // removes an observation from the observation list

    public void removeObservation(Observation observation) {
        mObservationList.remove(observation); // validate that this is correct
    }

    // returns list of observations

    public List<Observation> getObservationList() {
        return mObservationList;
    }
}
