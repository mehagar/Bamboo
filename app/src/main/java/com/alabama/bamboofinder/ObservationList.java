package com.alabama.bamboofinder;

import android.content.Context;

import java.util.LinkedList;
import java.util.List;
// import java.util.UUID;

/**
 * Created by Michael W. on 4/4/2015.
 */
public class ObservationList {

    private static ObservationList sObservationList;    // leading 's' for static variable
    private Context mAppContext;

    private List<Observation> mObservations;

    public ObservationList(Context appContent) {
        mAppContext = appContent;
        mObservations = new LinkedList<>();

        // populate the list with observations
        for (int i = 1; i <= 100; i++) {
            Observation observation = new Observation();
            observation.setSpeciesGuess("Observation #" + i);
            mObservations.add(observation);
        }
    }

    public static ObservationList get(Context context) {
        if (sObservationList == null) {
            // ensure that your singleton has a long-term Context to work with,
            // trade the passed-in Context for the application context which is
            // global to your application
            sObservationList = new ObservationList(context.getApplicationContext());
        }
        return sObservationList;
    }

    // adds an observation to the observation list

    public void addObservation(Observation observation) {
        mObservations.add(observation);
    }

    // removes an observation from the observation list

    public void removeObservation(Observation observation) {
        mObservations.remove(observation); // validate that this is correct
    }

    // returns list of observations

    public List<Observation> getObservations() {
        return mObservations;
    }

}
