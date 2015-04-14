package com.alabama.bamboofinder;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InteractiveMapActivity extends ActionBarActivity {
    private static final String TAG = "InteractiveMap";

    public static final int FILTER_REQUEST = 1;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private List<Observation> mObservations;
    private LatLng mLastMapPosition; // The location of the current center of the map.
    private LatLng mLastUserPosition; // The physical location of the user.
    private Map<Marker, Observation> mMarkerObservationMap;
    private GoogleApiClient mGoogleApiClient;
    private SearchFilter mSearchFilter; // Might be null if search filter has not been applied or has been cleared.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interactive_map);
        buildGoogleApiClient();
        setUpMapIfNeeded();

        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher);

        try {
            Log.d(TAG, "string got from main activity was: " + getIntent().getStringExtra("user"));
            User user = new User(new JSONObject(getIntent().getStringExtra("user")));
            // testing HTTP Post
            Observation o = new Observation();
            user.setmToken(getIntent().getStringExtra("token"));
            new PostObservationsTask().execute(o, user, "");
        } catch(JSONException e) {
            Log.e(TAG, "Could not create user from intent: " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume called");
        super.onResume();

        if(!mGoogleApiClient.isConnected()) {
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
                i.putExtra(ObservationDetailActivity.EXTRA_USER_LATITUDE, mLastUserPosition.latitude);
                i.putExtra(ObservationDetailActivity.EXTRA_USER_LONGITUDE, mLastUserPosition.longitude);
                startActivity(i);
                return true;
            case R.id.action_filter:
                i = new Intent(this, SearchFilterActivity.class);
                startActivityForResult(i, FILTER_REQUEST);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "In onActivityResult");
        if(requestCode == FILTER_REQUEST) {
            if(resultCode == RESULT_OK) {
                // apply search filter here
                // mSearchFilter = (SearchFilter) data.getSerializableExtra(EXTRA_SEARCH_FILTER);
                // showObservations(mSearchFilter)
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
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        mMarkerObservationMap = new HashMap<Marker, Observation>();

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if(cameraPosition.target != mLastMapPosition) {
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

    // Builds and connects to the google Location Services api that can retrieve the last known location.
    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new LocationCallbacks())
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.d(TAG, "Location connection failed" + connectionResult.toString());
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
            Log.d(TAG, "onLocationChanged called");
        }

        @Override
        public void onConnectionSuspended(int cause) {
            // left unimplemented
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

    private LatLngBounds getScreenBoundingBox() {
        return mMap.getProjection()
                .getVisibleRegion().latLngBounds;
    }

    private void showObservations(SearchFilter sf) {
        if(sf != null) {
            mMap.clear();
        }
        for(Observation o : mObservations) {
            // Only add a marker if it is not already show, and it meets the search criteria(if any)
            boolean meetsCriteria = sf == null || sf.meetsCriteria(mLastMapPosition, o);
            boolean alreadyShown = mMarkerObservationMap.containsValue(o);
            if(meetsCriteria && !alreadyShown) {
                addMarkerForObservation(o);
            }
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
        protected ArrayList<Observation> doInBackground(LatLngBounds... latLngBounds) {
            // this function must accept a variable number of arguments, but there should only be one.
            if(latLngBounds.length != 1) {
                Log.e(TAG, "GetObservationsTask has more than one argument");
            }
            return ApiManager.getObservationsFromNetwork(latLngBounds[0]);
        }

        protected void onPostExecute(ArrayList<Observation> result) {
            mObservations = result;
            showObservations(mSearchFilter);
        }
    }

    class PostObservationsTask extends AsyncTask<Object, Void, Void> {
        protected Void doInBackground(Object... objects) {
            // observation, user, photo file name
            Log.d(TAG, "in doInBackground");
            ApiManager.uploadObservation((Observation)objects[0], (User)objects[1], (String)objects[2]);
            return null;
        }
    }

    class ImageInfoWindowAdapter implements InfoWindowAdapter {
        @Override
        public View getInfoContents(final Marker marker) {
            View view = getLayoutInflater().inflate(R.layout.image_info_window, null);
            Observation o = mMarkerObservationMap.get(marker);

            ImageView imageView = (ImageView)view.findViewById(R.id.thumbnail_imageView);
            if(!o.getThumbnailUrl().equals("")) {
                Picasso.with(getApplicationContext())
                        .load(o.getThumbnailUrl())
                        .resize(50, 50)
                        .centerCrop()
                        .into(imageView, new InfoWindowRefresher(marker));
            } else {
                Log.e(TAG, "Observation created without a picture");
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
