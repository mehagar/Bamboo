package com.alabama.bamboofinder2;

import android.util.Log;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Michael H. on 3/19/2015.
 */
public class SearchFilter implements Serializable {
    private static final String TAG = "SearchFilter";

    Date mEarliestDate;
    boolean mMustBeBefore;
    boolean mOnlyOwnObservations;

    public SearchFilter(boolean ownObservations, boolean afterDate, Date date) {
        mOnlyOwnObservations = ownObservations;
        mMustBeBefore = afterDate;
        mEarliestDate = date;
    }

    // Returns true if all criteria are met, false otherwise.
    public boolean meetsCriteria(Observation o, String userName) {
        if(mMustBeBefore && mEarliestDate.after(o.getTimeStamp())) {
            Log.d(TAG, "Observation was too old: " + o.getTimeStamp() + " was before " + mEarliestDate);
            return false;
        } else if(mOnlyOwnObservations && !o.getUserLogin().equals(userName)) {
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

    public boolean isOnlyOwnObservations() {
        return mOnlyOwnObservations;
    }
}
