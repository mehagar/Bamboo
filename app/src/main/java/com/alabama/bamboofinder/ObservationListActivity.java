package com.alabama.bamboofinder;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;

import java.util.List;
import java.util.LinkedList;

/*
 * TODO:
 *
 * - This activity needs to be passed the list of the user's observations
 * - This activity needs to be
 *
 */


public class ObservationListActivity extends ActionBarActivity {

    private Integer mMaxNumberOfObservations;
    private ObservationList mSelectedObservations;  // changed from selectObservations

    private Button mEditButton;     // changed from modifyButton
    private Button mRemoveButton;
    private CheckBox mMarkObservationCheckBox;  // changed from markObservationBox

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observation_list);
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
