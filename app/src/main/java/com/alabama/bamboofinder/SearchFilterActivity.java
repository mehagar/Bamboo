package com.alabama.bamboofinder;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import java.util.Date;


public class SearchFilterActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_filter);

        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher);

        final DatePicker datePicker = (DatePicker)findViewById(R.id.datePicker);

        Button okButton = (Button)findViewById(R.id.sf_ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date selectedDate = new Date(datePicker.getCalendarView().getDate());
                // create the search filter
                SearchFilter searchFilter = new SearchFilter(selectedDate);
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
    }
}
