package com.alabama.bamboofinder;

import android.content.res.AssetManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class SpeciesDetailActivity extends ActionBarActivity {

    ArrayList<ArrayList<String>> species;

    private Spinner mSpeciesSpinner;
    private Spinner mGenusSpinner;
    private Button mGenerateInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_species_detail);

        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher);

        /*
         * The species ArrayList (of type ArrayList) holds an ArrayList (of type String)
         * Whenever a new species is read from the text file (denoted by a semicolon), the
         * species list will allocate an ArrayList (of type String).
         *
         *  Example:
         *
         *  Species [[Species 1]            [Species 2]]           [Species 3]
         *        "Species 1 Genus 1",  "Species 2 Genus 1",   "Species 3 Genus 1"
         *        "Species 1 Genus 2",  "Species 2 Genus 2",   "Species 3 Genus 2"
         */

        species = new ArrayList<>();  // array list of species
        species.add(new ArrayList<String>());
        readBambooSpeciesList();

        mSpeciesSpinner = (Spinner)findViewById(R.id.species_spinner);
        mGenusSpinner = (Spinner)findViewById(R.id.genus_spinner);

        ArrayList<String> list = new ArrayList<>();
        for (ArrayList<String> a : species) {
            for (String s : a) list.add(s);
        }

        ArrayAdapter<String> arrayAdapterSpecies = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, list.toArray(new String[list.size()]));

        arrayAdapterSpecies.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //arrayAdapterGenus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpeciesSpinner.setAdapter(arrayAdapterSpecies);
        //mGenusSpinner.setAdapter(arrayAdapterGenus);

        findViewById(R.id.species_generate_detail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // generate an image based on the species and genus
            }
        });
    }

    public void readBambooSpeciesList() {
        try {
            AssetManager assetManager = getAssets();
            InputStream inputStream = getResources().openRawResource(R.raw.species);//assetManager.open("species.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;

            while ((line = br.readLine()) != null) {
                /*
                 * When a semicolon is read, add another species to the array list
                 */
                if (line.equals(";")) {
                    species.add(new ArrayList<String>());
                } else {
                    species.get(species.size()-1).add(line); // add genus to the current list
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
