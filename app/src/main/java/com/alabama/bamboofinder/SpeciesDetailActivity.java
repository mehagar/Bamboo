package com.alabama.bamboofinder;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;


public class SpeciesDetailActivity extends ActionBarActivity {

    private Spinner mSpeciesSpinner;
    private Spinner mGenusSpinner;
    private Button mGenerateInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_species_detail);

        mSpeciesSpinner = (Spinner)findViewById(R.id.species_spinner);
        mGenusSpinner = (Spinner)findViewById(R.id.genus_spinner);

        ArrayAdapter<CharSequence> arrayAdapterSpecies = ArrayAdapter.createFromResource(this,
                R.array.species, android.R.layout.simple_spinner_item);

        ArrayAdapter<CharSequence> arrayAdapterGenus = ArrayAdapter.createFromResource(this,
                R.array.genus, android.R.layout.simple_spinner_item);

        arrayAdapterSpecies.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        arrayAdapterGenus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpeciesSpinner.setAdapter(arrayAdapterSpecies);
        mGenusSpinner.setAdapter(arrayAdapterGenus);

        findViewById(R.id.species_generate_detail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // generate an image based on the species and genus
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_species_detail, menu);
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
