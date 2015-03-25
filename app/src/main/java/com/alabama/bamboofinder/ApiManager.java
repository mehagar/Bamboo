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

import java.util.ArrayList;

/**
 * Created by Michael H. on 3/22/2015.
 */
public class ApiManager {

    private static String baseUrl = "www.inaturalist.org/observations.json";

    public ArrayList<Observation> getObservationsFromNetwork(LatLngBounds bounds) {
        ArrayList<Observation> observations = new ArrayList<Observation>();
        // TODO: call web service to find all observations within certain radius and of bamboo project
        // of mCurrentLocation, and store them in observations



        Uri.Builder builder = new Uri.Builder();
        // TODO: update values for query params with data that was passed in
        builder.scheme("http")
                .authority(baseUrl)
                .appendPath("observations.json")
                .appendQueryParameter("swlat", String.valueOf(0.0))
                .appendQueryParameter("swlng", String.valueOf(0.0))
                .appendQueryParameter("nelat", String.valueOf(0.0))
                .appendQueryParameter("nelng", String.valueOf(0.0))
                .build();

        //Log.d("ApiManager", "response was : " + response.toString());
        return observations;
    }
}
