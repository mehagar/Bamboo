package com.alabama.bamboofinder2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.net.ssl.HttpsURLConnection;

public class ObservationDetailActivity extends ActionBarActivity {
    private static final String TAG = "ObservationDetail";

    public static final String EXTRA_OBSERVATION = "observation";
    public static final String EXTRA_USER_LATITUDE = "latitude";
    public static final String EXTRA_USER_LONGITUDE = "longitude";
    public static final String BASE_URL = "www.inaturalist.org";

    private static final int ADD_OBSERVATION = 0;
    private static final int EDIT_OBSERVATION = 1;
    private static final int CAMERA_REQUEST = 1888;
    private static int mMode;
    private static File imagePath;
    private ImageView mImageView;
    private EditText mSpeciesText;
    private EditText mDescriptionText;
    private Button mSaveButton;
    private Button mCancelButton;
    private TextView mUsernameText;
    private TextView mLatLngText;
    private TextView mWebLinkText;
    private Observation mObservation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observation_detail);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher);

        mDescriptionText = (EditText) findViewById(R.id.descriptionEditText);
        mSpeciesText = (EditText) findViewById(R.id.speciesEditText);
        mImageView = (ImageView)findViewById(R.id.observationImage);
        mCancelButton = (Button) findViewById(R.id.cancelButton);
        mSaveButton = (Button) findViewById(R.id.saveButton);
        mUsernameText = (TextView) findViewById(R.id.usernameTextView);
        mLatLngText = (TextView) findViewById(R.id.latLngTextView);
        mWebLinkText = (TextView) findViewById(R.id.webLinkTextView);

        //Check if adding or editing
        Intent i = getIntent();
        double latitude = i.getDoubleExtra(EXTRA_USER_LATITUDE, -1);
        SharedPreferences prefs = this.getSharedPreferences(
                "com.alabama.bamboofinder", Context.MODE_PRIVATE);
        String prefUser = prefs.getString("user", "Empty user");

        if(latitude == -1) {
            //Latitude was not passed in Intent, so user is editing an observation
            mMode = EDIT_OBSERVATION;

            mObservation = (Observation) i.getSerializableExtra(EXTRA_OBSERVATION);
            if(!mObservation.getMediumUrl().equals("")) {
                Picasso.with(getApplicationContext())
                        .load(mObservation.getMediumUrl())
                        .resize(864, 486)
                        .centerCrop()
                        .into(mImageView);
            } else {
                Log.e("ImageView Error", "Observation created without a picture");
                mImageView.setVisibility(View.GONE); // Remove the imageView if there is no picture for it
            }

            //check if logged in user made this observation
            try {
                if(prefUser.contentEquals("Empty user")) {
                    mSpeciesText.setKeyListener(null);
                    mDescriptionText.setKeyListener(null);
                    mSaveButton.setVisibility(View.INVISIBLE);
                }
                else {
                    User user = new User(new JSONObject(prefUser));
                    if (!user.getUsername().contentEquals(mObservation.getUserLogin())) {
                        mSpeciesText.setKeyListener(null);
                        mDescriptionText.setKeyListener(null);
                        mSaveButton.setVisibility(View.INVISIBLE);
                    }
                }
            }
            catch (Exception e) {
                Log.e("ObservationDetail", e.toString());
            }

            mUsernameText.setText("Created by: " + mObservation.getUserLogin());
            LatLng latLng = mObservation.getLocation();
            mLatLngText.setText("Lat: " + latLng.latitude + " Long: " + latLng.longitude);
            String webLink = new String("View observation on iNaturalist");
            SpannableString content = new SpannableString(webLink);
            content.setSpan(new UnderlineSpan(), 0, webLink.length(), 0);
            mWebLinkText.setText(content);
            mWebLinkText.setTextColor(Color.parseColor("#0645AD"));
            mSpeciesText.setText(mObservation.getSpeciesGuess());
            mDescriptionText.setText(mObservation.getDescription());
        }
        else
            mMode = ADD_OBSERVATION;

        mWebLinkText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mWebLinkText.getText() != null) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.inaturalist.org/observations/" +
                                    mObservation.getId()));
                    startActivity(browserIntent);
                }
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                SharedPreferences prefs = ObservationDetailActivity.this.getSharedPreferences(
                        "com.alabama.bamboofinder", Context.MODE_PRIVATE);
                String token = prefs.getString("token", "Empty Token");

                switch(mMode) {
                    case(EDIT_OBSERVATION):
                        if(mImageView.getDrawable() == null) {
                            ShowAlert();
                            break;
                        }
                        else if(token.contentEquals("Empty Token")) {
                            ShowLoggedOutAlert();
                            break;
                        }

                        Uri.Builder putObservation = new Uri.Builder();
                        putObservation.scheme("https")
                                .authority(BASE_URL)
                                .appendPath("observations")
                                .appendPath(mObservation.getId())
                                .appendQueryParameter("ignore_photos", "1")
                                .appendQueryParameter("observation[description]", mDescriptionText.getText().toString())
                                .appendQueryParameter("observation[species_guess]", mSpeciesText.getText().toString())
                                .build();
                        new UpdateObservationTask().execute(mObservation, token, putObservation.toString());
                        setResult(RESULT_OK);
                        finish();
                        break;
                    case(ADD_OBSERVATION):
                        if(mImageView.getDrawable() == null) {
                            ShowAlert();
                            break;
                        }
                        else if(token.contentEquals("Empty Token")) {
                            ShowLoggedOutAlert();
                            break;
                        }

                        Observation o = new Observation();
                        double latitude = intent.getDoubleExtra(EXTRA_USER_LATITUDE, -1);
                        double longitude = intent.getDoubleExtra(EXTRA_USER_LONGITUDE, -1);
                        LatLng location = new LatLng(latitude, longitude);
                        o.setLocation(location);
                        o.setSpeciesGuess(mSpeciesText.getText().toString());
                        o.setDescription(mDescriptionText.getText().toString());
                        o.setTimeStamp(new Date());
                        try {
                            AsyncTask postObservation = new PostObservationsTask().execute(
                                    o, token, new FileInputStream(imagePath));
                            Log.i("Image Path", imagePath.toString());
                        }
                        catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                        setResult(RESULT_OK);
                        finish();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_observation_detail, menu);
        if(mMode == EDIT_OBSERVATION) {
            MenuItem item = menu.findItem(R.id.menu_item_new_picture);
            item.setVisible(false);
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
                finish();
                return true;
            case R.id.menu_item_new_picture:
                try {
                    imagePath = createImageFile();
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(imagePath != null) {
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(imagePath));
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath.toString());
                bitmap = Bitmap.createScaledBitmap(bitmap, 864, 486, true);
                mImageView.setImageBitmap(bitmap);
            }
            catch (Exception e) {
                Log.e("Result Exception", e.toString());
            }
        }
        else if(requestCode == CAMERA_REQUEST && resultCode == RESULT_CANCELED) {
            imagePath = null;
        }
    }

    private void ShowAlert() {
        new AlertDialog.Builder(this)
                .setTitle("No photo!")
                .setMessage("You must take a photo first.")
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void ShowLoggedOutAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Not Logged In!")
                .setMessage("You must be logged in to submit an observation.")
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    class PostObservationsTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... objects) {
            // params are the observation, token, and photo file name
            Log.d(TAG, "in doInBackground");
            ApiManager.uploadObservation((Observation)objects[0], (String)objects[1], (InputStream)objects[2]);
            return null;
        }
    }

    class UpdateObservationTask extends  AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... objects) {
            // params are the observation, token, and api url
            try {
                URL url = new URL((String)objects[2]);
                Log.i("PUT URL", url.toString());
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Authorization", "Bearer " + (String)objects[1]);

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String response = in.readLine();

                Log.i("PUT responseCode", String.valueOf(connection.getResponseCode()));

                connection.disconnect();
            }
            catch (Exception e) {
                Log.e("Token request failed", e.toString());
            }
            return null;
        }
    }
}
