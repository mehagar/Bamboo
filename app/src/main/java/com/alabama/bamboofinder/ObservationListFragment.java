package com.alabama.bamboofinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Michael Walker on 4/9/2015.
 */
public class ObservationListFragment extends ListFragment {

    private static final String TAG = "ObservationListFragment";
    private static List<Observation> mObservations = new ArrayList<Observation>();
    private static final String BASE_URL = "www.inaturalist.org";


    // 1. Shared preferences (
    // 2. Get users string from preferences

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // let the FragmentManager know that ObservationListFragment
        // needs to receive options menu callbacks

        getActivity().setTitle(R.string.title_activity_observation_list);

        SharedPreferences prefs = getActivity().getSharedPreferences("com.alabama.bamboofinder", Context.MODE_PRIVATE);
        String user = prefs.getString("user", "Empty User");

        if (!user.contentEquals("Empty User")) {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(user);
                String username = jsonObject.getString("login") + ".json";

                Uri.Builder getObservationsURL = new Uri.Builder();
                getObservationsURL.scheme("https")
                        .authority(BASE_URL)
                        .appendPath("observations")
                        .appendPath(username)
                        .build();

                new getObservationList().execute(username, getObservationsURL);
            }
            catch (Exception e) {
                Log.e(TAG, "Error converting to JSON Object");
            }
        }

        //mObservations = ObservationList.get(getActivity()).getObservations();

        ObservationAdapter adapter = new ObservationAdapter(mObservations);
        setListAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ObservationAdapter)getListAdapter()).notifyDataSetChanged(); // update the list view
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Observation observation = ((ObservationAdapter)getListAdapter()).getItem(position);
        Intent i = new Intent(getActivity(), ObservationDetailActivity.class);
        i.putExtra(ObservationDetailActivity.EXTRA_OBSERVATION, observation);
        // needs an extra passed
        startActivity(i);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_fragment_observation_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_item_new_observation:
                Observation observation = new Observation();
                ObservationList.get(getActivity()).addObservation(observation);
                Intent i = new Intent(getActivity(), InteractiveMapActivity.class);
                //i.putExtra(CrimeFragment.EXTRA_CRIME_ID, crime.getId());
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class ObservationAdapter extends ArrayAdapter<Observation> {
        public ObservationAdapter(List<Observation> observations) {
            super(getActivity(), 0, observations);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // if we weren't given a view, inflate one
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_observation, null);
            }

            // configure the view for this crime
            Observation observation = getItem(position);

            TextView titleTextView = (TextView)convertView.findViewById(R.id.observation_list_item_titleTextView);
            titleTextView.setText(observation.getSpeciesGuess());

            TextView dateTextView = (TextView)convertView.findViewById(R.id.observation_list_item_dateTextView);
            dateTextView.setText(observation.getTimeStamp().toString());

            CheckBox selectedObservationCheckBox = (CheckBox)convertView.findViewById(R.id.observation_list_item_solvedCheckBox);
            //solvedCheckBox.setChecked(.isSolved());

            return convertView;
        }
    }

    private class getObservationList extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... objects) {
            try {
                // need to update because of error
                String jsonObjects = ApiManager.callSendGet(objects[1].toString());
                Log.d(TAG, jsonObjects);
                mObservations = ApiManager.callJSONDataToObservations(jsonObjects);
            } catch (Exception e) {
                Log.e(TAG, "Error in API call to receive observations.");
            }
            return null;
        }
    }
}
