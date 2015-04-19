package com.alabama.bamboofinder;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Michael Walker on 4/9/2015.
 */
public class ObservationListFragment extends ListFragment {

    private static final String TAG = "ObservationListFragment";
    private List<Observation> mObservations;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.title_activity_observation_list);
        mObservations = ObservationList.get(getActivity()).getObservations();

        ObservationAdapter adapter = new ObservationAdapter(mObservations);
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Observation observation = ((ObservationAdapter)getListAdapter()).getItem(position);
        Intent i = new Intent(getActivity(), ObservationDetailActivity.class);
        startActivity(i);
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
}
