package com.alabama.bamboofinder;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.maps.model.LatLngBounds;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Michael H. on 3/22/2015.
 */
public class ApiManager {
    private static final String TAG = "ApiManager";

    private static final String URL_SWLAT = "swlat";
    private static final String URL_SWLNG = "swlng";
    private static final String URL_NELAT = "nelat";
    private static final String URL_NELNG = "nelng";
    private static final String URL_EXTRA = "extra";
    private static final String URL_LATITUDE = "observation[latitude]";
    private static final String URL_LONGITUDE = "observation[longitude]";
    private static final String URL_DATE = "observation[observed_on_string]";
    private static final String URL_DESCRIPTION = "observation[description]";
    private static final String JSON_PROJECT = "project_observations";
    private static final String JSON_PROJECT_ID = "project_id";

    private static final int PROJECT_ID = 3846; // The id of the BambooFinder project on iNaturalist.org

    private static final String BASE_URL = "www.inaturalist.org";

    /* Gets observations from iNaturalist's api within a specified range */
    public static ArrayList<Observation> getObservationsFromNetwork(LatLngBounds bounds) {
        Uri.Builder builder = new Uri.Builder();
        // appendQueryParameter() encodes values
        builder.scheme("https")
                .authority(BASE_URL)
                .appendPath("observations.json")
                .appendQueryParameter(URL_SWLAT, String.valueOf(bounds.southwest.latitude))
                .appendQueryParameter(URL_SWLNG, String.valueOf(bounds.southwest.longitude))
                .appendQueryParameter(URL_NELAT, String.valueOf(bounds.northeast.latitude))
                .appendQueryParameter(URL_NELNG, String.valueOf(bounds.northeast.longitude))
                .appendQueryParameter(URL_EXTRA, "projects,observation_photos")
                .build();
        String response;
        try {
            response = sendGet(builder.toString());
        } catch(IOException e) {
            Log.e(TAG, "HTTP GET Failed: " + e.getMessage());
            response = "";
        }

        return JSONDataToObservations(response);
    }

    /* Uploads one observation to iNaturalist */
    public static void uploadObservation(Observation o, String photoFileName) throws IOException {
        Uri.Builder baseBuilder = new Uri.Builder();
        baseBuilder.scheme("https")
                .authority(BASE_URL)
                .appendPath("observations.json")
                .build();

        Uri.Builder paramsBuilder = new Uri.Builder();
        paramsBuilder.appendQueryParameter(URL_LATITUDE, String.valueOf(o.getLocation().latitude))
                .appendQueryParameter(URL_LONGITUDE, String.valueOf(o.getLocation().longitude))
                .appendQueryParameter(URL_DATE, o.getTimeStamp().toString())
                .appendQueryParameter(URL_DESCRIPTION, o.getDescription())
                .build();
        Log.d(TAG, "Base URL: " + baseBuilder.toString());
        Log.d(TAG, "Params URL: " + paramsBuilder.toString());

        try {
            sendPost(baseBuilder.toString(), paramsBuilder.toString());
        } catch(IOException e) {
            Log.e(TAG, "HTTP POST Failed: " + e.getMessage());
        }

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("image", new File(photoFileName));

        Uri.Builder photoBuilder = new Uri.Builder();
        photoBuilder.scheme("https")
                .authority(BASE_URL)
                .appendPath("observation_photos")
                .build();

        client.post(photoBuilder.toString(), params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d(TAG, "Photo uploaded successfully");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d(TAG, "Failed to upload photo");
            }
        });
    }

    /* Converts an JSON string to a list of observations */
    private static ArrayList<Observation> JSONDataToObservations(String data) {
        ArrayList<Observation> observations = new ArrayList<Observation>();
        try {
            JSONArray observationsData = new JSONArray(data);
            for (int i = 0; i < observationsData.length(); ++i) {
                JSONObject obs = observationsData.getJSONObject(i);
                JSONArray projects = obs.getJSONArray(JSON_PROJECT);
                for(int j = 0; j < projects.length(); ++j) {
                    int id = projects.getJSONObject(j).getInt(JSON_PROJECT_ID);
                    if(id == PROJECT_ID) {
                        observations.add(new Observation(obs));
                    }
                }
            }
        } catch(JSONException e) {
            Log.e(TAG, "Error parsing json observations : " + e.getMessage());
        }
        return observations;
    }

    private static String sendGet(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } finally {
            connection.disconnect();
        }
    }

    private static void sendPost(String baseUrl, String params) throws IOException {
        // TODO: need to authenticate with token
        // e.g. String token = user.getToken();
        URL url = new URL(baseUrl);

        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        try {
            // setDoOutput sets POST as method
            con.setDoOutput(true);
            con.setFixedLengthStreamingMode(params.getBytes().length);

            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(con.getOutputStream(), "UTF-8"));
            out.write(params);
            out.close();
        } finally {
            con.disconnect();
        }
    }
}
