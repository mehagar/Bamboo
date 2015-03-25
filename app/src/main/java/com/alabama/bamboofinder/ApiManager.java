package com.alabama.bamboofinder;

import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.maps.model.LatLngBounds;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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

    private static String baseUrl = "www.inaturalist.org";

    public static ArrayList<Observation> getObservationsFromNetwork(LatLngBounds bounds) {
        ArrayList<Observation> observations = new ArrayList<Observation>();
        // TODO: call web service to find all observations within certain radius and of bamboo project
        // of mCurrentLocation, and store them in observations

        Uri.Builder builder = new Uri.Builder();
        // TODO: update values for query params with data that was passed in
        builder.scheme("http")
                .authority(baseUrl)
                .appendPath("observations.json")
                .appendQueryParameter("swlat", String.valueOf(30.0))
                .appendQueryParameter("swlng", String.valueOf(80.0))
                .appendQueryParameter("nelat", String.valueOf(33.0))
                .appendQueryParameter("nelng", String.valueOf(83.0))
                .build();

        String response;
        try {
            response = sendGet(builder.toString());
        } catch(IOException e) {
            Log.e("ApiManager", e.getMessage());
            response = "";
        }
        Log.d("ApiManager", "response was : " + response.toString());

        return observations;
    }

    private static String sendGet(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        Log.d("ApiManager", "Sending GET request to URL : " + url);
        Log.d("ApiManager", "Response code : " + responseCode);

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
