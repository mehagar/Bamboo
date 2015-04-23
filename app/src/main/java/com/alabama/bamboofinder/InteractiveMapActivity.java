package com.alabama.bamboofinder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.collect.HashBiMap;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InteractiveMapActivity extends ActionBarActivity {
    private static final String TAG = "InteractiveMap";

    public static final int FILTER_REQUEST = 1;
    public static final int DETAIL_REQUEST = 2;
    public static final String EXTRA_SEARCH_FILTER = "search_filter";
    private static final String STATE_FILTER = "search_filter";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private List<Observation> mObservations;
    private LatLng mLastMapPosition; // The location of the current center of the map.
    private LatLng mLastUserPosition; // The physical location of the user.
    private HashBiMap<Marker, Observation> mMarkerObservationMap;
    private GoogleApiClient mGoogleApiClient;
    private SearchFilter mSearchFilter; // Might be null if search filter has not been applied or has been cleared.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interactive_map);

        if(savedInstanceState != null) {
            mSearchFilter = (SearchFilter) savedInstanceState.getSerializable(STATE_FILTER);
        }

        buildGoogleApiClient();
        setUpMapIfNeeded();

        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume called");
        super.onResume();

        if(!isNetworkAvailable()) {
            Toast.makeText(this, "Must be connected to the internet to view observations!", Toast.LENGTH_LONG).show();
        }

        if(!mGoogleApiClient.isConnected()) {
            Log.d(TAG, "Reconnected google api client");
            mGoogleApiClient.connect();
        }
        setUpMapIfNeeded();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop called");
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(STATE_FILTER, mSearchFilter);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart called");
        super.onRestart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_interactive_map, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch(item.getItemId()) {
            case R.id.action_add:
                i = new Intent(this, ObservationDetailActivity.class);
                if(mLastUserPosition != null) {
                    i.putExtra(ObservationDetailActivity.EXTRA_USER_LATITUDE, mLastUserPosition.latitude);
                    i.putExtra(ObservationDetailActivity.EXTRA_USER_LONGITUDE, mLastUserPosition.longitude);
                    startActivityForResult(i, DETAIL_REQUEST);
                } else {
                    showNoGPSAlertDialog();
                }
                return true;
            case R.id.action_filter:
                i = new Intent(this, SearchFilterActivity.class);
                if(mSearchFilter != null) {
                    i.putExtra(EXTRA_SEARCH_FILTER, mSearchFilter);
                }
                startActivityForResult(i, FILTER_REQUEST);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private void showNoGPSAlertDialog() {
        new AlertDialog.Builder(this)
                .setMessage("GPS must be enabled to add an observation")
                .setPositiveButton("Enable GPS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == FILTER_REQUEST) {
            if(resultCode == RESULT_OK) {
                mSearchFilter = (SearchFilter) data.getSerializableExtra(EXTRA_SEARCH_FILTER);
                showObservations(mSearchFilter);
            } else if(resultCode == RESULT_CANCELED) {
                mSearchFilter = null;
                showObservations(mSearchFilter);
            }
        } else if(requestCode == DETAIL_REQUEST) {
            if(resultCode == RESULT_OK) {
                mMap.clear();
                mMarkerObservationMap.clear();
                showObservations(mSearchFilter);
            }
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, new LocationCallbacks());
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
     * This is where we can add markers or lines, add listeners or move the camera.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        mMarkerObservationMap = HashBiMap.create();

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (cameraPosition.target.latitude != mLastMapPosition.latitude ||
                        cameraPosition.target.longitude != mLastMapPosition.longitude) {
                    LatLngBounds curScreen = getScreenBoundingBox();
                    new GetObservationsTask().execute(curScreen);
                    mLastMapPosition = cameraPosition.target;
                }
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Observation o = mMarkerObservationMap.get(marker);
                Intent i = new Intent(InteractiveMapActivity.this, ObservationDetailActivity.class);
                i.putExtra(ObservationDetailActivity.EXTRA_OBSERVATION, o);
                startActivity(i);
            }
        });

        mMap.setInfoWindowAdapter(new ImageInfoWindowAdapter());
    }

    private LatLngBounds getScreenBoundingBox() {
        return mMap.getProjection()
                .getVisibleRegion().latLngBounds;
    }

    // Builds and connects to the google Location Services api that can retrieve the last known location.
    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new LocationCallbacks())
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.e(TAG, "Location connection failed" + connectionResult.toString());
                    }
                })
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    // Callbacks for when the user's Location is first connected, updated, etc.
    class LocationCallbacks implements GoogleApiClient.ConnectionCallbacks,
                                                    LocationListener{
        @Override
        public void onConnected(Bundle connectionHint) {
            Location loc = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if(loc != null) {
                mLastMapPosition = new LatLng(loc.getLatitude(), loc.getLongitude());
                mLastUserPosition = new LatLng(loc.getLatitude(), loc.getLongitude());
            } else {
                mLastMapPosition = new LatLng(33.2, -87.5); // default to Tuscaloosa
                mLastUserPosition = null; // User must have gps enabled to submit observations
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastMapPosition, 15.0f));

            startLocationUpdates(this);
        }

        @Override
        public void onLocationChanged(Location location) {
            mLastUserPosition = new LatLng(location.getLatitude(),
                                            location.getLongitude());
        }

        @Override
        public void onConnectionSuspended(int cause) {
            Log.d(TAG, "onConnectionSuspended called");
        }
    }

    private void startLocationUpdates(LocationListener listener) {
        LocationRequest locationRequest = createLocationRequest();
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, locationRequest, listener);
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void showObservations(SearchFilter sf) {
        for(Observation o : mObservations) {
            // Only add a marker if it is not already show, and it meets the search criteria(if any)
            boolean meetsCriteria = (sf == null || sf.meetsCriteria(o, getUserName()));
            boolean alreadyShown = mMarkerObservationMap.containsValue(o);
            if(meetsCriteria && !alreadyShown) {
                addMarkerForObservation(o);
            } else if(!meetsCriteria && alreadyShown) {
                Marker m = mMarkerObservationMap.inverse().get(o);
                m.remove();
                mMarkerObservationMap.inverse().remove(o);
            }
        }
    }

    private String getUserName() {
        String json = getIntent().getStringExtra("user");
        try {
            User user = new User(new JSONObject(json));
            Log.d(TAG, "user name: " + user.getUsername());
            return user.getUsername();
        } catch(JSONException jse) {
            Log.e(TAG, "Could not parse user sent to map");
            return "";
        }
    }

    // Adds a marker to the google maps object for an observation.
    private void addMarkerForObservation(Observation o) {
        Marker m = mMap.addMarker(
                new MarkerOptions()
                        .position(o.getLocation())
                        .title(o.getId())
                        .snippet(o.getSpeciesGuess())
                        .icon(BitmapDescriptorFactory.defaultMarker(65)));
        mMarkerObservationMap.put(m, o);
    }

    // This task retrieves observations from the network asynchronously.
    // When it has finished, the map is updated to show any new observations.
    class GetObservationsTask extends AsyncTask<LatLngBounds, Void, ArrayList<Observation>> {
        @Override
        protected ArrayList<Observation> doInBackground(LatLngBounds... latLngBounds) {
            // this function must accept a variable number of arguments, but there should only be one.
            if(latLngBounds.length != 1) {
                Log.e(TAG, "GetObservationsTask has more than one argument");
            }
            return ApiManager.getObservationsFromNetwork(latLngBounds[0]);
        }
        @Override
        protected void onPostExecute(ArrayList<Observation> result) {
            mObservations = result;
            showObservations(mSearchFilter);
        }
    }

    class ImageInfoWindowAdapter implements InfoWindowAdapter {
        @Override
        public View getInfoContents(final Marker marker) {
            View view = getLayoutInflater().inflate(R.layout.image_info_window, null);
            Observation o = mMarkerObservationMap.get(marker);

            ImageView imageView = (ImageView)view.findViewById(R.id.thumbnail_imageView);
            if(o != null && o.getThumbnailUrl() != null) {
                Picasso.with(getApplicationContext())
                        .load(o.getThumbnailUrl())
                        .resize(50, 50)
                        .centerCrop()
                        .into(imageView, new InfoWindowRefresher(marker));
            } else if(o == null) {
                Log.e(TAG, "Marker created without an observation for it! (Or inconsistent map)");
            } else {
                Log.e(TAG, "Observation created without a picture!");
                imageView.setVisibility(View.GONE); // Remove the imageView if there is no picture for it
            }
            return view;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }
    }

    // These methods are called once the image for the info window has been loaded.
    private class InfoWindowRefresher implements Callback {
        private Marker mMarkerToRefresh;

        private InfoWindowRefresher(Marker markerToRefresh) {
            mMarkerToRefresh = markerToRefresh;
        }

        @Override
        public void onSuccess() {
            // re-show the info window when we have the image.
            // Since this is a callback, by the time it finishes, the info window will be shown, but empty.
            // So hide and show the window to execute getInfoContents() again, but this time the image will be ready.
            if(mMarkerToRefresh.isInfoWindowShown()) {
                mMarkerToRefresh.hideInfoWindow();
                mMarkerToRefresh.showInfoWindow();
            }
        }

        @Override
        public void onError() {
            Log.e(TAG, "Error loading image");
        }
    }
}
