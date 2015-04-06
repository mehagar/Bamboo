package com.alabama.bamboofinder;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class InteractiveMapActivity extends ActionBarActivity {

    private static final String TAG = "InteractiveMap";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ArrayList<Observation> mObservations;
    private LatLng mLastPosition;
    private HashMap<Marker, String> mMarkerIds;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interactive_map);
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.interactive_map_activity_actions, menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_add:
                // switch to ObservationDetailActivity here
                // StartActivityForResult(ObservationDetailActivity)
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        mMap.setMyLocationEnabled(true);
        mMarkerIds = new HashMap<Marker, String>();

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if(cameraPosition.target != mLastPosition) {
                    LatLngBounds curScreen = getScreenBoundingBox();
                    new GetObservationsTask().execute(curScreen);
                    mLastPosition = cameraPosition.target;
                }
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String id = mMarkerIds.get(marker);
                Log.d(TAG, "Got id : " + id);
                // Start ObservationDetailActivity here with id of observation
                return false; // default behavior: still show info window
            }
        });
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Location loc = LocationServices.FusedLocationApi.getLastLocation(
                                mGoogleApiClient);
                        if(loc != null) {
                            mLastPosition = new LatLng(loc.getLatitude(), loc.getLongitude());
                        } else {
                            mLastPosition = new LatLng(0.0, 0.0);
                        }
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastPosition, 15.0f));
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        // left unimplemented
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.d(TAG, "Location connection failed" + connectionResult.toString());
                    }
                })
                .addApi(LocationServices.API)
                .build();
    }

    private LatLngBounds getScreenBoundingBox() {
        return mMap.getProjection()
                .getVisibleRegion().latLngBounds;
    }

    private void showObservations() {
        for(Observation o : mObservations) {
            // do not add a marker if one for this observation already exists
            if(!mMarkerIds.containsValue(o.getId())) {
                Marker m = mMap.addMarker(
                        new MarkerOptions()
                                .position(o.getLocation())
                                .title(o.getId())
                                .snippet(o.getSpeciesGuess())
                                .icon(BitmapDescriptorFactory.defaultMarker(65)));
                // TODO: set the picture on the marker
                mMarkerIds.put(m, o.getId());
            }
        }
        // This commented out code is being used for testing purposes, to test the HTTP POST
        /*if(mObservations.size() > 0) {
            mObservations.get(0).setTimeStamp(new Date());
            mObservations.get(0).setDescription("This is the description.");
            ApiManager.uploadObservation(mObservations.get(0));
        }*/
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
