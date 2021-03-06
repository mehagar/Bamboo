package com.alabama.bamboofinder2;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;


public class SearchFilterActivity extends ActionBarActivity {
    private static final String TAG = "SearchFilter";

    CheckBox datePickerCheckBox;
    CheckBox ownObservationsCheckBox;
    DatePicker datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_filter);

        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher);

        datePickerCheckBox = (CheckBox)findViewById(R.id.added_after_checkbox);
        datePicker = (DatePicker)findViewById(R.id.datePicker);
        ownObservationsCheckBox = (CheckBox)findViewById(R.id.own_observations_checkbox);

        Button okButton = (Button)findViewById(R.id.sf_ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!datePickerCheckBox.isChecked() && !ownObservationsCheckBox.isChecked()) {
                    setResult(RESULT_CANCELED);
                    finish();
                }
                Date selectedDate = new Date(datePicker.getCalendarView().getDate());
                // create the search filter
                SearchFilter searchFilter = new SearchFilter(
                        ownObservationsCheckBox.isChecked(),
                        datePickerCheckBox.isChecked(),
                        selectedDate);
                Intent resultIntent = new Intent();
                resultIntent.putExtra(InteractiveMapActivity.EXTRA_SEARCH_FILTER, searchFilter);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        Button cancelButton = (Button)findViewById(R.id.sf_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        // check bundle for existing search filter
        if(getIntent().hasExtra(InteractiveMapActivity.EXTRA_SEARCH_FILTER)) {
            SearchFilter sf = (SearchFilter) getIntent().getSerializableExtra(InteractiveMapActivity.EXTRA_SEARCH_FILTER);
            datePickerCheckBox.setChecked(sf.isMustBeBefore());
            ownObservationsCheckBox.setChecked(sf.isOnlyOwnObservations());
            setDatePickerDate(sf.getEarliestDate());
        }
    }

    void setDatePickerDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }
}
