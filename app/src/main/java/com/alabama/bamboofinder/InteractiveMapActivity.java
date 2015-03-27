package com.alabama.bamboofinder;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class InteractiveMapActivity extends ActionBarActivity {

    private static final String TAG = "InteractiveMap";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ArrayList<Observation> mObservations;
    private LatLng mLastPosition;
    private HashMap<String, Marker> mMarkerIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interactive_map);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        // TODO: get the user's position and make it the starting point
        mMap.setMyLocationEnabled(true);
        mMarkerIds = new HashMap<String, Marker>();

        mLastPosition = new LatLng(33.2, -87.5); // Tuscaloosa
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastPosition, 14.0f));

        LatLngBounds curScreen = getScreenBoundingBox();
        new GetObservationsTask().execute(curScreen);

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                Log.d(TAG, "Camera was changed." + " Latitude : " + cameraPosition.target.latitude +
                        " Longitude : " + cameraPosition.target.longitude);
                if(cameraPosition.target != mLastPosition) {
                    LatLngBounds curScreen = getScreenBoundingBox();
                    new GetObservationsTask().execute(curScreen);
                    mLastPosition = cameraPosition.target;
                }
            }
        });

        // TODO: set other listener methods for when a marker is clicked

    }

    private LatLngBounds getScreenBoundingBox() {
        return mMap.getProjection()
                .getVisibleRegion().latLngBounds;
    }

    private void showObservations() {
        for(Observation o : mObservations) {
            // do not add a marker if one for this observation already exists
            if(!mMarkerIds.containsKey(o.getId())) {
                Marker m = mMap.addMarker(new MarkerOptions().position(o.getLocation()).title(o.getId()));
                mMarkerIds.put(o.getId(), m);
            }
        }
    }

    private ArrayList<Observation> getFilteredObservations(SearchFilter sf) {
        ArrayList<Observation> filteredObservations = new ArrayList<Observation>();
        for(Observation observation : mObservations) {
            if(sf.meetsCriteria(observation)) {
                filteredObservations.add(observation);
            }
        }
        return filteredObservations;
    }

    class GetObservationsTask extends AsyncTask<LatLngBounds, Void, ArrayList<Observation>> {
        protected ArrayList<Observation> doInBackground(LatLngBounds... latLngBounds) {
            // this function must accept a variable number of arguments, but there should only be one.
            if(latLngBounds.length != 1) {
                Log.e(TAG, "GetObservationsTask has more than one argument");
            }
            return ApiManager.getObservationsFromNetwork(latLngBounds[0]);
        }

        protected void onPostExecute(ArrayList<Observation> result) {
            mObservations = result;
            showObservations();
        }
    }
}
