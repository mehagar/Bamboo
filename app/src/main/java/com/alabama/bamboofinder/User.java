package com.alabama.bamboofinder;

import android.util.Log;

import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.model.Token;

/**
 * Created by Jeremy on 4/6/2015.
 */
public class User {

    private static final String JSON_CREATED = "created_at";
    private static final String JSON_DESCRIPTION = "description";
    private static final String JSON_EMAIL = "email";
    private static final String JSON_ID = "id";
    private static final String JSON_LOGIN = "login";
    private static final String JSON_NAME = "name";
    private static final String JSON_OBSERVATIONS_COUNT = "observations_count";
    private static final String JSON_URI = "uri";

    private String mCreationDate;
    private String mDescription;
    private String mEmail;
    private int mID;
    private String mUsername;
    private String mName;
    private int mObservationsCount;
    private String mUri;
    private ArrayList<Observation> mObservationList;
    private Token mToken;
    private boolean mAdmin;

    public User (JSONObject jsonObject) {
        try {
            mCreationDate = jsonObject.getString(JSON_CREATED);
            mDescription = jsonObject.getString(JSON_DESCRIPTION);
            mEmail = jsonObject.getString(JSON_EMAIL);
            mID = jsonObject.getInt(JSON_ID);
            mUsername = jsonObject.getString(JSON_LOGIN);
            mName = jsonObject.getString(JSON_NAME);
            mObservationsCount = jsonObject.getInt(JSON_OBSERVATIONS_COUNT);
            mUri = jsonObject.getString(JSON_URI);
            mObservationList = new ArrayList<Observation>();
                //TODO populate mObservationList
            mToken = null;
        }
        catch (JSONException e) {
            Log.e("UserJSON", "Error parsing json for user");
        }
    }

    // Getters and Setters
    public String getmCreationDate() {
        return mCreationDate;
    }

    public void setmCreationDate(String mCreationDate) {
        this.mCreationDate = mCreationDate;
    }

    public String getmDescription() {
        return mDescription;
    }

    public void setmDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public void setmEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public String getmEmail() {
        return mEmail;
    }

    public int getmID() {
        return mID;
    }

    public void setmID(int mID) {
        this.mID = mID;
    }

    public String getmUsername() {
        return mUsername;
    }

    public void setmUsername(String mUsername) {
        this.mUsername = mUsername;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public int getmObservationsCount() {
        return mObservationsCount;
    }

    public void setmObservationsCount(int mObservationsCount) {
        this.mObservationsCount = mObservationsCount;
    }

    public String getmUri() {
        return mUri;
    }

    public void setmUri(String mUri) {
        this.mUri = mUri;
    }

    public ArrayList<Observation> getmObservationList() {
        return mObservationList;
    }

    public void setmObservationList(ArrayList<Observation> mObservationList) {
        this.mObservationList = mObservationList;
    }

    public Token getmToken() {
        return mToken;
    }

    public void setmToken(Token mToken) {
        this.mToken = mToken;
    }

    public boolean ismAdmin() {
        return mAdmin;
    }

    public void setmAdmin(boolean mAdmin) {
        this.mAdmin = mAdmin;
    }
}
