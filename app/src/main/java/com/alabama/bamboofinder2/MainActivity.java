package com.alabama.bamboofinder2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

public class MainActivity extends ActionBarActivity {
    private static final String TAG = "MainActivity";

    private static final int LOGIN_REQUEST_CODE = 57;

    private static User mUser;
    private ImageButton mMyObservationsButton;
    private ImageButton mLearnMoreButton;
    private ImageButton mViewMapButton;
    private TextView mLoggedInText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoggedInText = (TextView) findViewById(R.id.loggedInText);
        mMyObservationsButton = (ImageButton) findViewById(R.id.MyObservationsIButton);
        Resources res = this.getResources();
        Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.my_observations);
        bitmap = Bitmap.createScaledBitmap(bitmap, 640, 480, true);
        mMyObservationsButton.setImageBitmap(bitmap);

        mLearnMoreButton = (ImageButton) findViewById(R.id.LearnMoreIButton);
        bitmap = BitmapFactory.decodeResource(res, R.drawable.learn_more);
        bitmap = Bitmap.createScaledBitmap(bitmap, 640, 480, true);
        mLearnMoreButton.setImageBitmap(bitmap);

        mViewMapButton = (ImageButton) findViewById(R.id.ViewMapIButton);
        bitmap = BitmapFactory.decodeResource(res, R.drawable.view_map);
        bitmap = Bitmap.createScaledBitmap(bitmap, 500, 480, true);
        mViewMapButton.setImageBitmap(bitmap);

        mUser = new User();

        SharedPreferences prefs = this.getSharedPreferences(
                "com.alabama.bamboofinder", Context.MODE_PRIVATE);
        String token = prefs.getString("token", "Empty Token");
        if(!isNetworkAvailable()) {
            Toast toast = Toast.makeText(this, "No internet connection",
                    Toast.LENGTH_LONG);
            toast.show();
        }
        else if(!token.contentEquals("Empty Token")) {
            WelcomeUser();
        }

        mMyObservationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,ObservationListActivity.class);
                SharedPreferences prefs = MainActivity.this.getSharedPreferences(
                        "com.alabama.bamboofinder", Context.MODE_PRIVATE);
                String token = prefs.getString("token", "Empty Token");
                if(token.contentEquals("Empty Token")) {
                    Toast toast = Toast.makeText(MainActivity.this, "You must be logged in to do this",
                            Toast.LENGTH_LONG);
                    toast.show();
                }
                else
                    startActivity(i);
            }
        });

        mViewMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, InteractiveMapActivity.class);
                i.putExtra("user", mUser.convertToJSON().toString());
                Log.d(TAG, mUser.convertToJSON().toString());
                startActivity(i);
            }
        });

        mLearnMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, EducationalActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if(mUser.getUsername() == null) {
            MenuItem logoutItem = menu.findItem(R.id.menu_item_logout);
            logoutItem.setVisible(false);
            MenuItem loginItem = menu.findItem(R.id.menu_item_login);
            loginItem.setVisible(true);
            mLoggedInText.setText("Welcome to BambooFinder!");
            this.invalidateOptionsMenu();
        }
        else {
            MenuItem logoutItem = menu.findItem(R.id.menu_item_logout);
            logoutItem.setVisible(true);
            MenuItem loginItem = menu.findItem(R.id.menu_item_login);
            loginItem.setVisible(false);
            this.invalidateOptionsMenu();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.home:
                //finish();
                return true;
            case R.id.menu_item_logout:
                new AlertDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //clear all preferences
                                MainActivity.this.getSharedPreferences("com.alabama.bamboofinder",
                                        Context.MODE_PRIVATE).edit().clear().commit();
                                dialog.cancel();
                                MainActivity.this.recreate();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //do nothing
                                dialog.cancel();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;
            case R.id.menu_item_login:
                StartLogin();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private void StartLogin() {
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivityForResult(i, LOGIN_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
            WelcomeUser();
            this.invalidateOptionsMenu();
        }
    }

    private void WelcomeUser() {
        SharedPreferences prefs = getSharedPreferences(
                "com.alabama.bamboofinder", Activity.MODE_PRIVATE);
        String user = prefs.getString("user", "Empty User");
        try {
            JSONObject userJSON = new JSONObject(user);
            mUser = new User(userJSON);
            mLoggedInText.setText("Welcome, " + userJSON.getString("login") + "!");
        }
        catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
