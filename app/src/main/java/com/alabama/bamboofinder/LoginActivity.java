package com.alabama.bamboofinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class LoginActivity extends ActionBarActivity {
    private static final String BASE_URL = "www.inaturalist.org";
    private static final String REDIRECT_URI = "redirect_uri";
    private static final String APP_ID = "23a5158ed4385113eeef60291289c8c96fd3dc9ba3a646f10ea93bd4b215fe41";
    private static final String APP_SECRET = "40f13b324b0728378081964b3f5c0e8e2ef0860fd1114da858c249347108f685";
    private static final String CLIENT_ID = "client_id";
    private static final String CLIENT_SECRET = "client_secret";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String GRANT_TYPE = "grant_type";

    private static String token;
    private Button mLoginButton;
    private Button mCreateAccountButton;
    private EditText mUsername;
    private EditText mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher);

        mUsername = (EditText) findViewById(R.id.EmailText);
        mPassword = (EditText) findViewById(R.id.PasswordText);
        token = "Empty Token";

        mLoginButton = (Button) findViewById(R.id.loginButton);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri.Builder tokenRequest = new Uri.Builder();
                tokenRequest.scheme("https")
                        .authority(BASE_URL)
                        .appendPath("oauth")
                        .appendPath("token")
                        .appendQueryParameter(CLIENT_ID, APP_ID)
                        .appendQueryParameter(CLIENT_SECRET, APP_SECRET)
                        .appendQueryParameter(USERNAME, mUsername.getText().toString())
                        .appendQueryParameter(PASSWORD, mPassword.getText().toString())
                        .appendQueryParameter(GRANT_TYPE, "password")
                        .build();

                AsyncTask tokenTask = new GetRequestToken().execute(tokenRequest.toString());
                try {
                    tokenTask.get();
                }
                catch (Exception e) {
                    Log.e("Waiting Error", e.toString());
                }
                LoginActivity.this.setUser(token);
                if(!token.contentEquals("Empty Token")) {
                    setResult(RESULT_OK);
                    addUserToProject();
                    finish();
                }
                else if(!isNetworkAvailable()) {
                    Toast toast = Toast.makeText(LoginActivity.this, "No internet connection",
                            Toast.LENGTH_LONG);
                    toast.show();
                }
                else {
                    new AlertDialog.Builder(LoginActivity.this)
                            .setTitle("Invalid Login!")
                            .setMessage("Your login information was incorrect.")
                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mPassword.setText("");
                                    dialog.cancel();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        });

        mCreateAccountButton = (Button) findViewById(R.id.newAccountButton);
        mCreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.inaturalist.org/signup"));
                startActivity(browserIntent);
            }
        });
    }

    private void addUserToProject() {
        SharedPreferences prefs = getSharedPreferences(
                "com.alabama.bamboofinder", Context.MODE_PRIVATE);
        String token = prefs.getString("token", "Empty Token");
        new AddUserToProjectTask().execute(token);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    class GetRequestToken extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... tokenRequest) {
            if(tokenRequest.length != 1) {
                Log.e("GetRequestToken", "More than one parameter");
                return token;
            }

            try {
                URL url = new URL(tokenRequest[0]);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String response = in.readLine();

                JSONObject responseObject = new JSONObject(response);
                token = responseObject.getString("access_token");

                connection.disconnect();
            }
            catch (Exception e) {
                Log.e("Token request failed", e.toString());
            }

            SharedPreferences prefs = getSharedPreferences(
                    "com.alabama.bamboofinder", Activity.MODE_PRIVATE);
            prefs.edit().putString("token", token).apply();
            //setUser(token);
            return token;
        }
    }

    private void setUser(String token) {
        AsyncTask userTask = new User().execute(token);
        try {
            User user = (User) userTask.get();
            //mLoggedInText.setText("Welcome, " + mUser.getUsername() + "!");

            String prefUser = user.convertToJSON().toString();
            SharedPreferences prefs = getSharedPreferences(
                    "com.alabama.bamboofinder", Activity.MODE_PRIVATE);
            prefs.edit().putString("user", prefUser).apply();
            Log.i("User prefs string", prefUser);
        }
        catch (Exception e) {
            Log.e("LoginActivity", "Failed to get user");
        }
    }

    class AddUserToProjectTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... objects) {
            // params are the observation, token, and photo file name
            ApiManager.addUserToProject(objects[0]);
            return null;
        }
    }
}
