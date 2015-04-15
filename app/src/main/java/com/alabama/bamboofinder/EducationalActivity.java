package com.alabama.bamboofinder;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

public class EducationalActivity extends ActionBarActivity {

    private ViewFlipper mViewFlipper;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_educational);

        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher);

        mContext = this;
        mViewFlipper = (ViewFlipper)this.findViewById(R.id.educational_view_flipper);

        mViewFlipper.setInAnimation(this, android.R.anim.slide_in_left);
        mViewFlipper.setOutAnimation(this, android.R.anim.slide_out_right);

        mViewFlipper.setAutoStart(true);
        mViewFlipper.setFlipInterval(4000); // 4 seconds
        mViewFlipper.startFlipping();

        findViewById(R.id.species_detail_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(EducationalActivity.this, SpeciesDetailActivity.class);
                startActivity(i);
            }
        });
    }
}
