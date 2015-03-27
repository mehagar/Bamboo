package com.alabama.bamboofinder;

import android.location.Location;
import android.media.Image;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Michael H. on 3/19/2015.
 */

public class Observation {
    private static final String TAG = "observation";

    private static final String JSON_LATITUDE = "latitude";
    private static final String JSON_LONGITUDE = "longitude";
    private static final String JSON_ID = "id";

    private Date mTimeStamp;
    private String mDescription;
    private boolean mValidated;
    private String mOwnerUserName;
    private Image mImage;
    private String mId;
    private LatLng mLocation;

    public Observation(JSONObject obs) {
        double latitude;
        double longitude;
        try {
            latitude = obs.getDouble(JSON_LATITUDE);
            longitude = obs.getDouble(JSON_LONGITUDE);
            mLocation = new LatLng(latitude, longitude);
            mId = obs.getString(JSON_ID);
        } catch(JSONException e) {
            Log.e(TAG, "Error parsing json for observation");
        }
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public LatLng getLocation() {
        return mLocation;
    }

    public void setLocation(LatLng location) {
        this.mLocation = location;
    }

    public Date getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.mTimeStamp = timeStamp;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public boolean isValidated() {
        return mValidated;
    }

    public void setValidated(boolean validated) {
        this.mValidated = validated;
    }

    public String getOwnerUserName() {
        return mOwnerUserName;
    }

    public void setOwnerUserName(String ownerUserName) {
        this.mOwnerUserName = ownerUserName;
    }

    public Image getImage() {
        return mImage;
    }

    public void setImage(Image image) {
        this.mImage = image;
    }
}