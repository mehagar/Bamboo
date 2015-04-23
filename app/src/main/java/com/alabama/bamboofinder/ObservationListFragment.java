package com.alabama.bamboofinder;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
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
    private static final String BASE_URL = "www.inaturalist.org";
    private static final String URL_EXTRA = "extra";

    private static List<Observation> mObservations = null;

    //private ProgressDialog progressDialog = new ProgressDialog(getActivity());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // let the FragmentManager know that ObservationListFragment
        // needs to receive options menu callbacks

        getActivity().setTitle(R.string.title_activity_observation_list);

        SharedPreferences prefs = getActivity().getSharedPreferences("com.alabama.bamboofinder", Context.MODE_PRIVATE);
        String user = prefs.getString("user", "Empty User");

        AsyncTask asyncTaskObservations;

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
                        .appendQueryParameter(URL_EXTRA, "projects,observation_photos")
                        .build();

                asyncTaskObservations = new getObservationList().execute(username, getObservationsURL);
                try {
                    asyncTaskObservations.get();
                }
                catch (Exception e) {
                    Log.e(TAG, "Waiting error: " + e.toString());
                }
            }
            catch (Exception e) {
                Log.e(TAG, "Error converting to JSON Object");
            }
        }

        if (mObservations == null || mObservations.size() == 0) {
            mObservations = new ArrayList<Observation>();
            mObservations.add(new Observation());
        }

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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, parent, savedInstanceState);

        ListView listView = (ListView)v.findViewById(android.R.id.list);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // Use floating context menu on Froyo and Gingerbread
            registerForContextMenu(listView);
        } else {
            // Use contextual action bar on HoneyComb and higher
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                    // Required, but not used in this implementation
                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.menu_observation_list_item_context, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    // Required, but not used in this implementation
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch(item.getItemId()) {
                        case R.id.menu_item_delete_crime:
                            ObservationAdapter observationAdapter = (ObservationAdapter)getListAdapter();
                            //CrimeLab crimeLab = CrimeLab.get(getActivity());
                            for (int i = observationAdapter.getCount() - 1; i >= 0; i--) {
                                if (getListView().isItemChecked(i)) {
                                    mObservations.remove(observationAdapter.getItem(i));
                                }
                            }
                            mode.finish();
                            observationAdapter.notifyDataSetChanged();
                            return true;
                        default:
                            return false;
                    }
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    // Required, but not used in this implementation
                }
            });
        }
        return v;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.menu_observation_list_item_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int position = info.position;
        ObservationAdapter adapter = (ObservationAdapter)getListAdapter();
        Observation observation = adapter.getItem(position);

        switch(item.getItemId()) {
            case R.id.menu_item_delete_crime:
                //CrimeLab.get(getActivity()).deleteCrime(crime);
                mObservations.remove(observation);
                adapter.notifyDataSetChanged();
                return true;
        }

        return super.onContextItemSelected(item);
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

            Observation observation = getItem(position);

            TextView titleTextView = (TextView)convertView.findViewById(R.id.observation_list_item_titleTextView);
            titleTextView.setText(observation.getSpeciesGuess());

            TextView dateTextView = (TextView)convertView.findViewById(R.id.observation_list_item_dateTextView);
            dateTextView.setText(observation.getTimeStamp().toString());

            return convertView;
        }
    }

    private class getObservationList extends AsyncTask<Object, Void, Void> {
        @Override
        protected void onPreExecute() {
            //progressDialog.setMessage("Loading observations...");
            //progressDialog.show();
        }

        @Override
        protected Void doInBackground(Object... objects) {
            try {
                // need to update because of error
                String jsonObjects = ApiManager.callSendGet(objects[1].toString());
                Log.d(TAG, jsonObjects);
                mObservations = ApiManager.callJSONDataToObservations(jsonObjects);
                Log.d(TAG, Integer.toString(mObservations.size()));
            } catch (Exception e) {
                Log.e(TAG, "Error in API call to receive observations.");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            //if (progressDialog.isShowing())
            //    progressDialog.dismiss();
        }
    }
}
