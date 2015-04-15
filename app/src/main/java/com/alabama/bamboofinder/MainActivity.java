package com.alabama.bamboofinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

public class MainActivity extends ActionBarActivity {
    private static final String TAG = "MainActivity";

    private static final int LOGIN_REQUEST_CODE = 57;

    private static User mUser;
    private Button mMainLoginButton;
    private Button mEducationButton;
    private Button mMapButton;
    private Button mObservationsButton;
    private TextView mLoggedInText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mObservationsButton = (Button) findViewById(R.id.MyObservationsButton);
        mMapButton = (Button) findViewById(R.id.ViewMapButton);
        mEducationButton = (Button) findViewById(R.id.LearnButton);
        mMainLoginButton = (Button) findViewById(R.id.MainLoginButton);
        mLoggedInText = (TextView) findViewById(R.id.loggedInText);
        mUser = new User();

        mObservationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,ObservationListActivity.class);
                i.putExtra("user", mUser.convertToJSON().toString());
                Log.i(TAG, i.getStringExtra("user"));
                startActivity(i);
            }
        });

        mMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, InteractiveMapActivity.class);
                i.putExtra("user", mUser.convertToJSON().toString());
                i.putExtra("token", getSharedPreferences("com.alabama.bamboofinder", Context.MODE_PRIVATE).getString("token", "Empty Token")); // just testing...
                Log.d(TAG, mUser.convertToJSON().toString());
                startActivity(i);
            }
        });

        mEducationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, EducationalActivity.class);
                startActivity(i);
            }
        });

        mMainLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(i, LOGIN_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {

            SharedPreferences prefs = this.getSharedPreferences(
                    "com.alabama.bamboofinder", Context.MODE_PRIVATE);
            String token = prefs.getString("token", "Empty Token");

            AsyncTask userTask = new User().execute(token);
            try {
                mUser = (User) userTask.get();
            }
            catch (Exception e) {
                Log.e(TAG, "Failed to get user");
            }
            mLoggedInText.setText("Welcome, " + mUser.getmUsername() + "!");
        }
    }
}
