package com.alabama.bamboofinder;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

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
    private String mToken;

    public User() {
        mCreationDate = null;
        mDescription = null;
        mEmail = null;
        mID = -1;
        mUsername = null;
        mName = null;
        mObservationsCount = -1;
        mUri = null;
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

        return new User(object);
    }

    public JSONObject convertToJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put(JSON_CREATED, getCreationDate());
            json.put(JSON_DESCRIPTION, getDescription());
            json.put(JSON_EMAIL, getEmail());
            json.put(JSON_ID, getID());
            json.put(JSON_LOGIN, getUsername());
            json.put(JSON_NAME, getName());
            json.put(JSON_OBSERVATIONS_COUNT, getObservationsCount());
            json.put(JSON_URI, getUri());
        }
        catch (Exception e) {
            Log.e("JSON Conversion Error", e.toString());
        }
        return json;
    }

    // Getters and Setters
    public String getCreationDate() {
        return mCreationDate;
    }

    public void setCreationDate(String mCreationDate) {
        this.mCreationDate = mCreationDate;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public void setEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public String getEmail() {
        return mEmail;
    }

    public int getID() {
        return mID;
    }

    public void setID(int mID) {
        this.mID = mID;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String mUsername) {
        this.mUsername = mUsername;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public int getObservationsCount() {
        return mObservationsCount;
    }

    public void setObservationsCount(int mObservationsCount) {
        this.mObservationsCount = mObservationsCount;
    }

    public String getUri() {
        return mUri;
    }

    public void setUri(String mUri) {
        this.mUri = mUri;
    }

    public String getToken() {
        return mToken;
    }

    public void setToken(String mToken) {
        this.mToken = mToken;
    }
}
