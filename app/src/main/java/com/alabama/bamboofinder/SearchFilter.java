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

    public SearchFilter(Date date) {
        mEarliestDate = date;
    }

    // Returns true if all criteria are met, false otherwise.
    public boolean meetsCriteria(Observation o) {
        if(o.getTimeStamp().before(mEarliestDate)) {
            Log.d(TAG, "Observation was too old: " + o.getTimeStamp() + " was before " + mEarliestDate);
            return false;
        }
        Log.d(TAG, "Observation met filter");
        return true;
    }
}
