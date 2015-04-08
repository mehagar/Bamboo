package com.alabama.bamboofinder;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Michael H. on 3/19/2015.
 */
public class SearchFilter implements Serializable {
    Date mEarliestDate;
    int mRadius;

    public SearchFilter(Date date, int radius) {
        mEarliestDate = date;
        mRadius = radius;
    }

    // Returns true if all criteria are met, false otherwise.
    public boolean meetsCriteria(LatLng coordinates, Observation o) {
        if(SphericalUtil.computeDistanceBetween(o.getLocation(), coordinates) > mRadius) {
            return false;
        }
        if(o.getTimeStamp().before(mEarliestDate)) {
            return false;
        }
        return true;
    }
}
