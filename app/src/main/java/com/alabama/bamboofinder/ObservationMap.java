package com.alabama.bamboofinder;

import android.location.Location;

import java.util.ArrayList;

/**
 * Created by Michael H. on 3/19/2015.
 */
public class ObservationMap {
    private ArrayList<Observation> mObservations;
    private Location mCurrentLocation;

    public ObservationMap(Location location) {
        mObservations = new ArrayList<Observation>();
        mCurrentLocation = location;
    }

    public ArrayList<Observation> getAllObservations() {
        return mObservations;
    }

    public ArrayList<Observation> getFilteredObservations(SearchFilter sf) {
        ArrayList<Observation> filteredObservations = new ArrayList<Observation>();
        for(Observation observation : mObservations) {
            if(sf.meetsCriteria(observation)) {
                filteredObservations.add(observation);
            }
        }
        return filteredObservations;
    }

    public void addObservation(Observation observation) {
        mObservations.add(observation);
    }
}
