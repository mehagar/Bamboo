package com.alabama.bamboofinder;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class EducationalActivity extends ActionBarActivity {

    private LinearLayout mMainLayout;

    private int[] mImages = {R.drawable.bamboo_pic_1, R.drawable.bamboo_pic_2, R.drawable.bamboo_pic_3};

    private View mCell;
    private TextView mImageText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_educational);

        mMainLayout = (LinearLayout)findViewById(R.id.image_linear_layout);

        for (int i = 0; i < mImages.length; i++) {
            mCell = getLayoutInflater().inflate(R.layout.image_horizontal_cell, null);

            final ImageView imageView = (ImageView)mCell.findViewById(R.id.image);
            imageView.setTag("Image " + (i+1));

            mImageText = (TextView)mCell.findViewById(R.id.image_cell_name);
            mImageText.setText("Image " + (i+1));

            mMainLayout.addView(mCell);
        }
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
