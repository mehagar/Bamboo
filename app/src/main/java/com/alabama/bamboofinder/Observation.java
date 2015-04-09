package com.alabama.bamboofinder;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Michael H. on 3/19/2015.
 */

public class Observation implements Serializable {
    private static final String TAG = "observation";

    private static final String JSON_LATITUDE = "latitude";
    private static final String JSON_LONGITUDE = "longitude";
    private static final String JSON_ID = "id";
    private static final String JSON_PHOTOS = "photos";
    private static final String JSON_THUMBNAIL_URL = "square_url";
    private static final String JSON_NUM_PHOTOS = "observation_photos_count";
    private static final String JSON_SPECIES_GUESS = "species_guess";
    private static final String JSON_OBSERVED_DATE = "observed_on";

    private Date mDateObserved;
    private String mSpeciesGuess;
    private String mDescription;
    private boolean mValidated;
    private String mOwnerUserName;
    private String mId;
    //private LatLng mLocation;
    double mLatitude;
    double mLongitude;

    private String mThumbnailURL;

    public Observation() {
        mDateObserved = null;
        mSpeciesGuess = "";
        mDescription = "";
        mValidated = false;
        mOwnerUserName = "";
        mId = "";
        //mLocation = null;
        mLatitude = 0.0;
        mLongitude = 0.0;
        mThumbnailURL = "";
    }

    public Observation(JSONObject jsonObject) {
        //double latitude;
        //double longitude;
        try {
            mLatitude = jsonObject.getDouble(JSON_LATITUDE);
            mLongitude = jsonObject.getDouble(JSON_LONGITUDE);
            //mLocation = new LatLng(latitude, longitude);
            mId = jsonObject.getString(JSON_ID);
            mSpeciesGuess = jsonObject.getString(JSON_SPECIES_GUESS);
            int numPhotos = jsonObject.getInt(JSON_NUM_PHOTOS);
            if(numPhotos >= 1) {
                JSONArray photos = jsonObject.getJSONArray(JSON_PHOTOS);
                // just use the first photo as the thumbnail for a marker
                mThumbnailURL = photos.getJSONObject(0).getString(JSON_THUMBNAIL_URL);
                Log.d(TAG, "Got thumbnail url: " + mThumbnailURL);
            } else {
                Log.e(TAG, "Observation being created without a photo");
                mThumbnailURL = "";
            }
            // TODO: parse the time stamp from the "created_at" field in JSON
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date dateObserved;
            try {
                dateObserved = formatter.parse(jsonObject.getString(JSON_OBSERVED_DATE));
            } catch(ParseException pe) {
                Log.e(TAG, "Could not parse date: " + pe.getMessage());
                dateObserved = new Date();
            }
            mDateObserved = dateObserved;
        } catch(JSONException e) {
            Log.e(TAG, "Error parsing json for observation: " + e.getMessage());
        }
    }

    public String getSpeciesGuess() {
        return mSpeciesGuess;
    }

    public void setSpeciesGuess(String speciesGuess) {
        mSpeciesGuess = speciesGuess;
    }

    public String getThumbnailURL() {
        return mThumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        mThumbnailURL = thumbnailURL;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public LatLng getLocation() {
        return new LatLng(mLatitude, mLongitude);
    }

    public void setLocation(LatLng location) {
        this.mLatitude = location.latitude; this.mLongitude = location.longitude;
    }

    public Date getTimeStamp() {
        return mDateObserved;
    }

    public void setTimeStamp(Date timeStamp) {
        this.mDateObserved = timeStamp;
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
}