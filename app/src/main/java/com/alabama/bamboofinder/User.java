package com.alabama.bamboofinder;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Jeremy on 4/6/2015.
 */
public class User extends AsyncTask<String, Void, User> {

    private static final String JSON_CREATED = "created_at";
    private static final String JSON_DESCRIPTION = "description";
    private static final String JSON_EMAIL = "email";
    private static final String JSON_ID = "id";
    private static final String JSON_LOGIN = "login";
    private static final String JSON_NAME = "name";
    private static final String JSON_OBSERVATIONS_COUNT = "observations_count";
    private static final String JSON_URI = "uri";
    private static final String BASE_URL = "www.inaturalist.org";

    private String mCreationDate;
    private String mDescription;
    private String mEmail;
    private int mID;
    private String mUsername;
    private String mName;
    private int mObservationsCount;
    private String mUri;
    private ArrayList<Observation> mObservationList;
    private String mToken;
    private boolean mAdmin;

    public User() {
        mCreationDate = null;
        mDescription = null;
        mEmail = null;
        mID = -1;
        mUsername = null;
        mName = null;
        mObservationsCount = -1;
        mUri = null;
        mObservationList = null;
    }

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

    public User doInBackground(String... token) {
        if(token.length != 1) {
            Log.e("Error Retrieving User", "Passed in more than one token");
            return null;
        }

        String auth = "Bearer " + token[0];

        JSONObject object = null;
        Uri.Builder userRequest = new Uri.Builder();
        userRequest.scheme("https")
                .authority(BASE_URL)
                .appendPath("users")
                .appendPath("edit.json")
                .build();

        URL url;
        HttpsURLConnection connection;
        try {
            //URL url = new URL(userRequest.toString());
            url = new URL("https://www.inaturalist.org/users/edit.json");
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", auth);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoInput(true);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String temp, response = "";
            while ((temp = in.readLine()) != null)
                response += temp;
            in.close();

            object = (JSONObject) new JSONTokener(response).nextValue();
            connection.disconnect();
        }
        catch (Exception e) {

            Log.e("Error Retrieving User", e.toString());
        }

        User user = new User(object);
        return user;
    }

    public JSONObject convertToJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put(JSON_CREATED, getmCreationDate());
            json.put(JSON_DESCRIPTION, getmDescription());
            json.put(JSON_EMAIL, getmEmail());
            json.put(JSON_ID, getmID());
            json.put(JSON_LOGIN, getmUsername());
            json.put(JSON_NAME, getmName());
            json.put(JSON_OBSERVATIONS_COUNT, getmObservationsCount());
            json.put(JSON_URI, getmUri());
            json.put("observation_list", getmObservationList());
        }
        catch (Exception e) {
            Log.e("JSON Conversion Error", e.toString());
        }
        return json;
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

    public String getmToken() {
        return mToken;
    }

    public void setmToken(String mToken) {
        this.mToken = mToken;
    }

    public boolean ismAdmin() {
        return mAdmin;
    }

    public void setmAdmin(boolean mAdmin) {
        this.mAdmin = mAdmin;
    }
}
