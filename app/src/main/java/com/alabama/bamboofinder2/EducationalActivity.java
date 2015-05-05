package com.alabama.bamboofinder2;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class EducationalActivity extends ActionBarActivity {

    TextSwitcher textSwitcher_title;
    TextSwitcher textSwitcher_description;

    Animation slide_in_left, slide_out_right;

    String[] TextToSwitched = { "What About Bamboo?", "Bamboo grows fast!", "And it's strong!",
            "It's yummy!", "Fights Erosion!", "Find out more!" };
    int curIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_educational);

        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher);

        this.findViewById(android.R.id.content).setBackgroundColor(0xB6C0D2);

        textSwitcher_title = (TextSwitcher) findViewById(R.id.educational_textswitcher_title);
        textSwitcher_description = (TextSwitcher) findViewById(R.id.educational_textswitcher_description);

        slide_in_left = AnimationUtils.loadAnimation(this,
                android.R.anim.slide_in_left);
        slide_out_right = AnimationUtils.loadAnimation(this,
                android.R.anim.slide_out_right);

        textSwitcher_title.setInAnimation(slide_in_left);
        textSwitcher_title.setOutAnimation(slide_out_right);

        textSwitcher_description.setInAnimation(slide_in_left);
        textSwitcher_description.setOutAnimation(slide_out_right);

        textSwitcher_title.setFactory(new ViewSwitcher.ViewFactory(){

        @Override
        public View makeView() {
        TextView textView = new TextView(EducationalActivity.this);
            textView.setTextSize(50);
            textView.setTextColor(Color.parseColor("#F1F8E9"));
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.setShadowLayer(10, 10, 10, Color.BLACK);
            return textView;
        }});

        textSwitcher_description.setFactory(new ViewSwitcher.ViewFactory(){

            @Override
            public View makeView() {
                TextView textView = new TextView(EducationalActivity.this);
                textView.setTextSize(25);
                textView.setTextColor(Color.parseColor("#F1F8E9"));
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                textView.setTypeface(Typeface.DEFAULT_BOLD);
                textView.setShadowLayer(10, 10, 10, Color.BLACK);
                return textView;
            }});

        curIndex = 0;
        Resources res = getResources();

        textSwitcher_title.setText(TextToSwitched[curIndex]);
        textSwitcher_description.setText(res.getString(R.string.educational_info_text_0));

        findViewById(R.id.educational_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Resources res = getResources();

                if (curIndex == TextToSwitched.length - 1) {
                    curIndex = 0;
                    textSwitcher_title.setText(TextToSwitched[curIndex]);
                } else {
                    textSwitcher_title.setText(TextToSwitched[++curIndex]);
                }

                switch (curIndex) {
                    case 0:
                        textSwitcher_description.setText(res.getString(R.string.educational_info_text_0));
                        break;
                    case 1:
                        textSwitcher_description.setText(res.getString(R.string.educational_info_text_1));
                        break;
                    case 2:
                        textSwitcher_description.setText(res.getString(R.string.educational_info_text_2));
                        break;
                    case 3:
                        textSwitcher_description.setText(res.getString(R.string.educational_info_text_3));
                        break;
                    case 4:
                        textSwitcher_description.setText(res.getString(R.string.educational_info_text_4));
                        break;
                    case 5:
                        textSwitcher_description.setText(res.getString(R.string.educational_info_text_5));
                        break;
                }
            }
        });
    }
}