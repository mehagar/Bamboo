package com.alabama.bamboofinder;
import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
//import android.widget.CheckBox;
import android.widget.AdapterView.OnItemClickListener;

import java.util.List;
import java.util.LinkedList;

/*
 * TODO:
 *
 * - This activity needs to be passed the list of the user's observations
 * - This activity needs to present a list view of observations instead of strings
 *
 */


public class ObservationListActivity extends Activity {

    private Integer mMaxNumberOfObservations;

    //private ObservationList mObservations;
    private List<String> mObservations;

    private ObservationList mSelectedObservations;  // changed from selectObservations

    private Button mEditButton;     // changed from modifyButton
    private Button mRemoveButton;
    //private CheckBox mMarkObservationCheckBox;  // changed from markObservationBox

    //private ArrayAdapter<Observation> mArrayAdapter;
    private ArrayAdapter<String> mArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observation_list);

        // Setup the list view and array adapter for obtaining data from observation list
        //mObservations = new ObservationList();
        mObservations = new LinkedList<String>();
        mObservations.add("Observation 1");
        mObservations.add("Observation 2");
        mObservations.add("Observation 3");


        ListView listView = (ListView) findViewById(R.id.observation_list);
        //mArrayAdapter = new ArrayAdapter<Observation>(this,
        //        android.R.layout.simple_list_item_1, mObservations.getObservationList());

        mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mObservations);
        listView.setAdapter(mArrayAdapter);

        // Selecting a list element
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Start ObservationDetailActivity
                Intent i = new Intent(getAct)
            }
        });
    }

        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_observation_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
