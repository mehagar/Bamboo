package com.alabama.bamboofinder;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class EducationalActivity extends ActionBarActivity {

    private LinearLayout mMainLayout;

    private int[] mImages = {R.drawable.bamboo_pic_1, R.drawable.bamboo_pic_2, R.drawable.bamboo_pic_3};

    private View mCell;

    private Button mSpeciesDetailButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_educational);

        mMainLayout = (LinearLayout)findViewById(R.id.image_linear_layout);

        for (int i = 0; i < mImages.length; i++) {
            mCell = getLayoutInflater().inflate(R.layout.image_horizontal_cell, null);

            final ImageView imageView = (ImageView)mCell.findViewById(R.id.image);
            imageView.setTag("Image " + (i+1));
            imageView.setImageResource(mImages[i]);

            mMainLayout.addView(mCell);
        }

        // Start species detail activity
        mSpeciesDetailButton = (Button)findViewById(R.id.species_detail_button);
        mSpeciesDetailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(EducationalActivity.this, SpeciesDetailActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_educational, menu);
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
