package com.alabama.bamboofinder;

import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.maps.model.LatLngBounds;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Michael H. on 3/22/2015.
 */
public class ApiManager {
    private static final String TAG = "ApiManager";

    private static final String JSON_SWLAT = "swlat";
    private static final String JSON_SWLNG = "swlng";
    private static final String JSON_NELAT = "nelat";
    private static final String JSON_NELNG = "nelng";

    private static final String baseUrl = "www.inaturalist.org";

    public static ArrayList<Observation> getObservationsFromNetwork(LatLngBounds bounds) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority(baseUrl)
                .appendPath("observations.json")
                .appendQueryParameter(JSON_SWLAT, String.valueOf(bounds.southwest.latitude))
                .appendQueryParameter(JSON_SWLNG, String.valueOf(bounds.southwest.longitude))
                .appendQueryParameter(JSON_NELAT, String.valueOf(bounds.northeast.latitude))
                .appendQueryParameter(JSON_NELNG, String.valueOf(bounds.northeast.longitude))
                .build();

        String response;
        try {
            response = sendGet(builder.toString());
        } catch(IOException e) {
            Log.e(TAG, e.getMessage());
            response = "";
        }
        ArrayList<Observation> observations = new ArrayList<Observation>();
        try {
            JSONArray observationsData = new JSONArray(response.toString());
            for (int i = 0; i < observationsData.length(); ++i) {
                JSONObject obs = observationsData.getJSONObject(i);
                observations.add(new Observation(obs));
            }
        } catch(JSONException e) {
            Log.e(TAG, "Error parsing json observations : " + e.getMessage());
        }

        return observations;
    }

    private static String sendGet(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        Log.d(TAG, "Response code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));

        StringBuffer response = new StringBuffer();
        String inputLine;
        while((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }


}
