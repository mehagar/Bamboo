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
    private static final String TAG = "Observation";

    private static final String JSON_LATITUDE = "latitude";
    private static final String JSON_LONGITUDE = "longitude";
    private static final String JSON_ID = "id";
    private static final String JSON_PHOTOS = "photos";
    private static final String JSON_THUMBNAIL_URL = "square_url";
    private static final String JSON_MEDIUM_URL = "medium_url";
    private static final String JSON_NUM_PHOTOS = "observation_photos_count";
    private static final String JSON_SPECIES_GUESS = "species_guess";
    private static final String JSON_OBSERVED_DATE = "observed_on";

    private Date mDateObserved;
    private String mSpeciesGuess;
    private String mDescription;
    private boolean mValidated;
    private String mId;
    double mLatitude;
    double mLongitude;
    private String mThumbnailUrl;  
    private String mMediumUrl;

    public Observation() {
        mDateObserved = new Date();
        mSpeciesGuess = "Default Species Guess";
        mDescription = "Default Description";
        mValidated = false;
        mId = "00000";
        mLatitude = 0.0;
        mLongitude = 0.0;
        mThumbnailUrl = "unassigned thumbnail url";
        mMediumUrl = "unassigned medium url";
    }

    public Observation(JSONObject jsonObject) {
        try {
            mLatitude = jsonObject.getDouble(JSON_LATITUDE);
            mLongitude = jsonObject.getDouble(JSON_LONGITUDE);
            mId = jsonObject.getString(JSON_ID);
            mSpeciesGuess = jsonObject.getString(JSON_SPECIES_GUESS);
            mThumbnailUrl = parsePhotoUrl(jsonObject, JSON_THUMBNAIL_URL);
            mMediumUrl = parsePhotoUrl(jsonObject, JSON_MEDIUM_URL);
            mDateObserved = parseDateFromString(jsonObject.getString(JSON_OBSERVED_DATE));
        } catch(JSONException e) {
            Log.e(TAG, "Error parsing json for observation: " + e.getMessage());
        }
    }

    private String parsePhotoUrl(JSONObject jsonObject, String photoKey) throws JSONException {
        String thumbnailUrl;
        int numPhotos = jsonObject.getInt(JSON_NUM_PHOTOS);
        if(numPhotos >= 1) {
            JSONArray photos = jsonObject.getJSONArray(JSON_PHOTOS);
            // just use the first photo as the thumbnail for a marker
            thumbnailUrl = photos.getJSONObject(0).getString(photoKey);
            Log.d(TAG, "Got thumbnail url: " + thumbnailUrl);
        } else {
            Log.e(TAG, "Observation being created without a photo");
            thumbnailUrl = "";
        }
        return thumbnailUrl;
    }

    private Date parseDateFromString(String dateString) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date dateObserved;
        try {
            dateObserved = formatter.parse(dateString);
        } catch(ParseException pe) {
            Log.e(TAG, "Could not parse date: " + pe.getMessage());
            dateObserved = new Date();
        }
        return dateObserved;
    }

    public String getSpeciesGuess() {
        return mSpeciesGuess;
    }

    public void setSpeciesGuess(String speciesGuess) {
        mSpeciesGuess = speciesGuess;
    }

    public String getThumbnailUrl() {
        return mThumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        mThumbnailUrl = thumbnailUrl;
    }

    public String getMediumUrl() {
        return mMediumUrl;
    }

    public void setMediumUrl(String mediumUrl) {
        mMediumUrl = mediumUrl;
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
}