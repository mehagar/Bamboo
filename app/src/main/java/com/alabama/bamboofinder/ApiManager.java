package com.alabama.bamboofinder;

import android.location.Location;

import java.util.ArrayList;

/**
 * Created by Michael H. on 3/22/2015.
 */
public class ApiManager {

    public ArrayList<Observation> getObservationsFromNetwork(Location location) {
        ArrayList<Observation> observations = new ArrayList<Observation>();
        // TODO: call web service to find all observations within certain radius
        // of mCurrentLocation, and store them in observations
        return observations;
    }
}
