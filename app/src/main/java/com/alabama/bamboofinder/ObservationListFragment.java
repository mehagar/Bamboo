package com.alabama.bamboofinder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Michael Walker on 4/9/2015.
 */
public class ObservationListFragment extends ListFragment {
    private static SharedPreferences prefs;

    private static final String TAG = "ObservationListFragment";
    private static final String BASE_URL = "www.inaturalist.org";
    private static final String URL_EXTRA = "extra";
    private static final int DETAIL_REQUEST = 1992;

    private static List<Observation> mObservations = null;

    ObservationAdapter observationAdapter;

    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // let the FragmentManager know that ObservationListFragment
        // needs to receive options menu callbacks

        getActivity().setTitle(R.string.title_activity_observation_list);

        progressDialog = new ProgressDialog(getActivity());
        prefs = getActivity().getSharedPreferences("com.alabama.bamboofinder", Context.MODE_PRIVATE);

        pullUserObservationList();

        if (mObservations == null) {
            mObservations = new ArrayList<Observation>();
        }

        observationAdapter = new ObservationAdapter(mObservations);
        setListAdapter(observationAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ObservationAdapter)getListAdapter()).notifyDataSetChanged(); // update the list view
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == DETAIL_REQUEST && resultCode == Activity.RESULT_OK) {
            ((ObservationAdapter)getListAdapter()).clear();
            pullUserObservationList();
            ((ObservationAdapter)getListAdapter()).addAll(mObservations);
            ((ObservationAdapter)getListAdapter()).notifyDataSetChanged();
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.d(TAG, "Activity Result was canceled");
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Observation observation = ((ObservationAdapter)getListAdapter()).getItem(position);
        Intent i = new Intent(getActivity(), ObservationDetailActivity.class);
        i.putExtra(ObservationDetailActivity.EXTRA_OBSERVATION, observation);
        startActivityForResult(i, DETAIL_REQUEST);
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
                        for (int i = observationAdapter.getCount() - 1; i >= 0; i--) {
                            if (getListView().isItemChecked(i)) {
                                String token = prefs.getString("token", "Empty Token");
                                if (!token.contentEquals("Empty Token")) {
                                    try {
                                        Uri.Builder deleteObservationsURL = new Uri.Builder();
                                        deleteObservationsURL.scheme("https")
                                                .authority(BASE_URL)
                                                .appendPath("observations")
                                                .appendPath(observationAdapter.getItem(i).getId() + ".json")
                                                .build();

                                        AsyncTask asyncTaskObservations;
                                        asyncTaskObservations = new deleteObservationFromList().execute(token, deleteObservationsURL.toString());
                                        try {
                                            asyncTaskObservations.get();
                                        }
                                        catch (Exception e) {
                                            Log.e(TAG, "Waiting error: " + e.getMessage());
                                        }
                                        mObservations.remove(observationAdapter.getItem(i));
                                    } catch (Exception e) {
                                        Log.e(TAG, e.getMessage());
                                    }
                                }
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

    // This method is used for api < 11 (Android 3.0)
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int position = info.position;
        ObservationAdapter adapter = (ObservationAdapter)getListAdapter();
        Observation observation = adapter.getItem(position);

        switch(item.getItemId()) {
            case R.id.menu_item_delete_crime:
                String token = prefs.getString("token", "Empty Token");
                if (!token.contentEquals("Empty Token")) {
                    try {
                        Uri.Builder deleteObservationsURL = new Uri.Builder();
                        deleteObservationsURL.scheme("https")
                                .authority(BASE_URL)
                                .appendPath("observations")
                                .appendPath(observation.getId())
                                .build();

                        AsyncTask asyncTaskObservations;
                        asyncTaskObservations = new deleteObservationFromList().execute(token, deleteObservationsURL.toString());
                        try {
                            asyncTaskObservations.get();
                        }
                        catch (Exception e) {
                            Log.e(TAG, "Waiting error: " + e.getMessage());
                        }
                        //mObservations.remove(observationAdapter.getItem(i));
                        mObservations.remove(observation);
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
                adapter.notifyDataSetChanged();
                return true;
        }

        return super.onContextItemSelected(item);
    }


    private void pullUserObservationList() {

        String user = prefs.getString("user", "Empty User");

        if (!user.contentEquals("Empty User")) {
            JSONObject jsonObject;
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

                AsyncTask asyncTaskObservations;
                asyncTaskObservations = new getObservationList().execute(username, getObservationsURL);
                try {
                    asyncTaskObservations.get();
                }
                catch (Exception e) {
                    Log.e(TAG, "Waiting error: " + e.getMessage());
                }
            }
            catch (Exception e) {
                Log.e(TAG, "Error converting to JSON Object");
            }
        }
    }


    /*
     *
     * ObservationAdapter Class
     *
     */


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

            ImageView iconImageView = (ImageView)convertView.findViewById(R.id.observation_list_item_icon);

            if(!observation.getThumbnailUrl().equals("")) {
                Picasso.with(getActivity())
                        .load(observation.getThumbnailUrl())
                        .resize(60, 60)
                        .centerCrop()
                        .into(iconImageView);
            } else {
                Log.e(TAG, "Observation created without a picture");
                iconImageView.setVisibility(View.GONE); // Remove the imageView if there is no picture for it
            }

            TextView titleTextView = (TextView)convertView.findViewById(R.id.observation_list_item_titleTextView);
            titleTextView.setText(observation.getSpeciesGuess());

            SimpleDateFormat format = new SimpleDateFormat("MMMM-dd-yyyy");
            TextView dateTextView = (TextView)convertView.findViewById(R.id.observation_list_item_dateTextView);
            dateTextView.setText(format.format(observation.getTimeStamp()));

            return convertView;
        }
    }

    private class getObservationList extends AsyncTask<Object, Void, Void> {
        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Loading observations...");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Object... objects) {
            try {
                String jsonObjects = ApiManager.callSendGet(objects[1].toString());
                Log.d(TAG, "Returned jsonObject: " + jsonObjects);
                mObservations = ApiManager.callJSONDataToObservations(jsonObjects);
                Log.d(TAG, "Size of observations: " + Integer.toString(mObservations.size()));
            } catch (Exception e) {
                Log.e(TAG, "Error in API call to receive observations.");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if (progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }

    private class deleteObservationFromList extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... str) {
            try {
                // token, url
                ApiManager.deleteObservation(str[1], str[0]);
            } catch (Exception e) {
                Log.e(TAG, "Error in API call to delete observation.");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {

        }
    }
}
