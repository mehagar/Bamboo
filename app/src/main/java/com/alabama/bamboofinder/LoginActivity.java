package com.alabama.bamboofinder;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
                setResult(RESULT_OK);
                finish();
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
            return token;
        }
    }


}
