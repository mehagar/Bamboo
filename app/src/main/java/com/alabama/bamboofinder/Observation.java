package com.alabama.bamboofinder;

import android.location.Location;
import android.media.Image;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.Date;

/**
 * Created by Michael H. on 3/19/2015.
 */

public class Observation {
    private static final String TAG = "observation";

    private static final String JSON_LATITUDE = "latitude";
    private static final String JSON_LONGITUDE = "longitude";
    private static final String JSON_ID = "id";
    private static final String JSON_PHOTOS = "photos";
    private static final String JSON_THUMBNAIL_URL = "square_url";
    private static final String JSON_NUM_PHOTOS = "observation_photos_count";
    private static final String JSON_SPECIES_GUESS = "species_guess";

    private Date mTimeStamp;
    private String mSpeciesGuess;
    private String mDescription;
    private boolean mValidated;
    private String mOwnerUserName;
    private String mId;
    private LatLng mLocation;
    private String mThumbnailURL;

    public Observation(JSONObject obs) {
        double latitude;
        double longitude;
        try {
            latitude = obs.getDouble(JSON_LATITUDE);
            longitude = obs.getDouble(JSON_LONGITUDE);
            mLocation = new LatLng(latitude, longitude);
            mId = obs.getString(JSON_ID);
            mSpeciesGuess = obs.getString(JSON_SPECIES_GUESS);
            int numPhotos = obs.getInt(JSON_NUM_PHOTOS);
            if(numPhotos >= 1) {
                JSONArray photos = obs.getJSONArray(JSON_PHOTOS);
                // just use the first photo as the thumbnail for a marker
                mThumbnailURL = photos.getJSONObject(0).getString(JSON_THUMBNAIL_URL);
                Log.d(TAG, "Got thumbnail url: " + mThumbnailURL);
            }
            // TODO: parse the time stamp from the "created_at" field in JSON
        } catch(JSONException e) {
            Log.e(TAG, "Error parsing json for observation");
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
}