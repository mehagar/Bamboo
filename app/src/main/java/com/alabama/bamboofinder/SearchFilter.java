package com.alabama.bamboofinder;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Michael H. on 3/19/2015.
 */
public class SearchFilter implements Serializable {
    private static final String TAG = "SearchFilter";

    Date mEarliestDate;
    boolean mMustBeBefore;
    boolean mOwnObservations;

    public SearchFilter(boolean ownObservations, boolean afterDate, Date date) {
        mOwnObservations = ownObservations;
        mMustBeBefore = afterDate;
        mEarliestDate = date;
    }

    // Returns true if all criteria are met, false otherwise.
    public boolean meetsCriteria(Observation o, String userName) {
        if(mMustBeBefore && o.getTimeStamp().before(mEarliestDate)) {
            Log.d(TAG, "Observation was too old: " + o.getTimeStamp() + " was before " + mEarliestDate);
            return false;
        } else if(mOwnObservations && !o.getUserLogin().equals(userName)) {
            Log.d(TAG, "Observation by user " + o.getUserLogin() + " not one of the users'");
            return false;
        }
        Log.d(TAG, "Observation met filter");
        return true;
    }

    public Date getEarliestDate() {
        return mEarliestDate;
    }

    public boolean isMustBeBefore() {
        return mMustBeBefore;
    }

    public boolean isOwnObservations() {
        return mOwnObservations;
    }
}
